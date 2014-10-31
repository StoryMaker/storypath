package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.UUID;

import scal.io.liger.Constants;
import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.ReviewCardView;

public class ReviewCard extends GenericCard {

    @Expose private ClipMetadata narration;

    public ReviewCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new ReviewCardView(context, this);
    }

    public String getMediaPath() { return fillReferences(mediaPath); }

    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }

    public void setNarration(MediaFile narrationMediaFile) {
        ClipMetadata cmd = new ClipMetadata(Constants.NARRATION, UUID.randomUUID().toString());

        getStoryPath().saveMediaFile(cmd.getUuid(), narrationMediaFile);
        setNarration(cmd);
    }

    public void setNarration(ClipMetadata narrationClip) {
        setNarration(narrationClip, true);
    }

    public void setNarration(ClipMetadata clip, boolean notify) {
        this.narration = clip;

        // send notification that a narration has been saved so that cards will be refreshed
        if (notify) {
            setChanged();
            notifyObservers();
        }
    }

    public MediaFile getSelectedNarrationFile(){
        if (narration == null) {
            Log.e(this.getClass().getName(), "no narration metadata was found, cannot get file");
            return null;
        }
        return loadMediaFile(narration);
    }

    public ClipMetadata getSelectedNarrationClip() {
        if (narration == null) {
            Log.e(this.getClass().getName(), "no narration metadata was found, cannot get file");
            return null;
        }

        return narration;
    }

    public MediaFile loadMediaFile(ClipMetadata cmd) {
        return getStoryPath().loadMediaFile(cmd.getUuid());
    }
}