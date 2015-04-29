package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import java.util.Observable;

import scal.io.liger.view.ClipCardView;
import scal.io.liger.view.DisplayableCard;

/**
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public abstract class HeadlessCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    public HeadlessCard() {
        super();
    }

    @Override
    public boolean getStateVisiblity() {
        // card is never visible
        return false;
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        // card is never displayed
        return null;
    }

//    @Override
//    public void update(Observable observable, Object o) {
//        if (!(observable instanceof Card)) {
//            Log.e(this.getClass().getName(), "update notification received from non-card observable");
//            return;
//        }
//        if (storyPath == null) {
//            Log.e(this.getClass().getName(), "STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
//            return;
//        }
//
//        Card card = (Card)observable;
//
//        // recycling stateVisibility to ensure this only triggers once
//        if (!stateVisiblity) {
//            if (checkReferencedValues()) {
//                stateVisiblity = true;
//
//            }
//        }
//    }

    // need to prevent this from appearing once its references are satisfied
    // doesn't seem to work.  possible race condition?
    @Override
    public boolean checkReferencedValues() {
        if (stateVisiblity) {
            return false;
        } else {
            return super.checkReferencedValues();
        }
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof HeadlessCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF HeadlessCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        HeadlessCard castCard = (HeadlessCard)card;

        this.title = castCard.getTitle();
    }
}
