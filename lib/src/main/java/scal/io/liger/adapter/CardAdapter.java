package scal.io.liger.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twotoasters.android.support.v7.widget.CardView;
import com.twotoasters.android.support.v7.widget.RecyclerView;

import java.util.List;

import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.view.DisplayableCard;

/**
 * Created by davidbrodsky on 10/12/14.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private List<Card> mDataset;
    private Activity mHostActivity;

    // Provide a reference to the type of views that you are using
    // (custom viewholder)
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** List of clipViews in initial z order.
         * e.g: The first element has highest z order (appears unobscured)
        */
        public CardView cardView;
        public Card boundCard;
//        public boolean expanded = false;

        public ViewHolder(View v) {
            super(v);
            cardView = (CardView) v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CardAdapter(Activity host, List<Card> myDataset) {
        mDataset = myDataset;
        mHostActivity = host;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_base, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Card newCard = mDataset.get(position);

        if ( false /*holder.boundCard.getType().equals(newCard.getType())*/) {
            // TODO
            // Recycle the view if possible
        } else {
            // Rebuild the view
            Context context = holder.cardView.getContext();
            DisplayableCard cardsuiCard = newCard.getDisplayableCard(holder.cardView.getContext());
            holder.cardView.removeAllViews();
            // TODO: Remove CardsUi library entirely so we don't have to
            // negate its card container creation
            ViewGroup oldCardContainer = (ViewGroup) cardsuiCard.getCardView(context);
            ViewGroup cardsUiCardContent = (ViewGroup) oldCardContainer.getChildAt(0);
            oldCardContainer.removeView(cardsUiCardContent);
            holder.cardView.addView(cardsUiCardContent);
        }
//        // - get element from your dataset at this position
//        // - replace the contents of the view with that element
//        ViewGroup.LayoutParams params = holder.collapsableContainer.getLayoutParams();
//        params.height = 0;
//        holder.collapsableContainer.setLayoutParams(params);
//
//
//        // TODO: If the recycled view previously belonged to a different
//        // card type, tear down and rebuild the view as in onCreateViewHolder.
//
//        // TODO: Can we add hooks to notify the Adapter when the Clips are changed
//        if (!getClipsForCard().equals(holder.displayedClips)) {
//            // Our list of clip cards is out of date
//            // TODO: Better algorithm . See above TODO
//            holder.displayedClips.clear();
//            holder.clipCandidatesContainer.removeAllViews();
//            List<Object> cardsToDisplay = getClipsForCard();
//            // cardsToDisplay is in order of
//            for (int x = 0; x < cardsToDisplay.size(); x++) {
//                // Create view for new clip
//                inflateAndAddThumbnailForClip(holder, /* cardsToDisplay.get(x)*/type , x, cardsToDisplay.size()-1);
//            }
//        }
//        holder.headerText.setText(type.name());
//
//        // Expand / Collapse footer on click
//        Resources r = holder.bodyText.getContext().getResources();
//        final int footerHeight  = r.getDimensionPixelSize(R.dimen.clip_card_footer_height);
//        holder.headerText.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                final View collapsable = ((View) view.getParent()).findViewById(R.id.collapsable);
//                final ViewGroup.LayoutParams params = collapsable.getLayoutParams();
//
//                ValueAnimator animator = null;
//                if (collapsable.getHeight() < footerHeight) {
//                    // Expand
//                    animator = ValueAnimator.ofInt(0, footerHeight);
//                } else {
//                    // Collapse
//                    animator = ValueAnimator.ofInt(footerHeight, 0);
//                }
//                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                        params.height = (Integer) valueAnimator.getAnimatedValue();
//                        collapsable.setLayoutParams(params);
//                    }
//                });
//                animator.start();
//            }
//        });
//
//        // Expand / Collapse clip stack on thumbnail click
//        holder.clipCandidatesContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onClipThumbnailSelected((ViewHolder) view.getTag());
//            }
//        });
//        holder.clipCandidatesContainer.setTag(holder);
//        holder.mType = type;
//        holder.bodyText.setText(mDataset.get(position).toString());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

//    private void onClipThumbnailSelected(ViewHolder holder) {
//        // When this method is called all clips in holder.displayedClips should be
//        // added to holder.clipCandidatesContainer in order (e.g: First item is highest z order)
//        final int clipCandidateCount = holder.clipCandidatesContainer.getChildCount();
//        if (clipCandidateCount < 2) {
//            // If less than 2 clips, there's no reason to expand / collapse
//            return;
//        }
//        // Dp to px
//        Resources r = holder.bodyText.getContext().getResources();
//
//        int topMargin  = r.getDimensionPixelSize(R.dimen.clip_stack_margin_top);
//        int clipHeight = r.getDimensionPixelSize(R.dimen.clip_thumb_height);
//
//        // Loop over all views except the last
//        for (int i = 0; i < clipCandidateCount - 1; i ++) {
//            int viewIdx = i;
//            if (holder.expanded) {
//                // when expanded : 0, 1
//                // when compressing : 1, 0
//                viewIdx = (clipCandidateCount - 2) - i;
//            }
//
//            final View child = holder.displayedClips.get(viewIdx);
//            final ViewGroup.LayoutParams params = child.getLayoutParams();
//            int marginPerChild = topMargin + clipHeight;
//
//            ValueAnimator animator;
//            int startAnimationMargin;
//            int stopAnimationMargin;
//            int marginMultipler = (clipCandidateCount - 1) - viewIdx;
//            if (holder.expanded) {
//                // compress
//                startAnimationMargin = marginMultipler * marginPerChild;
//                stopAnimationMargin = topMargin * marginMultipler;
//            } else {
//                // expand
//                startAnimationMargin = topMargin * marginMultipler;
//                stopAnimationMargin = marginMultipler * marginPerChild;
//            }
//            //Log.i("anim", String.format("Animating margin from %d to %d", startAnimationMargin, stopAnimationMargin));
//            animator = ValueAnimator.ofInt(startAnimationMargin, stopAnimationMargin);
//            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                    ((ViewGroup.MarginLayoutParams) params).topMargin = (int) valueAnimator.getAnimatedValue();
//                    child.setLayoutParams(params);
//                }
//            });
//            animator.setStartDelay((1+ i) * 70);
//            animator.start();
//            //Log.i("anim", "View idx " + viewIdx + " animating with delay " + (1+ i) * 400 + " to margin " + stopAnimationMargin);
//        }
//        holder.expanded = !holder.expanded;
//    }

//    /**
//     * TODO: This is a substitute for a method that delivers
//     * us the number of clips to display in this card stack.
//     * Upon completion this should return a list of tye Card etc.
//     * ordered by increasing z order. e.g: The last Card in the List
//     * will be displayed on the top of the stack as the current clip selection.
//     */
//    private List<Object> getClipsForCard() {
//        // TODO: Replace with actual logic
//        List<Object> fakeResult = new ArrayList<>(10);
//        for (int x = 0; x < 4 ; x++) {
//            fakeResult.add(new Object());
//        }
//        return fakeResult;
//    }

//    /**
//     * Note: zOrder 0 is the bottom of the clip list.
//     */
//    private void inflateAndAddThumbnailForClip(ViewHolder viewHolder, Object clip, int zOrder, int zTop) {
//        Resources r = viewHolder.bodyText.getContext().getResources();
//        int topMarginPerZ = r.getDimensionPixelSize(R.dimen.clip_stack_margin_top);
//
//        LayoutInflater inflater = (LayoutInflater) viewHolder.clipCandidatesContainer.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        ImageView thumbnail = (ImageView) inflater.inflate(R.layout.clip_thumbnail, viewHolder.clipCandidatesContainer, false);
//        // TODO: Get thumbnail from clip
//        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) thumbnail.getLayoutParams();
//        params.topMargin = topMarginPerZ * (zTop - zOrder);
//        //Log.i("inflate", String.format("Inflating thumbnail for z %d with top margin %d", zOrder, params.topMargin));
//        thumbnail.setLayoutParams(params);
//        if( ((ClipCard.ClipCardType) clip) == ClipCard.ClipCardType.CHARACTER) {
//            switch (zOrder) {
//                case 0:
//                    thumbnail.setImageResource(R.drawable.geisha);
//                    break;
//                case 1:
//                    thumbnail.setImageResource(R.drawable.dude);
//                    break;
//                case 2:
//                    thumbnail.setImageResource(R.drawable.bloke);
//                    break;
//                case 3:
//                    thumbnail.setImageResource(R.drawable.cage);
//                    break;
//            }
//        } else {
//            switch (zOrder) {
//                case 0:
//                    thumbnail.setImageResource(R.drawable.lake);
//                    break;
//                case 1:
//                    thumbnail.setImageResource(R.drawable.wave);
//                    break;
//                case 2:
//                    thumbnail.setImageResource(R.drawable.versailles);
//                    break;
//                case 3:
//                    thumbnail.setImageResource(R.drawable.city);
//                    break;
//        }
//
//        }
//        viewHolder.clipCandidatesContainer.addView(thumbnail);
//        viewHolder.displayedClips.add(thumbnail);
//        Log.i("displayedClips", String.format("idx %d has margin %d", viewHolder.displayedClips.indexOf(thumbnail), params.topMargin));
//    }
}