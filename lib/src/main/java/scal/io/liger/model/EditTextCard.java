package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.EditTextCardView;

public class EditTextCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String hintText;
    @Expose private String header;

    public EditTextCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new EditTextCardView(context, this);
    }

    public String getHintText() {
        return fillReferences(hintText);
    }

    public void setHintText(String text) {
        this.hintText = text;
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof EditTextCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF EditTextCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        EditTextCard castCard = (EditTextCard)card;

        this.title = castCard.getTitle();
        this.hintText = castCard.getHintText();
        this.header = castCard.getHeader();
    }
}
