package scal.io.liger.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.VideoView;

import com.edmodo.rangebar.RangeBar;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private ArrayDeque<MediaFile> mClipMedia = new ArrayDeque<>();
    private String mMedium;
    private boolean mHasMediaFiles;

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
//        mClipMedia.poll()
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
                List<Card> mediaCards = getClipCardsWithAttachedMedia();
                Util.showOrderMediaPopup((Activity) mContext, mMedium, mediaCards);
            }
        });

        btnNarrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        queueMediaFiles();

        if (hasUnplayedMediaFiles()) {
            ivCardPhoto.setVisibility(View.GONE);
            vvCardVideo.setVisibility(View.VISIBLE);

            MediaFile firstMedia = getNextMediaFile(true);
            Uri video = Uri.parse(firstMedia.getPath());
            vvCardVideo.setMediaController(null);
            vvCardVideo.setVideoURI(video);
            vvCardVideo.seekTo(5); // seems to be need to be done to show its thumbnail?

            setupMediaViewClickListeners(vvCardVideo, ivCardPhoto, firstMedia);
        } else {
            Log.e(TAG, "No Clips available");
        }

        vvCardVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer vvCardPlayer) {
                if (hasUnplayedMediaFiles()) {
                    Log.i(TAG, "ReviewCard playing next clip");
                    vvCardVideo.setVideoURI(Uri.parse(getNextMediaFile(true).getPath()));
                    vvCardVideo.start();
                } else {
                    // Refill queue and return to first video
                    queueMediaFiles();
                    vvCardVideo.setVideoURI(Uri.parse(getNextMediaFile(true).getPath()));
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
     * @return whether unplayed clips remain in the queue
     */
    private boolean hasUnplayedMediaFiles() {
        return mClipMedia.size() > 0;
    }

    /**
     * @return the next MediaFile for playback from {@link #mClipMedia}
     */
    private MediaFile getNextMediaFile(boolean removeFromQueue) {
        if (mClipMedia.size() == 0) return null;
        return removeFromQueue ? mClipMedia.poll() : mClipMedia.peek();
    }

    /**
     * Add all MediaFiles belonging to the card model's story path to {@link #mClipMedia}
     * Also determines the medium and sets {@link #mMedium}
     */
    private void queueMediaFiles() {
        List<ClipMetadata> clipMetaDataCollection = mCardModel.getStoryPath().exportMetadata();
        Log.i(TAG, String.format("Found %d media files for card", clipMetaDataCollection.size()));
        for (ClipMetadata clipMetadata : clipMetaDataCollection) {
            MediaFile mediaFile = mCardModel.getStoryPath().loadMediaFile(clipMetadata.getUuid());
            mClipMedia.add(mediaFile);
            if (mMedium == null) mMedium = mediaFile.getMedium();
            mHasMediaFiles = true;
        }
        Log.i(TAG, String.format("Queued %d media files for playback", mClipMedia.size()));
    }

    /**
     * Return a List of ClipCards with attached media
     */
    private ArrayList<Card> getClipCardsWithAttachedMedia() {
        ArrayList<Card> mediaCards = mCardModel.getStoryPath().gatherCards("<<ClipCard>>");
        Iterator iterator = mediaCards.iterator();
        while (iterator.hasNext()) {
            ClipCard clipCard = (ClipCard) iterator.next();
            if ( clipCard.getClips() == null || clipCard.getClips().size() < 1 ) {
                iterator.remove();
            }
        }
        return mediaCards;
    }

    /**
     * Show a dialog allowing the user to record narration for a Clip
     */
    private void showClipNarrationDialog(ArrayList<ClipCard> mediaCards) {
        View v = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_clip_narration, null);

        /** Trim dialog views */
        final TextureView videoView = (TextureView) v.findViewById(R.id.textureView);
        final ImageView thumbnailView = (ImageView) v.findViewById(R.id.thumbnail);
        final TextView clipLength = (TextView) v.findViewById(R.id.clipLength);
        final SeekBar playbackBar = (SeekBar) v.findViewById(R.id.playbackProgress);

        /** Media player and media */
        final MediaPlayer player = new MediaPlayer();
        final ClipMetadata firstClip = mediaCards.get(0).getSelectedClip();
        final AtomicInteger clipDurationMs = new AtomicInteger();

        Log.i(TAG, String.format("Showing clip trim dialog with initial start: %d stop: %d", firstClip.getStartTime(), firstClip.getStopTime()));

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
//                    player.seekTo();
                    player.start();
                }
            }
        });

        videoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setThumbnailForClip(thumbnailView, mCardModel.getStoryPath().loadMediaFile(firstClip.getUuid()));

                Uri video = Uri.parse(mCardModel.getSelectedMediaFile().getPath());
                Surface s = new Surface(surface);
                try {
                    player.setDataSource(mContext, video);
                    player.setSurface(s);
                    player.prepare();
                    player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            player.seekTo(clipStartMs.get());
                        }
                    });
                    thumbnailView.setVisibility(View.GONE);
                    clipDurationMs.set(player.getDuration());
                    if (clipStopMs.get() == 0) clipStopMs.set(clipDurationMs.get()); // If no stop point set, play whole clip

                    // Setup initial views requiring knowledge of clip media
                    if (firstClip.getStopTime() == 0) firstClip.setStopTime(clipDurationMs.get());
                    player.seekTo(firstClip.getStartTime());
                    rangeBar.setThumbIndices(getRangeBarIndexForMs(firstClip.getStartTime(), tickCount, clipDurationMs.get()),
                            getRangeBarIndexForMs(firstClip.getStopTime(), tickCount, clipDurationMs.get()));
                    clipLength.setText("Total : " + makeTimeString(clipDurationMs.get()));
                    clipEnd.setText(makeTimeString(firstClip.getStopTime()));
                } catch (IllegalArgumentException | IllegalStateException | SecurityException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }

        });

        // Poll MediaPlayer for position, ensuring it never exceeds clipStopMs
        final Timer timer = new Timer("mplayer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (player.isPlaying()) {
                        if (player.getCurrentPosition() > clipStopMs.get()) {
                            player.pause();
                            Log.i(TAG, "stopping playback at clip end selection");
                        }
                        playbackBar.setProgress((int) (tickCount * ((float) player.getCurrentPosition()) / player.getDuration()));
                    }
                } catch (IllegalStateException e) { /* MediaPlayer in invalid state. Ignore */}
            }
        }, 100, 100);


        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(v)
                .setPositiveButton("TRIM CLIP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCardModel.getSelectedClip().setStartTime(clipStartMs.get());
                        mCardModel.getSelectedClip().setStopTime(clipStopMs.get());
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        player.release();
                        timer.cancel();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Set a thumbnail on the given ImageView for the given MediaFile
     */
    private void setThumbnailForClip(@NonNull ImageView thumbnail, @NonNull MediaFile media) {
        // Clip has attached media. Show an appropriate preview
        // e.g: A thumbnail for video
        String medium = mCardModel.getMedium();
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
            String clipType = mCardModel.getClipType();
            setClipExampleDrawables(clipType, thumbnail);
            thumbnail.setVisibility(View.VISIBLE);
        }
    }
}
