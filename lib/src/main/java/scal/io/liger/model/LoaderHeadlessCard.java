package scal.io.liger.model;

import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.Observable;

/**
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class LoaderHeadlessCard extends HeadlessCard {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String action;
    @Expose private String target;

    public LoaderHeadlessCard() {
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
            Log.e(this.getClass().getName(), "update notification received from non-card observable");
            return;
        }
        if (storyPath == null) {
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

                    loadStoryPath(target);
                } else {
                    Log.e(this.getClass().getName(), "UNSUPPORTED ACTION: " + action);
                }
            }
        }
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof LoaderHeadlessCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF LoaderHeadlessCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        LoaderHeadlessCard castCard = (LoaderHeadlessCard)card;

        this.title = castCard.getTitle();
    }
}
