package scal.io.liger.model;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.StoryPathDeserializer;

/**
 * Created by mnbogner on 7/10/14.
 */
public class StoryPath {
    public String id;
    public String title;
    public ArrayList<Card> cards;
    public ArrayList<Dependency> dependencies;
    public String fileLocation;
    public Story storyReference;

    // this is used by the JsonHelper class to load json assets
    // if there is an alternate way to load them, this should be removed
    // also must be cleared before serializing story path
    public Context context;

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

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public void addCard(Card card) {
        if (this.cards == null)
            this.cards = new ArrayList<Card>();

        this.cards.add(card);
    }

    public Card getCardById(String fullPath) {
        // assumes the format story::card::field::value
        String[] pathParts = fullPath.split("::");

        // sanity check
        if (!this.id.equals(pathParts[0])) {
            System.err.println("STORY PATH ID " + pathParts[0] + " DOES NOT MATCH");
            return null;
        }

        for (Card card : cards) {
            if (card.getId().equals(pathParts[1])) {
                return card;
            }
        }

        System.err.println("CARD ID " + pathParts[1] + " WAS NOT FOUND");
        return null;
    }

    // new method to get batches of cards while preserving card order
    public ArrayList<Card> getCardsByIds(ArrayList<String> fullPaths) {
        ArrayList<String> cardIds = new ArrayList<String>();
        for (String fullPath : fullPaths) {
            // assumes the format story::card::field::value
            String[] pathParts = fullPath.split("::");
            cardIds.add(pathParts[1]);
        }

        ArrayList<Card> foundCards = new ArrayList<Card>();
        for (Card card : cards) {
            if (cardIds.contains(card.getId())) {
                foundCards.add(card);
            }
        }

        return foundCards;
    }

    public ArrayList<Card> getValidCards() {
        ArrayList<Card> validCards = new ArrayList<Card>();

        for (Card card : cards) {
            if (card.checkReferencedValues()) {
                validCards.add(card);
            }
        }

        return validCards;
    }

    public ArrayList<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(ArrayList<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(Dependency dependency) {
        if (this.dependencies == null)
            this.dependencies = new ArrayList<Dependency>();

        this.dependencies.add(dependency);
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public Story getStoryReference() {
        return storyReference;
    }

    public void setStoryReference(Story storyReference) {
        this.storyReference = storyReference;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    // set a reference to this story path in each card
    // must be done before cards attempt to reference
    // values from previous story paths or cards
    public void setCardReferences() {
        for (Card card : cards) {
            card.setStoryPathReference(this);
        }
    }

    // clear references to this story path from each card
    // must be done before serializing this story path to
    // prevent duplication or circular references
    public void clearCardReferences() {
        for (Card card : cards) {
            card.setStoryPathReference(null);
        }
    }

    public String getReferencedValue(String fullPath) {
        // assumes the format story::card::field::value
        String[] pathParts = fullPath.split("::");

        if (!this.getId().equals(pathParts[0])) {
            return Constants.EXTERNAL;
        }

        Card card = this.getCardById(fullPath);

        if (card == null) {
            return null;
        } else {
            String value = card.getValueById(fullPath);

            if (value == null) {
                return null;
            } else {
                return value;
            }
        }
    }

    public String getExternalReferencedValue(String fullPath) {
        // assumes the format story::card::field::value
        String[] pathParts = fullPath.split("::");

        StoryPath story = null;

        // reference targets a serialized story path
        for (Dependency dependency : dependencies) {
            if (dependency.getDependencyId().equals(pathParts[0])) {
                GsonBuilder gBuild = new GsonBuilder();
                gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
                Gson gson = gBuild.create();

                String json = JsonHelper.loadJSONFromPath(buildPath(dependency.getDependencyFile()));
                story = gson.fromJson(json, StoryPath.class);

                story.context = this.context;
                story.setCardReferences();
                story.setFileLocation(buildPath(dependency.getDependencyFile()));
            }
        }

        if (story == null) {
            Log.e(this.getClass().getName(), "STORY PATH ID " + pathParts[0] + " WAS NOT FOUND");
            return null;
        }

        Card card = story.getCardById(fullPath);

        if (card == null) {
            return null;
        } else {
            String value = card.getValueById(fullPath);

            if (value == null) {
                return null;
            } else {
                return value;
            }
        }
    }

    public String buildPath(String originalPath) {
        if (originalPath.startsWith(File.separator)) {
            return originalPath;
        }

        // construct path relative to location of story path
        String relativePath = getFileLocation();

        if ((relativePath != null) && (relativePath.length() != 0)) {
            relativePath = relativePath.substring(0, relativePath.lastIndexOf(File.separator));
            relativePath = relativePath + File.separator + originalPath;
            return relativePath;
        } else {
            Log.e(this.getClass().getName(), "NO ROOT TO CONSTRUCT RELATIVE PATH FOR " + originalPath);
            return originalPath;
        }
    }

    public void notifyActivity() {
        Log.d("StoryPathModel", "notifyActivity");
        if (context != null) {
            MainActivity mainActivity = (MainActivity) context; // FIXME this isn't a save cast as context can sometimes not be an activity (getApplicationContext())
            mainActivity.refreshCardView();
        } else {
            System.err.println("APP CONTEXT REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
        }
    }

    public void linkNotification(String linkPath) {
        if (context != null) {
            try {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.goToCard(linkPath);
            } catch (Exception e) {
                Toast.makeText(context, "JSON parsing error: " + e.getMessage().substring(e.getMessage().indexOf(":") + 2), Toast.LENGTH_LONG).show();
            }
        } else {
            System.err.println("APP CONTEXT REFERENCE NOT FOUND, CANNOT SEND LINK NOTIFICATION");
        }
    }

    public int getCardIndex(Card cardModel) {
        return cards.indexOf(cardModel);
    }

    public int getValidCardIndex(Card cardModel) {
        return getValidCards().indexOf(cardModel);
    }

    public Card getCardFromIndex(int index) {
        if(index >= cards.size()) {
            return null;
        }

        return cards.get(index);
    }

    public Card getValidCardFromIndex(int index) {
        ArrayList<Card> validCards = getValidCards();

        if(index >= validCards.size()) {
            return null;
        }

        return validCards.get(index);
    }

    public void rearrangeCards(int currentIndex, int newIndex) {
        Card card = cards.remove(currentIndex);
        cards.add(newIndex, card);
        notifyActivity();
    }

    public void saveMediaFile(String uuid, MediaFile file) {
        storyReference.saveMediaFile(uuid, file);
    }

    public MediaFile loadMediaFile(String uuid) {
        return storyReference.loadMediaFile(uuid);
    }
}
