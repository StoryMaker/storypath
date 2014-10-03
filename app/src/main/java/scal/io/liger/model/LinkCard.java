package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.LinkCardView;

public class LinkCard extends Card {
    private String text;
    private String link;

    public LinkCard() {
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) { return new LinkCardView(context, this); }

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
        if (storyPathReference != null) {
            storyPathReference.linkNotification(linkPath);
        } else {
            System.err.println("STORY PATH REFERENCE NOT FOUND, CANNOT SEND LINK NOTIFICATION");
        }
    }
}
