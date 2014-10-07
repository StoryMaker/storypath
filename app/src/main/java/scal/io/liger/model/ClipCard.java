package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.fima.cardsui.objects.Card;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import scal.io.liger.view.ClipCardView;


public class ClipCard extends ExampleCard {

    // need length, medium, type restrictions, etc
    public String clip_type;
    public ArrayList<ClipMetadata> clips;

    public ClipCard() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new ClipCardView(context, this);
    }

    public String getClip_type() {
        return fillReferences(clip_type);
    }

    public void setClip_type(String clip_type) {
        this.clip_type = clip_type;
    }

    public ArrayList<ClipMetadata> getClips() {
        return clips;
    }

    public void setClips(ArrayList<ClipMetadata> clips) {
        this.clips = clips;
    }

    public void addClip(ClipMetadata clip) {
        addClip(clip, true);
    }

    public void addClip(ClipMetadata clip, boolean notify) {
        if (this.clips == null) {
            this.clips = new ArrayList<ClipMetadata>();
        }

        // by default, the last recorded clip is considered "selected"
        this.clips.add(0, clip);

        // send notification that a clip has been saved so that cards will be refreshed
        if (storyPathReference != null) {
            if (notify) {
                storyPathReference.notifyActivity();
            }
        } else {
            Log.e(this.getClass().getName(), "story path reference not found, cannot sent notification");
        }
    }

    public void saveMediaFile(MediaFile mf) {

        ClipMetadata cmd = new ClipMetadata();
        cmd.type = clip_type;
        cmd.uuid = UUID.randomUUID().toString();

        getStoryPathReference().saveMediaFile(cmd.uuid, mf);
        addClip(cmd);
    }

    public MediaFile loadMediaFile(ClipMetadata cmd) {
        return getStoryPathReference().loadMediaFile(cmd.uuid);
    }

    public void selectMediaFile(int index) {
        // unsure if selection should be based on index or object

        if ((clips == null) || (clips.size() < 1)) {
            Log.e(this.getClass().getName(), "no clip metadata was found, cannot select a file");
            return;
        }

        ClipMetadata cmd = clips.remove(index);
        clips.add(0, cmd);
    }

    public MediaFile getSelectedMediaFile() {
        if ((clips == null) || (clips.size() < 1)) {
            Log.e(this.getClass().getName(), "no clip metadata was found, cannot get a selected file");
            return null;
        }

        return loadMediaFile(clips.get(0));
    }

    // the card-level delete method only deletes the local reference, not the actual media file
    public void deleteMediaFile(int index) {
        // unsure if selection should be based on index or object

        if ((clips == null) || (index >= clips.size())) {
            Log.e(this.getClass().getName(), "index out of range, cannot delete file");
            return;
        }

        clips.remove(index);
    }
}
