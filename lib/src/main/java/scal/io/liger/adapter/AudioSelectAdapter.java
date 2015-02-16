package scal.io.liger.adapter;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import scal.io.liger.R;
import scal.io.liger.model.AudioClip;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPathLibrary;

/**
 * An adapter for selecting a audio tracks from a collection.
 * Call {@link #getSelectedClips()} to retrieve the currently selected AudioClip
 *
 * Created by davidbrodsky on 10/23/14.
 */
public class AudioSelectAdapter extends RecyclerView.Adapter<AudioSelectAdapter.ViewHolder> {
    public static final String TAG = "AudioAdapter";

    private StoryPathLibrary mStoryPathLibrary;
    private ArrayList<AudioClip> mAudioClips;
    private boolean[] mSelectedPosition;
    private MediaPlayer mPlayer;
    private int mCurrentlyPlayingPosition = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView thumbnail;
        public CheckBox checkBox;

        public ViewHolder(View v) {
            super(v);
            thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            checkBox  = (CheckBox) v.findViewById(R.id.check_box);
        }
    }

    public AudioSelectAdapter(StoryPathLibrary storyPathLibrary,
                              ArrayList<AudioClip> audioClips) {
        mStoryPathLibrary = storyPathLibrary;
        mAudioClips = audioClips;
        mSelectedPosition = new boolean[audioClips.size()];
    }

    @Override
    public AudioSelectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ((CheckBox) v.findViewById(R.id.check_box)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = (int) buttonView.getTag();
                mSelectedPosition[position] = isChecked;
            }
        });
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AudioSelectAdapter.ViewHolder viewHolder, int position) {

        AudioClip audio = mAudioClips.get(position);

        viewHolder.checkBox.setTag(position);
        viewHolder.checkBox.setChecked(mSelectedPosition[position]);

        MediaFile mf = mStoryPathLibrary.getMediaFile(audio.getUuid());
        if (mf == null) {
            Log.e(this.getClass().getName(), "no media file was found");
        } else {
            mf.loadThumbnail(viewHolder.thumbnail);
        }

        viewHolder.thumbnail.setTag(position);
        viewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();

                // If we click the currently playing cell, stop playback and take no further action
                if (mCurrentlyPlayingPosition == position && mPlayer != null && mPlayer.isPlaying()) {
                    mPlayer.stop();
                    mCurrentlyPlayingPosition = -1;
                    return;
                }

                MediaFile mediaFile = mStoryPathLibrary.getMediaFile(mAudioClips.get(position).getUuid());
                if (mPlayer == null) {
                    mPlayer = MediaPlayer.create(v.getContext(), Uri.parse(mediaFile.getPath()));
                    mPlayer.start();
                }
                else {
                    if (mPlayer.isPlaying()) mPlayer.stop();
                    mPlayer.reset();
                    try {
                        mPlayer.setDataSource(mediaFile.getPath());
                        mPlayer.prepare();
                        mPlayer.start();
                        mCurrentlyPlayingPosition = position;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAudioClips.size();
    }

    public List<AudioClip> getSelectedClips() {
        List<AudioClip> selectedClips = new ArrayList<>();
        for (int idx = 0; idx < mAudioClips.size(); idx++) {
            if (mSelectedPosition[idx]) selectedClips.add(mAudioClips.get(idx));
        }
        return selectedClips;
    }

}