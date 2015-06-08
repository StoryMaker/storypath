package scal.io.liger.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;
import scal.io.liger.popup.EditClipPopup;
import scal.io.liger.view.ReorderableRecyclerView;

/**
 * Created by davidbrodsky on 10/23/14.
 */
public class OrderMediaAdapter extends RecyclerView.Adapter<OrderMediaAdapter.ViewHolder> implements ReorderableAdapter {
    public static final String TAG = "OrderMediaAdapter";

    private ReorderableRecyclerView mRecyclerView;
    private HashMap<ClipCard, Long> mCardToStableId = new HashMap<>();
    private List<ClipCard> mClipCards;
    private String mMedium;

    private OnReorderListener mReorderListener;

    public interface OnReorderListener {
        /**
         * The item at firstIndex switched places with the item
         * at secondIndex
         */
        public void onReorder(int firstIndex, int secondIndex);
    }

    @Override
    public void swapItems(int positionOne, int positionTwo) {
        ClipCard itemOne = mClipCards.get(positionOne);
        mClipCards.set(positionOne, mClipCards.get(positionTwo));
        mClipCards.set(positionTwo, itemOne);
        notifyItemChanged(positionOne);
        notifyItemChanged(positionTwo);

        if (mReorderListener != null) mReorderListener.onReorder(positionOne, positionTwo);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail;
        public TextView title;
        public ImageView draggable;

        public ViewHolder(View v) {
            super(v);
            thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            title = (TextView) v.findViewById(R.id.title);
            draggable = (ImageView) v.findViewById(R.id.draggable);
        }
    }

    public OrderMediaAdapter(ReorderableRecyclerView recyclerView, List<ClipCard> cards, String medium) {
        mRecyclerView = recyclerView;
        mClipCards = cards;
        mMedium = medium;
        long id = 0;
        for (ClipCard card : mClipCards) {
            mCardToStableId.put(card, id++);
        }
    }

    public void setOnReorderListener(OnReorderListener listener) {
        mReorderListener = listener;
    }

    @Override
    public OrderMediaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_media_clip_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OrderMediaAdapter.ViewHolder viewHolder, int position) {
        final Context context = viewHolder.thumbnail.getContext();
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
        });

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
            Log.e(this.getClass().getName(), "no media file was found");
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
        if (position < mClipCards.size() && position >= 0) {
            return mCardToStableId.get(mClipCards.get(position));
        }
        return RecyclerView.NO_ID;
    }

}