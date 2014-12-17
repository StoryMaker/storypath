package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.MilestoneCardView;

public class MilestoneCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String text;
    @Expose private ArrayList<Link> links;

    public MilestoneCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) { return new MilestoneCardView(context, this); }

    public String getText() {
        return fillReferences(this.text);
    }

    public void setText(String time) {
        this.text = time;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<Link> links) {
        this.links = links;
    }

    public void addLink(Link link) {
        if (this.links == null)
            this.links = new ArrayList<Link>();

        this.links.add(link);
    }

    public void linkNotification(String linkPath) {
        if (storyPath != null) {
            storyPath.linkNotification(linkPath);
        } else {
            System.err.println("STORY PATH REFERENCE NOT FOUND, CANNOT SEND LINK NOTIFICATION");
        }
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof MilestoneCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF MilestoneCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        MilestoneCard castCard = (MilestoneCard)card;

        this.title = castCard.getTitle();
        this.text = castCard.getText();
        this.links = castCard.getLinks(); // only the text of these links should be language-dependent
    }
}
