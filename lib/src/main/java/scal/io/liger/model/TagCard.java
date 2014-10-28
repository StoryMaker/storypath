package scal.io.liger.model;

import android.content.Context;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.GenericCardView;

public class TagCard extends Card {

    @Expose private String mediaPath;

    public TagCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new GenericCardView(context, this);
    }

    public String getMediaPath() { return fillReferences(mediaPath); }

    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }
}