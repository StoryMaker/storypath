package scal.io.liger.model;

import timber.log.Timber;

import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class TipCard extends MarkdownCard {

    public final String TAG = this.getClass().getSimpleName();

    private Random random;
    @Expose private ArrayList<String> tags;

    private ArrayList<String> tips = null;

    public TipCard() {
        super();
        this.random = new Random();
    }

    @Override
    public String getText() {
        if (text == null)
            text = randomTip();
        return super.getText();
    }

    public ArrayList<String> getTips() {
        if (tips == null) {
            ArrayList<Card> cards = getCardsByClass(storyPath.getId() + "::<<scal.io.liger.model.TipCollectionHeadlessCard>>");
            if (cards.size() > 0) {
                TipCollectionHeadlessCard tipCollection = (TipCollectionHeadlessCard) cards.get(0);
                tips = tipCollection.getTipsTextByTags(tags);
            } else {
                return null;
            }
        }
        return tips;
    }

    // FIXME This is an inefficient algorithm and causes dropped frames when called from
    // main thread e.g: MarkdownCard#getCardView
    public String randomTip() {
        ArrayList<String> ts = getTips();
        if (ts != null && (ts.size() > 0)) {
            int r = random.nextInt(ts.size());
            return ts.get(r);
        } else {
            return "(no tips match for tags: " + tags + ")"; // FIXME this is probably the wrong thing to do
        }
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof TipCard)) {
            Timber.e("CARD " + card.getId() + " IS NOT AN INSTANCE OF TipCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Timber.e("CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        TipCard castCard = (TipCard)card;

        this.title = castCard.getTitle();
        this.text = castCard.getText();
        this.tips = castCard.getTips();
    }
}
