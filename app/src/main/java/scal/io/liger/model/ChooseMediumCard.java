package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.ChooseMediumCardView;


public class ChooseMediumCard extends Card {

    private String header;

    public ChooseMediumCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
        return new ChooseMediumCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
