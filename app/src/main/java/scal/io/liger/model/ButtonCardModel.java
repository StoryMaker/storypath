package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.ButtonCardView;

public class ButtonCardModel extends CardModel {
    private String text;

    public ButtonCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) { return new ButtonCardView(context, this); }

    public String getText() {
        return fillReferences(this.text);
    }

    public void setText(String time) {
        this.text = time;
    }
}
