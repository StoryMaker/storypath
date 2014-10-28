package scal.io.liger.model;

import android.content.Context;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.MarkdownCardView;

public class MarkdownCard extends Card {

    @Expose protected String text;

    public MarkdownCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new MarkdownCardView(context, this);
    }

    public String getText() {
        return fillReferences(text);
    }

    public void setText(String text) {
        this.text = text;
    }
}
