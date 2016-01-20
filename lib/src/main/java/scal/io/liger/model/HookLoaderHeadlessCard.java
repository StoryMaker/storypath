package scal.io.liger.model;

import timber.log.Timber;

import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Observable;

/**
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class HookLoaderHeadlessCard extends HeadlessCard {

    /*
     * FIXME: IMPORTANT! 
     *
     * in its current form this class will support any quiz where the intended outcome
     * is to select a story path file from storyPathTemplateFiles with a key of format
     *   <referenced choice a>_<referenced choice b>_<referenced choice c>_etc
     * there are hard-coded exceptions to handle the default library, which constructs
     * keys differently depending on certain referenced choices.
     *
     */


    public final String TAG = this.getClass().getSimpleName();

    @Expose private String action;
    @Expose private String target;

    public HookLoaderHeadlessCard() {
        super();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public void update(Observable observable, Object o) {
        if (!(observable instanceof Card)) {
            Timber.e("update notification received from non-card observable");
            return;
        }
        if (storyPath == null) {
            Timber.e("STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
            return;
        }

        Card card = (Card)observable;

        // recycling stateVisibility to ensure this only triggers once
        if (!stateVisiblity) {
            if (checkReferencedValues()) {
                stateVisiblity = true;

                if (action.equals("LOAD")) {

                    // trying something new...
                    ArrayList<String> refs = getReferences();

                    target = "";

                    for (String ref : refs) {

                        // normally we won't end up here with nulls due to the initial check
                        // this is intended to accomodate the default library
                        String targetPart = getStoryPath().getReferencedValue(ref);
                        if (targetPart == null) {
                            Timber.d("HOOK - NOTHING TO ADD FOR " + ref);
                        } else {
                            Timber.d("HOOK - ADDING CHOICE: " + targetPart);

                            if (target.length() > 0) {
                                target = target + "_";
                            }

                            target = target + targetPart;
                        }
                    }

                    /*
                    String topic = getStoryPath().getReferencedValue("default_library::quiz_card_topic::choice");
                    String format = getStoryPath().getReferencedValue("default_library::quiz_card_format::choice");
                    String medium = getStoryPath().getReferencedValue("default_library::quiz_card_medium::choice");
                    String clipType = getStoryPath().getReferencedValue("default_library::quiz_card_cliptype::choice");
                    String clipQuestion = getStoryPath().getReferencedValue("default_library::quiz_card_clipquestion::choice");

                    target = topic + "_" + format + "_" + medium;
                    if (clipType != null && !clipType.equals("")) {
                        target += "_" + clipType;
                    }
                    if (clipQuestion != null && !clipQuestion.equals("")) {
                        target += "_" + clipQuestion;
                    }
                    */

                    Timber.d("LOADING FILE: " + target);

                    loadStoryPath(target);
                } else {
                    Timber.e("UNSUPPORTED ACTION: " + action);
                }
            }
        }
    }

    @Override
    public boolean checkReferencedValues() {
        // need to accomodate and/or logic
        boolean result = true;

        // trying something new...
        ArrayList<String> refs = getReferences();

        for (String ref : refs) {
            // these are handled below, they must be omitted here
            if (ref.equals("default_library::quiz_card_cliptype::choice")) {
                Timber.d("HOOK - SKIPPING CHECK FOR " + ref);
            } else if (ref.equals("default_library::quiz_card_clipquestion::choice")) {
                Timber.d("HOOK - SKIPPING CHECK FOR " + ref);
            } else {
                if (!checkReferencedValueMatches(ref)) {
                    Timber.d("HOOK - " + ref + " HAS NO VALUE");
                    result = false;
                } else {
                    Timber.d("HOOK - " + ref + " HAS A VALUE");
                }
            }
        }

        // already hard coded in update(), so hard coding them here for simplicity
        /*
        if ((!checkReferencedValueMatches("default_library::quiz_card_topic::choice")) ||
            (!checkReferencedValueMatches("default_library::quiz_card_format::choice")) ||
            (!checkReferencedValueMatches("default_library::quiz_card_medium::choice"))) {
            result = false;
        }
        */

        // i think it's safe to leave this here
        // for a library with no "format" option it will just be ignored

        // cards 4 and 5 appear under specific conditions
        // if the conditions are met but the cards have no values, the check fails
        String format = getStoryPath().getReferencedValue("default_library::quiz_card_format::choice");
        if (format != null) {
            if (format.equals("series") && !checkReferencedValueMatches("default_library::quiz_card_cliptype::choice")) {
                result = false;
            }
            if (format.equals("discussion") && !checkReferencedValueMatches("default_library::quiz_card_clipquestion::choice")) {
                result = false;
            }
        }

        return result;
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof HookLoaderHeadlessCard)) {
            Timber.e("CARD " + card.getId() + " IS NOT AN INSTANCE OF HookLoaderHeadlessCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Timber.e("CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        HookLoaderHeadlessCard castCard = (HookLoaderHeadlessCard)card;

        this.title = castCard.getTitle();
    }
}
