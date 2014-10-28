package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Observable;

import scal.io.liger.view.DisplayableCard;

/**
 * Created by mnbogner on 10/21/14.
 */
public class TipCollectionHeadlessCard extends HeadlessCard {

    @Expose private ArrayList<Tip> tips; // NOT SURE THIS WILL WORK WITH INNER CLASS...

    public TipCollectionHeadlessCard(ArrayList<Tip> tips) {
        super();
        this.type = this.getClass().getName();
        this.tips = tips;
    }

    // FIXME opt: this can probably be more efficient
    public ArrayList<Tip> getTipsByTags(ArrayList<String> tags) {
        ArrayList<Tip> matchingTips = new ArrayList<Tip>();
        for (Tip tip: tips) {
            boolean match = false;
            for (String tipTag: tip.tags) {
                for (String tag: tags) {
                    if (tag.toLowerCase().equals(tipTag.toLowerCase())) {
                        matchingTips.add(tip);
                        match = true;
                        break;
                    }
                }
                if (match) break;
            }
        }
        return matchingTips;
    }

    public ArrayList<String> getTipsTextByTags(ArrayList<String> tags) {
        ArrayList<Tip> _tips = getTipsByTags(tags);
        ArrayList<String> texts = null;
        if (_tips.size() > 0) {
            texts = new ArrayList<String>();
            for (Tip tip : _tips) {
                texts.add(tip.text);
            }
        }
        return texts;
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
