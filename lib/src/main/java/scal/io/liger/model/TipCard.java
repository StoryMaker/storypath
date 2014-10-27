package scal.io.liger.model;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class TipCard extends MarkdownCard {

    // FIXME make sure this isn't being serialized
    private Random random;
    private ArrayList<String> tags;

    // FIXME make sure this isn't being serialized
    private ArrayList<String> tips = null;

    public TipCard() {
        super();
        this.type = this.getClass().getName();
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
            ArrayList<Card> cards = getCardsByClass("thispath::<<scal.io.liger.model.TipCollectionHeadlessCard>>");
            if (cards.size() > 0) {
                TipCollectionHeadlessCard tipCollection = (TipCollectionHeadlessCard) cards.get(0);
                tips = tipCollection.getTipsTextByTags(tags);
            } else {
                return null;
            }
        }
        return tips;
    }

    public String randomTip() {
        ArrayList<String> ts = getTips();
        if (ts != null && (ts.size() > 0)) {
            int r = random.nextInt(ts.size());
            return ts.get(r);
        } else {
            return "(no tips match for tags: " + tags + ")"; // FIXME this is probably the wrong thing to do
        }
    }
}
