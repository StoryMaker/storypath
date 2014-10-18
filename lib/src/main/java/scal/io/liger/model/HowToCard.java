package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.HowToCardView;
import scal.io.liger.view.DisplayableCard;

public class HowToCard extends Card {

    private String text;

    public HowToCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new HowToCardView(context, this);
    }

    public String getText() {
        return fillReferences(text);
    }

    public void setText(String text) {
        this.text = text;
    }
}
