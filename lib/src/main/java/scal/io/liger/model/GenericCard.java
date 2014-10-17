package scal.io.liger.model;

import android.content.Context;

import java.util.ArrayList;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.GenericCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class GenericCard extends Card {

    private String mediaPath; // FIXME provide a default if they don't specify
    private String header;
    private String text;
    private ArrayList<String> storyPaths;

    public GenericCard() {
        super();
        this.type = this.getClass().getName();
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
}
