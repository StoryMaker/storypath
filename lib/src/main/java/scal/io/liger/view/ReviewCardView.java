package scal.io.liger.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import java.util.ArrayDeque;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipMetadata;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.ReviewCard;

/**
 * ReviewCardView allows the user to review the order of clips
 * attached to a ReviewCard's Story Path. This card will also support
 * adding narration, changing the order of clip, and jumbling the order.
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
                //TODO
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
            MediaFile mediaFile = mCardModel.getStoryPath().loadMediaFileSP(clipMetadata.getUuid());
            mClipMedia.add(mediaFile);
            if (mMedium == null) mMedium = mediaFile.getMedium();
            mHasMediaFiles = true;
        }
        Log.i(TAG, String.format("Queued %d media files for playback", mClipMedia.size()));
    }
}
