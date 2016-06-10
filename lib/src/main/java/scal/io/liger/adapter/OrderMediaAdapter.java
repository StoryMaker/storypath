package scal.io.liger.adapter;

import timber.log.Timber;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ViewUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import java.util.HashMap;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;
import scal.io.liger.popup.EditClipPopup;

/**
 * Created by davidbrodsky on 10/23/14.
 * Update by n8fr8 on 18 May 2016
 */
public class OrderMediaAdapter extends RecyclerView.Adapter<OrderMediaAdapter.ViewHolder> implements DraggableItemAdapter<OrderMediaAdapter.ViewHolder> {
    public static final String TAG = "OrderMediaAdapter";

    // NOTE: Make accessible with short name
    private interface Draggable extends DraggableItemConstants {
    }

    private HashMap<ClipCard, Long> mCardToStableId = new HashMap<>();
    private List<ClipCard> mClipCards;
    private String mMedium;

    private OnReorderListener mReorderListener;

    private boolean mChanged = false;

    public interface OnReorderListener {
        /**
         * The item at firstIndex switched places with the item
         * at secondIndex
         */
        public void onReorder(int firstIndex, int secondIndex);
    }

    public boolean didChange ()
    {
        return mChanged;
    }

    /**
    @Override
    public void swapItems(int positionOne, int positionTwo) {
        ClipCard itemOne = mClipCards.get(positionOne);
        mClipCards.set(positionOne, mClipCards.get(positionTwo));
        mClipCards.set(positionTwo, itemOne);
        notifyItemChanged(positionOne);
        notifyItemChanged(positionTwo);

        if (mReorderListener != null) mReorderListener.onReorder(positionOne, positionTwo);
    }*/

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        Log.d(TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");

        //first notify the model
        if (mReorderListener != null) mReorderListener.onReorder(fromPosition, toPosition);

        //then update the local view
        ClipCard itemFrom = mClipCards.remove(fromPosition);
        mClipCards.add(toPosition, itemFrom);

        mChanged = true;


        notifyItemMoved(fromPosition, toPosition);

    }

    public static class ViewHolder extends AbstractDraggableItemViewHolder {

        public View container;
        public ImageView thumbnail;
        public TextView title;
        public ImageView draggable;

        public ViewHolder(View v) {
            super(v);
            container = v.findViewById(R.id.container);
            thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            title = (TextView) v.findViewById(R.id.title);
            draggable = (ImageView) v.findViewById(R.id.draggable);
        }
    }

    public OrderMediaAdapter(List<ClipCard> cards, String medium) {

        mClipCards = cards;
        mMedium = medium;
        long id = 1000;
        for (ClipCard card : mClipCards) {
            mCardToStableId.put(card, id++);
        }

        // DraggableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);

    }


    @Override
    public boolean onCheckCanStartDrag(OrderMediaAdapter.ViewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        final View containerView = holder.container;
        final View dragHandleView = holder.draggable;

        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
        final int offsetY = 0;//containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);

        return hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(OrderMediaAdapter.ViewHolder holder, int position) {
        // no drag-sortable range specified
        return null;
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    public void setOnReorderListener(OnReorderListener listener) {
        mReorderListener = listener;
    }

    @Override
    public OrderMediaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_media_clip_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OrderMediaAdapter.ViewHolder viewHolder, int position) {
        final Context context = viewHolder.thumbnail.getContext();

        /**
        // TESTING
        ((View) viewHolder.draggable.getParent()).setTag(position);
        viewHolder.draggable.setTag(position);
        viewHolder.draggable.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        //Log.i(TAG, "sending reorder drag to recyclerview for position " + v.getTag());
                        mRecyclerView.startReorderDrag((View) v.getParent(), (Integer) v.getTag());
                }
                return false;
            }
        });*/


        // set background resource (target view ID: container)
        final int dragState = viewHolder.getDragStateFlags();

        if (((dragState & Draggable.STATE_FLAG_IS_UPDATED) != 0)) {
            int bgResId;

            if ((dragState & Draggable.STATE_FLAG_IS_ACTIVE) != 0) {
               // bgResId = R.drawable.bg_item_dragging_active_state;

                // need to clear drawable state here to get correct appearance of the dragging item.
                //DrawableUtils.clearState(holder.mContainer.getForeground());
            } else if ((dragState & Draggable.STATE_FLAG_DRAGGING) != 0) {
                //bgResId = R.drawable.bg_item_dragging_state;
            } else {
                //bgResId = R.drawable.bg_item_normal_state;
            }

            //holder.mContainer.setBackgroundResource(bgResId);
        }

        Card cm = mClipCards.get(position);
        ClipCard ccm;

        if (cm instanceof ClipCard) {
            ccm = (ClipCard) cm;
        } else {
            return; // Should filter ArrayList at construction so we don't have meaningless list items
        }


        String title;
        if (ccm.getTitle() == null || ccm.getTitle().length() == 0) {
            String goal = ccm.getFirstGoal();
            title = String.format("%s: %s", Constants.getClipTypeLocalized(context, ccm.getClipType()), goal);
        } else {
            title = ccm.getTitle();
        }

        viewHolder.title.setText(title);

        MediaFile mf = ccm.getSelectedMediaFile();
        if (mf == null) {
            Timber.e("no media file was found");
        } else {
            mf.loadThumbnail(viewHolder.thumbnail);
        }
        final ClipCard fccm = ccm;
        viewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditClipPopup ecp = new EditClipPopup(context, fccm.getStoryPath(), fccm.getSelectedClip(), fccm.getSelectedMediaFile());
                ecp.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mClipCards.size();
    }

    @Override
    public long getItemId (int position) {

        long resultId = RecyclerView.NO_ID;

        if (position < mClipCards.size() && position >= 0) {
            resultId = mCardToStableId.get(mClipCards.get(position));
        }

        return resultId;
    }


    @Override
    public int getItemViewType(int position) {
        return 0;// mProvider.getItem(position).getViewType();
    }

}
