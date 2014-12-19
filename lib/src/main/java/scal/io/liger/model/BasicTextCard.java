package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.BasicTextCardView;
import scal.io.liger.view.DisplayableCard;

public class BasicTextCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String text;

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

    @Override
    public void copyText(Card card) {
        if (!(card instanceof BasicTextCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF BasicTextCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        BasicTextCard castCard = (BasicTextCard)card;

        this.title = castCard.getTitle();
        this.text = castCard.getText();
    }
}
