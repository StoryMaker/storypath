package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.ButtonCardView;

public class ButtonCard extends Card {
    private String text;

    public ButtonCard() {
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) { return new ButtonCardView(context, this); }

    public String getText() {
        return fillReferences(this.text);
    }

    public void setText(String time) {
        this.text = time;
    }
}
