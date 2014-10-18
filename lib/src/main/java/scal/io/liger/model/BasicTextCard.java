package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.BasicTextCardView;
import scal.io.liger.view.DisplayableCard;

public class BasicTextCard extends Card {

    private String text;

    public BasicTextCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new BasicTextCardView(context, this);
    }

    public String getText() {
        return fillReferences(text);
    }

    public void setText(String text) {
        this.text = text;
    }
}
