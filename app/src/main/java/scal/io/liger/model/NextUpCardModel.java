package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.NextUpCardView;

public class NextUpCardModel extends CardModel {
    private String text;
    public ArrayList<LinkModel> links;

    public NextUpCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) { return new NextUpCardView(context, this); }

    public String getText() {
        return fillReferences(this.text);
    }

    public void setText(String time) {
        this.text = time;
    }

    public ArrayList<LinkModel> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<LinkModel> links) {
        this.links = links;
    }

    public void addLink(LinkModel link) {
        if (this.links == null)
            this.links = new ArrayList<LinkModel>();

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
