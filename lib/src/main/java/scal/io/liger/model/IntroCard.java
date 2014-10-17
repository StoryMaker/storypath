package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class IntroCard extends Card {

    private String headline;
    private String level;
    private String time;

    public IntroCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new IntroCardView(context, this); //TODO
    }

    public String getHeadline() {
        return fillReferences(headline);
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getLevel() {
        return fillReferences(level);
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTime() {
        return fillReferences(time);
    }

    public void setTime(String time) {
        this.time = time;
    }
}
