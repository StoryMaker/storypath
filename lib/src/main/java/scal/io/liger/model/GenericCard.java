package scal.io.liger.model;

import timber.log.Timber;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.GenericCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class GenericCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose protected String mediaPath; // FIXME provide a default if they don't specify
    @Expose protected String header;
    @Expose protected String text;
    @Expose protected ArrayList<String> storyPaths;

    public GenericCard() {
        super();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new GenericCardView(context, this);
    }

    public String getMediaPath() { return fillReferences(mediaPath); }

    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getText() {
        return fillReferences(text);
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<String> getStoryPaths() {
        ArrayList<String> a = new ArrayList<String>();
        if (storyPaths != null) {
            for (String s : storyPaths) {
                a.add(fillReferences(s));
            }
        }
        return a;
    }

    public void setStoryPaths(ArrayList<String> storyPaths) {
        this.storyPaths = storyPaths;
    }

    public void addStoryPath(String storyPath) {
        if (this.storyPaths == null)
            this.storyPaths = new ArrayList<String>();

        this.storyPaths.add(storyPath);
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof GenericCard)) {
            Timber.e("CARD " + card.getId() + " IS NOT AN INSTANCE OF GenericCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Timber.e("CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        GenericCard castCard = (GenericCard)card;

        this.title = castCard.getTitle();
        this.header = castCard.getHeader();
        this.text = castCard.getText();
    }
}
