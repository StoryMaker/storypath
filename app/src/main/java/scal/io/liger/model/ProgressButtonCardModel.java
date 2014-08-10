package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.ProgressButtonCardView;

public class ProgressButtonCardModel extends CardModel {
    private String text;

    public ProgressButtonCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) { return new ProgressButtonCardView(context, this); }

    public String getText() {
        return this.text;
    }

    public void setText(String time) {
        this.text = time;
    }
}
