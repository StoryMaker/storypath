package scal.io.liger.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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
import scal.io.liger.view.ReorderableRecyclerView;

/**
 * Created by davidbrodsky on 10/23/14.
 */
public class OrderMediaAdapter extends RecyclerView.Adapter<OrderMediaAdapter.ViewHolder> implements ReorderableAdapter {
    public static final String TAG = "OrderMediaAdapter";

    private ReorderableRecyclerView mRecyclerView;
    private HashMap<Card, Long> mCardToStableId = new HashMap<>();
    private List<Card> mClipCards;
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
        Card itemOne = mClipCards.get(positionOne);
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

    public OrderMediaAdapter(ReorderableRecyclerView recyclerView, List<Card> cards, String medium) {
        mRecyclerView = recyclerView;
        mClipCards = cards;
        mMedium = medium;
        long id = 0;
        for (Card card : mClipCards) {
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
        Context context = viewHolder.thumbnail.getContext();
        // TESTING
        ((View) viewHolder.draggable.getParent()).setTag(position);
        viewHolder.draggable.setTag(position);
        viewHolder.draggable.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        //Log.i(TAG, "sending reorder drag to recyclerview for position " + v.getTag());
                        mRecyclerView.startReorderDrag((View) v.getParent(), event, (Integer) v.getTag());
                }
                return false;
            }
        });

        Card cm = mClipCards.get(position);
        ClipCard ccm = null;

        if (cm instanceof ClipCard) {
            ccm = (ClipCard) cm;
        } else {
            return; // Should filter ArrayList at construction so we don't have meaningless list items
        }
        String title = null;
        if (cm.getTitle() == null || cm.getTitle().length() == 0) {
            title = String.format("Untitled %s clip", ((ClipCard) cm).getClipType());
        } else {
            title = cm.getTitle();
        }
        viewHolder.title.setText(title);

        String mediaPath = null;
        MediaFile mf = ccm.getSelectedMediaFile();

        if (mf == null) {
            Log.e(this.getClass().getName(), "no media file was found");
        } else {
            mediaPath = mf.getPath();
        }

        //File mediaFile = null;
        Uri mediaURI = null;

        if (mediaPath != null) {
                /*
                mediaFile = MediaHelper.loadFileFromPath(ccm.getStoryPath().buildPath(mediaPath));
                if(mediaFile.exists() && !mediaFile.isDirectory()) {
                    mediaURI = Uri.parse(mediaFile.getPath());
                }
                */
            mediaURI = Uri.parse(mediaPath);
        }

        if (mMedium != null && mediaURI != null) {
            if (mMedium.equals(Constants.VIDEO)) {
                //Bitmap videoFrame = Utility.getFrameFromVideo(mediaURI.getPath());
                Bitmap videoFrame = mf.getThumbnail();
                if (null != videoFrame) {
                    viewHolder.thumbnail.setImageBitmap(videoFrame);
                }
                return;
            } else if (mMedium.equals(Constants.PHOTO)) {
                viewHolder.thumbnail.setImageURI(mediaURI);
                return;
            }
        }

        //handle fall-through cases: (media==null || medium==AUDIO)

        String clipType = ccm.getClipType();
        int drawable = R.drawable.ic_launcher;

        if (clipType.equals(Constants.CHARACTER)) {
            drawable = R.drawable.cliptype_close;
        } else if (clipType.equals(Constants.ACTION)) {
            drawable = R.drawable.cliptype_medium;
        } else if (clipType.equals(Constants.RESULT)) {
            drawable = R.drawable.cliptype_long;
        }
        viewHolder.thumbnail.setImageDrawable(context.getResources().getDrawable(drawable));
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