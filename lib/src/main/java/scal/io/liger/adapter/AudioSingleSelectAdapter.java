package scal.io.liger.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.util.ArrayList;

import scal.io.liger.R;
import scal.io.liger.model.AudioClip;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPathLibrary;

/**
 * An adapter for selecting a single audio track from a collection.
 * Call {@link #getSelectedClip()} to retrieve the currently selected AudioClip
 *
 * Created by davidbrodsky on 10/23/14.
 */
public class AudioSingleSelectAdapter extends RecyclerView.Adapter<AudioSingleSelectAdapter.ViewHolder> {
    public static final String TAG = "AudioAdapter";

    private StoryPathLibrary mStoryPathLibrary;
    private ArrayList<AudioClip> mAudioClips;
    private int mSelectedPosition = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail;
        public CheckBox checkBox;

        public ViewHolder(View v) {
            super(v);
            thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            checkBox  = (CheckBox) v.findViewById(R.id.check_box);
        }
    }

    public AudioSingleSelectAdapter(StoryPathLibrary storyPathLibrary,
                                    ArrayList<AudioClip> audioClips) {
        mStoryPathLibrary = storyPathLibrary;
        mAudioClips = audioClips;
    }

    @Override
    public AudioSingleSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.narration_clip_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ((CheckBox) v.findViewById(R.id.check_box)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Uncheck previously checked view
                if (isChecked) {
                    int lastSelectedPosition = mSelectedPosition;
                    mSelectedPosition = (int) buttonView.getTag();
                    if (lastSelectedPosition != -1) {
                        notifyItemChanged(lastSelectedPosition);
                    }
                }
            }
        });
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AudioSingleSelectAdapter.ViewHolder viewHolder, int position) {

        AudioClip audio = mAudioClips.get(position);

        viewHolder.checkBox.setTag(position);
        viewHolder.checkBox.setChecked(mSelectedPosition == position);

        MediaFile mf = mStoryPathLibrary.getMediaFile(audio.getUuid());
        if (mf == null) {
            Log.e(this.getClass().getName(), "no media file was found");
        } else {
            mf.loadThumbnail(viewHolder.thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return mAudioClips.size();
    }

    public AudioClip getSelectedClip() {
        return mAudioClips.get(mSelectedPosition);
    }

}