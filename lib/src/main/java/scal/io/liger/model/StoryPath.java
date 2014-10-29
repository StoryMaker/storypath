package scal.io.liger.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.ReferenceHelper;
import scal.io.liger.StoryPathDeserializer;

/**
 * An ordered collection of cards de-serialized from JSON
 *
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class StoryPath {
    public static final String TAG = "StoryPath";

    /** An identifier unique to this Story Path */
    @Expose protected String id;
    @Expose protected String title;
    @Expose protected String classPackage;
    @Expose ArrayList<Card> cards;
    @Expose protected ArrayList<Dependency> dependencies;
    @Expose protected String fileLocation;
    protected StoryPathLibrary storyPathLibrary; // not serialized
    @Expose protected String storyPathLibraryFile;

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
        // add it to the adapter
    }

    public void activateCard(Card card) {
//        if (this.visibleCards == null)
//            this.visibleCards = new ArrayList<Card>();

//        this.visibleCards.add(card);
        // add it to the adapter
        ((MainActivity) context).activateCard(card); // FIXME unsafe cast
    }

    public void inactivateCard(Card card) {
//        this.visibleCards.remove(card);
        // remove it from the adapter
        ((MainActivity) context).inactivateCard(card); // FIXME unsafe cast
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

    /**
     * Get all the cards that should be displayed to the user for this StoryPath
     *
     * Note this method only needs to be called once. Updates to this list can be received
     * by calling {@link StoryPathLibrary#setStoryPathLibraryListener(scal.io.liger.model.StoryPathLibrary.StoryPathLibraryListener)}
     */
    public ArrayList<Card> getValidCards() {
        /*
        if (visibleCards == null) {
            visibleCards = new ArrayList<Card>();

            for (Card card : cards) {
                if (card.checkVisibilityChanged()) {
                    visibleCards.add(card);
                }
            }
        }

        return visibleCards;
        */


        ArrayList<Card> validCards = new ArrayList<Card>();

        for (Card card : cards) {
            if (card.checkReferencedValues()) {
                if (card instanceof HeadlessCard) {
                    Log.d(this.getClass().getName(), "headless card " + card.getId() + " should not return " + card.checkReferencedValues() + " for checkReferencedValues()");
                } else {
                    validCards.add(card);
                }
            }
        }

        return validCards;

    }

    // required for serialization/deserialization
    /*
    public void setValidCards(ArrayList<Card> validCards) {
        this.visibleCards = visibleCards;
    }
    */

    /**
     * @return a Collection of Dependencies describing other StoryPath files this instance references
     */
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

    public StoryPathLibrary getStoryPathLibrary() {
        return storyPathLibrary;
    }

    public void setStoryPathLibrary(StoryPathLibrary storyPathLibrary) {
        this.storyPathLibrary = storyPathLibrary;
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

    /**
     * Set a reference to this story path in each card.
     * Must be done before cards attempt to reference
     * values from previous story paths or cards
     */
    public void setCardReferences() {
        for (Card card : cards) {
            card.setStoryPath(this);
        }
    }

    /**
     * Clear references to this story path from each card.
     * must be done before serializing this story path to
     * prevent duplication or circular references
     *
     * TODO Can this docstring be revised now that we have explicit de/serialization with @Expose?
     */
    public void clearCardReferences() {
        for (Card card : cards) {
            card.setStoryPath(null);
        }
    }

    /**
     * Observers must be initialized after cards are deserialized.
     * Observers must be cleared before serializing to prevent TODO Is this still necessary w/ @Expose?
     * circular references (cards pointing to cards)
     */
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

    /** Return the value corresponding to the fully qualified Id or null if it could not be found.
     * e.g: "default_library::quiz_card_topic::choice"
    */
    public String getReferencedValue(String fullPath) {
        // assumes the format story::card::field::value
        String[] pathParts = fullPath.split("::");

        if (!this.getId().equals(pathParts[0])) {
            return Constants.EXTERNAL; // FIXME we should probably use an exception instead of overloading the return value
        }

        Card card = this.getCardById(fullPath);

        if (card == null) {
            return null;
        } else {
            String value = card.getValueById(fullPath);

            return value;
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

    /**
     * This method tracks changes to cards within this StoryPath
     *
     * A client wishing to maintain a view controller
     * for this StoryPath need only call {@link #getValidCards()} once. This method is responsible
     * for propagating events affecting the value of {@link #getValidCards()} to the listener set in
     * {@link scal.io.liger.model.StoryPathLibrary#setStoryPathLibraryListener(scal.io.liger.model.StoryPathLibrary.StoryPathLibraryListener)}
     */
    public void notifyCardChanged(@NonNull Card firstCard) {
        Log.i(TAG, "notifyCardChanged of update to card " + firstCard.getId());
        if (storyPathLibrary == null || storyPathLibrary.mListener == null) {
            return;
        }

        String action = ((MainActivity)context).checkCard(firstCard);

        if (action.equals("ADD")) {
            storyPathLibrary.mListener.onCardAdded(firstCard);
        }
        if (action.equals("UPDATE")) {
            storyPathLibrary.mListener.onCardChanged(firstCard);
        }
        if (action.equals("DELETE")) {
            storyPathLibrary.mListener.onCardRemoved(firstCard);
        }
    }

    /**
     * This method tracks swapping events between two cards
     * currently in this StoryPath
     */
    public void notifyCardsSwapped(Card cardOne, Card cardTwo) {
        if (storyPathLibrary == null || storyPathLibrary.mListener == null) {
            return;
        }
        storyPathLibrary.mListener.onCardsSwapped(cardOne, cardTwo);
    }

    /*
    public void notifyCardChanged(Card updatedCard) {
        Log.d("StoryPathModel", "notifyCardChanged");

        if (updatedCard.getStateVisiblity()) {
            // new or updated

            int cardIndex = 0;

            if (visibleCards.contains(updatedCard)) {
                visibleCards.remove(updatedCard);
                inactivateCard(updatedCard);
            } else {
                // foo
            }

            cardIndex = findSpot(updatedCard);

            if (cardIndex >= visibleCards.size()) {
                visibleCards.add(updatedCard);
                activateCard(updatedCard);
            } else {
                visibleCards.add(cardIndex, updatedCard);
            }
        } else {
            // deleted

            if (visibleCards.contains(updatedCard)) {
                visibleCards.remove(updatedCard);
                inactivateCard(updatedCard);
            } else {
                // foo
            }
        }

        // call refresh for now, card list will be returned

//        if (context != null) {
//            MainActivity mainActivity = (MainActivity) context; // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())
//            mainActivity.refreshCardView();
//        } else {
//            System.err.println("APP CONTEXT REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
//        }
    }
    */

    /*
    public int findSpot(Card card) {
        int baseIndex = cards.indexOf(card);
        Log.d(" *** FINDSPOT *** ", "CARD " + card.getId() + " - DEFAULT POSITION " + baseIndex);
        int newIndex = 0;
        for (int i = (baseIndex - 1); i >= 0; i--) {
            Card previousCard = cards.get(i);
            Log.d(" *** FINDSPOT *** ", "LOOKING FOR " + previousCard.getId());
            if (visibleCards.contains(previousCard)) {
                newIndex = visibleCards.indexOf(previousCard) + 1;
                Log.d(" *** FINDSPOT *** ", "CARD " + previousCard.getId() + " FOUND AT POSITION " + visibleCards.indexOf(previousCard));

                break;
            }
        }

        Log.d(" *** FINDSPOT *** ", "INSERTING " + card.getId() + " AT POSITION " + newIndex);

        return newIndex;
    }
    */

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

        // Log.d(" *** REARRANGE *** ", "MOVED " + card.getId() + " FROM " + currentIndex + " TO " + newIndex);

        notifyCardChanged(card);
        // We should also notify the other affected card, right?
        // e.g: The one that was at newIndex when the op started?
    }

    /**
     * Swap the cards at the given indexes.
     * This is like {@link #rearrangeCards(int, int)} but also supports
     * operations where cards aren't moving to adjacent positions
     */
    public void swapCards(int firstIndex, int secondIndex) {
        Card tempCard = cards.get(firstIndex);
        cards.set(firstIndex, cards.get(secondIndex));
        cards.set(secondIndex, tempCard);

        notifyCardsSwapped(tempCard, cards.get(firstIndex));
    }

    public void saveMediaFileSP(String uuid, MediaFile file) {
        storyPathLibrary.saveMediaFileSPL(uuid, file);
    }

    public MediaFile loadMediaFileSP(String uuid) {
        return storyPathLibrary.loadMediaFileSPL(uuid);
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
