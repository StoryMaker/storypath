package scal.io.liger.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Observable;

/**
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class HookLoaderHeadlessCard extends HeadlessCard {
    private String action;
    private String target;

    public HookLoaderHeadlessCard() {
        super();
        this.type = this.getClass().getName();
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
            Log.e(this.getClass().getName(), "update notification received from non-card observable");
            return;
        }
        if (storyPathReference == null) {
            Log.e(this.getClass().getName(), "STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
            return;
        }

        Card card = (Card)observable;

        // recycling stateVisibility to ensure this only triggers once
        if (!stateVisiblity) {
            if (checkReferencedValues()) {
                stateVisiblity = true;

                if (action.equals("LOAD")) {
                    Log.d(this.getClass().getName(), "LOADING FILE: " + target);
//                    ArrayList<String> refs = getReferences();

                    String topic = getStoryPathReference().getReferencedValue("default_library::quiz_card_topic::choice");
                    String format = getStoryPathReference().getReferencedValue("default_library::quiz_card_format::choice");
                    String medium = getStoryPathReference().getReferencedValue("default_library::quiz_card_medium::choice");
                    String clipType = getStoryPathReference().getReferencedValue("default_library::quiz_card_cliptype::choice");
                    String clipQuestion = getStoryPathReference().getReferencedValue("default_library::quiz_card_clipquestion::choice");

                    target = topic + "_" + format + "_" + medium;
                    if (clipType != null && !clipType.equals("")) {
                        target += "_" + clipType;
                    }
                    if (clipQuestion != null && !clipQuestion.equals("")) {
                        target += "_" + clipQuestion;
                    }

                    loadStoryPath(target);
                } else {
                    Log.e(this.getClass().getName(), "UNSUPPORTED ACTION: " + action);
                }
            }
        }
    }

    @Override
    public boolean checkReferencedValues() {
        // need to accomodate and/or logic
        boolean result = true;

        // already hard coded in update(), so hard coding them here for simplicity
        if ((!checkReferencedValueMatches("default_library::quiz_card_topic::choice")) ||
            (!checkReferencedValueMatches("default_library::quiz_card_format::choice")) ||
            (!checkReferencedValueMatches("default_library::quiz_card_medium::choice"))) {
            result = false;
        }

        // cards 4 and 5 appear under specific conditions
        // if the conditions are met but the cards have no values, the check fails
        String format = getStoryPathReference().getReferencedValue("default_library::quiz_card_format::choice");
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
}
