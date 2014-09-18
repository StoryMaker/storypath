package scal.io.liger;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import scal.io.liger.model.CardModel;
import scal.io.liger.model.StoryPathModel;

/**
 * Created by mnbogner on 9/8/14.
 */
public class ReferenceHelper {

    public static boolean getBoolean(StoryPathModel story, ArrayList<String> references) {
        return false;
    }

    public static ArrayList<CardModel> getCards(StoryPathModel story, ArrayList<String> references) {
        ArrayList<CardModel> results = new ArrayList<CardModel>();

        ArrayList<CardModel> unfilteredCards = null;
        ArrayList<CardModel> filteredCards = null;

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
            for (CardModel card : filteredCards) {
                if (!results.contains(card)) {
                    results.add(card);
                }
            }
        }

        return results;
    }

    public static ArrayList<String> getValues(StoryPathModel story, ArrayList<String> references) {
        HashMap<String, String> resultMap = new HashMap<String, String>();

        ArrayList<CardModel> unfilteredCards = null;
        ArrayList<CardModel> filteredCards = null;

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
                for (CardModel card : filteredCards) {
                    resultMap.put(card.getId(), card.getValueByKey(parts[2]));
                }
            }
        }

        ArrayList<String> results = new ArrayList<String>(resultMap.values());
        return results;
    }

    private static ArrayList<CardModel> gatherCards(StoryPathModel story, String cardTarget) {
        ArrayList<CardModel> results = new ArrayList<CardModel>();

        if (cardTarget.equals("*")) {
            results = story.getCards();
        } else if (cardTarget.startsWith("<<")) {
            // strip "<<" and ">>"
            cardTarget = cardTarget.substring(2, cardTarget.length()-2);
            for (CardModel card : story.getCards()) {
                if (card.getType().equals(cardTarget)) {
                    results.add(card);
                }
            }
        } else {
            for (CardModel card : story.getCards()) {
                if (card.getId().equals(cardTarget)) {
                    results.add(card);
                }
            }
        }

        return results;
    }

    private static ArrayList<CardModel> filterCards(ArrayList<CardModel> cards, String keyTarget, String valueTarget) {
        ArrayList<CardModel> results = new ArrayList<CardModel>();

        for (CardModel card : cards) {
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
    private static ArrayList<String> gatherValues(ArrayList<CardModel> cards, String keyTarget, String valueTarget) {
        ArrayList<String> results = new ArrayList<String>();

        return results;
    }
}
