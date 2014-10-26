package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Observable;

import scal.io.liger.view.DisplayableCard;

/**
 * Created by mnbogner on 10/21/14.
 */
public class TipCollectionHeadlessCard extends HeadlessCard {

    private ArrayList<Tip> tips;

    public class Tip {
        private String text;
        private ArrayList<String> tags;
    }

    public ArrayList<Tip> getTipsByTags(ArrayList<String> tags) {
        return tips; // FIXME filter based on tags
    }

    public TipCollectionHeadlessCard() {
        super();
        this.type = this.getClass().getName();
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

//        Card card = (Card)observable;
//
//        // recycling stateVisibility to ensure this only triggers once
//        if (!stateVisiblity) {
//            if (checkReferencedValues()) {
//                stateVisiblity = true;
//
//                if (action.equals("LOAD")) {
//                    Log.d(this.getClass().getName(), "LOADING FILE: " + target);
//
//                    loadStoryPath(target);
//                } else {
//                    Log.e(this.getClass().getName(), "UNSUPPORTED ACTION: " + action);
//                }
//            }
//        }
    }
}
