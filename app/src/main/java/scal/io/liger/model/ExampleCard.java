package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.ExampleCardView;


public class ExampleCard extends Card {

    private String header;
    private String clip_medium;
    private String example_media_path;

    public ExampleCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
        return new ExampleCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getClipMedium() { return fillReferences(clip_medium); }

    public void setClipMedium(String clip_medium) { this.clip_medium = clip_medium; }

    public String getExampleMediaPath() { return example_media_path; }

    public void setExampleMediaPath(String example_media_path) { this.example_media_path = example_media_path; }
}
