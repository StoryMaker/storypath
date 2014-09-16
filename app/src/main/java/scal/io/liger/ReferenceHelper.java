package scal.io.liger;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import scal.io.liger.model.CardModel;
import scal.io.liger.model.StoryPathModel;

/**
 * Created by mnbogner on 9/8/14.
 */
public class ReferenceHelper {

    public static boolean checkReferences(StoryPathModel spm, String name, ArrayList<Object> references) {

        // it no longer makes sense to have multiple strings or arrays tested for true/false
        // boolean checks should be as follows:
        // 1. "name::story::card::key::value"
        // 2. "name::and/or::story::wildcard::key::wildcard"
        // 3. "name::story::card::key::value::and/or::story::wildcard::key::wildcard"
        // 4. [
        //        "name::and/or",
        //        "story::card::key::value",
        //        "and/or::story::wildcard::key::wildcard",
        //        "story::card::key::value::and/or::story::wildcard::key::wildcard"
        //    ]

        // references may be strings or arrays of strings (which may have wildcards)
        for (Object obj : references) {

            if (obj instanceof String) {
                String referenceString = (String)obj;
                if (referenceString.startsWith(name)) {
                    // trim name + "::"
                    referenceString = referenceString.substring(name.length() + 2);
                    Log.d("TESTING", "FOUND STRING: " + referenceString);

                    return doString(spm, referenceString);
                }
            }
            else if (obj instanceof ArrayList) {
                ArrayList<String> referenceArray = (ArrayList<String>)obj;
                if (referenceArray.get(0).startsWith(name)) {
                    Log.d("TESTING", "FOUND ARRAY: " + referenceArray.get(1));
                    String[] parts = referenceArray.remove(0).split("::"); // get parts and leave only references in array
                    if (parts[1].equals("and")) {
                        // if anything is false, return false
                        for (String s : referenceArray) {
                            if (!doString(spm, s)) {
                                return false;
                            }
                        }
                        return true;
                    }
                    else if (parts[1].equals("or")) {
                        // if anything is true, return true
                        for (String s : referenceArray) {
                            if (doString(spm, s)) {
                                return true;
                            }
                        }
                        return false;
                    }
                }
            }
            else {
                Log.e("TESTING", "UNEXPECTED REFERENCE TYPE");
            }
        }

        return false;
    }

    private static boolean doString(StoryPathModel spm, String referenceString) {
        if (referenceString.startsWith("and::")) {
            // single string
            return getBoolean(spm, "and", referenceString.substring(5));
        }
        else if (referenceString.startsWith("or::")) {
            // single string
            return getBoolean(spm, "or", referenceString.substring(4));
        }
        else if (referenceString.contains("::and::")) {
            // multiple strings
            ArrayList<String> strings = new ArrayList<String>(Arrays.asList(referenceString.split("::and::")));
            return andStrings(spm, strings);

        }
        else if (referenceString.contains("::or::")) {
            // multiple strings
            ArrayList<String> strings = new ArrayList<String>(Arrays.asList(referenceString.split("::or::")));
            return orStrings(spm, strings);
        }
        else {
            // regular reference
            // getBoolean will handle regular references, and/or is ignored
            return getBoolean(spm, "and", referenceString);
        }
    }

    private static boolean andStrings(StoryPathModel spm, ArrayList<String> strings) {
        // if anything is false, return false
        for (String s : strings) {
            if (!getBoolean(spm, "and", s)) {
                return false;
            }
        }

        return true;
    }

    private static boolean orStrings(StoryPathModel spm, ArrayList<String> strings) {
        // if anything is true, return true
        for (String s : strings) {
            if (getBoolean(spm, "or", s)) {
                return true;
            }
        }

        return true;
    }

    private static boolean getBoolean(StoryPathModel spm, String type, String wildcardString) {

        String[] parts = wildcardString.split("::");
        if (parts.length < 4) {
            Log.e("TESTING", "PART MISSING: " + wildcardString);
            return false;
        }
        // create strings for clarity
        String story = parts[0];
        String card = parts[1];
        String key = parts[2];
        String value = parts[3];

        ArrayList<CardModel> cards = wildcardCards(spm, card);

        if (type.equals("and")) {
            return getAnd(cards, key, value);
        }
        if (type.equals("or")) {
            return getOr(cards, key, value);
        }

        Log.e("TESTING", "UNEXPECTED TYPE: " + type);
        return false;
    }

    private static boolean getAnd(ArrayList<CardModel> cards, String key, String value) {
        boolean result = true;

        // if anything is false, return false  SWITCH ASSIGN TO RETURN

        for (CardModel cm : cards) {
            String cardValue = cm.getValueByKey(key);

            //Log.e("TESTING", "COMPUTE AND: -" + value + "- vs. -" + cardValue + "-");

            if (value.equals("*") && (cardValue == null)) {
                result = false;
                Log.e("TESTING", "FALSE: (1) -" + value + "- vs. -" + cardValue + "-");
            }
            else if (value.equals("null") && (cardValue != null)) {
                result = false;
                Log.e("TESTING", "FALSE: -" + value + "- vs. -" + cardValue + "-");
            }
            else if (!value.equals("*") && !value.equals(cardValue)) {
                result = false;
                Log.e("TESTING", "FALSE: -" + value + "- vs. -" + cardValue + "-");
            }
            else {
                Log.e("TESTING", "TRUE: -" + value + "- vs. -" + cardValue + "-");
            }
        }

        return result;
    }

    private static boolean getOr(ArrayList<CardModel> cards, String key, String value) {
        boolean result = false;

        // if anything is true, return true  SWITCH ASSIGN TO RETURN

        for (CardModel cm : cards) {
            String cardValue = cm.getValueByKey(key);

            //Log.e("TESTING", "COMPUTE OR: -" + value + "- vs. -" + cardValue + "-");

            if (value.equals("*") && (cardValue != null)) {
                result = true;
                Log.e("TESTING", "TRUE: (2) -" + value + "- vs. -" + cardValue + "-");
            }
            else if (value.equals("null") && (cardValue == null)) {
                result = true;
                Log.e("TESTING", "TRUE: -" + value + "- vs. -" + cardValue + "-");
            }
            else if (!value.equals("*") && value.equals(cardValue)) {
                result = true;
                Log.e("TESTING", "TRUE: -" + value + "- vs. -" + cardValue + "-");
            }
            else {
                Log.e("TESTING", "FALSE: -" + value + "- vs. -" + cardValue + "-");
            }
        }

        return result;
    }

    public static ArrayList<String> getReferencedValues(StoryPathModel spm, String name, ArrayList<Object> references) {

        ArrayList<String> values = new ArrayList<String>();

        ArrayList<CardModel> allCards = new ArrayList<CardModel>();

        for (Object obj : references) {

            // no reason to use arrays for non-logic references
            if (obj instanceof String) {
                String referenceString = (String) obj;
                if (referenceString.startsWith(name)) {
                    // trim name + "::"
                    referenceString = referenceString.substring(name.length() + 2);
                    Log.d("TESTING", "FOUND STRING: " + referenceString);

                    String[] parts = referenceString.split("::");
                    if (parts.length < 4) {
                        Log.e("TESTING", "PART MISSING: " + referenceString);
                        return null;
                    }
                    // create strings for clarity
                    String story = parts[0];
                    String card = parts[1];
                    String key = parts[2];
                    //String value = parts[3];

                    // this may be overkill, but duplicates will probably break logic downstream
                    ArrayList<CardModel> foundCards = wildcardCards(spm, card);
                    for (CardModel cm : foundCards) {
                        if (!allCards.contains(cm)) {

                            String value = cm.getValueByKey(key);
                            if (value != null) {
                                values.add(value);

                                Log.e("TESTING", "GOT VALUE: -" + value + "-");
                            }
                            else {
                                Log.e("TESTING", "KEY -" + key + "- NOT FOUND IN CARD: -" + cm.getId() + "-");
                            }

                            allCards.add(cm);
                        }
                    }
                }
            }
        }

        return values;
    }

    public static ArrayList<CardModel> getReferencedCards(StoryPathModel spm, String name, ArrayList<Object> references) {

        ArrayList<CardModel> allCards = new ArrayList<CardModel>();

        for (Object obj : references) {

            // no reason to use arrays for non-logic references
            if (obj instanceof String) {
                String referenceString = (String) obj;
                if (referenceString.startsWith(name)) {
                    // trim name + "::"
                    referenceString = referenceString.substring(name.length() + 2);
                    Log.d("TESTING", "FOUND STRING: " + referenceString);

                    String[] parts = referenceString.split("::");
                    if (parts.length < 4) {
                        Log.e("TESTING", "PART MISSING: " + referenceString);
                        return null;
                    }
                    // create strings for clarity
                    String story = parts[0];
                    String card = parts[1];
                    String key = parts[2];
                    String value = parts[3];

                    // this may be overkill, but duplicates will probably break logic downstream
                    ArrayList<CardModel> foundCards = wildcardCards(spm, card);
                    for (CardModel cm : foundCards) {
                        if (!allCards.contains(cm)) {
                            allCards.add(cm);

                            Log.e("TESTING", "GOT CARD: -" + cm.getId() + "-");
                        }
                    }
                }
            }
        }

        return allCards;
    }

    private static ArrayList<CardModel> wildcardCards(StoryPathModel spm, String card) {

        // TODO: deal with references to other story paths

        ArrayList<CardModel> cards = new ArrayList<CardModel>();

        if (card.equals("*")) {
            cards = spm.getCards();
        }
        else if (card.startsWith("<<")) {
            // strip "<<" and ">>"
            card = card.substring(2, card.length()-2);
            Log.d("TESTING", "CHECK: " + card);
            for (CardModel cm : spm.getCards()) {
                if (cm.getType().equals(card)) {
                    cards.add(cm);
                }
            }
        }
        else {
            for (CardModel cm : spm.getCards()) {
                if (cm.getId().equals(card)) {
                    cards.add(cm);
                }
            }
        }

        return cards;
    }
}
