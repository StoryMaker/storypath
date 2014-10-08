package scal.io.liger.model;

import android.content.Context;

import java.util.ArrayList;

import scal.io.liger.view.GenericCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class GenericCard extends Card {

    private String media_path; // FIXME provide a default if they don't specify
    private String header;
    private String text;
    private ArrayList<String> story_paths;

    public GenericCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
        return new GenericCardView(context, this);
    }

    public String getMedia_path() { return fillReferences(media_path); }

    public void setMedia_path(String media_path) { this.media_path = media_path; }

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

    public ArrayList<String> getStory_paths() {
        ArrayList<String> a = new ArrayList<String>();
        if (story_paths != null) {
            for (String s : story_paths) {
                a.add(fillReferences(s));
            }
        }
        return a;
    }

    public void setStory_paths(ArrayList<String> story_paths) {
        this.story_paths = story_paths;
    }

    public void addStoryPath(String storyPath) {
        if (this.story_paths == null)
            this.story_paths = new ArrayList<String>();

        this.story_paths.add(storyPath);
    }
}
