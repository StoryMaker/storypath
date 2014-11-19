package scal.io.liger.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import scal.io.liger.Constants;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.ClipMetadata;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.ReviewCard;

/**
 * ReviewCardView allows the user to review the order of clips
 * attached to a ReviewCard's Story Path. This card will also support
 * adding narration, changing the order of clips, and jumbling the order.
 */
public class ReviewCardView implements DisplayableCard {
    public static final String TAG = "ReviewCardView";

    private ReviewCard mCardModel;
    private Context mContext;
    private String mMedium;

    /** Current Narration State. To change use
     * {@link #changeRecordNarrationStateChanged(scal.io.liger.view.ReviewCardView.RecordNarrationState)}
    */
    private RecordNarrationState mRecordNarrationState = RecordNarrationState.READY;

    /** The ReviewCard Narration Dialog State */
    static enum RecordNarrationState {

        /** Done / Record Buttons present */
        READY,

        /** Recording countdown then Pause / Stop Buttons present */
        RECORDING,

        /** Recording paused. Resume / Stop Buttons present */
        PAUSED,

        /** Recording stopped. Done / Redo Buttons present */
        STOPPED

    }

    public ReviewCardView(Context context, Card cardModel) {
        Log.d("RevieCardView", "constructor");
        mContext = context;
        mCardModel = (ReviewCard) cardModel;
    }

    @Override
    public View getCardView(final Context context) {
        Log.d("RevieCardView", "getCardView");
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_review, null);
        final ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_thumbnail));
        final TextureView tvCardVideo = ((TextureView) view.findViewById(R.id.tv_card_video));

        Button btnJumble = ((Button) view.findViewById(R.id.btn_jumble));
        Button btnOrder = ((Button) view.findViewById(R.id.btn_order));
        Button btnNarrate = ((Button) view.findViewById(R.id.btn_narrate));
        Button btnPublish = ((Button) view.findViewById(R.id.btn_publish));

        btnJumble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaCards.size() > 0)
                    Util.showOrderMediaPopup((Activity) mContext, mMedium, mMediaCards);
                else
                    Toast.makeText(mContext, mContext.getString(R.string.add_clips_before_reordering), Toast.LENGTH_SHORT).show();
            }
        });

        btnNarrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMediaCards.size() > 0)
                    showClipNarrationDialog();
                else
                    Toast.makeText(mContext, mContext.getString(R.string.add_clips_before_narrating), Toast.LENGTH_SHORT).show();
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) mCardModel.getStoryPath().getContext(); // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())
                Util.startPublishActivity(mainActivity, mCardModel.getStoryPath());
            }
        });

        // Initialize mMediaCards from current StoryPath
        getClipCardsWithAttachedMedia();

        if (mMediaCards.size() > 0) {
            switch (((ClipCard) mMediaCards.get(0)).getMedium()) {
                case Constants.VIDEO:
                case Constants.PHOTO:
                case Constants.AUDIO:
                    // For now the SurfaceTextureListener handles all kinds of media playback and interaction
                    // TODO Move as much logic as possible to a "MixedMediaPlayer" that will handle the ImageView / TextureView
                    // as asppropriate for Audio, Video, and Photos
                    setThumbnailForClip(ivCardPhoto, (ClipCard) mMediaCards.get(0));
                    ivCardPhoto.setVisibility(View.VISIBLE);
                    tvCardVideo.setVisibility(View.VISIBLE);
                    tvCardVideo.setSurfaceTextureListener(new VideoWithNarrationSurfaceTextureListener(tvCardVideo, mMediaCards, ivCardPhoto));
                    break;
            }
        } else {
            Log.e(TAG, "No Clips available");
        }

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    /**
     * Initializes the value of {@link #mMediaCards} to a List of ClipCards with attached media
     * within the current StoryPath
     */
    private void getClipCardsWithAttachedMedia() {
        ArrayList<Card> mediaCards = mCardModel.getStoryPath().gatherCards("<<ClipCard>>");
        Iterator iterator = mediaCards.iterator();
        while (iterator.hasNext()) {
            ClipCard clipCard = (ClipCard) iterator.next();
            mMedium = clipCard.getMedium();
            if ( clipCard.getClips() == null || clipCard.getClips().size() < 1 ) {
                iterator.remove();
            }
        }
        mMediaCards = mediaCards;
    }

    /** Record Narration Dialog */

    /** Collection of ClipCards with attached media within the current StoryPath. */
    ArrayList<Card> mMediaCards;

    /** Records audio */
    MediaRecorder mMediaRecorder;
    File mNarrationOutput;

    /** Record Narration Dialog Views */
    Button mDonePauseResumeBtn;
    Button mRecordStopRedoBtn;


    /**
     * Show a dialog allowing the user to record narration for a Clip
     */
    private void showClipNarrationDialog() {
        View v = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_clip_narration, null);

        /** Narrate dialog views */
        final TextureView videoView     = (TextureView) v.findViewById(R.id.textureView);
        final ImageView thumbnailView   = (ImageView) v.findViewById(R.id.thumbnail);
        final TextView clipLength       = (TextView) v.findViewById(R.id.clipLength);
        final SeekBar playbackBar       = (SeekBar) v.findViewById(R.id.playbackProgress);
        mDonePauseResumeBtn             = (Button) v.findViewById(R.id.donePauseResumeButton);
        mRecordStopRedoBtn              = (Button) v.findViewById(R.id.recordStopRedoButton);

        /** Configure views for initial state */
        changeRecordNarrationStateChanged(RecordNarrationState.READY);

        final VideoWithNarrationSurfaceTextureListener surfaceListener = new VideoWithNarrationSurfaceTextureListener(videoView, mMediaCards, thumbnailView);
        videoView.setSurfaceTextureListener(surfaceListener);
        surfaceListener.setPlaybackProgressSeekBar(playbackBar);

        clipLength.setText("Total: " + Util.makeTimeString(surfaceListener.getTotalClipCollectionDuration()));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(v);
        final Dialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.i(TAG, "Narrate Dialog Dismissed. Cleaning up");
                surfaceListener.timer.cancel();
                releaseMediaRecorder();
            }
        });
        dialog.show();

        mDonePauseResumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mRecordNarrationState) {
                    case STOPPED:
                    case READY:
                        // Done Button
                        dialog.dismiss();
                        // TODO Attach narration to clip if not already done
                        break;
                    case RECORDING:
                        // Pause Button
                        // TODO Pausing & resuming a recording will take some extra effort
                        //pauseRecordingNarration(player);
                        Toast.makeText(mContext, "Not yet supported", Toast.LENGTH_SHORT).show();
                        break;
                    case PAUSED:
                        // Resume Button
                        // TODO See above
                        Toast.makeText(mContext, "Not yet supported", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        mRecordStopRedoBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (mRecordNarrationState) {
                    case READY:
                        // Record Button
                        // TODO Show recording countdown first
                        switch (surfaceListener.currentlyPlayingCard.getMedium()) {
                            case Constants.VIDEO:
                            case Constants.AUDIO:
                                // TODO : If audio, show level meter or something
                                if(thumbnailView.getVisibility() == View.VISIBLE) {
                                    thumbnailView.setVisibility(View.GONE);
                                }
                            break;
                        }

                        surfaceListener.stopPlayback();
                        changeRecordNarrationStateChanged(RecordNarrationState.RECORDING);

                        startRecordingNarration();
                        surfaceListener.startPlayback();
                        surfaceListener.isPlaying = true;
                        break;
                    case RECORDING:
                    case PAUSED:
                        // Stop Button
                        surfaceListener.stopPlayback();
                        surfaceListener.isPlaying = false;
                        stopRecordingNarration();
                        changeRecordNarrationStateChanged(RecordNarrationState.STOPPED);
                        break;
                    case STOPPED:
                        // Redo Button
                        // TODO Show recording countdown first
                        // TODO reset player to first clip
                        surfaceListener.stopPlayback();
                        startRecordingNarration();
                        changeRecordNarrationStateChanged(RecordNarrationState.RECORDING);
                        surfaceListener.startPlayback(); // Make sure to call this after changing state to RECORDING
                        surfaceListener.isPlaying = true;
                        break;
                }
            }
        });
    }

    /**
     * Set a thumbnail on the given ImageView for the given ClipCard
     */
    private void setThumbnailForClip(@NonNull ImageView thumbnail, @NonNull ClipCard clipCard) {
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
            String clipType = clipCard.getClipType();
            setClipExampleDrawables(clipType, thumbnail);
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

    /**
     * Start recording an audio narration track synced and instruct player to
     * begin playback simultaneously.
     */
    private void startRecordingNarration() {
        DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String now = df.format(new Date());
        mNarrationOutput = new File(Environment.getExternalStorageDirectory(), now + /*".aac"*/ ".mp4");

        if (mMediaRecorder == null) mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioEncodingBitRate(96 * 1000);
        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // raw AAC ADTS container records properly but results in Unknown MediaPlayer errors when playback attempted. :/
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(mNarrationOutput.getAbsolutePath());

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            Toast.makeText(mContext, "Recording Narration", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
//        mClipCollectionPlayer.setVolume(0, 0); // Mute track volume while recording narration
//        mClipCollectionPlayer.start();
    }

    /**
     * Stop and reset {@link #mMediaRecorder} so it may be used again
     */
    private void stopRecordingNarration() {
//        if (mClipCollectionPlayer.isPlaying()) mClipCollectionPlayer.pause();
//        mClipCollectionPlayer.setVolume(1, 1); // Restore track volume when finished recording narration

        mMediaRecorder.stop();
        mMediaRecorder.reset();

        // Attach the just-recorded narration to ReviewCard
        MediaFile narrationMediaFile = new MediaFile(mNarrationOutput.getAbsolutePath(), Constants.AUDIO);
        mCardModel.setNarration(narrationMediaFile);
        Log.i(TAG, "Saving narration file " + mNarrationOutput.getAbsolutePath());
    }

    private void pauseRecordingNarration(MediaPlayer player) {
        throw new UnsupportedOperationException("Pausing and resuming a recording is not yet supported!");
        // TODO This is going to require Android 4.3's MediaCodec APIs or
        // TODO file concatenation of multiple recordings.
    }

    /**
     * Release {@link #mMediaRecorder} when no more recordings will be made
     */
    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) mMediaRecorder.release();
    }

    /**
     * Update the UI in response to a new value assignment to {@link #mRecordNarrationState}
     */
    private void changeRecordNarrationStateChanged(RecordNarrationState newState) {
        mRecordNarrationState = newState;
        switch(mRecordNarrationState) {
            case READY:
                mRecordStopRedoBtn.setText(R.string.dialog_record);
                mDonePauseResumeBtn.setText(R.string.dialog_done);
                break;
            case RECORDING:
                mRecordStopRedoBtn.setText(R.string.dialog_stop);
                mDonePauseResumeBtn.setText(R.string.dialog_pause);
                break;
            case PAUSED:
                mRecordStopRedoBtn.setText(R.string.dialog_stop);
                mDonePauseResumeBtn.setText(R.string.dialog_resume);
                break;
            case STOPPED:
                mRecordStopRedoBtn.setText(R.string.dialog_redo);
                mDonePauseResumeBtn.setText(R.string.dialog_done);
                break;
        }
    }


    /** A convenience SurfaceTextureListener to configure a TextureView for video playback of a Collection
     * of ClipCards. Note the constructor argument is a list of Cards due to the nature of {@link scal.io.liger.model.StoryPath#gatherCards(String)}
     * and inability to cast a typed Collection.
    */
    private abstract class VideoSurfaceTextureListener implements TextureView.SurfaceTextureListener {

        Surface surface;

        protected MediaPlayer mediaPlayer;
        protected volatile boolean isPlaying; // Need a separate variable as images won't be processed by the MediaPlayer
        protected boolean isPaused = false;

        protected ClipCard currentlyPlayingCard;
        protected final List<Card> mediaCards;
        /**
         * Collection of cumulative duration parallel to mMediaCards.
         * e.g: the first entry is the duration of the first clip, the second is the duration
         * of the first and second clips etc. Used to properly report playback progress relative to
         * entire mMediaCards collection.
         */
        ArrayList<Integer> accumulatedDurationByMediaCard;
        final AtomicInteger clipCollectionDuration = new AtomicInteger();

        final ImageView ivThumbnail;
        final TextureView tvVideo;
        SeekBar sbPlaybackProgress;

        Timer timer;
        final int TIMER_INTERVAL_MS = 100;
        volatile int currentPhotoElapsedTime; // Keep track of how long current photo has been "playing" so we can advance state

        volatile boolean advancingClip = false;

        public VideoSurfaceTextureListener(@NonNull TextureView textureView, @NonNull List<Card> mediaCards, @Nullable ImageView thumbnailView) {
            ivThumbnail = thumbnailView;
            tvVideo = textureView;
            this.mediaCards = mediaCards;
            init();
        }

        /** Set a SeekBar to represent playback progress. May be set at any time
         */
        public void setPlaybackProgressSeekBar(SeekBar progress) {
            sbPlaybackProgress = progress;
        }

        /**
         * Return the total duration of all clips in the passed collection, accounting for start and
         * stop trim times. Will return a valid value any time after construction.
         */
        public int getTotalClipCollectionDuration() {
            return clipCollectionDuration.get();
        }

        private void init() {
            // Setup views
            currentlyPlayingCard = (ClipCard) mediaCards.get(0);
            setThumbnailForClip(ivThumbnail, currentlyPlayingCard);
            ivThumbnail.setOnClickListener(getVideoPlaybackToggleClickListener());
            tvVideo.setOnClickListener(getVideoPlaybackToggleClickListener());

            clipCollectionDuration.set(calculateTotalClipCollectionLengthMs(mediaCards));
            setupTimer();
        }

        private void setupTimer() {
            // Poll MediaPlayer for position, ensuring it never exceeds stop point indicated by ClipMetadata
            if (timer != null) timer.cancel(); // should never happen but JIC
            timer = new Timer("mplayer");
            timer.schedule(new TimerTask() {
                               @Override
                               public void run() {
                                   try {
                                       if (isPlaying ) {
                                           int currentClipElapsedTime = 0;
                                           switch (currentlyPlayingCard.getMedium()) {
                                               case Constants.VIDEO:
                                               case Constants.AUDIO:
                                                   currentClipElapsedTime = Math.min(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());// Seen issues where getCurrentPosition returns
                                                   if (currentClipElapsedTime == 1957962536) {
                                                       Log.i("Timer", "WTF");
                                                   }
                                                   // If getStopTime() is equal to 0 or mediaPlayer.getDuration(), clip advancing will be handled by the MediaPlayer onCompletionListener
                                                   int clipStopTimeMs = currentlyPlayingCard.getSelectedClip().getStopTime();
                                                   if (!advancingClip && clipStopTimeMs > 0 && currentClipElapsedTime > clipStopTimeMs && clipStopTimeMs != mediaPlayer.getDuration()) {
                                                       //mediaPlayer.pause();
                                                       Log.i(TAG, String.format("video timer advancing clip. Clip stop time: %d. MediaPlayer duration: %d", clipStopTimeMs, mediaPlayer.getDuration()));
                                                       advanceToNextClip(mediaPlayer);
                                                   } else if (advancingClip) {
                                                       // MediaPlayer is transitioning and cannot report progress
                                                       Log.i("Timer", "MediaPlayer is advancing, using currentClipElapsedTime of 0");
                                                       currentClipElapsedTime = 0;
                                                   }
                                                   break;
                                               case Constants.PHOTO:
                                                   //Log.i("Timer", String.format("Photo elapsed time %d / %d", currentPhotoElapsedTime, mCardModel.getStoryPath().getStoryPathLibrary().photoSlideDurationMs));
                                                   currentPhotoElapsedTime += TIMER_INTERVAL_MS; // For Photo cards, this is reset on each call to advanceToNextClip
                                                   currentClipElapsedTime = currentPhotoElapsedTime;

                                                   if (!advancingClip && currentClipElapsedTime > mCardModel.getStoryPath().getStoryPathLibrary().photoSlideDurationMs) {
                                                       //Log.i("Timer", "advancing photo");
                                                       Log.i(TAG, "photo timer advancing clip");
                                                       advanceToNextClip(null);
                                                       currentPhotoElapsedTime = 0;
                                                       currentClipElapsedTime = 0;
                                                   }
                                                   break;
                                           }

                                           int durationOfPreviousClips = 0;
                                           int currentlyPlayingCardIndex = mediaCards.indexOf(currentlyPlayingCard);
                                           if (currentlyPlayingCardIndex > 0)
                                               durationOfPreviousClips = accumulatedDurationByMediaCard.get(currentlyPlayingCardIndex - 1);
                                           if (sbPlaybackProgress != null) {
                                               sbPlaybackProgress.setProgress((int) (sbPlaybackProgress.getMax() * ((float) durationOfPreviousClips + currentClipElapsedTime) / clipCollectionDuration.get())); // Show progress relative to clip collection duration
                                           }
                                           Log.i("Timer", String.format("current clip (%d) elapsed time: %d. max photo time: %d. progress: %d", currentlyPlayingCardIndex, currentClipElapsedTime, mCardModel.getStoryPath().getStoryPathLibrary().photoSlideDurationMs, sbPlaybackProgress == null ? 0 : sbPlaybackProgress.getProgress()));
                                       }
                                   } catch (IllegalStateException e) { /* MediaPlayer in invalid state. Ignore */}
                               }
                           },
            50,                   // Initial delay ms
            TIMER_INTERVAL_MS);   // Repeat interval ms
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            surface = new Surface(surfaceTexture);

            switch (currentlyPlayingCard.getMedium()) {
                case Constants.VIDEO:
                case Constants.AUDIO:
                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(mContext, Uri.parse(currentlyPlayingCard.getSelectedMediaFile().getPath()));
                        mediaPlayer.setSurface(surface);
                        mediaPlayer.prepareAsync();
                        mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                advancingClip = false;
                                mediaPlayer.seekTo(currentlyPlayingCard.getSelectedClip().getStartTime());

                                int currentClipIdx = mediaCards.indexOf(currentlyPlayingCard);

                                if (currentClipIdx != 0) {
                                    mediaPlayer.start();
                                } else {
                                    isPlaying = false;
                                    isPaused = false; // Next touch should initiate startPlaying
                                    Log.i(TAG, "onPrepared setting isPaused false");
                                }
                            }
                        });
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                Log.i(TAG, "mediaplayer onComplete. advancing clip");
                                if (!advancingClip) advanceToNextClip(mp);
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
            Log.i(TAG, "surfaceTexture destroyed. releasing MediaPlayer and Timer");
            mediaPlayer.release();
            timer.cancel();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }

        /**
         * Calculates the cumulative length in ms of each selected clip of each Card in cards.
         * Also populates {@link #accumulatedDurationByMediaCard}.
         *
         * @param cards An ArrayList of {@link scal.io.liger.model.ClipCard}s.
         *              Only cards of type ClipCard will be evaluated.
         *
         * Note we don't accept an ArrayList<ClipCard> due to the current operation of StoryPath#gatherCards(..)
         */
        private int calculateTotalClipCollectionLengthMs(List<Card> cards) {
            int totalDuration = 0;
            int clipDuration;
            accumulatedDurationByMediaCard = new ArrayList<>(cards.size());
            for (Card card : cards) {
                if (card instanceof ClipCard) {
                    clipDuration = 0;
                    switch (((ClipCard) card).getMedium()) {
                        case Constants.VIDEO:
                            ClipMetadata clipMeta = ((ClipCard) card).getSelectedClip();
                            MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(((ClipCard)card).getSelectedMediaFile().getPath()));
                            clipDuration = ( clipMeta.getStopTime() == 0 ? mp.getDuration() : (clipMeta.getStopTime() - clipMeta.getStartTime()) );
                            mp.release();
                            break;
                        case Constants.PHOTO:
                            clipDuration += mCardModel.getStoryPath().getStoryPathLibrary().photoSlideDurationMs;
                            break;
                    }
                    totalDuration += clipDuration;
                    accumulatedDurationByMediaCard.add(totalDuration);
                    Log.i(TAG, String.format("Got duration for media: %d", clipDuration));
                    Log.i(TAG, "Total duration now " + totalDuration);
                }
            }
            return totalDuration;
        }

        /** Craft a OnClickListener to toggle video playback and thumbnail visibility */
        protected abstract View.OnClickListener getVideoPlaybackToggleClickListener();

        /**
         * Advance the given MediaPlayer to the next clip in mMediaCards
         * and ends with a call to {@link android.media.MediaPlayer#prepare()}. Therefore
         * an OnPreparedListener must be set on MediaPlayer to start playback
         *
         * If this is a Photo card player may be null.
         */
        protected abstract void advanceToNextClip(@Nullable MediaPlayer player);
    }

    private class VideoWithNarrationSurfaceTextureListener extends VideoSurfaceTextureListener {

        MediaPlayer narrationPlayer;

        public VideoWithNarrationSurfaceTextureListener(@NonNull TextureView textureView, @NonNull List<Card> mediaCards, @Nullable ImageView thumbnailView) {
            super(textureView, mediaCards, thumbnailView);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            release();
            return false;
        }

        /** Craft a OnClickListener to toggle video playback and thumbnail visibility */
        @Override
        protected View.OnClickListener getVideoPlaybackToggleClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentlyPlayingCard.getMedium().equals(Constants.VIDEO) && mediaPlayer == null) return;

                    if (isPlaying) {
                        pausePlayback();
                        isPaused = true;
                        isPlaying = false;
                    }
                    else if (isPaused){
                        // paused
                        resumePlayback();
                        isPaused = false;
                        isPlaying = true;
                    }
                    else {
                        // stopped
                        startPlayback();
                        isPlaying = true;
                    }
                }
            };
        }

        private void startPlayback() {
            // Connect narrationPlayer to narration mediaFile on each request to start playback
            // to ensure we have the most current narration recording
            if (mCardModel.getSelectedNarrationFile() != null && !mRecordNarrationState.equals(RecordNarrationState.RECORDING)) {
                Uri narrationUri = Uri.parse(mCardModel.getSelectedNarrationFile().getPath());
                if (narrationPlayer == null) narrationPlayer = MediaPlayer.create(mContext, narrationUri);
                else {
                    // TODO Only necessary if narration media file changed
                    try {
                        narrationPlayer.reset();
                        narrationPlayer.setDataSource(mContext, narrationUri);
                        narrationPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.i(TAG, "Starting narration player for uri " + narrationUri.toString());
                narrationPlayer.start();
            }

            switch(currentlyPlayingCard.getMedium()) {
                case Constants.VIDEO:
                    // Mute the main media volume if we're recording narration
                    if (mRecordNarrationState.equals(RecordNarrationState.RECORDING)) {
                        mediaPlayer.setVolume(0, 0);
                    } else {
                        mediaPlayer.setVolume(1, 1);
                    }

                    mediaPlayer.start();

                    if (ivThumbnail.getVisibility() == View.VISIBLE) {
                        ivThumbnail.setVisibility(View.GONE);
                    }
                    break;
                case Constants.PHOTO:
                    ivThumbnail.setVisibility(View.VISIBLE);
                    setThumbnailForClip(ivThumbnail, currentlyPlayingCard);
                    break;
            }
        }

        private void pausePlayback() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }

            if (narrationPlayer != null && narrationPlayer.isPlaying()) {
                narrationPlayer.pause();
            }
        }

        private void resumePlayback() {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }

            if (narrationPlayer != null && !narrationPlayer.isPlaying()) {
                narrationPlayer.start();
            }
        }

        private void stopPlayback() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }

            if (narrationPlayer != null && narrationPlayer.isPlaying()) {
                narrationPlayer.stop();
                narrationPlayer.reset();
            }
            currentlyPlayingCard = (ClipCard) mediaCards.get(0);
            currentPhotoElapsedTime = 0;
        }

        private void release() {
            if (narrationPlayer != null) narrationPlayer.release();
            if (mediaPlayer != null) mediaPlayer.release();
        }

        /**
         * Override to advance recording state if playback is complete
         */
        @Override
        protected void advanceToNextClip(MediaPlayer player) {
            advancingClip = true;

            int currentClipIdx = mediaCards.indexOf(currentlyPlayingCard);
            if (currentClipIdx == (mediaCards.size() - 1)) {
                // We've played through all the clips
                if (mRecordNarrationState == RecordNarrationState.RECORDING) {
                    stopRecordingNarration();
                    ivThumbnail.post(new Runnable() {
                        @Override
                        public void run() {
                            changeRecordNarrationStateChanged(RecordNarrationState.STOPPED);
                        }
                    });
                }
            }
            // We need to check the current clip index *before* calling super(), which will advance it
            Uri video;
            if (currentClipIdx == (mediaCards.size() - 1)) {
                // We've played through all the clips
                Log.i(TAG, "Played all clips. stopping");
                isPlaying = false;
                stopPlayback();
            } else {
                // Advance to next clip
                currentlyPlayingCard = (ClipCard) mediaCards.get(++currentClipIdx);
                Log.i(TAG, "Advancing to next clip " + mediaCards.indexOf(currentlyPlayingCard));
            }

            switch (currentlyPlayingCard.getMedium()) {
                case Constants.VIDEO:
                    tvVideo.setVisibility(View.VISIBLE); // In case previous card wasn't video medium
                    video = Uri.parse(currentlyPlayingCard.getSelectedMediaFile().getPath());
                    try {
                        // Don't set isPlaying false. We're only 'stopping' to switch media sources
                        player.stop();
                        player.reset();
                        Log.i(TAG, "Setting player data source " + video.toString());
                        player.setDataSource(mContext, video);
                        player.setSurface(surface);
                        player.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case Constants.PHOTO:
                    Log.i(TAG, "set currentelapsedtime to 0");
                    if (mediaCards.indexOf(currentlyPlayingCard) == 0) {
                        // Stop playback. With video / audio this would be handled onPreparedListener
                        isPlaying = false;
                        stopPlayback();
                    }
                    if (ivThumbnail != null) {
                        ivThumbnail.post(new Runnable() {
                            @Override
                            public void run() {
                                setThumbnailForClip(ivThumbnail, currentlyPlayingCard);
                                if (!isPlaying && sbPlaybackProgress != null) sbPlaybackProgress.setProgress(0);
                                advancingClip = false;
                            }
                        });
                    }
                    break;
            }
        }
    }

}
