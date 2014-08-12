package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.MarkdownCardView;

public class MarkdownCardModel extends CardModel {
    private String text;

    public MarkdownCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new MarkdownCardView(context, this);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
