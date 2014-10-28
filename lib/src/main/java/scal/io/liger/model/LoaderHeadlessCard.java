package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.Observable;

import scal.io.liger.view.DisplayableCard;

/**
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class LoaderHeadlessCard extends HeadlessCard {
    @Expose private String action;
    @Expose private String target;

    public LoaderHeadlessCard() {
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

                    loadStoryPath(target);
                } else {
                    Log.e(this.getClass().getName(), "UNSUPPORTED ACTION: " + action);
                }
            }
        }
    }
}
