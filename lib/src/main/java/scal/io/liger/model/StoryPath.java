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
import scal.io.liger.ReferenceHelper;
import scal.io.liger.StoryPathDeserializer;

/**
 * Created by mnbogner on 7/10/14.
 */
public class StoryPath {

    protected String id;
    protected String title;
    protected String classPackage;
    protected ArrayList<Card> cards;
    protected ArrayList<Card> visibleCards;
    protected ArrayList<Dependency> dependencies;
    protected String fileLocation;
    protected StoryPathLibrary storyPathLibraryReference; // not serialized
    protected String storyPathLibraryFile;

    // this is used by the JsonHelper class to load json assets
    // if there is an alternate way to load them, this should be removed
    // also must be cleared before serializing story path
    protected Context context;

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

    public String getClassPackage() {
        return classPackage;
    }

    public void setClassPackage(String classPackage) {
        this.classPackage = classPackage;
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
        if (visibleCards == null) {
            visibleCards = new ArrayList<Card>();

            for (Card card : cards) {
                if (card.checkStateVisibility()) {
                    visibleCards.add(card);
                }
            }
        }

        return visibleCards;

        /*
        ArrayList<Card> validCards = new ArrayList<Card>();

        for (Card card : cards) {
            if (card.checkReferencedValues()) {
                validCards.add(card);
            }
        }

        return validCards;
        */
    }

    // required for serialization/deserialization
    public void setValidCards(ArrayList<Card> validCards) {
        this.visibleCards = visibleCards;
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

    public StoryPathLibrary getStoryPathLibraryReference() {
        return storyPathLibraryReference;
    }

    public void setStoryPathLibraryReference(StoryPathLibrary storyPathLibraryReference) {
        this.storyPathLibraryReference = storyPathLibraryReference;
    }

    public String getStoryPathLibraryFile() {
        return storyPathLibraryFile;
    }

    public void setStoryPathLibraryFile(String storyPathLibraryFile) {
        this.storyPathLibraryFile = storyPathLibraryFile;
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

    // observers must be initialized after cards are deserialized
    // observers must be cleared before serializing to prevent
    // circular references (cards pointing to cards)
    public void initializeObservers() {
        for (Card card : cards) {
            card.registerObservers();
        }
    }

    public void clearObservers() {
        for (Card card : cards) {
            card.removeObservers();
        }
    }

    public void resetVisibility() {
        for (Card card : cards) {
            card.resetStateVisibility();
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

            //Log.d("TESTING", "ID: " + this.getId() + " BASE PART: " + relativePath + " OTHER PART: " + originalPath);

            relativePath = relativePath + File.separator + originalPath;
            return relativePath;
        } else {
            Log.e(this.getClass().getName(), "NO ROOT TO CONSTRUCT RELATIVE PATH FOR " + originalPath);
            return originalPath;
        }
    }

    public void notifyActivity(Card updatedCard) {
        Log.d("StoryPathModel", "notifyActivity");

        if (updatedCard.getStateVisiblity()) {
            // new or updated

            int cardIndex = 0;

            if (visibleCards.contains(updatedCard)) {
                visibleCards.remove(updatedCard);
            } else {
                // foo
            }

            cardIndex = findSpot(updatedCard);

            if (cardIndex >= visibleCards.size()) {
                visibleCards.add(updatedCard);
            } else {
                visibleCards.add(cardIndex, updatedCard);
            }
        } else {
            // deleted

            if (visibleCards.contains(updatedCard)) {
                visibleCards.remove(updatedCard);
            } else {
                // foo
            }
        }

        // call refresh for now, card list will be returned

        if (context != null) {
            MainActivity mainActivity = (MainActivity) context; // FIXME this isn't a save cast as context can sometimes not be an activity (getApplicationContext())
            mainActivity.refreshCardView();
        } else {
            System.err.println("APP CONTEXT REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
        }
    }

    public int findSpot(Card card) {
        int baseIndex = cards.indexOf(card);
        int newIndex = 0;
        for (int i = (baseIndex - 1); i >= 0; i--) {
            Card previousCard = cards.get(i);
            if (visibleCards.contains(previousCard)) {
                newIndex = visibleCards.indexOf(previousCard) + 1;
                break;
            }
        }

        return newIndex;
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
        notifyActivity(card);
    }

    public void saveMediaFileSP(String uuid, MediaFile file) {
        storyPathLibraryReference.saveMediaFileSPL(uuid, file);
    }

    public MediaFile loadMediaFileSP(String uuid) {
        return storyPathLibraryReference.loadMediaFileSPL(uuid);
    }

    public ArrayList<ClipMetadata> exportMetadata() {
        ArrayList<ClipMetadata> metadata = new ArrayList<ClipMetadata>();
        ArrayList<String> classReference = new ArrayList<String>();
        classReference.add(this.getId() + "::<<" + ClipCard.class.getName() + ">>");
        ArrayList<Card> clipCards = ReferenceHelper.getCards(this, classReference);
        for (Card c : clipCards) {
            // should be safe to cast, cards fetched based on class
            ClipCard cc = (ClipCard)c;

            if (cc.getClips() != null) {
                for (ClipMetadata cmd : cc.getClips()) {
                    if (!metadata.contains(cmd)) {
                        metadata.add(cmd);
                    }
                }
            }
        }
        return metadata;
    }

    public void importMetadata(ArrayList<ClipMetadata> metadata) {
        ArrayList<String> classReference = new ArrayList<String>();
        classReference.add(this.getId() + "::<<" + ClipCard.class.getName() + ">>");
        ArrayList<Card> clipCards = ReferenceHelper.getCards(this, classReference);
        for (Card c : clipCards) {
            // should be safe to cast, cards fetched based on class
            ClipCard cc = (ClipCard) c;

            // NEED TO REPLACE THIS WITH A BETTER ALGORITHM FOR MATCHING CLIPS TO CARDS
            if (metadata.size() > 0) {
                ClipMetadata cmd = metadata.remove(0);
                cc.addClip(cmd, false);
            }
        }
    }

    public ArrayList<FullMetadata> exportAllMetadata() {
        ArrayList<ClipMetadata> metadata = exportMetadata();
        ArrayList<FullMetadata> allMetadata = new ArrayList<FullMetadata>();
        for (ClipMetadata cm : metadata) {
            MediaFile mf = loadMediaFileSP(cm.getUuid());

            if (mf == null) {
                Log.e(this.getClass().getName(), "no media file was found for uuid " + cm.getUuid());
            } else {
                FullMetadata fm = new FullMetadata(cm, mf);
                allMetadata.add(fm);
            }
        }
        return allMetadata;
    }
}
