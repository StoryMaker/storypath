package scal.io.liger.model;

import android.content.Context;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.PublishButtonCardView;
import scal.io.liger.view.DisplayableCard;

public class PublishButtonCard extends Card {

    @Expose private String text;

    public PublishButtonCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) { return new PublishButtonCardView(context, this); }

    public String getText() {
        return fillReferences(this.text);
    }

    public void setText(String time) {
        this.text = time;
    }
}
