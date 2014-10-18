package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.CoverCardView;

public class CoverCard extends Card {

    private String header;
    private String text;
    private String mediaPath;

    public CoverCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new CoverCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getText() {
        return fillReferences(text);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaPath() { return fillReferences(mediaPath); }

    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }
}