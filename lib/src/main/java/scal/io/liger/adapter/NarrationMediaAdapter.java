package scal.io.liger.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import scal.io.liger.R;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;

/**
 * Created by davidbrodsky on 10/23/14.
 */
public class NarrationMediaAdapter extends RecyclerView.Adapter<NarrationMediaAdapter.ViewHolder> {
    public static final String TAG = "OrderMediaAdapter";

    private RecyclerView mRecyclerView;
    private HashMap<ClipCard, Long> mCardToStableId = new HashMap<>();
    private List<ClipCard> mClipCards;
    private Boolean[] mSelectedItems;

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

    public NarrationMediaAdapter(RecyclerView recyclerView, List<ClipCard> cards) {
        mRecyclerView = recyclerView;
        mClipCards = cards;
        long id = 0;
        for (ClipCard card : mClipCards) {
            mCardToStableId.put(card, id++);
        }
        mSelectedItems = new Boolean[cards.size()];
        for(int x = 0; x < mSelectedItems.length; x++) {
            mSelectedItems[x] = false;
        }
    }

    @Override
    public NarrationMediaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.narration_clip_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ((CheckBox) v.findViewById(R.id.check_box)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int checkPosition = (int) buttonView.getTag();
                mSelectedItems[checkPosition] = isChecked;
                validateSelectedItems();
            }
        });
        return new ViewHolder(v);
    }

    private void validateSelectedItems() {
        int firstSelectedIdx = Integer.MAX_VALUE;
        int lastSelectedIdx = -1;

        for (int x = 0; x < mSelectedItems.length; x++) {
            if (mSelectedItems[x]) {

                if (x < firstSelectedIdx) firstSelectedIdx = x;

                if (x > lastSelectedIdx) lastSelectedIdx = x;

            }
        }

        if (firstSelectedIdx == Integer.MAX_VALUE) {
            return;
        }

        for (int x = firstSelectedIdx; x <= lastSelectedIdx; x++) {
            boolean notifyChanged = !mSelectedItems[x];
            mSelectedItems[x] = true;
            if (notifyChanged) notifyItemChanged(x);
        }
    }

    public List<ClipCard> getSelectedCards() {
        ArrayList<ClipCard> result = new ArrayList<>();
        for (int x = 0; x < mSelectedItems.length; x++) {
            if (mSelectedItems[x]) {
                result.add(mClipCards.get(x));
            }
        }
        return result;
    }

    @Override
    public void onBindViewHolder(NarrationMediaAdapter.ViewHolder viewHolder, int position) {

        ClipCard clipCard = mClipCards.get(position);

        String title;
        if (clipCard.getTitle() == null || clipCard.getTitle().length() == 0) {
            String goal = clipCard.getFirstGoal();
            title = String.format("%s: %s", clipCard.getClipType(), goal);
        } else {
            title = clipCard.getTitle();
        }

        viewHolder.title.setText(title);
        viewHolder.checkBox.setTag(position);
        if (mSelectedItems[position]) viewHolder.checkBox.setChecked(true);

        MediaFile mf = clipCard.getSelectedMediaFile();
        if (mf == null) {
            Log.e(this.getClass().getName(), "no media file was found");
        } else {
            mf.loadThumbnail(viewHolder.title.getContext(), viewHolder.thumbnail);
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