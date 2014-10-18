package scal.io.liger.model;

import android.content.Context;
import android.util.Log;


import java.util.ArrayList;
import java.util.UUID;

import scal.io.liger.view.ClipCardView;
import scal.io.liger.view.DisplayableCard;


public class ClipCard extends ExampleCard {

    private String clipType;
    private ArrayList<ClipMetadata> clips;
    // need length, medium, type restrictions, etc

    public ClipCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new ClipCardView(context, this);
    }

    public String getClipType() {
        return fillReferences(clipType);
    }

    public void setClipType(String clipType) {
        this.clipType = clipType;
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
        if (notify) {
            setChanged();
            notifyObservers();
        }
    }

    public void saveMediaFile(MediaFile mf) {
        ClipMetadata cmd = new ClipMetadata(clipType, UUID.randomUUID().toString());

        getStoryPathReference().saveMediaFile(cmd.getUuid(), mf);
        addClip(cmd);
    }

    public MediaFile loadMediaFile(ClipMetadata cmd) {
        return getStoryPathReference().loadMediaFile(cmd.getUuid());
    }

    public void selectMediaFile(ClipMetadata clip) {
        if ((clips == null) || (clips.size() < 1)) {
            Log.e(this.getClass().getName(), "no clip metadata was found, cannot select a file");
            return;
        }

        if (clips.indexOf(clip) != -1) {
            selectMediaFile(clips.indexOf(clip));
        } else {
            Log.e(this.getClass().getName(), "specified clip not found in clips");
        }
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
