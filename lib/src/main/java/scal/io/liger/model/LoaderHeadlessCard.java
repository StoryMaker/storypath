package scal.io.liger.model;

import timber.log.Timber;

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
                    Timber.d("LOADING FILE: " + target);

                    loadStoryPath(target);
                } else {
                    Timber.e("UNSUPPORTED ACTION: " + action);
                }
            }
        }
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof LoaderHeadlessCard)) {
            Timber.e("CARD " + card.getId() + " IS NOT AN INSTANCE OF LoaderHeadlessCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Timber.e("CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        LoaderHeadlessCard castCard = (LoaderHeadlessCard)card;

        this.title = castCard.getTitle();
    }
}
