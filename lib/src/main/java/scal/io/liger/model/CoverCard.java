package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.CoverCardView;

public class CoverCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String header;
    @Expose private String text;
    @Expose private String mediaPath;

    public CoverCard() {
        super();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new CoverCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getText() {
        return fillReferences(text);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaPath() { return fillReferences(mediaPath); }

    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof CoverCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF CoverCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        CoverCard castCard = (CoverCard)card;

        this.title = castCard.getTitle();
        this.header = castCard.getHeader();
        this.text = castCard.getText();
    }
}