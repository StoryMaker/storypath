package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/11/14.
 */
public class VideoCaptureTypeCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private ArrayList<Object> body;

    public VideoCaptureTypeCard() {
        super();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new IntroCardView(context, this); //TODO
    }

    public ArrayList<Object> getBody() {
        return body;
    }

    public void setBody(ArrayList<Object> body) {
        this.body = body;
    }

    public void addBody(Object body) {
        if (this.body == null)
            this.body = new ArrayList<Object>();

        this.body.add(body);
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof TipCollectionHeadlessCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF TipCollectionHeadlessCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        TipCollectionHeadlessCard castCard = (TipCollectionHeadlessCard)card;

        this.title = castCard.getTitle();
    }
}
