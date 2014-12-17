package scal.io.liger.model;

import android.util.Log;

/**
 * Created by josh on 8/14/14.
 */
public class CongratulationsCard extends GenericCard {

    public final String TAG = this.getClass().getSimpleName();

    @Override
    public void copyText(Card card) {
        if (!(card instanceof CongratulationsCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF CongratulationsCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        CongratulationsCard castCard = (CongratulationsCard)card;

        this.title = castCard.getTitle();
        this.header = castCard.getHeader();
        this.text = castCard.getText();
    }
}
