package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.ButtonCardView;
import scal.io.liger.view.DisplayableCard;

public class ButtonCard extends Card {

    private String text;

    public ButtonCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) { return new ButtonCardView(context, this); }

    public String getText() {
        return fillReferences(this.text);
    }

    public void setText(String time) {
        this.text = time;
    }
}
