package scal.io.liger.model;

import android.content.Context;

import java.util.ArrayList;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.NextUpCardView;

public class NextUpCard extends Card {

    private String text;
    private ArrayList<Link> links;

    public NextUpCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) { return new NextUpCardView(context, this); }

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
        if (storyPathReference != null) {
            storyPathReference.linkNotification(linkPath);
        } else {
            System.err.println("STORY PATH REFERENCE NOT FOUND, CANNOT SEND LINK NOTIFICATION");
        }
    }
}
