package scal.io.liger;

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

    public static ArrayList<Card> filterCards(ArrayList<Card> cards, String keyTarget, String valueTarget) {
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
    public static ArrayList<String> gatherValues(ArrayList<Card> cards, String keyTarget, String valueTarget) {
        ArrayList<String> results = new ArrayList<String>();

        return results;
    }
}
