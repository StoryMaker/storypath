package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.ClipCardView;
import scal.io.liger.view.ExampleCardView;


public class ExampleCardModel extends CardModel {

    public String header;
    public String clip_medium;
    public String clip_type;
    public String example_media_path;

    public ExampleCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
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

    public String getClipType() { return fillReferences(clip_type); }

    public void setClipType(String clip_type) { this.clip_type = clip_type; }

    public String getExampleMediaPath() { return example_media_path; }

    public void setExampleMediaPath(String example_media_path) { this.example_media_path = example_media_path; }
}
