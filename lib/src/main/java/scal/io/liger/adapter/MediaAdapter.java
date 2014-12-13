package scal.io.liger.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;

/**
 * Created by davidbrodsky on 10/23/14.
 */
public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {
    public static final String TAG = "OrderMediaAdapter";

    private RecyclerView mRecyclerView;
    private HashMap<ClipCard, Long> mCardToStableId = new HashMap<>();
    private List<ClipCard> mClipCards;

    private OnReorderListener mReorderListener;

    public interface OnReorderListener {
        /**
         * The item at firstIndex switched places with the item
         * at secondIndex
         */
        public void onReorder(int firstIndex, int secondIndex);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail;
        public TextView title;
        public CheckBox checkBox;

        public ViewHolder(View v) {
            super(v);
            thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            title = (TextView) v.findViewById(R.id.title);
            checkBox = (CheckBox) v.findViewById(R.id.check_box);
        }
    }

    public MediaAdapter(RecyclerView recyclerView, List<ClipCard> cards) {
        mRecyclerView = recyclerView;
        mClipCards = cards;
        long id = 0;
        for (ClipCard card : mClipCards) {
            mCardToStableId.put(card, id++);
        }
    }

    public void setOnReorderListener(OnReorderListener listener) {
        mReorderListener = listener;
    }

    @Override
    public MediaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_media_clip_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MediaAdapter.ViewHolder viewHolder, int position) {

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
            title = String.format("%s: %s", ccm.getClipType(), goal);
        } else {
            title = ccm.getTitle();
        }

        viewHolder.title.setText(title);

        MediaFile mf = ccm.getSelectedMediaFile();
        if (mf == null) {
            Log.e(this.getClass().getName(), "no media file was found");
        } else {
            Bitmap thumbnail = mf.getThumbnail(viewHolder.title.getContext());

            if (thumbnail != null) {
                viewHolder.thumbnail.setImageBitmap(thumbnail);
            }
        }
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