package scal.io.liger;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by mnbogner on 7/10/14.
 */
public class StoryPathModel {
    public String id;
    public String title;
    public ArrayList<CardModel> cards;

    public ArrayList<DependencyModel> dependencies;

    public Context c; // probably should remove this

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

    public ArrayList<CardModel> getCards() {
        return cards;
    }

    public void setCards(ArrayList<CardModel> cards) {
        this.cards = cards;
    }

    public void addCard(CardModel card) {
        if (this.cards == null)
            this.cards = new ArrayList<CardModel>();

        this.cards.add(card);
    }

    public ArrayList<CardModel> getVisibleCards() {
        return null;
    }

    public CardModel getCardById(String fullPath) {
        String[] pathParts = fullPath.split("::");

        // sanity check
        if (!this.id.equals(pathParts[0]))
        {
            System.out.println("WRONG CARD ID " + this.id + " vs. " + pathParts[0]);
            return null;
        }

        // assumes the format story::card::field::value
        for (CardModel card : cards)
        {
            if (card.getId().equals(pathParts[1]))
                {
                    System.out.println("FOUND CARD " + card.getId());
                    return card;
                }
            }

            System.out.println("CARD NOT FOUND FOR " + pathParts[1]);
            return null;
    }

    public ArrayList<DependencyModel> getDependencies() {
        return dependencies;
    }

    public void setDependencies(ArrayList<DependencyModel> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(DependencyModel dependency) {
        if (this.dependencies == null)
            this.dependencies = new ArrayList<DependencyModel>();

        this.dependencies.add(dependency);
    }

    // set a reference to this story path in each card
    // must be done before cards attempt to reference
    // values from previous story paths or cards
    public void setCardReferences(StoryPathModel spm) {
        for (CardModel card : cards) {
            card.setStoryPathReference(this);
        }
    }

    // clear references to this story path from each card
    // must be done before serializing this story path to
    // prevent duplication or circular references
    public void clearCardReferences() {
        for (CardModel card : cards) {
            card.setStoryPathReference(null);
        }
    }

    public String getReferencedValue(String fullPath) {
        System.out.println("PATH: " + fullPath);
        String[] pathParts = fullPath.split("::");

        StoryPathModel spm = null;
        if (this.getId().equals(pathParts[0])) {
            System.out.println("INTERNAL DEPENDENCY");
            spm = this;
        } else {
            System.out.println("EXTERNAL DEPENDENCY");
            for (DependencyModel dm : dependencies) {
                System.out.println("LOOKING FOR " + pathParts[0] + " -> " + dm.getDependencyId());
                if (dm.getDependencyId().equals(pathParts[0])) {
                    System.out.println("FOUND");
                    GsonBuilder gBuild = new GsonBuilder();
                    gBuild.registerTypeAdapter(StoryPathModel.class, new StoryPathDeserializer());
                    Gson gson = gBuild.create();

                    String json = JsonHelper.loadJSON(c, dm.getDependencyFile());
                    spm = gson.fromJson(json, StoryPathModel.class);
                }
            }
        }

        for (CardModel cm : spm.getCards()) {
            if (cm.getId().equals(pathParts[1])) {
                ArrayList<String> values = cm.getValues();

                for (String value : values) {
                    String[] valueParts = value.split("::");
                    if (valueParts[0].equals(pathParts[2]))
                    {
                        System.out.println("FOUND VALUE " + valueParts[0] + " -> " + valueParts[1]);
                        return valueParts[1];
                    }
                }

                // nothing found
                return null;

                // unnecessary if only saved values are retrieved
                // saved values now stored in "values" collection
                /*
                Class c = cm.getClass();
                Field f = null;
                try {
                    f = c.getField(pathParts[2]);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                    System.err.println("FIELD " + pathParts[2] + " NOT FOUND IN CLASS " + c.getName());
                    return null;
                }

                try {
                    return (String)(f.get(cm));
                } catch (IllegalAccessException e) {
                    System.err.println("COULD NOT GET VALUE FOR FIELD " + pathParts[2] + " IN CLASS " + c.getName());
                    e.printStackTrace();
                    return null;
                }
                */
            }
        }

        System.err.println("NO CARDS?");
        return null;
    }

    public ArrayList<CardModel> getValidCards() {
        ArrayList<CardModel> validCards = new ArrayList<CardModel>();

        for (CardModel cm : cards) {
            if (cm.checkReferencedValues()) {
                System.err.println("VALID CARD: " + cm.getId());
                validCards.add(cm);
            } else {
                System.err.println("INVALID CARD: " + cm.getId());
            }
        }

        return validCards;
    }
}
