package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scal.io.liger.Constants;
import scal.io.liger.ReferenceHelper;
import scal.io.liger.view.DisplayableCard;

/**
 * Base model for a Card.
 *
 * All subclasses of that wish to implement Cloneable must
 * include a no-arg constructor. See {@link #clone()}
 *
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public abstract class Card extends Observable implements Observer, Cloneable {  // REFACTOR TO AVOID CONFLICT w/ UI CARD CLASS

    public final String TAG = this.getClass().getSimpleName();

    @Expose protected String type;
    @Expose private String id;
    @Expose protected String title;
    protected StoryPath storyPath; // not serialized
    @Expose protected ArrayList<String> references;
    @Expose private HashMap<String, String> values;

    public String toString() {
        return "card: " + id;
    }

    // NEW
    @Expose protected boolean stateVisiblity = false; // SHOULD THIS BE SERIALIZED?

    // NEW
    public boolean getStateVisiblity() {
        return stateVisiblity;
    }

    // necessary for setting loaded story paths to a clean state
    public void resetStateVisibility() {
        stateVisiblity = false;
    }

    // NEW
    // NOTE: may be sensible to revise this so that it checks references against the card received
    //       by the update method instead of fetching values from the entire story path
    @Override
    public void update(Observable observable, Object o) {
        if (!(observable instanceof Card)) {
            Log.e(TAG, "update notification received from non-card observable");
            return;
        }
        if (storyPath == null) {
            Log.e(TAG, "STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
            return;
        }

        Card card = (Card)observable;

        //changeCardVisibilityState();
        if (checkVisibilityChanged()) {
            storyPath.notifyCardChanged(this);
        }
    }

    public void changeCardVisibilityState() {
        if (stateVisiblity) {
            if (checkReferencedValues() != stateVisiblity) {
                // therefore, we went from visible to invisible, remove
                //getStoryPath().inactivateCard(this);
            }
        } else {
            if (checkReferencedValues() != stateVisiblity) {
                // therefore, we went from invisible to visible, add it
                //getStoryPath().activateCard(this);
            }
        }
    }

    // NEW
    public boolean checkVisibilityChanged() {
        // TODO return visibility state
        boolean newVisibility = checkReferencedValues(); // FIXME "references" is just "visibility" right?
        if (stateVisiblity != newVisibility) {
            stateVisiblity = newVisibility;
            return true;
        } else {
            return false;
        }
    }

    // foo

    public void registerObservers() {
        if (references != null) {
            for (String reference : references) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.addObserver(this);
                }
            }
        }
    }

    public void removeObservers() {
        if (references != null) {
            for (String reference : references) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.deleteObserver(this);
                }
            }
        }
    }

    public Card() {
        // required for JSON/GSON
    }

    /**
     * @return a DisplayableCard responsible for generating a view for this Card's state.
     */
    public abstract DisplayableCard getDisplayableCard(Context context);

    /**
     * @return the type of this card, generally the value returned by {@link Class#getSimpleName()}
     *         e.g: "ReviewCard"
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type of this card, generally the value returned by {@link Class#getSimpleName()}
     * e.g: "ReviewCard"
     * TODO What is the use case?
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return an identifier for this Card unique to its {@link scal.io.liger.model.StoryPath}
     * see {@link #getStoryPath()}
     */
    public String getId() {
        return id;
    }

    /**
     * Set an identifier to uniquely identify this Card in its {@link scal.io.liger.model.StoryPath}
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() { return title;}

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the {@link scal.io.liger.model.StoryPath} this card belongs to. The value returned
     * by {@link #getId()} is unique to this StoryPath.
     */
    public StoryPath getStoryPath() {
        return storyPath;
    }

    public void setStoryPath(StoryPath storyPath) {
        this.storyPath = storyPath;
    }

    /**
     * @return A Collection of fully qualified Card Ids which this Card references
     * e.g { "default_library::quiz_card_topic::choice", ... }
     */
    public ArrayList<String> getReferences() {
        return references;
    }

    public void setReferences(ArrayList<String> references) {
        this.references = references;
    }

    public void addReference(String reference) {
        if (this.references == null)
            this.references = new ArrayList<String>();

        this.references.add(reference);
    }

    // FIXME we should extend this to allow 1 and 2 part ids for local checks only (key::value or key)

    /**
     * Determine if a non-null value exists in our StoryPath for the specified id
     *
     * @param reference a fully-qualified Id. e.g: "default_library::quiz_card_topic::choice"
     * @return Whether a Card in this card's StoryPath has a non-null value for the specified key
     */
    public boolean checkReferencedValueMatches(String reference) {
        boolean result = true;

        String[] pathParts = reference.split("::");
        String referencedValue = storyPath.getReferencedValue(reference);

        if ((referencedValue != null) && (referencedValue.equals(Constants.EXTERNAL))) {
            referencedValue = storyPath.getExternalReferencedValue(reference);
        }

        if (pathParts.length == 3) {
            // just check that the value is not null
            if (referencedValue == null) { // FIXME this cold be simplified
                result = false;
            }
        } else { // if length == 4, check its not null and it matches
            if ((referencedValue == null) || (!referencedValue.equals(pathParts[3]))) {
                result = false;
            }
        }

        return result;
    }

    public boolean checkReferencedValues() {
        boolean result = true;

        if (references != null) {
            for (String reference : references) {
                // assumes the format story::card::field::value
                if (!checkReferencedValueMatches(reference)) {
                    result = false;
                }
            }
        }

        return result;
    }

    public void clearValues() {
        values = null;
    }

    public void addValue(String key, String value) {
        addValue(key, value, true);
    }

    public void addValue(String key, String value, boolean notify) {
        if (this.values == null)
            this.values = new HashMap<String, String>();

        this.values.put(key, value);

        if (notify) {
            setChanged();
            notifyObservers();

            /*
            // send notification that a value has been saved so that cards can re-check references
            if (storyPath != null) {
                storyPath.notifyCardChanged();
            } else {
                System.err.println("STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
            }
            */
        }
    }

    public boolean isKey(String key) {
        if (values != null) {
            if (values.keySet().contains(key)) {
                return true;
            }
        }

        return false;
    }

    public boolean isField(String key) {
        Class c = null;
        Field f = null;
        try {
            c = this.getClass();
            f = c.getField(key);
            return true;
        } catch (Exception e) {
            // field not found
        }

        return false;
    }
    /**
     * Get the value stored by this Card for a bare Id. e.g: "choice".
     * Uses reflection to search class field if key not present in values
    */
    public String getValueByKey(String key) {
        if (values != null) {
            if (values.keySet().contains(key)) {
                return values.get(key);
            }
        }

        // check class properties if no saved value was found
        Class c = null;
        Field f = null;
        try {
            c = this.getClass();
            f = c.getDeclaredField(key);
            f.setAccessible(true);
            return f.get(this).toString(); // not the best solution, but somehow int fields come back with Integer values
                                           // NEEDS REVISION TO HANDLE NON-STRING FIELDS (IE: CLIPS)
        } catch (Exception e) {
            //Log.d("TESTING", "EXCEPTION THROWN WHILE SEARCHING CLASS PROPERTIES FOR VALUE: " + e.getMessage());
        }

        //Log.e("TESTING", "VALUE " + key + " WAS NOT FOUND");
        return null;
    }

    /**
     * Get this Card's value for a key described by fullPath. Return null if no entry for key exists.
     *
     * @param fullPath a fully qualified value key. e.g: "default_library::quiz_card_topic::choice"
     * @return this Card's value for the key contained in fullPath
     */
    public String getValueById (String fullPath) {
        // assumes the format story::card::field
        String[] pathParts = fullPath.split("::");
        //FIXME this should also work for length 3!

         if (pathParts.length == 4 || pathParts.length == 3) {
            // sanity check
            if (!this.id.equals(pathParts[1])) {
                System.err.println("CARD ID " + pathParts[1] + " DOES NOT MATCH");
                return null;
            }

            return getValueByKey(pathParts[2]);
        }

        return null;
    }

    /**
     * Get a Collection of Cards in this card's StoryPath by fully qualified path
     *
     * TODO Why isn't this a method on StoryPath?
     *
     * @param fullPath e.g: "thispath::<<scal.io.liger.model.TipCollectionHeadlessCard>>"
     * @return a collection of cards matching fullPath within this cards StoryPath
     */
    public ArrayList<Card> getCardsByClass (String fullPath) {
        // helper method takes a collection of references
        StoryPath storyPath = getStoryPath();
        ArrayList<String> pathArray = new ArrayList<>();
        pathArray.add(fullPath);

        ArrayList<Card> matchingCards = new ArrayList<>();
        matchingCards.addAll(storyPath.getCards(pathArray));

        // check for attached story path/story path library
        if (storyPath.getStoryPathLibrary() != null) {
            String libraryPath = storyPath.getStoryPathLibrary().getId() + fullPath.substring(fullPath.indexOf(":"));
            pathArray.clear();
            pathArray.add(libraryPath);
            Log.d("CARDS", "LIBRARY REFERENCE: " + libraryPath);
            matchingCards.addAll(storyPath.getStoryPathLibrary().getCards(pathArray));
        }
        if (storyPath instanceof StoryPathLibrary) {
            StoryPathLibrary storyPathLibrary = ((StoryPathLibrary)storyPath);
            if (storyPathLibrary.getCurrentStoryPath() != null) {
                String path = storyPathLibrary.getCurrentStoryPath().getId() + fullPath.substring(fullPath.indexOf(":"));
                pathArray.clear();
                pathArray.add(path);
                Log.d("CARDS", "PATH REFERENCE: " + path);
                matchingCards.addAll(storyPathLibrary.getCurrentStoryPath().getCards(pathArray));
            }
        }

        return matchingCards;
    }

    /**
     * Replaces fully qualified Ids in originalString with their values,
     * using an empty string if no value available.
     *
     * @param originalString a string which may contain embedded fully-qualified Ids:
     *                       e.g: "Your choice of {{default_library::quiz_card_topic::choice}} is a great one!"
     * @return originalString with ids replaced for values. e.g "Your choice of cake is a great one!"
     */
    public String fillReferences(String originalString) { // <- need to integrate with observer/update process
        if (originalString == null) {
            return originalString;
        }

        String newString = originalString;

        Pattern p = Pattern.compile("\\{\\{(.+?)\\}\\}");
        Matcher m = p.matcher(newString);

        while (m.find()) {
            String referenceString = m.group(1);
            if (referenceString != null) {
                referenceString = referenceString.trim();
                String referenceValue = storyPath.getReferencedValue(referenceString);

                if ((referenceValue != null) && (referenceValue.equals(Constants.EXTERNAL))) {
                    referenceValue = storyPath.getExternalReferencedValue(referenceString);
                }

                // doing a replace with a null seems to cause issues
                if (referenceValue == null)
                    referenceValue = "";
                newString = m.replaceFirst(referenceValue);
                m = p.matcher(newString);
            }
        }

        return newString;
    }

    public void loadStoryPath(String storyPathTemplateKey) {
        Log.d(TAG, "loading " + storyPathTemplateKey);
        if (storyPath instanceof StoryPathLibrary) {
            ((StoryPathLibrary) storyPath).loadStoryPathTemplate(storyPathTemplateKey);
        } else {
            Log.e(TAG, "cannot initiate a story path load from a story path card (use a link card)");
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            // Card is abstract so this method will only ever be called from a concrete
            // subclass. We use reflection to construct an instance of the child class.
            // NOTE : This requires all children of Card to have a no-arg constructor
            Card clone = getClass().getDeclaredConstructor().newInstance();
            // TODO : How to assign a new unique id for this card?
            clone.id = this.id; // Strings are immutable
            clone.title = this.title;
            clone.storyPath = this.storyPath; // do not copy
            if (this.references != null) clone.references = (ArrayList<String>) this.references.clone();
            if (this.values != null) clone.values = (HashMap<String, String>) this.values.clone();
            return clone;
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException |
                SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Log.e("Card#clone", "Failed to clone Card");
            e.printStackTrace();
        }
        return null;
    }

    // need a method to import text strings from a matching card to support translation/versioning
    public void copyText(Card card) {
        Log.e(TAG, "copyText() METHOD SHOULD NOT BE CALLED FROM THE BASE CLASS");
    }
}
