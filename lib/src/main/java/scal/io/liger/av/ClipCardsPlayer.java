package scal.io.liger.av;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.ClipMetadata;
import scal.io.liger.model.MediaFile;
import scal.io.liger.view.Util;

/**
 * Plays a collection of ClipCards, as well as a secondary audio track.
 *
 * Development Note : All methods prefixed with '_' are intended for exclusive
 * use by {@link Handler#handleMessage(android.os.Message)} and other '_' prefixed methods.
 *
 * Created by davidbrodsky on 12/12/14.
 */
public class ClipCardsPlayer implements TextureView.SurfaceTextureListener {
    public final String TAG = getClass().getSimpleName();

    private final int TIMER_INTERVAL_MS = 100;

    protected final float FULL_VOLUME = 1.0f;
    protected final float MUTE_VOLUME = 0.0f;

    protected Context mContext;
    protected FrameLayout mContainerLayout;
    protected List<ClipCard> mClipCards;
    private ArrayList<Integer> accumulatedDurationByMediaCard;
    protected ClipCard mCurrentlyPlayingCard;
    protected MediaPlayer mMainPlayer;
    protected MediaPlayer mSecondaryPlayer;
    private Uri mSecondaryAudioUri;
    private Surface mSurface;
    protected ToggleButton mMuteBtn;
    private TextView mTimeCode;
    protected ImageView mPlayBtn;
    protected ImageView mThumbnailView;
    private TextureView mTextureView;
    private SeekBar mPlaybackProgress;
    private Timer mTimer;
    protected Handler mHandler;

    private boolean mAdvancingClips;
    protected boolean mIsPlaying;
    private boolean mIsPaused;
    private int mCurrentPhotoElapsedTime;
    private int mPhotoSlideDurationMs = 5 * 1000;
    private int mClipCollectionDurationMs;
    protected float mRequestedVolume = FULL_VOLUME;

    private View.OnClickListener mPlaybackClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.i(TAG, "PlaybackClickListener fired");
            if ((mCurrentlyPlayingCard.getMedium().equals(Constants.VIDEO) ||
                 mCurrentlyPlayingCard.getMedium().equals(Constants.AUDIO)) && mMainPlayer == null) return;

            if (mIsPlaying) {
                pausePlayback();
            }
            else if (mIsPaused) {
                resumePlayback();
            }
            else {
                startPlayback();
            }
        }
    };

    private ToggleButton.OnCheckedChangeListener mMuteCheckedListener = new ToggleButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            setVolume(isChecked ? MUTE_VOLUME : FULL_VOLUME);
        }
    };

    /**
     * Handler to coordinate actions affecting the internal MediaPlayers.
     * Events may come from the UI or Timer thread.
     */
    protected static class ClipCardsPlayerHandler extends Handler {

        // NOTE : Avoid conflicts with ClipCardsNarratorHandler constants
        public static final int START   = 0;
        public static final int RESUME  = 1;
        public static final int PAUSE   = 2;
        public static final int STOP    = 3;
        public static final int ADVANCE = 4;
        public static final int RELEASE = 5;
        public static final int TIMER   = 6;
        public static final int VOLUME  = 7;

        private WeakReference<ClipCardsPlayer> mWeakPlayer;

        public ClipCardsPlayerHandler(ClipCardsPlayer player) {
            mWeakPlayer = new WeakReference<>(player);
        }

        @Override
        public void handleMessage (Message msg) {

            ClipCardsPlayer player = mWeakPlayer.get();
            if (player == null) {
                Log.w(getClass().getSimpleName(), "ClipCardsPlayer.handleMessage: player is null!");
                return;
            }

            Object obj = msg.obj;

            switch(msg.what) {
                case START:
                    player._startPlayback();
                    break;
                case RESUME:
                    player._resumePlayback();
                    break;
                case PAUSE:
                    player._pausePlayback();
                    break;
                case STOP:
                    player._stopPlayback();
                    break;
                case ADVANCE:
                    player._advanceToNextClip((MediaPlayer) obj);
                    break;
                case RELEASE:
                    player._release();
                    break;
                case TIMER:
                    player._onTimerTick();
                    break;
                case VOLUME:
                    player._setVolume((float) obj);
                    break;
            }
        }

    }

    // <editor-fold desc="Public API">

    public ClipCardsPlayer(@NonNull FrameLayout container, @NonNull List<ClipCard> clipCards) {
        mContainerLayout = container;
        mClipCards = clipCards;
        init();
    }

    public void setPhotoSlideDurationMs(int durationMs) {
        mPhotoSlideDurationMs = durationMs;
    }

    public void addAudioTrack(MediaFile mediaFile) {
        mSecondaryAudioUri = Uri.parse(mediaFile.getPath());
    }

    public void setVolume(float volume) {
        if (volume == MUTE_VOLUME) mMuteBtn.setChecked(true);
        else mMuteBtn.setChecked(false);
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.VOLUME, volume));
    }

    protected void _setVolume(float volume) {
        if (mMainPlayer != null) mMainPlayer.setVolume(volume, volume);
        if (mSecondaryPlayer != null) mSecondaryPlayer.setVolume(volume, volume);
        mRequestedVolume = volume;
    }

    /**
     * Begin playback of the main media files, as well as the secondary auto track if set.
     */
    public void startPlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.START));
    }

    /**
     * Stop playback and reset the playback location to the first clip.
     */
    public void stopPlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.STOP));
    }

    public void setTimecodeVisible(boolean isVisible) {
        mTimeCode.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    // </editor-fold desc="Public API">

    private void init() {
        mContext = mContainerLayout.getContext();
        mHandler = new ClipCardsPlayerHandler(this);
        inflateViews(mContainerLayout);
        attachViewListeners();
        mCurrentlyPlayingCard = mClipCards.get(0);
        setThumbnailForClip(mThumbnailView, mCurrentlyPlayingCard);
        mClipCollectionDurationMs = calculateTotalClipCollectionLengthMs(mClipCards);
        setupTimer();
    }

    private void inflateViews(@NonNull FrameLayout root) {
        Context context = root.getContext();

        /* Media Views (Image + Texture) */
        FrameLayout.LayoutParams mediaViewParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                             ViewGroup.LayoutParams.MATCH_PARENT);

        Resources r = context.getResources();
        int bottomPadPx = r.getDimensionPixelSize(R.dimen.padding_small);
        mediaViewParams.setMargins(0, 0, 0, bottomPadPx);

        mThumbnailView = new ImageView(context);
        mThumbnailView.setLayoutParams(mediaViewParams);

        mTextureView = new TextureView(context);
        mTextureView.setLayoutParams(mediaViewParams);
        mTextureView.setSurfaceTextureListener(this);

        /* Playback progress */
        FrameLayout.LayoutParams alignBottomParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                             ViewGroup.LayoutParams.WRAP_CONTENT);
        alignBottomParams.gravity = Gravity.BOTTOM;
        mPlaybackProgress = new SeekBar(context);
        mPlaybackProgress.setLayoutParams(alignBottomParams);
        mPlaybackProgress.setEnabled(false);

        /* Timecode label */
        FrameLayout.LayoutParams alignTopParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                             ViewGroup.LayoutParams.WRAP_CONTENT);
        alignTopParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        mTimeCode = new TextView(context);
        mTimeCode.setLayoutParams(alignTopParams);
        mTimeCode.setVisibility(View.INVISIBLE); // Invisible by default
        mTimeCode.setShadowLayer(2, 2, 2, Color.WHITE);

        /* Play Button */
        FrameLayout.LayoutParams overlayParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                             ViewGroup.LayoutParams.MATCH_PARENT);
        overlayParams.gravity = Gravity.CENTER;

        mPlayBtn = new ImageView(context);
        mPlayBtn.setImageResource(R.drawable.play);
        mPlayBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        /* Mute Button */
        int muteBtnSize = r.getDimensionPixelSize(R.dimen.mute_btn_size);
        FrameLayout.LayoutParams alignTopRightParams =
                new FrameLayout.LayoutParams(muteBtnSize,
                                             muteBtnSize);
        alignTopRightParams.gravity = Gravity.TOP | Gravity.RIGHT;

        mMuteBtn = new ToggleButton(context);
        mMuteBtn.setBackgroundResource(R.drawable.btn_mute);
        mMuteBtn.setLayoutParams(alignTopRightParams);
        mMuteBtn.setTextOn("");
        mMuteBtn.setTextOff("");
        mMuteBtn.setText("");

        root.addView(mTextureView);
        root.addView(mThumbnailView);
        root.addView(mPlaybackProgress);
        root.addView(mTimeCode);
        root.addView(mPlayBtn);
        root.addView(mMuteBtn);
    }

    private void attachViewListeners() {
        mThumbnailView.setOnClickListener(mPlaybackClickListener);
        mTextureView.setOnClickListener(mPlaybackClickListener);
        mPlayBtn.setOnClickListener(mPlaybackClickListener);
        mMuteBtn.setOnCheckedChangeListener(mMuteCheckedListener);
    }

    private void setupTimer() {
        // Poll MediaPlayer for position, ensuring it never exceeds stop point indicated by ClipMetadata
        if (mTimer != null) mTimer.cancel(); // should never happen but JIC
        mTimer = new Timer("mplayer");
        mTimer.schedule(
            new TimerTask() {
                    @Override
                    public void run() {
                        onTimerTick();
                    }
            },
            50,                   // Initial delay ms
            TIMER_INTERVAL_MS);   // Repeat interval ms
    }

    private void onTimerTick() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.TIMER));
    }

    private void _onTimerTick() {
        try {
            if (mIsPlaying) {
                //Log.i("Timer", "isPlaying");
                int currentClipElapsedTime = 0;
                switch (mCurrentlyPlayingCard.getMedium()) {
                    case Constants.VIDEO:
                    case Constants.AUDIO:
                        currentClipElapsedTime = Math.min(mMainPlayer.getCurrentPosition(), mMainPlayer.getDuration()); // Seen issues where getCurrentPosition returns
                        // If getStopTime() is equal to 0 or mediaPlayer.getDuration(), clip advancing will be handled by the MediaPlayer onCompletionListener
                        int clipStopTimeMs = mCurrentlyPlayingCard.getSelectedClip().getStopTime();
                        if (!mAdvancingClips && clipStopTimeMs > 0 && currentClipElapsedTime > clipStopTimeMs && clipStopTimeMs != mMainPlayer.getDuration()) {
                            //mediaPlayer.pause();
                            Log.i(TAG, String.format("video mTimer advancing clip. Clip stop time: %d. MediaPlayer duration: %d", clipStopTimeMs, mMainPlayer.getDuration()));
                            _advanceToNextClip(mMainPlayer);
                        } else if (mAdvancingClips) {
                            // MediaPlayer is transitioning and cannot report progress
                            Log.i("Timer", "MediaPlayer is advancing, using currentClipElapsedTime of 0");
                            currentClipElapsedTime = 0;
                        }
                        break;
                    case Constants.PHOTO:
                        //Log.i("Timer", String.format("Photo elapsed time %d / %d", currentPhotoElapsedTime, mCardModel.getStoryPath().getStoryPathLibrary().photoSlideDurationMs));
                        mCurrentPhotoElapsedTime += TIMER_INTERVAL_MS; // For Photo cards, this is reset on each call to advanceToNextClip
                        currentClipElapsedTime = mCurrentPhotoElapsedTime;

                        if (!mAdvancingClips && currentClipElapsedTime > mPhotoSlideDurationMs) {
                            //Log.i("Timer", "advancing photo");
                            Log.i(TAG, "photo mTimer advancing clip");
                            _advanceToNextClip(null);
                            mCurrentPhotoElapsedTime = 0;
                            currentClipElapsedTime = 0;
                        }
                        break;
                }

                int durationOfPreviousClips = 0;
                int currentlyPlayingCardIndex = mClipCards.indexOf(mCurrentlyPlayingCard);
                if (currentlyPlayingCardIndex > 0)
                    durationOfPreviousClips = accumulatedDurationByMediaCard.get(currentlyPlayingCardIndex - 1);
                if (mPlaybackProgress != null) {
                    mPlaybackProgress.setProgress((int) (mPlaybackProgress.getMax() * ((float) durationOfPreviousClips + currentClipElapsedTime) / mClipCollectionDurationMs)); // Show progress relative to clip collection duration
                }
                mTimeCode.setText(Util.makeTimeString(durationOfPreviousClips + currentClipElapsedTime));
                //Log.i("Timer", String.format("current clip (%d) elapsed time: %d. max photo time: %d. progress: %d", currentlyPlayingCardIndex, currentClipElapsedTime, mPhotoSlideDurationMs, mPlaybackProgress == null ? 0 : mPlaybackProgress.getProgress()));
            }
        } catch (IllegalStateException e) { /* MediaPlayer in invalid state. Ignore */}
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        mSurface = new Surface(surfaceTexture);

        switch (mCurrentlyPlayingCard.getMedium()) {
            case Constants.VIDEO:
            case Constants.AUDIO:
                try {
                    mMainPlayer = new MediaPlayer();
                    mMainPlayer.setDataSource(mContext, Uri.parse(mCurrentlyPlayingCard.getSelectedMediaFile().getPath()));
                    mMainPlayer.setSurface(mSurface);
                    prepareMainMediaPlayer(mMainPlayer);
                    mMainPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    mMainPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Log.i(TAG, "mediaplayer onComplete. advancing clip");
                            if (!mAdvancingClips) {
                                advanceToNextClip(mp);
                            }
                        }
                    });
                } catch (IllegalArgumentException | IllegalStateException | SecurityException | IOException e) {
                    e.printStackTrace();
                }
                break;
            case Constants.PHOTO:
                // do nothing
                break;
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, String.format("Texture size changed to %dx%d", width, height));

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mTimer != null) mTimer.cancel();
        release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    protected void advanceToNextClip(MediaPlayer player) {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.ADVANCE, player));
    }

    protected void _advanceToNextClip(MediaPlayer player) {
        mAdvancingClips = true;

        int nextClipIndex = mClipCards.indexOf(mCurrentlyPlayingCard);
        if (nextClipIndex == (mClipCards.size() - 1)) {
            Log.i(TAG, "Played all clips. stopping");
            nextClipIndex = 0;
            _stopPlayback();
        } else {
            Log.i(TAG, "Advancing to next clip " + nextClipIndex);
            nextClipIndex++;
        }

        _advanceToClip(player, mClipCards.get(nextClipIndex), !(nextClipIndex == 0));
    }

    protected void _advanceToClip(MediaPlayer player, ClipCard targetClip, boolean autoPlay) {
        if (mClipCards.indexOf(targetClip) == -1) {
            Log.e(TAG, "Invalid Card passed to _advanceToClip");
            return;
        }

        mAdvancingClips = true;
        mCurrentlyPlayingCard = targetClip;

        Uri media;
        switch (mCurrentlyPlayingCard.getMedium()) {
            case Constants.VIDEO:
            case Constants.AUDIO:
                mTextureView.setVisibility(View.VISIBLE); // In case previous card wasn't video medium
                media = Uri.parse(mCurrentlyPlayingCard.getSelectedMediaFile().getPath());
                try {
                    // Don't set isPlaying false. We're only 'stopping' to switch media sources
                    player.stop();
                    player.reset();
                    Log.i(TAG, "Setting player data source " + media.toString());
                    player.setDataSource(mContext, media);
                    player.setSurface(mSurface);
                    prepareMainMediaPlayer(player);
                    if (autoPlay) _resumePlayback();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Constants.PHOTO:
                Log.i(TAG, "set currentelapsedtime to 0");
                if (firstClipCurrent()) {
                    // Stop playback. With video / audio this would be handled onPreparedListener
                    _stopPlayback();
                }
                if (mThumbnailView != null) {
                    setThumbnailForClip(mThumbnailView, mCurrentlyPlayingCard);
                    if (!mIsPlaying && mPlaybackProgress != null) mPlaybackProgress.setProgress(0);
                }
                break;
        }
        mAdvancingClips = false;
    }

    protected void _startPlayback() {
        mPlayBtn.setVisibility(View.GONE);

        mIsPlaying = true;
        // Connect narrationPlayer to narration mediaFile on each request to start playback
        // to ensure we have the most current narration recording
        if (mSecondaryAudioUri != null) {
            if (mSecondaryPlayer == null) mSecondaryPlayer = MediaPlayer.create(mContext, mSecondaryAudioUri);
            else {
                // TODO Only necessary if narration media file changed
                try {
                    mSecondaryPlayer.reset();
                    mSecondaryPlayer.setDataSource(mContext, mSecondaryAudioUri);
                    mSecondaryPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "Starting narration player for uri " + mSecondaryAudioUri.toString());
            mSecondaryPlayer.start();
        }

        mThumbnailView.setVisibility(View.VISIBLE);

        switch(mCurrentlyPlayingCard.getMedium()) {
            case Constants.VIDEO:
                mThumbnailView.setVisibility(View.GONE);
            case Constants.AUDIO:
                mMainPlayer.start();
                break;
            case Constants.PHOTO:
                setThumbnailForClip(mThumbnailView, mCurrentlyPlayingCard);
                break;
        }
    }

    /**
     * Pause playback, preserving the playback location for a following call to
     * {@link #resumePlayback()}
     */
    private void pausePlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.PAUSE));
    }

    private void _pausePlayback() {
        mPlayBtn.setVisibility(View.VISIBLE);

        if (mMainPlayer != null && mMainPlayer.isPlaying()) {
            mMainPlayer.pause();
        }

        if (mSecondaryPlayer != null && mSecondaryPlayer.isPlaying()) {
            mSecondaryPlayer.pause();
        }

        mIsPaused = true;
        mIsPlaying = false;
    }

    /**
     * Resume playback at the location determined by a previous call to
     * {@link #pausePlayback()}
     */
    private void resumePlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.RESUME));
    }

    private void _resumePlayback() {
        mPlayBtn.setVisibility(View.GONE);

        if (mMainPlayer != null && !mMainPlayer.isPlaying()) {
            mMainPlayer.start();
        }

        if (mSecondaryPlayer != null && !mSecondaryPlayer.isPlaying()) {
            mSecondaryPlayer.start();
        }

        mIsPaused = false;
        mIsPlaying = true;
    }

    protected void _stopPlayback() {
        mPlayBtn.setVisibility(View.VISIBLE);

        mIsPaused = false;
        mIsPlaying = false;

        if (mMainPlayer != null && mMainPlayer.isPlaying()) {
            mMainPlayer.stop();
            mMainPlayer.reset();
        }

        if (mSecondaryPlayer != null && mSecondaryPlayer.isPlaying()) {
            mSecondaryPlayer.stop();
            mSecondaryPlayer.reset();
        }

        mCurrentlyPlayingCard = mClipCards.get(0);
        mCurrentPhotoElapsedTime = 0;
    }

    /**
     * Release all MediaPlayers when playback will no longer be required
     */
    private void release() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.RELEASE));
    }

    private void _release() {
        if (mMainPlayer != null) mMainPlayer.release();
        if (mSecondaryPlayer != null) mSecondaryPlayer.release();
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    protected void prepareMainMediaPlayer(MediaPlayer mainPlayer) {
        try {
            mainPlayer.prepare();
            adjustAspectRatio(mTextureView, mainPlayer.getVideoWidth(), mainPlayer.getVideoHeight());
            mAdvancingClips = false;
            mainPlayer.seekTo(mCurrentlyPlayingCard.getSelectedClip().getStartTime());
        } catch (IOException e) {
            Log.e(TAG, "Error preparing mediaplayer");
            e.printStackTrace();
        }
    }

    protected boolean firstClipCurrent() {
        return mClipCards.indexOf(mCurrentlyPlayingCard) == 0;
    }

    /**
     * Set a thumbnail on the given ImageView for the given ClipCard
     */
    protected void setThumbnailForClip(@NonNull ImageView thumbnail, @NonNull ClipCard clipCard) {
        // Clip has attached media. Show an appropriate preview
        // e.g: A thumbnail for video

        MediaFile mediaFile = clipCard.getStoryPath().loadMediaFile(clipCard.getSelectedClip().getUuid());

        String medium = clipCard.getMedium();
        if (medium.equals(Constants.VIDEO)) {
            Bitmap thumbnailBitmap = mediaFile.getThumbnail(mContext);
            if (thumbnailBitmap != null) {
                thumbnail.setImageBitmap(thumbnailBitmap);
            }
            thumbnail.setVisibility(View.VISIBLE);
        } else if (medium.equals(Constants.PHOTO)) {
            Uri uri = Uri.parse(mediaFile.getPath());
            thumbnail.setImageURI(uri);
            thumbnail.setVisibility(View.VISIBLE);
        } else if (medium.equals(Constants.AUDIO)) {
            Uri myUri = Uri.parse(mediaFile.getPath());
            final MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            //set background image
            thumbnail.setImageResource(R.drawable.audio_waveform);
            //String clipType = clipCard.getClipType();
            //setClipExampleDrawables(clipType, thumbnail);
            thumbnail.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set an example thumbnail on imageView for this clipType
     *
     * This should be called if no thumbnail directly representing the clip is available.
     */
    private void setClipExampleDrawables(String clipType, ImageView imageView) {
        Drawable drawable;
        switch (clipType) {
            case Constants.CHARACTER:
                drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_character);
                break;
            case Constants.ACTION:
                drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_action);
                break;
            case Constants.RESULT:
                drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_result);
                break;
            case Constants.SIGNATURE:
                drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_signature);
                break;
            case Constants.PLACE:
                drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_place);
                break;
            default:
                Log.d(TAG, "No clipType matching '" + clipType + "' found.");
                drawable = mContext.getResources().getDrawable(R.drawable.ic_launcher); // FIXME replace with a sensible placeholder image
        }
        imageView.setImageDrawable(drawable);
    }

    private int calculateTotalClipCollectionLengthMs(List<ClipCard> cards) {
        int totalDuration = 0;
        int clipDuration;
        accumulatedDurationByMediaCard = new ArrayList<>(cards.size());
        for (ClipCard card : cards) {
            clipDuration = 0;
            switch (card.getMedium()) {
                case Constants.VIDEO:
                case Constants.AUDIO:
                    ClipMetadata clipMeta = card.getSelectedClip();
                    MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(card.getSelectedMediaFile().getPath()));
                    if (mp != null) {
                        clipDuration = (clipMeta.getStopTime() == 0 ? mp.getDuration() : (clipMeta.getStopTime() - clipMeta.getStartTime()));
                        mp.release();
                    }
                    break;
                case Constants.PHOTO:
                    clipDuration += mPhotoSlideDurationMs;
                    break;
            }
            totalDuration += clipDuration;
            accumulatedDurationByMediaCard.add(totalDuration);
            Log.i(TAG, String.format("Got duration for media: %d", clipDuration));
            Log.i(TAG, "Total duration now " + totalDuration);
        }
        return totalDuration;
    }

    /**
     * Sets the TextureView transform to preserve the aspect ratio specified by the
     * given width and height.
     *
     * Courtesy of Andrew McFadden's grafika project:
     * https://github.com/google/grafika/blob/master/src/com/android/grafika/PlayMovieActivity.java
     */
    private void adjustAspectRatio(TextureView textureView, int width, int height) {
        int viewWidth = textureView.getWidth();
        int viewHeight = textureView.getHeight();
        double aspectRatio = (double) height / width;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + width + "x" + height +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
        textureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        //txform.postRotate(10);          // just for fun
        txform.postTranslate(xoff, yoff);
        textureView.setTransform(txform);
    }
}
