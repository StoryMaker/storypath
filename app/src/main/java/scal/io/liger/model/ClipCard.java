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
        if (this.clips == null) {
            this.clips = new ArrayList<ClipMetadata>();
        }

        this.clips.add(clip);

        // send notification that a clip has been saved so that cards will be refreshed
        if (storyPathReference != null) {
            storyPathReference.notifyActivity();
        } else {
            System.err.println("STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
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
}
