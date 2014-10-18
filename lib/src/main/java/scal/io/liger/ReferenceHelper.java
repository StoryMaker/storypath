package scal.io.liger;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import scal.io.liger.model.Card;
import scal.io.liger.model.StoryPath;

/**
 * Created by mnbogner on 9/8/14.
 */
public class ReferenceHelper {

    public static boolean getBoolean(StoryPath story, ArrayList<String> references) {
        return false;
    }

    public static ArrayList<Card> getCards(StoryPath story, ArrayList<String> references) {
        ArrayList<Card> results = new ArrayList<Card>();

        ArrayList<Card> unfilteredCards = null;
        ArrayList<Card> filteredCards = null;

        for (String reference : references) {
            String[] parts = reference.split("::");

            // TODO: check parts[0] and load external story path if necessary

            unfilteredCards = gatherCards(story, parts[1]);

            // check for key/value parts
            if (parts.length == 4) {
                filteredCards = filterCards(unfilteredCards, parts[2], parts[3]);
            } else {
                filteredCards = unfilteredCards;
            }

            // check for duplicates
            for (Card card : filteredCards) {
                if (!results.contains(card)) {
                    results.add(card);
                }
            }
        }

        return results;
    }

    public static ArrayList<String> getValues(StoryPath story, ArrayList<String> references) {
        HashMap<String, String> resultMap = new HashMap<String, String>();

        ArrayList<Card> unfilteredCards = null;
        ArrayList<Card> filteredCards = null;

        for (String reference : references) {
            String[] parts = reference.split("::");

            // TODO: check parts[0] and load external story path if necessary

            unfilteredCards = gatherCards(story, parts[1]);

            // check for key/value parts
            if (parts.length == 4) {
                filteredCards = filterCards(unfilteredCards, parts[2], parts[3]);
            } else {
                filteredCards = unfilteredCards;
            }

            // check for key part
            if (parts.length > 2) {
                // check for duplicates
                for (Card card : filteredCards) {
                    resultMap.put(card.getId(), card.getValueByKey(parts[2]));
                }
            }
        }

        ArrayList<String> results = new ArrayList<String>(resultMap.values());
        return results;
    }

    private static ArrayList<Card> gatherCards(StoryPath story, String cardTarget) {
        ArrayList<Card> results = new ArrayList<Card>();

        if (cardTarget.equals("*")) {
            results = story.getCards();
        } else if (cardTarget.startsWith("<<")) {
            // strip "<<" and ">>"
            cardTarget = cardTarget.substring(2, cardTarget.length()-2);
            for (Card card : story.getCards()) {
                // need to account for separation of package and class name
                // or should class "wildcards" for cards ignore package?
                if ((card.getStoryPathReference().getClassPackage() + "." + card.getType()).equals(cardTarget) ||
                    (card.getType().equals(cardTarget))) {
                    results.add(card);
                }
            }
        } else {
            for (Card card : story.getCards()) {
                if (card.getId().equals(cardTarget)) {
                    results.add(card);
                }
            }
        }

        return results;
    }

    private static ArrayList<Card> filterCards(ArrayList<Card> cards, String keyTarget, String valueTarget) {
        ArrayList<Card> results = new ArrayList<Card>();

        for (Card card : cards) {
            if (valueTarget.equals("*")) {
                if (card.getValueByKey(keyTarget) != null) {
                    results.add(card);
                }
            } else {
                if ((card.getValueByKey(keyTarget) != null) && (card.getValueByKey(keyTarget).equals(valueTarget))) {
                    results.add(card);
                }
            }
        }

        return results;
    }

    // uncertain of use case, values must be extracted at the point in the code where a specific
    // reference is available, so found cards cannot be aggregated and checked for values
    private static ArrayList<String> gatherValues(ArrayList<Card> cards, String keyTarget, String valueTarget) {
        ArrayList<String> results = new ArrayList<String>();

        return results;
    }
}
