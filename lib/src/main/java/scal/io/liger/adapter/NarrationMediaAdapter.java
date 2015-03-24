package scal.io.liger.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import scal.io.liger.R;
import scal.io.liger.model.AudioClip;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPathLibrary;

/**
 * Created by davidbrodsky on 10/23/14.
 */
public class NarrationMediaAdapter extends RecyclerView.Adapter<NarrationMediaAdapter.ViewHolder> {
    public static final String TAG = "NarrationMediaAdapter";

    private RecyclerView mRecyclerView;
    private HashMap<ClipCard, Long> mCardToStableId = new HashMap<>();
    private List<ClipCard> mClipCards;
    private StoryPathLibrary mStoryPathLibrary;
    private ArrayList<AudioClip> mAudioClips;
    private Boolean[] mSelectedItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail;
        public TextView title;
        public CheckBox checkBox;
        public ImageView narrationIndicator;

        public ViewHolder(View v) {
            super(v);
            thumbnail          = (ImageView) v.findViewById(R.id.thumbnail);
            title              = (TextView) v.findViewById(R.id.title);
            checkBox           = (CheckBox) v.findViewById(R.id.check_box);
            narrationIndicator = (ImageView) v.findViewById(R.id.narrationIndicator);

            // Temporarily remove ability to select clips
            checkBox.setVisibility(View.GONE);
        }
    }

    public NarrationMediaAdapter(RecyclerView recyclerView,
                                 List<ClipCard> cards,
                                 ArrayList<AudioClip> audioClips) {

        mRecyclerView = recyclerView;
        mClipCards = cards;
        mAudioClips = audioClips;
        long id = 0;
        for (ClipCard card : mClipCards) {
            mCardToStableId.put(card, id++);
        }
        mSelectedItems = new Boolean[cards.size()];
        for(int x = 0; x < mSelectedItems.length; x++) {
            // Temporarily remove ability to select clips
            //mSelectedItems[x] = false;
            mSelectedItems[x] = true;
        }

        mStoryPathLibrary = mClipCards.get(0).getStoryPath().getStoryPathLibrary();
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
            mf.loadThumbnail(viewHolder.thumbnail);
        }

        if (clipHasNarration(position)) {
            viewHolder.narrationIndicator.setVisibility(View.VISIBLE);
            viewHolder.narrationIndicator.setTag(position);
            viewHolder.narrationIndicator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag();
                    Log.d(TAG, "Narration dialog selected for position " + position);
                    removeNarrationForClip(position);
                }
            });
        } else
            viewHolder.narrationIndicator.setVisibility(View.GONE);
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

    /**
     * Placeholder until an API exists
     */
    private void removeNarrationForClip(final int position) {
        // TODO When a narration overlaps multiple clips, what happens when I remove it from one?
        // Is it also removed from the other?
        List<AudioClip> audio = getAudioAtPosition(position);
        if (audio.size() == 0) return; // Caller should have ensured an AudioClip was present here

        final AudioSelectAdapter adapter = new AudioSelectAdapter(mClipCards.get(position)
                                                                            .getStoryPath()
                                                                            .getStoryPathLibrary(),
                                                                  getAudioAtPosition(position));

        View root = ((LayoutInflater) mRecyclerView.getContext()
                                                   .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                                                   .inflate(R.layout.dialog_narration_tracks, null);

        RecyclerView audioRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        audioRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        audioRecyclerView.setAdapter(adapter);

        mClipCards.get(position).getSelectedMediaFile().loadThumbnail(((ImageView) root.findViewById(R.id.thumbnail)));

        AlertDialog.Builder builder = new AlertDialog.Builder(mRecyclerView.getContext());
        Dialog dialog = builder.setView(root)
               .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       List<AudioClip> audioClipsToRemove = adapter.getSelectedClips();

                       for (AudioClip audioClip : audioClipsToRemove) {
                           mClipCards.get(0)
                                   .getStoryPath()
                                   .getStoryPathLibrary()
                                   .removeAudioClipFromClipCard(mClipCards,
                                           audioClip,
                                           mClipCards.get(position));
                       }


                       mClipCards.get(0)
                               .getStoryPath()
                               .getStoryPathLibrary()
                               .save(false);

                       notifyItemChanged(position);
                   }
               })
               .setNegativeButton("Cancel", null)
               .create();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                adapter.release();
            }
        });
        dialog.show();
//        audioRecyclerView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Placeholder until an API exists
     * @return whether the given clip has an associated narration track
     */
    private boolean clipHasNarration(int position) {
        return getAudioAtPosition(position).size() > 0;
    }

    /**
     * Return a List of all the AudioClips that span the ClipCard
     * at the given position
     */
    private ArrayList<AudioClip> getAudioAtPosition(int position) {
        ArrayList<AudioClip> result = new ArrayList<>();

        if (mAudioClips == null) {
            Log.d(TAG, "No AudioClips for position " + position);
            return result;
        }

        for (AudioClip audio : mAudioClips) {
            int firstIdx = mClipCards.indexOf(mStoryPathLibrary.getFirstClipCardForAudioClip(audio, mClipCards));
            int lastIdx = mClipCards.indexOf(mStoryPathLibrary.getLastClipCardForAudioClip(audio, mClipCards));

            Log.d(TAG, String.format("Audio %s spans pos %d - %d. Clip is pos %d", audio.getUuid().substring(0,3), firstIdx, lastIdx, position));
            if (firstIdx <= position && position <= lastIdx) {
                result.add(audio);
                Log.d(TAG, "Audio spans Clip at pos " + position);
            }

//            if (audio.getPositionClipId() != null) {
//                if (audio.getPositionClipId().equals(
//                    mClipCards.get(position).getId())) {
//
//                    result.add(audio);
//                }
//            } else if (audio.getPositionIndex() == position) {
//                result.add(audio);
//            }
        }
        Log.d(TAG, String.format("Found %d clips for position %d", result.size(), position));
        return result;
    }

}