package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.ButtonCardView;
import scal.io.liger.view.DisplayableCard;

public class ButtonCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String text;

    public ButtonCard() {
        super();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) { return new ButtonCardView(context, this); }

    public String getText() {
        return fillReferences(this.text);
    }

    public void setText(String time) {
        this.text = time;
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof ButtonCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF ButtonCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        ButtonCard castCard = (ButtonCard)card;

        this.title = castCard.getTitle();
        this.text = castCard.getText();
    }
}
