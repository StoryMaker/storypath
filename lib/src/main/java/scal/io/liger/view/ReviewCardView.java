package scal.io.liger.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.util.ArrayList;

import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.adapter.OrderMediaAdapter;
import scal.io.liger.av.ClipCardsNarrator;
import scal.io.liger.av.ClipCardsPlayer;
import scal.io.liger.model.AudioClip;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.ReviewCard;
import scal.io.liger.popup.NarrationPopup;
import scal.io.liger.popup.OrderMediaPopup;
import timber.log.Timber;

/**
 * ReviewCardView allows the user to review the order of clips
 * attached to a ReviewCard's Story Path. This card will also support
 * adding narration, changing the order of clips, and jumbling the order.
 */
public class ReviewCardView extends ExampleCardView implements ClipCardsNarrator.NarrationListener,
                                                               OrderMediaAdapter.OnReorderListener {
    public static final String TAG = "ReviewCardView";

    private ReviewCard mCardModel;
    /** Collection of ClipCards with attached media within the current StoryPath. */
    private ArrayList<ClipCard> mMediaCards;
    private ArrayList<AudioClip> mAudioClips;
    private ClipCardsPlayer mCardsPlayer;
//    private Context mContext;
    private String mMedium;

    public ReviewCardView(Context context, Card cardModel) {
        Timber.d("constructor");
        mContext = context;
        mCardModel = (ReviewCard) cardModel;
        mAudioClips = mCardModel.getStoryPath().getStoryPathLibrary().getAudioClips();
        if (mAudioClips == null) mAudioClips = new ArrayList<>();
    }

    @Override
    public View getCardView(final Context context) {
        Timber.d("getCardView");
        if (mCardModel == null) {
            return null;
        }

        mMedium = mCardModel.getStoryPath().getMedium();

        View view = LayoutInflater.from(context).inflate(R.layout.card_review, null);
        FrameLayout flPlayer = (FrameLayout) view.findViewById(R.id.card_player);

        initClipCardsWithAttachedMedia();
        if (mMediaCards.size() > 0) {
            mCardsPlayer = new ClipCardsPlayer(flPlayer, mMediaCards, mAudioClips);
        } else
            showNoClipPlaceholder(flPlayer);

        Button btnJumble = ((Button) view.findViewById(R.id.btn_jumble));
        Button btnOrder = ((Button) view.findViewById(R.id.btn_order));
        Button btnNarrate = ((Button) view.findViewById(R.id.btn_narrate));
        Button btnPublish = ((Button) view.findViewById(R.id.btn_publish));

        if (mMedium != null && mMedium.equals("video")) {
            btnNarrate.setVisibility(View.VISIBLE);
        } else {
            btnNarrate.setVisibility(View.GONE);
        }

        //prepare drawable
        final int drawableSizeDp = 30;
        Resources r = mContext.getResources();
        int drawableSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, drawableSizeDp, r.getDisplayMetrics());

        //set drawables for actions
        Drawable drwTemp = new IconDrawable(mContext, Iconify.IconValue.fa_ic_card_jumble).colorRes(R.color.storymaker_highlight);
        drwTemp.setBounds(0, 0, drawableSizePx, drawableSizePx);
        btnJumble.setCompoundDrawables(drwTemp, null, null, null);

        drwTemp = new IconDrawable(mContext, Iconify.IconValue.fa_ic_card_order).colorRes(R.color.storymaker_highlight);
        drwTemp.setBounds(0, 0, drawableSizePx, drawableSizePx);
        btnOrder.setCompoundDrawables(drwTemp, null, null, null);

        drwTemp = new IconDrawable(mContext, Iconify.IconValue.fa_ic_card_narrate).colorRes(R.color.storymaker_highlight);
        drwTemp.setBounds(0, 0, drawableSizePx, drawableSizePx);
        btnNarrate.setCompoundDrawables(drwTemp, null, null, null);

        drwTemp = new IconDrawable(mContext, Iconify.IconValue.fa_ic_card_upload).colorRes(R.color.storymaker_highlight);
        drwTemp.setBounds(0, 0, drawableSizePx, drawableSizePx);
        btnPublish.setCompoundDrawables(drwTemp, null, null, null);

        //set OnClickListeners
        btnJumble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaCards.size() > 0) {
                    if (mCardsPlayer != null) {
                        if (mCardsPlayer.isPlaying()) {
                            mCardsPlayer.stopPlayback();
                        }
                    }
                    OrderMediaPopup.show((Activity) mContext, mMedium, mMediaCards, ReviewCardView.this);
                } else
                    Toast.makeText(mContext, mContext.getString(R.string.add_clips_before_reordering), Toast.LENGTH_SHORT).show();
            }
        });

        btnNarrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMediaCards.size() > 0) {
                    NarrationPopup narrationPopup = new NarrationPopup((MainActivity) mCardModel.getStoryPath().getContext());
                    narrationPopup.show(mMediaCards, mAudioClips, ReviewCardView.this);
                    if (mCardsPlayer != null && mCardsPlayer.isPlaying()) mCardsPlayer.stopPlayback();
                } else
                    Toast.makeText(mContext, mContext.getString(R.string.add_clips_before_narrating), Toast.LENGTH_SHORT).show();
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCardsPlayer != null && mCardsPlayer.isPlaying()) mCardsPlayer.stopPlayback();
                MainActivity mainActivity = (MainActivity) mCardModel.getStoryPath().getContext(); // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())
                Util.startPublishActivity(mainActivity, mCardModel.getStoryPath());
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    private void showNoClipPlaceholder(ViewGroup container) {
        ImageView ivPlaceholder = new ImageView(mContext);
        ivPlaceholder.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mCardModel.getStoryPath().setCoverImageThumbnail(ivPlaceholder);
        container.addView(ivPlaceholder);
    }

    /**
     * Initializes the value of {@link #mMediaCards} to a List of ClipCards with attached media
     * within the current StoryPath
     */
    public void initClipCardsWithAttachedMedia() {
        mMediaCards = mCardModel.getStoryPath().getClipCardsWithAttachedMedia();
    }

    @Override
    public void onNarrationFinished(AudioClip audioClip, MediaFile narration) {
//        mCardModel.setNarration(narration);

        mCardModel.getStoryPath()
                  .getStoryPathLibrary()
                  .saveNarrationAudioClip(audioClip, narration);

        mCardModel.getStoryPath().getStoryPathLibrary().save(false);
        mCardModel.getStoryPath().notifyCardChanged(mCardModel);
    }

    @Override
    public void onReorder(int firstIndex, int secondIndex) {
        mCardModel.getStoryPath().getStoryPathLibrary().save(true);
        mCardModel.getStoryPath().notifyCardChanged(mCardModel);
    }
}
