package scal.io.liger.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class TipCard extends MarkdownCard {

    private Random random;
    @Expose private ArrayList<String> tags;

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
