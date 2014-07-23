package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.CongratsCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class CongratsCardModel extends CardModel {
    public String headline;
    public String text;
    public ArrayList<String> story_paths;

    public CongratsCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new CongratsCardView(context, this);
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<String> getStory_paths() {
        return story_paths;
    }

    public void setStory_paths(ArrayList<String> story_paths) {
        this.story_paths = story_paths;
    }

    public void addStory_path(String story_path) {
        if (this.story_paths == null)
            this.story_paths = new ArrayList<String>();

        this.story_paths.add(story_path);
    }
}
