package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.MediumCardView;
import scal.io.liger.view.DisplayableCard;


public class MediumCard extends Card {

    private String header;

    public MediumCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new MediumCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
