package scal.io.liger.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import scal.io.liger.Constants;
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
    private boolean mHasMediaFiles;

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
    public View getCardView(Context context) {
        Log.d("RevieCardView", "getCardView");
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_review, null);
        final ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));
        final VideoView vvCardVideo = ((VideoView) view.findViewById(R.id.vv_card_video));

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
                getClipCardsWithAttachedMedia();
                Util.showOrderMediaPopup((Activity) mContext, mMedium, mMediaCards);
            }
        });

        btnNarrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClipNarrationDialog();
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        // Initialize data structures from current StoryPath
        getClipCardsWithAttachedMedia();

        if (hasUnplayedMediaFiles()) {
            ivCardPhoto.setVisibility(View.GONE);
            vvCardVideo.setVisibility(View.VISIBLE);

            MediaFile firstMedia = getNextMediaFile();
            Uri video = Uri.parse(firstMedia.getPath());
            vvCardVideo.setMediaController(null);
            vvCardVideo.setVideoURI(video);
            vvCardVideo.seekTo(5); // seems to be need to be done to show its thumbnail?

            setupMediaViewClickListeners(vvCardVideo, ivCardPhoto, firstMedia);
        } else {
            Log.e(TAG, "No Clips available");
        }

        /** VideoView for reviewing clip order */
        vvCardVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer vvCardPlayer) {
                if (hasUnplayedMediaFiles()) {
                    // We have unplayed clips to proceed to
                    Log.i(TAG, "ReviewCard playing next clip");
                    vvCardVideo.setVideoURI(Uri.parse(getNextMediaFile().getPath()));
                    vvCardVideo.start();
                } else {
                    // Point us back at the first ClipCard in this StoryPath
                    mCurrentlyPlayingClipCard = (ClipCard) mMediaCards.get(0);
                    vvCardVideo.setVideoURI(Uri.parse(getNextMediaFile().getPath()));
                    vvCardVideo.seekTo((5));
                }
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    /**
     * Setup click listeners on the image or video view depending on
     * the type of the given MediaFile
     */
    private void setupMediaViewClickListeners(final VideoView videoView, final ImageView imageView, final MediaFile mediaFile) {
        switch (mediaFile.getMedium()) {
            case Constants.VIDEO:
                videoView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {

                            if (!videoView.isPlaying() && mHasMediaFiles) {
                                videoView.start();
                            }
                            else {
                                videoView.pause();
                            }
                        }
                        return true;
                    }
                });
                break;
            case Constants.AUDIO:
                // TODO
                break;
            case Constants.PHOTO:
                // TODO
                break;
        }
    }

    /**
     * @return whether a clip exists in {@link #mMediaCards} after the position of {@link #mCurrentlyPlayingClipCard}
     */
    private boolean hasUnplayedMediaFiles() {
        return (mMediaCards.indexOf(mCurrentlyPlayingClipCard) < (mMediaCards.size() - 1));
    }

    /**
     * @return the MediaFile corresponding to the next unplayed ClipCard in {@link #mMediaCards} or
     * null if no more ClipCards remain.
     *
     * see {@link #hasUnplayedMediaFiles()}
     */
    private MediaFile getNextMediaFile() {
        if (!hasUnplayedMediaFiles()) return null;
        int currentClipIdx = mMediaCards.indexOf(mCurrentlyPlayingClipCard);
        return ((ClipCard) mMediaCards.get(++currentClipIdx)).getSelectedMediaFile();
    }

    /**
     * @return the MediaFile corresponding to the currently playing ClipCard in {@link #mMediaCards}
     */
    private MediaFile getCurrentMediaFile() {
        return mCurrentlyPlayingClipCard.getSelectedMediaFile();
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
            if ( clipCard.getClips() == null || clipCard.getClips().size() < 1 ) {
                iterator.remove();
            }
        }
        mMediaCards = mediaCards;
    }

    /** Record Narration Dialog */

    /** The ClipCard currently being played by the dialog
     * created by {@link #showClipNarrationDialog()}
     * TODO We should probably isolate the ClipNarrationDialog and all its members
    */
    ClipCard mCurrentlyPlayingClipCard;

    /**
     * The Surface created by the TextureView onto which we'll play video
     */
    Surface mSurface;

    /**
     * Collection of ClipCards with attached media within the current StoryPath.
     */
    ArrayList<Card> mMediaCards;

    /**
     * Collection of cumulative duration parallel to mMediaCards.
     * e.g: the first entry is the duration of the first clip, the second is the duration
     * of the first and second clips etc. Used to properly report playback progress relative to
     * entire mMediaCards collection.
     */
    ArrayList<Integer> mAccumulatedDurationByMediaCard;

    /** Records audio */
    MediaRecorder mMediaRecorder;

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

        /** Media player and media */
        final MediaPlayer player = new MediaPlayer();
        mCurrentlyPlayingClipCard = (ClipCard) mMediaCards.get(0);
        final AtomicInteger clipCollectionDuration = new AtomicInteger();

        /** SeekBar value varies from 0 to seekBarMax */
        final int seekBarMax = mContext.getResources().getInteger(R.integer.trim_bar_tick_count);

        Log.i(TAG, String.format("Showing clip trim dialog with initial start: %d stop: %d", mCurrentlyPlayingClipCard.getSelectedClip().getStartTime(), mCurrentlyPlayingClipCard.getSelectedClip().getStopTime()));

        /** Configure views for initial state */
        changeRecordNarrationStateChanged(RecordNarrationState.READY);

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
            }
        });

        videoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurface = new Surface(surface);

                setThumbnailForClip(thumbnailView, mCurrentlyPlayingClipCard);

                final Uri video = Uri.parse(mCurrentlyPlayingClipCard.getSelectedMediaFile().getPath());
                final Surface s = new Surface(surface);
                try {
                    player.setDataSource(mContext, video);
                    player.setSurface(s);
                    player.prepareAsync();
                    player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            player.seekTo(mCurrentlyPlayingClipCard.getSelectedClip().getStartTime());

                            int currentClipIdx = mMediaCards.indexOf(mCurrentlyPlayingClipCard);
                            if (currentClipIdx == 0) {
                                Log.i(TAG, "prepared finished for first clip");
                                // This is the first clip
                                // Setup initial views requiring knowledge of clip media
                                if (mCurrentlyPlayingClipCard.getSelectedClip().getStopTime() == 0)
                                    mCurrentlyPlayingClipCard.getSelectedClip().setStopTime(player.getDuration());
                                player.seekTo(mCurrentlyPlayingClipCard.getSelectedClip().getStartTime());
                                if (clipCollectionDuration.get() == 0)
                                    clipCollectionDuration.set(calculateTotalClipCollectionLengthMs(mMediaCards));

                                clipLength.setText("Total : " + Util.makeTimeString(clipCollectionDuration.get()));
                            } else {
                                Log.i(TAG, "Auto starting subsequent clip");
                                // automatically begin playing subsequent clips
                                player.start();
                            }
                        }
                    });
                    /** MediaPlayer for narration recording */
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            advanceToNextClip(mp);
                        }
                    });
                    thumbnailView.setVisibility(View.GONE);
                } catch (IllegalArgumentException | IllegalStateException | SecurityException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { /* do nothing */}

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) { /* do nothing */}

        });

        // Poll MediaPlayer for position, ensuring it never exceeds stop point indicated by ClipMetadata
        final Timer timer = new Timer("mplayer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (player.isPlaying()) {
                        if (mCurrentlyPlayingClipCard.getSelectedClip().getStopTime() > 0 && player.getCurrentPosition() > mCurrentlyPlayingClipCard.getSelectedClip().getStopTime()) {
                            player.pause();
                            advanceToNextClip(player);
                        }
                        int durationOfPreviousClips = 0;
                        int currentlyPlayingCardIndex = mMediaCards.indexOf(mCurrentlyPlayingClipCard);
                        if (currentlyPlayingCardIndex > 0)
                            durationOfPreviousClips = mAccumulatedDurationByMediaCard.get(currentlyPlayingCardIndex - 1);
                        playbackBar.setProgress((int) (seekBarMax * ((float) durationOfPreviousClips + player.getCurrentPosition()) / clipCollectionDuration.get())); // Show progress relative to clip collection duration
                    }
                } catch (IllegalStateException e) { /* MediaPlayer in invalid state. Ignore */}
            }
        },
        100,    // Initial delay ms
        100);   // Repeat interval ms

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(v);
        final Dialog dialog = builder.create();
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
                        startRecordingNarration(player);
                        changeRecordNarrationStateChanged(RecordNarrationState.RECORDING);
                        break;
                    case RECORDING:
                    case PAUSED:
                        // Stop Button
                        stopRecordingNarration(player);
                        changeRecordNarrationStateChanged(RecordNarrationState.STOPPED);
                        break;
                    case STOPPED:
                        // Redo Button
                        // TODO Show recording countdown first
                        // TODO reset player to first clip
                        startRecordingNarration(player);
                        changeRecordNarrationStateChanged(RecordNarrationState.RECORDING);
                        break;
                }
            }
        });
    }

    /**
     * Advance the given MediaPlayer to the next clip in mMediaCards
     * and ends with a call to {@link android.media.MediaPlayer#prepare()}. Therefore
     * an OnPreparedListener must be set on MediaPlayer to start playback
     */
    private void advanceToNextClip(MediaPlayer player) {
        Uri video;
        int currentClipIdx = mMediaCards.indexOf(mCurrentlyPlayingClipCard);
        if (currentClipIdx == (mMediaCards.size() - 1)) {
            // We've played through all the clips
            if (mRecordNarrationState == RecordNarrationState.RECORDING) {
                stopRecordingNarration(player);
                changeRecordNarrationStateChanged(RecordNarrationState.STOPPED);
            }
            Log.i(TAG, "Played all clips. Resetting to first");
            mCurrentlyPlayingClipCard = (ClipCard) mMediaCards.get(0);
        } else {
            // Advance to next clip
            Log.i(TAG, "Advancing to next clip");
            mCurrentlyPlayingClipCard = (ClipCard) mMediaCards.get(++currentClipIdx);
        }

        video = Uri.parse(mCurrentlyPlayingClipCard.getSelectedMediaFile().getPath());
        try {
            player.stop();
            player.reset();
            Log.i(TAG, "Setting player data source " + video.toString());
            player.setDataSource(mContext, video);
            player.setSurface(mSurface);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the cumulative length in ms of each selected clip of each Card in cards.
     * Also populates {@link #mAccumulatedDurationByMediaCard}.
     *
     * @param cards An ArrayList of {@link scal.io.liger.model.ClipCard}s.
     *              Only cards of type ClipCard will be evaluated.
     *
     * Note we don't accept an ArrayList<ClipCard> due to the current operation of StoryPath#gatherCards(..)
     */
    private int calculateTotalClipCollectionLengthMs(ArrayList<Card> cards) {
        int totalDuration = 0;
        mAccumulatedDurationByMediaCard = new ArrayList<>(cards.size());
        for (Card card : cards) {
            if (card instanceof ClipCard) {
                ClipMetadata clipMeta = ((ClipCard) card).getSelectedClip();
                MediaPlayer mp = MediaPlayer.create(mContext, Uri.parse(((ClipCard)card).getSelectedMediaFile().getPath()));
                int clipDuration = mp.getDuration();
                Log.i(TAG, String.format("Got duration for media: %d. start time: %d. stop time: %d", clipDuration, clipMeta.getStartTime(), clipMeta.getStopTime()));
                totalDuration += (((clipMeta.getStopTime() == 0) ? clipDuration : clipMeta.getStopTime()) - clipMeta.getStartTime());
                mAccumulatedDurationByMediaCard.add(totalDuration);
                Log.i(TAG, "Total duration now " + totalDuration);
                mp.release();
            }
        }
        return totalDuration;
    }

    /**
     * Set a thumbnail on the given ImageView for the given MediaFile
     */
    private void setThumbnailForClip(@NonNull ImageView thumbnail, @NonNull ClipCard clipCard) {
        // Clip has attached media. Show an appropriate preview
        // e.g: A thumbnail for video

        MediaFile mediaFile = mCardModel.getStoryPath().loadMediaFile(mCurrentlyPlayingClipCard.getSelectedClip().getUuid());

        String medium = clipCard.getMedium();
        if (medium.equals(Constants.VIDEO)) {
            Bitmap videoFrame = mediaFile.getThumbnail();
            if(null != videoFrame) {
                thumbnail.setImageBitmap(videoFrame);
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
    private void startRecordingNarration(MediaPlayer player) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String now = df.format(new Date());
        File outputFile = new File(Environment.getExternalStorageDirectory(), now + ".aac");

        if (mMediaRecorder == null) mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mMediaRecorder.setOutputFile(outputFile.getAbsolutePath());
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        mMediaRecorder.start();
        player.start();
        Toast.makeText(mContext, "Recording Narration", Toast.LENGTH_SHORT).show();
    }

    /**
     * Stop and reset {@link #mMediaRecorder} so it may be used again
     */
    private void stopRecordingNarration(MediaPlayer player) {
        if (player.isPlaying()) player.pause();
        mMediaRecorder.stop();
        mMediaRecorder.reset();
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
        mMediaRecorder.release();
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
}
