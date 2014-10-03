package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.BasicTextCardView;

public class BasicTextCard extends Card {
    public String text;

    public BasicTextCard() {
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
        return new BasicTextCardView(context, this);
    }

    public String getText() {
        return fillReferences(text);
    }

    public void setText(String text) {
        this.text = text;
    }
}
