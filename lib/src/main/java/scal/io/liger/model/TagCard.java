package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.GenericCardView;

public class TagCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String mediaPath;

    public TagCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new GenericCardView(context, this);
    }

    public String getMediaPath() { return fillReferences(mediaPath); }

    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof SelfEvalCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF SelfEvalCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        SelfEvalCard castCard = (SelfEvalCard)card;

        this.title = castCard.getTitle();
    }
}