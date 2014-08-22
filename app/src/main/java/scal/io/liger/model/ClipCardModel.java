package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.ClipCardView;


public class ClipCardModel extends ExampleCardModel {

    public String clip_type;

    public ClipCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new ClipCardView(context, this);
    }

    public String getClipType() { return fillReferences(clip_type); }

    public void setClipType(String clip_type) { this.clip_type = clip_type; }
}
