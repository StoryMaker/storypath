package scal.io.liger.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import scal.io.liger.model.FullMetadata;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.ReviewCard;
import scal.io.liger.model.StoryPath;

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
                    setThumbnailForClip(ivCardPhoto, (ClipCard) mMediaCards.get(0));
                    ivCardPhoto.setVisibility(View.VISIBLE);
                    tvCardVideo.setVisibility(View.VISIBLE);
                    tvCardVideo.setSurfaceTextureListener(new VideoWithNarrationSurfaceTextureListener(tvCardVideo, mMediaCards, ivCardPhoto));
                    break;
                case Constants.AUDIO:
                    // TODO
                    break;
                case Constants.PHOTO:
                    // TODO
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
                        if(thumbnailView.getVisibility() == View.VISIBLE) {
                            thumbnailView.setVisibility(View.GONE);
                        }
                        surfaceListener.stopPlayback();
                        changeRecordNarrationStateChanged(RecordNarrationState.RECORDING);

                        startRecordingNarration();
                        surfaceListener.startPlayback();
                        break;
                    case RECORDING:
                    case PAUSED:
                        // Stop Button
                        surfaceListener.stopPlayback();
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
                        break;
                }
            }
        });
    }

    /**
     * Set a thumbnail on the given ImageView for the given MediaFile
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
    private class VideoSurfaceTextureListener implements TextureView.SurfaceTextureListener {

        Surface surface;

        protected MediaPlayer mediaPlayer;

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
            timer = new Timer("mplayer");
            timer.schedule(new TimerTask() {
                               @Override
                               public void run() {
                                   try {
                                       if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                                           if (currentlyPlayingCard.getSelectedClip().getStopTime() > 0 && mediaPlayer.getCurrentPosition() > currentlyPlayingCard.getSelectedClip().getStopTime()) {
                                               mediaPlayer.pause();
                                               advanceToNextClip(mediaPlayer);
                                           }
                                           int durationOfPreviousClips = 0;
                                           int currentlyPlayingCardIndex = mediaCards.indexOf(currentlyPlayingCard);
                                           if (currentlyPlayingCardIndex > 0)
                                               durationOfPreviousClips = accumulatedDurationByMediaCard.get(currentlyPlayingCardIndex - 1);
                                           if (sbPlaybackProgress != null) {
                                               sbPlaybackProgress.setProgress((int) (sbPlaybackProgress.getMax() * ((float) durationOfPreviousClips + mediaPlayer.getCurrentPosition()) / clipCollectionDuration.get())); // Show progress relative to clip collection duration
                                           }
                                       }
                                   } catch (IllegalStateException e) { /* MediaPlayer in invalid state. Ignore */}
                               }
                           },
            100,    // Initial delay ms
            100);   // Repeat interval ms
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            surface = new Surface(surfaceTexture);

            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(mContext, Uri.parse(currentlyPlayingCard.getSelectedMediaFile().getPath()));
                mediaPlayer.setSurface(surface);
                mediaPlayer.prepareAsync();
                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.seekTo(currentlyPlayingCard.getSelectedClip().getStartTime());

                        int currentClipIdx = mediaCards.indexOf(currentlyPlayingCard);
                        if (currentClipIdx == 0) {
                            Log.i(TAG, "prepared finished for first clip");
                            // This is the first clip
                            // Setup initial views requiring knowledge of clip media
                            if (currentlyPlayingCard.getSelectedClip().getStopTime() == 0)
                                currentlyPlayingCard.getSelectedClip().setStopTime(mediaPlayer.getDuration());
                            mediaPlayer.seekTo(currentlyPlayingCard.getSelectedClip().getStartTime());

                            // TODO Handle outside of this class
                            //clipLength.setText("Total : " + Util.makeTimeString(clipCollectionDuration.get()));
                        } else {
                            Log.i(TAG, "Auto starting subsequent clip");
                            // automatically begin playing subsequent clips
                            mediaPlayer.start();
                        }
                    }
                });
                /** MediaPlayer for narration recording */
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        advanceToNextClip(mp);
                    }
                });
            } catch (IllegalArgumentException | IllegalStateException | SecurityException | IOException e) {
                e.printStackTrace();
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
            accumulatedDurationByMediaCard = new ArrayList<>(cards.size());
            for (Card card : cards) {
                if (card instanceof ClipCard) {
                    ClipMetadata clipMeta = ((ClipCard) card).getSelectedClip();
                    MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(((ClipCard)card).getSelectedMediaFile().getPath()));
                    int clipDuration = mp.getDuration();
                    Log.i(TAG, String.format("Got duration for media: %d. start time: %d. stop time: %d", clipDuration, clipMeta.getStartTime(), clipMeta.getStopTime()));
                    totalDuration += (((clipMeta.getStopTime() == 0) ? clipDuration : clipMeta.getStopTime()) - clipMeta.getStartTime());
                    accumulatedDurationByMediaCard.add(totalDuration);
                    Log.i(TAG, "Total duration now " + totalDuration);
                    mp.release();
                }
            }
            return totalDuration;
        }

        /** Craft a OnClickListener to toggle video playback and thumbnail visibility */
        protected View.OnClickListener getVideoPlaybackToggleClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer == null) return;

                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        //pauseClipCollectionPlaybackWithNarration();
                    }
                    else {
                        mediaPlayer.start();
                        //startClipCollectionPlaybackWithNarration();
                        if (ivThumbnail.getVisibility() == View.VISIBLE) {
                            ivThumbnail.setVisibility(View.GONE);
                        }
                    }
                }
            };
        }

        /**
         * Advance the given MediaPlayer to the next clip in mMediaCards
         * and ends with a call to {@link android.media.MediaPlayer#prepare()}. Therefore
         * an OnPreparedListener must be set on MediaPlayer to start playback
         */
        protected void advanceToNextClip(MediaPlayer player) {
            Uri video;
            int currentClipIdx = mediaCards.indexOf(currentlyPlayingCard);
            if (currentClipIdx == (mediaCards.size() - 1)) {
                // We've played through all the clips
                // TODO Extend this class and override to add simultaneous narration playback
//                if (mRecordNarrationState == RecordNarrationState.RECORDING) {
//                    stopRecordingNarration();
//                    changeRecordNarrationStateChanged(RecordNarrationState.STOPPED);
//                }
                Log.i(TAG, "Played all clips. Resetting to first");
                currentlyPlayingCard = (ClipCard) mediaCards.get(0);
            } else {
                // Advance to next clip
                Log.i(TAG, "Advancing to next clip");
                currentlyPlayingCard = (ClipCard) mediaCards.get(++currentClipIdx);
            }

            video = Uri.parse(currentlyPlayingCard.getSelectedMediaFile().getPath());
            try {
                player.stop();
                player.reset();
                Log.i(TAG, "Setting player data source " + video.toString());
                player.setDataSource(mContext, video);
                player.setSurface(surface);
                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

                boolean paused = false;

                @Override
                public void onClick(View v) {
                    if (mediaPlayer == null) return;

                    if (mediaPlayer.isPlaying()) {
                        pausePlayback();
                        paused = true;
                    }
                    else if (paused){
                        // paused
                        resumePlayback();
                        paused = false;
                    }
                    else {
                        // stopped
                        startPlayback();
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
                narrationPlayer.start();
            }

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
            int currentClipIdx = mediaCards.indexOf(currentlyPlayingCard);
            if (currentClipIdx == (mediaCards.size() - 1)) {
                // We've played through all the clips
                if (mRecordNarrationState == RecordNarrationState.RECORDING) {
                    stopRecordingNarration();
                    changeRecordNarrationStateChanged(RecordNarrationState.STOPPED);
                }
            }
            // We need to check the current clip index *before* calling super(), which will advance it
            super.advanceToNextClip(player);
        }
    }

}
