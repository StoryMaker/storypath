package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.LinkCardView;

public class LinkCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String text;
    @Expose private String link;

    public LinkCard() {
        super();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) { return new LinkCardView(context, this); }

    public String getText() {
        return fillReferences(this.text);
    }

    public void setText(String time) {
        this.text = time;
    }

    public String getLink() {
        return fillReferences(this.link);
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void linkNotification(String linkPath) {

        // TEMP
        if (text.equals("LOAD")) {
            Log.d(" *** TESTING *** ", "LOADING " + link);
            loadStoryPath(link);
            return;
        }

        if (storyPath != null) {
            storyPath.linkNotification(linkPath);
        } else {
            System.err.println("STORY PATH REFERENCE NOT FOUND, CANNOT SEND LINK NOTIFICATION");
        }
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof LinkCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF LinkCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        LinkCard castCard = (LinkCard)card;

        this.title = castCard.getTitle();
        this.text = castCard.getText();
    }
}
