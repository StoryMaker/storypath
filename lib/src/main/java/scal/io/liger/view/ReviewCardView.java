package scal.io.liger.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
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

import scal.io.liger.Constants;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.av.ClipCardsNarrator;
import scal.io.liger.av.ClipCardsPlayer;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.ReviewCard;
import scal.io.liger.popup.NarrationPopup;
import scal.io.liger.popup.OrderMediaPopup;

/**
 * ReviewCardView allows the user to review the order of clips
 * attached to a ReviewCard's Story Path. This card will also support
 * adding narration, changing the order of clips, and jumbling the order.
 */
public class ReviewCardView extends ExampleCardView implements ClipCardsNarrator.NarrationListener {
    public static final String TAG = "ReviewCardView";

    private ReviewCard mCardModel;
    private ClipCardsPlayer mCardsPlayer;
//    private Context mContext;
    private String mMedium;

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
        FrameLayout flPlayer = (FrameLayout) view.findViewById(R.id.card_player);

        initClipCardsWithAttachedMedia();
        if (mMediaCards.size() > 0)
            mCardsPlayer = new ClipCardsPlayer(flPlayer, mMediaCards);
        else
            showNoClipPlaceholder(flPlayer);

        Button btnJumble = ((Button) view.findViewById(R.id.btn_jumble));
        Button btnOrder = ((Button) view.findViewById(R.id.btn_order));
        Button btnNarrate = ((Button) view.findViewById(R.id.btn_narrate));
        Button btnPublish = ((Button) view.findViewById(R.id.btn_publish));

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
                if (mMediaCards.size() > 0)
                    OrderMediaPopup.show((Activity) mContext, mMedium, mMediaCards);
                else
                    Toast.makeText(mContext, mContext.getString(R.string.add_clips_before_reordering), Toast.LENGTH_SHORT).show();
            }
        });

        btnNarrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMediaCards.size() > 0) {
                    NarrationPopup narrationPopup = new NarrationPopup((MainActivity) mCardModel.getStoryPath().getContext());
                    narrationPopup.show(mMediaCards, ReviewCardView.this);
                } else
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

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    private void showNoClipPlaceholder(ViewGroup container) {
        ImageView ivPlaceholder = new ImageView(mContext);
        ivPlaceholder.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Bitmap thumb = mCardModel.getStoryPath().getCoverImageThumbnail();
        if (thumb != null) {
            ivPlaceholder.setImageBitmap(thumb);
        } else {
            ivPlaceholder.setImageResource(R.drawable.no_thumbnail);
        }
        container.addView(ivPlaceholder);
    }

    /**
     * Initializes the value of {@link #mMediaCards} to a List of ClipCards with attached media
     * within the current StoryPath
     */
    public void initClipCardsWithAttachedMedia() {
        mMediaCards = mCardModel.getStoryPath().getClipCardsWithAttachedMedia();
    }

    /** Collection of ClipCards with attached media within the current StoryPath. */
    ArrayList<ClipCard> mMediaCards;

    @Override
    public void onNarrationFinished(MediaFile narration) {
        mCardModel.setNarration(narration);
    }
}
