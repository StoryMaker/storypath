package scal.io.liger.av;

import timber.log.Timber;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.model.AudioClip;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.ClipMetadata;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPathLibrary;
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
    protected List<AudioClip> mAudioClips = new ArrayList<>();
    protected StoryPathLibrary mStoryPathLibrary;
    private ArrayList<Integer> accumulatedDurationByMediaCard;
    protected ClipCard mCurrentlyPlayingCard;
    protected MediaPlayer mMainPlayer;
    protected List<MediaPlayer> mAudioClipPlayers = new ArrayList<>();
    protected HashMap<AudioClip, MediaPlayer> mAudioClipToMediaPlayer = new HashMap<>();
    protected HashMap<MediaPlayer, AudioClip> mMediaPlayerToAudioClip = new HashMap<>();
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
    private int mCurrentPhotoElapsedTime;
    private int mPhotoSlideDurationMs = 5 * 1000;
    private int mClipCollectionDurationMs;
    protected float mRequestedVolume = FULL_VOLUME;
    private PlayerState mState = PlayerState.PREPARING;
    private boolean mPlaySecondaryTracks = true;
    private boolean mAudioTracksDirty = true;
    private boolean mMuteChecked = false;

    public static enum PlayerState {

        /**
         * Preparing for playback. This occurs briefly after construction while media is
         * asynchronously analyzed.
         * The only valid next state is READY
         */
        PREPARING,

        /** Prepared to begin playback. Seeking via
         * {@link #_advanceToClip(android.media.MediaPlayer, scal.io.liger.model.ClipCard, boolean)}
         * or {@link #advanceToNextClip(android.media.MediaPlayer)}
         * is valid in this state.
         * The only valid next state is PLAYING
         */
        READY,

        /** Playing. Seeking is valid in this state. Seeking via
         * {@link #_advanceToClip(android.media.MediaPlayer, scal.io.liger.model.ClipCard, boolean)}
         * or {@link #advanceToNextClip(android.media.MediaPlayer)}
         * is valid in this state.
         * The only valid next state is READY
         */
        PLAYING,

    }

    private View.OnClickListener mPlaybackClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.i(TAG, "PlaybackClickListener fired");
            if (stateIs(PlayerState.PREPARING) ||
                ((mCurrentlyPlayingCard.getMedium().equals(Constants.VIDEO) ||
                mCurrentlyPlayingCard.getMedium().equals(Constants.AUDIO)) && mMainPlayer == null)) {
                Toast.makeText(mContext, "Preparing for playback. Try again in a few seconds.", Toast.LENGTH_LONG).show();
                return;
            }

            switch(mState) {
                case PLAYING:
                    pausePlayback();
                    break;
                case READY:
                        try {
                            if (mAudioTracksDirty) prepareSecondaryPlayers();
                            resumePlayback();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;
            }
        }
    };

    private ToggleButton.OnCheckedChangeListener mMuteCheckedListener = new ToggleButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mMuteChecked = isChecked;
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
                Timber.w("ClipCardsPlayer.handleMessage: player is null!");
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

    /**
     * Create a new ClipCardsPlayer to play clipCards in order.
     * Views will be provided to control playback, and their events will be handled internally.
     * All resources will be automatically released when the containing views are destroyed.
     * A client of this class should ordinarily have to take no action after construction.
     *
     * Must be called on the Main thread.
     */
    public ClipCardsPlayer(@NonNull FrameLayout container,
                           @NonNull List<ClipCard> clipCards,
                           @NonNull List<AudioClip> audioClips) {

        mContainerLayout = container;
        mClipCards = clipCards;
        mAudioClips = audioClips;
        init();
    }

    public void setPhotoSlideDurationMs(int durationMs) {
        mPhotoSlideDurationMs = durationMs;
    }

    public void notifyAudioClipsChanged() {
        mAudioTracksDirty = true;
        Timber.d("Audio track changed");
    }

    /**
     * Set whether secondary audio tracks (e.g: Narration, music) should be played.
     * @param shouldPlay
     */
    public void setPlaySecondaryTracks(boolean shouldPlay) {
        mPlaySecondaryTracks = shouldPlay;
    }

    /**
     * Change the playback volume. This does not affect the stored volume data
     * within any clips.
     */
    public void setVolume(float volume) {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.VOLUME, volume));
    }

    protected void _setVolume(float volume) {
        if (volume == MUTE_VOLUME)
            mMuteBtn.setChecked(true);
        else
            mMuteBtn.setChecked(false);

        if (mMainPlayer != null) mMainPlayer.setVolume(volume, volume);
        if (mAudioClipPlayers != null) {
            for (MediaPlayer audioPlayer : mAudioClipPlayers)
                audioPlayer.setVolume(volume, volume);
        }
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
        mPlaySecondaryTracks = true;
        mContext = mContainerLayout.getContext();
        mHandler = new ClipCardsPlayerHandler(this);
        inflateViews(mContainerLayout);
        attachViewListeners();
        mCurrentlyPlayingCard = mClipCards.get(0);
        displayThumbnailForClip(mThumbnailView, mCurrentlyPlayingCard);
        calculateTotalClipCollectionLengthMsAsync(mClipCards);
        setupTimer();
        mStoryPathLibrary = mCurrentlyPlayingCard.getStoryPath().getStoryPathLibrary();
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
        mThumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mPlaybackProgress.setLayoutDirection(View.LAYOUT_DIRECTION_LTR); // Hard code to LTR as video progress shouldn't ever be RTL
        }
        mPlaybackProgress.setEnabled(false);

        /* Timecode label */
        FrameLayout.LayoutParams alignTopParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                             ViewGroup.LayoutParams.WRAP_CONTENT);
        alignTopParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        mTimeCode = new TextView(context);
        mTimeCode.setLayoutParams(alignTopParams);
        mTimeCode.setVisibility(View.VISIBLE);
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

        int topMarginPx = r.getDimensionPixelSize(R.dimen.padding_xlarge);
        int rightMarginPx = r.getDimensionPixelSize(R.dimen.padding_medium);
        alignTopRightParams.setMargins(0, topMarginPx, rightMarginPx, 0);

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
            if (stateIs(PlayerState.PLAYING)) {
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

                int currentClipStartTimeMs = mCurrentlyPlayingCard.getSelectedClip().getStartTime();
                int durationOfPreviousClips = 0;
                int currentlyPlayingCardIndex = mClipCards.indexOf(mCurrentlyPlayingCard);
                if (currentlyPlayingCardIndex > 0)
                    durationOfPreviousClips = accumulatedDurationByMediaCard.get(currentlyPlayingCardIndex - 1);
                if (mPlaybackProgress != null) {
                    mPlaybackProgress.setProgress((int) (mPlaybackProgress.getMax() * ((float) durationOfPreviousClips + currentClipElapsedTime - currentClipStartTimeMs) / mClipCollectionDurationMs)); // Show progress relative to clip collection duration
                }
                mTimeCode.setText(Util.makeTimeString(durationOfPreviousClips + currentClipElapsedTime));
                //Log.i("Timer", String.format("current clip (%d) elapsed time: %d. max photo time: %d. progress: %d", currentlyPlayingCardIndex, currentClipElapsedTime, mPhotoSlideDurationMs, mPlaybackProgress == null ? 0 : mPlaybackProgress.getProgress()));
            }
        } catch (IllegalStateException e) { /* MediaPlayer in invalid state. Ignore */}
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        mSurface = new Surface(surfaceTexture);

        boolean isVideo = false;
        switch (mCurrentlyPlayingCard.getMedium()) {
            case Constants.VIDEO:
                isVideo = true;
            case Constants.AUDIO:
                try {
                    mMainPlayer = new MediaPlayer();
                    prepareMediaPlayer(mMainPlayer, mCurrentlyPlayingCard, isVideo);
                    //mMainPlayer.setSurface(mSurface);
                    //mMainPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    // onCompletionListener is not cleared until #release() is called, so we can safely
                    // attach it once here.
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

        try {
            prepareSecondaryPlayers();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, String.format("Texture size changed to %dx%d", width, height));

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
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
            nextClipIndex++;
            Log.i(TAG, "Advancing to next clip " + nextClipIndex);
        }

        _advanceToClip(player, mClipCards.get(nextClipIndex), !(nextClipIndex == 0));
    }

    /**
     * Advance player to the next clip.
     * If autoPlay, player will be playing after this call completes, else prepared to play.
     */
    protected void _advanceToClip(MediaPlayer player, ClipCard targetClipCard, boolean autoPlay) {
        if (mClipCards.indexOf(targetClipCard) == -1) {
            Timber.e("Invalid Card passed to _advanceToClip");
            return;
        }

        mAdvancingClips = true;
        mCurrentlyPlayingCard = targetClipCard;

        Uri media;
        boolean isVideo = false;
        switch (mCurrentlyPlayingCard.getMedium()) {
            case Constants.VIDEO:
                isVideo = true;
            case Constants.AUDIO:
                mTextureView.setVisibility(View.VISIBLE); // In case previous card wasn't video medium
                media = Uri.parse(mCurrentlyPlayingCard.getSelectedMediaFile().getPath());
                try {
                    // Don't set isPlaying false. We're only 'stopping' to switch media sources
                    player.stop();
                    Log.i(TAG, "Setting player data source " + media.toString());
                    prepareMediaPlayer(player, mCurrentlyPlayingCard, isVideo);
                    //player.setSurface(mSurface);
                    mAdvancingClips = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Constants.PHOTO:
                mCurrentPhotoElapsedTime = 0;
                Log.i(TAG, "set currentelapsedtime to 0");
                // FIXME if Photo, after this method concludes player is not prepared
                // FIXME: Meaning we assume the rest of the ClipCards are also photos
                if (firstClipCurrent()) {
                    _stopPlayback();
                    if (mPlaybackProgress != null) mPlaybackProgress.setProgress(0);
                }
                if (mThumbnailView != null) {
                    displayThumbnailForClip(mThumbnailView, mCurrentlyPlayingCard);
                }
                break;
        }
        try {
            prepareSecondaryPlayers();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mAdvancingClips = false;
        if (autoPlay) _resumePlayback();
    }

    protected void _startPlayback() {
        if (!isNewStateValid(PlayerState.PLAYING)) return; // Don't call #changeUiState as we yield to _resumePlayback

        try {
            if (!mCurrentlyPlayingCard.getMedium().equals(Constants.PHOTO))
                prepareMediaPlayer(mMainPlayer, mCurrentlyPlayingCard, true);
            prepareSecondaryPlayers();
        } catch (IOException e) {
            e.printStackTrace();
        }

        _resumePlayback();
    }

    /**
     * Pause playback, preserving the playback location for a following call to
     * {@link #resumePlayback()}
     */
    private void pausePlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.PAUSE));
    }

    private void _pausePlayback() {
        if (!changeUiState(PlayerState.READY)) return;

        mTextureView.setKeepScreenOn(false);

        if (mMainPlayer != null && mMainPlayer.isPlaying()) {
            mMainPlayer.pause();
        }

        if (mAudioClipPlayers != null) {
            for (MediaPlayer audioPlayer : mAudioClipPlayers) {
                if (audioPlayer.isPlaying()) audioPlayer.pause();
            }
        }
    }

    /**
     * Resume playback at the location determined by a previous call to
     * {@link #pausePlayback()}
     */
    private void resumePlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.RESUME));
    }

    private void _resumePlayback() {
        if (!changeUiState(PlayerState.PLAYING)) return;

        mTextureView.setKeepScreenOn(true);

        if (!mCurrentlyPlayingCard.getMedium().equals(Constants.PHOTO) &&
            mMainPlayer != null &&
            !mMainPlayer.isPlaying()) {

            mMainPlayer.setVolume(mRequestedVolume, mRequestedVolume);
            mMainPlayer.start();
        }

        if (mAudioClipPlayers != null) {
            for (MediaPlayer audioPlayer : mAudioClipPlayers) {
                if (!audioPlayer.isPlaying()) {
                    audioPlayer.setVolume(mRequestedVolume, mRequestedVolume);
                    audioPlayer.start();
                    Timber.d(String.format("Starting audio player for media %s with volume %f",
                            mMediaPlayerToAudioClip.get(audioPlayer).getUuid().substring(0,3), mRequestedVolume));
                } else Timber.d("audioPlayer is already playing on resumePlayback!");
            }
        } else Timber.d("mAudioClipPlayers is null on resumePlayback!");
    }

    protected void _stopPlayback() {
        if (!changeUiState(PlayerState.READY)) return;

        mTextureView.setKeepScreenOn(false);

        if (mMainPlayer != null && mMainPlayer.isPlaying()) {
            mMainPlayer.stop();
            mMainPlayer.reset();
            _advanceToClip(mMainPlayer, mClipCards.get(0), false);
        }

        if (mAudioClipPlayers != null) {
            for (MediaPlayer audioPlayer : mAudioClipPlayers) {
                if (audioPlayer.isPlaying()) {
                    audioPlayer.stop();
                    audioPlayer.reset();
                }
            }
        }
    }

    /**
     * Release all MediaPlayers when playback will no longer be required.
     * No ClipCardsPlayer behavior is defined after this call.
     */
    private void release() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.RELEASE));
    }

    protected void _release() {
        // This is the last method call and so we don't need to update UI state
        if (mMainPlayer != null) {
            mMainPlayer.release();
            mMainPlayer = null;
        }
        if (mAudioClipPlayers != null) {
            for (MediaPlayer audioPlayer : mAudioClipPlayers) {
                audioPlayer.release();
            }
            mAudioClipPlayers.clear();
        }
    }

    public boolean isPlaying() {
        return stateIs(PlayerState.PLAYING);
    }

    /**
     * Set the data source, start time and volume indicated by clipCard. After this call
     * it is safe to call player.start()
     */
    protected void prepareMediaPlayer(@NonNull MediaPlayer player,
                                      @NonNull ClipCard clipCard,
                                      boolean isVideo)
                                      throws IOException {

        prepareMediaPlayer(player, clipCard.getSelectedMediaFile(), clipCard.getSelectedClip(), isVideo);
    }

    protected void prepareMediaPlayer(@NonNull MediaPlayer player,
                                      @NonNull MediaFile mediaFile,
                                      @Nullable ClipMetadata metaData,
                                      boolean isVideo)
                                      throws IOException {
        try {
            Timber.d("Preparing " + (isVideo ? "video" : "audio") + " media player for file " + mediaFile.getPath());
            Uri media = Uri.parse(mediaFile.getPath());
            player.reset();
            player.setDataSource(mContext, media);
            player.prepare();

            if (isVideo) {
                player.setSurface(mSurface);
                adjustAspectRatio(mTextureView, player.getVideoWidth(), player.getVideoHeight());
            }

            if (metaData != null) {
                player.seekTo(metaData.getStartTime());
                // Specified clip volume is set unless player is muted
                if (!mMuteChecked) mRequestedVolume = metaData.getVolume();
            }

            player.setVolume(mRequestedVolume, mRequestedVolume);
        } catch (IOException e) {
            Timber.e("Error preparing mediaplayer for media " + mediaFile.getPath());
            e.printStackTrace();
            throw e;
        }
    }

    protected boolean firstClipCurrent() {
        return mClipCards.indexOf(mCurrentlyPlayingCard) == 0;
    }

    /**
     * Set a thumbnail on the given ImageView for the given ClipCard
     */
    protected void displayThumbnailForClip(@NonNull ImageView thumbnail, @NonNull ClipCard clipCard) {
        // Clip has attached media. Show an appropriate preview
        // e.g: A thumbnail for video

        MediaFile mediaFile = clipCard.getStoryPath().loadMediaFile(clipCard.getSelectedClip().getUuid());
        String medium = clipCard.getMedium();
        switch(medium) {
            case Constants.VIDEO:
            case Constants.AUDIO:
            case Constants.PHOTO:
                mediaFile.loadThumbnail(thumbnail);
                thumbnail.setVisibility(View.VISIBLE);
                break;
            default:
                Timber.w("Cannot fetch thumbnail. Unknown clipcard medium.");
                break;
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
                Timber.d("No clipType matching '" + clipType + "' found.");
                drawable = mContext.getResources().getDrawable(R.drawable.ic_launcher); // FIXME replace with a sensible placeholder image
        }
        imageView.setImageDrawable(drawable);
    }

    private void calculateTotalClipCollectionLengthMsAsync(final List<ClipCard> cards) {

        new AsyncTask<Void, Void, Integer>() {

            @Override
            protected Integer doInBackground(Void... params) {
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
                    Log.i(TAG, String.format("Got duration for media: %d. total %d", clipDuration, totalDuration));
                }
                Log.i(TAG, "Total duration " + totalDuration);
                return totalDuration;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (result == -1) {
                    Timber.e("Unable to calculate ClipCard list duration! ClipCardsPlayer will not properly function");
                } else {
                    mClipCollectionDurationMs = result;
                    changeUiState(PlayerState.READY);
                }
            }
        }.execute();
    }

    /**
     * Sets the TextureView transform to preserve the aspect ratio specified by the
     * given width and height.
     *
     * Courtesy of Andrew McFadden's grafika project:
     * https://github.com/google/grafika/blob/master/src/com/android/grafika/PlayMovieActivity.java
     */
    public static void adjustAspectRatio(TextureView textureView, int width, int height) {
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
        Log.v("Aspect", "video=" + width + "x" + height +
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

    /**
     * Change the UI state of the class if newState is a valid transition from the current state.
     * This will NOT affect the state of the MediaPlayers or their progress Seekbars.
     * This is intentional as the "pause" and "stop" states are identical in terms of the UI actions to perform
     * Must be called from the main thread.
     * @return whether the state change was successful.
     */
    protected boolean changeUiState(PlayerState newState) {
        if (!isNewStateValid(newState)) {
            Timber.e(String.format("Cannot advance to state %s from %s", newState, mState));
            return false;
        }

        mState = newState;

        updateUI();
        return true;
    }

    private void updateUI() {
        switch(mState) {
            case PREPARING:
                // We are contructed into the PREPARING state and should never transition into it.
                break;
            case PLAYING:
                mPlayBtn.setVisibility(View.GONE);
                switch(mCurrentlyPlayingCard.getMedium()) {
                    case Constants.VIDEO:
                        mThumbnailView.setVisibility(View.GONE);
                        break;
                    case Constants.AUDIO:
                    case Constants.PHOTO:
                        displayThumbnailForClip(mThumbnailView, mCurrentlyPlayingCard);
                        break;
                }
                break;
            case READY:
                mPlayBtn.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Return whether newState is a valid transition from current state
     */
    private boolean isNewStateValid(PlayerState newState) {
        // Allow 'changing' to same state. This allows us to avoid an ADVANCING state which is
        // currently managed as a sub-state within PLAYING via the mAdvancingClips variable.
        // Advancing happens very quickly and synchronously so I view this oddity as the less
        // complex and more maintainable solution
        if (mState == newState) return true;

        switch(mState) {
            case PREPARING:
                return newState == PlayerState.READY;
            case PLAYING:
                return newState == PlayerState.READY;
            case READY:
                return newState == PlayerState.PLAYING;
        }
        return false;
    }

    protected boolean stateIs(PlayerState targetState) {
        return targetState == mState;
    }

    private void prepareSecondaryPlayers() throws IOException {
        if (mAudioClips.size() > 0 && mPlaySecondaryTracks) {
            StoryPathLibrary spl = mClipCards.get(0).getStoryPath().getStoryPathLibrary();
            // TODO : All secondary audio tracks should have associated MediaFile and ClipMetadata
            stopFinishedAudioMediaPlayers();
            for (int x = 0; x < mAudioClips.size(); x++) {

                AudioClip audioClip = mAudioClips.get(x);

                if (spl.getFirstClipCardForAudioClip(audioClip, mClipCards).equals(mCurrentlyPlayingCard)) {
                    Timber.d("Found AudioTrack matching ClipCard at pos " + mClipCards.indexOf(mCurrentlyPlayingCard));
                    MediaPlayer audioPlayer = fetchAudioMediaPlayer();
                    mAudioClipToMediaPlayer.put(audioClip, audioPlayer);
                    mMediaPlayerToAudioClip.put(audioPlayer, audioClip);
                    MediaFile audioMediaFile = mStoryPathLibrary.getMediaFile(audioClip.getUuid());
                    prepareMediaPlayer(audioPlayer,
                                       audioMediaFile,
                                       null,
                                       false);
                }
            }
            mAudioTracksDirty = false;
        }
    }

    /**
     * @return a MediaPlayer for audio playback. This will be a recycled instance if possible.
     * Handles registering newly created MediaPlayers with {@link #mAudioClipPlayers} and removing
     * recycled players from {@link #mAudioClipToMediaPlayer} and {@link #mMediaPlayerToAudioClip}.
     *
     * It is the caller's responsibility to register returned instances with {@link #mAudioClipToMediaPlayer}
     * and {@link #mMediaPlayerToAudioClip}
     */
    private MediaPlayer fetchAudioMediaPlayer() {
        for (MediaPlayer player : mAudioClipPlayers) {
            if (player != null && !player.isPlaying()) {

                AudioClip audioToRemove = mMediaPlayerToAudioClip.remove(player);
                mAudioClipToMediaPlayer.remove(audioToRemove);

                Timber.d("Recycling audio MediaPlayer at index " + mAudioClipPlayers.indexOf(player));
                return player;
            }

        }

        Timber.d("Initializing new audio MediaPlayer");
        MediaPlayer newPlayer = new MediaPlayer();
        mAudioClipPlayers.add(newPlayer);
        return newPlayer;
    }

    /**
     * Stop any audio MediaPlayers whose AudioClips indicate they should not be playing
     * on {@link #mCurrentlyPlayingCard}
     */
    private void stopFinishedAudioMediaPlayers() {
        StoryPathLibrary library = mClipCards.get(0).getStoryPath().getStoryPathLibrary();

        for (MediaPlayer player : mAudioClipPlayers) {
            AudioClip audioClip = mMediaPlayerToAudioClip.get(player);

            if (!library.isClipCardWithinAudioClipRange(mCurrentlyPlayingCard,
                                                       audioClip,
                                                       mClipCards)) {
                Timber.d("Stopping finished AudioClip " + audioClip.getUuid());
                player.stop();
            }
        }
    }
}
