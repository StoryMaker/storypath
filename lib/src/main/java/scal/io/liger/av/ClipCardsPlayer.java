package scal.io.liger.av;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

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

/**
 * Plays a collection of ClipCards, as well as a secondary audio track.
 *
 * Created by davidbrodsky on 12/12/14.
 */
public class ClipCardsPlayer implements TextureView.SurfaceTextureListener {
    public final String TAG = getClass().getSimpleName();

    protected Context mContext;
    protected FrameLayout mContainerLayout;
    private List<ClipCard> mClipCards;
    private ArrayList<Integer> accumulatedDurationByMediaCard;
    protected ClipCard mCurrentlyPlayingCard;
    protected MediaPlayer mMainPlayer;
    protected MediaPlayer mSecondaryPlayer;
    private Uri mSecondaryAudioUri;
    private Surface mSurface;
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

    private final int TIMER_INTERVAL_MS = 100;

    private View.OnClickListener PlaybackClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (( mCurrentlyPlayingCard.getMedium().equals(Constants.VIDEO) ||
                  mCurrentlyPlayingCard.getMedium().equals(Constants.AUDIO)) && mMainPlayer == null) return;

            if (mIsPlaying) {
                pausePlayback();
            }
            else if (mIsPaused){
                resumePlayback();
            }
            else {
                startPlayback();
            }
        }
    };

    protected static class ClipCardsPlayerHandler extends Handler {

        public static final int START   = 0;
        public static final int RESUME  = 1;
        public static final int PAUSE   = 2;
        public static final int STOP    = 3;
        public static final int ADVANCE = 4;
        public static final int RELEASE = 5;
        public static final int TIMER   = 6;

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

    // </editor-fold desc="Public API">

    private void init() {
        mContext = mContainerLayout.getContext();
        mHandler = new ClipCardsPlayerHandler(this);
        setupViews(mContainerLayout);
        mCurrentlyPlayingCard = mClipCards.get(0);
        setThumbnailForClip(mThumbnailView, mCurrentlyPlayingCard);
        mThumbnailView.setOnClickListener(PlaybackClickListener);
        mTextureView.setOnClickListener(PlaybackClickListener);
        mClipCollectionDurationMs = calculateTotalClipCollectionLengthMs(mClipCards);
        setupTimer();
    }

    private void setupViews(@NonNull FrameLayout root) {
        Context context = root.getContext();
        FrameLayout.LayoutParams matchParentParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                             ViewGroup.LayoutParams.MATCH_PARENT);

        mThumbnailView = new ImageView(context);
        mThumbnailView.setLayoutParams(matchParentParams);

        mTextureView = new TextureView(context);
        mTextureView.setLayoutParams(matchParentParams);
        mTextureView.setSurfaceTextureListener(this);

        FrameLayout.LayoutParams alignBottomParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        alignBottomParams.gravity = Gravity.BOTTOM;
        mPlaybackProgress = new SeekBar(context);
        mPlaybackProgress.setLayoutParams(alignBottomParams);
        mPlaybackProgress.setEnabled(false);

        root.addView(mTextureView);
        root.addView(mThumbnailView);
        root.addView(mPlaybackProgress);
    }

    private void setupTimer() {
        // Poll MediaPlayer for position, ensuring it never exceeds stop point indicated by ClipMetadata
        if (mTimer != null) mTimer.cancel(); // should never happen but JIC
        mTimer = new Timer("mplayer");
        mTimer.schedule(new TimerTask() {
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
                Log.i("Timer", "isPlaying");
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
                Log.i("Timer", String.format("current clip (%d) elapsed time: %d. max photo time: %d. progress: %d", currentlyPlayingCardIndex, currentClipElapsedTime, mPhotoSlideDurationMs, mPlaybackProgress == null ? 0 : mPlaybackProgress.getProgress()));
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
                    mMainPlayer.prepareAsync();
                    mMainPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    mMainPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mAdvancingClips = false;
                            mMainPlayer.seekTo(mCurrentlyPlayingCard.getSelectedClip().getStartTime());

                            int currentClipIdx = mClipCards.indexOf(mCurrentlyPlayingCard);

                            if (currentClipIdx != 0) {
                                mMainPlayer.start();
                            } else {
                                mIsPlaying = false;
                                mIsPaused = false; // Next touch should initiate startPlaying
                                Log.i(TAG, "onPrepared setting isPaused false");
                            }
                        }
                    });
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

        Uri media;
        int currentClipIdx = mClipCards.indexOf(mCurrentlyPlayingCard);
        if (currentClipIdx == (mClipCards.size() - 1)) {
            mIsPlaying = false;
            // We've played through all the clips
//            if (mRecordNarrationState == RecordNarrationState.RECORDING) {
//                MediaFile mf = stopRecordingNarration();
//                mCardModel.setNarration(mf);
//                ivThumbnail.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        changeRecordNarrationStateChanged(RecordNarrationState.STOPPED);
//                    }
//                });
//            }
            // TODO StopPlayback callback
            // We've played through all the clips
            Log.i(TAG, "Played all clips. stopping");
            _stopPlayback();
        } else {
            // Advance to next clip
            mCurrentlyPlayingCard = mClipCards.get(++currentClipIdx);
            Log.i(TAG, "Advancing to next clip " + mClipCards.indexOf(mCurrentlyPlayingCard));
        }

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
                    player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case Constants.PHOTO:
                Log.i(TAG, "set currentelapsedtime to 0");
                if (mClipCards.indexOf(mCurrentlyPlayingCard) == 0) {
                    // Stop playback. With video / audio this would be handled onPreparedListener
                    mIsPlaying = false;
                    _stopPlayback();
                }
                if (mThumbnailView != null) {
                    mThumbnailView.post(new Runnable() {
                        @Override
                        public void run() {
                            setThumbnailForClip(mThumbnailView, mCurrentlyPlayingCard);
                            if (!mIsPlaying && mPlaybackProgress != null) mPlaybackProgress.setProgress(0);
                            mAdvancingClips = false;
                        }
                    });
                }
                break;
        }
    }

    protected void startPlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.START));
    }

    protected void _startPlayback() {
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

    private void pausePlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.PAUSE));
    }

    private void _pausePlayback() {
        if (mMainPlayer != null && mMainPlayer.isPlaying()) {
            mMainPlayer.pause();
        }

        if (mSecondaryPlayer != null && mSecondaryPlayer.isPlaying()) {
            mSecondaryPlayer.pause();
        }

        mIsPaused = true;
        mIsPlaying = false;
    }

    private void resumePlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.RESUME));
    }

    private void _resumePlayback() {
        if (mMainPlayer != null && !mMainPlayer.isPlaying()) {
            mMainPlayer.start();
        }

        if (mSecondaryPlayer != null && mSecondaryPlayer.isPlaying()) {
            mSecondaryPlayer.pause();
        }

        mIsPaused = false;
        mIsPlaying = true;
    }

    protected void stopPlayback() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.STOP));
    }

    protected void _stopPlayback() {
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

    private void release() {
        mHandler.sendMessage(mHandler.obtainMessage(ClipCardsPlayerHandler.RELEASE));
    }

    private void _release() {
        if (mMainPlayer != null) mMainPlayer.release();
        if (mSecondaryPlayer != null) mSecondaryPlayer.release();
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
}
