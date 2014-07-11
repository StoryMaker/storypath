package scal.io.liger;

import java.util.ArrayList;

/**
 * Created by mnbogner on 7/10/14.
 */
public class StoryPathModel {
    public String id;
    public String title;
    public ArrayList<Object> cards;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Object> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Object> cards) {
        this.cards = cards;
    }

    public void addCard(Object card) {
        if (this.cards == null)
            this.cards = new ArrayList<Object>();

        this.cards.add(card);
    }
}
