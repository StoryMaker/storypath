package scal.io.liger.model;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class TipCard extends MarkdownCard {

    // FIXME make sure random isnt being serizlized
    private Random random;
    private ArrayList<String> tags;

    public TipCard() {
        super();
        this.type = this.getClass().getName();
        this.random = new Random();
    }

    @Override
    public String getText() {
        if (text == null)
            text = randomTag();
        return super.getText();
    }

    public ArrayList<String> getTags() {
        ArrayList<String> a = new ArrayList<String>();
        if (tags != null) {
            for (String s : tags) {
                a.add(fillReferences(s));
            }
        }
        return a;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        if (this.tags == null)
            this.tags = new ArrayList<String>();

        this.tags.add(tag);
    }

    public String randomTag() {
        return tags.get(random.nextInt(tags.size()));
    }
}
