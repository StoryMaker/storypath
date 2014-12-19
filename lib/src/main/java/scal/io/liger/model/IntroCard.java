package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class IntroCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String headline;
    @Expose private String level;
    @Expose private String time;
    @Expose private String exampleMediaPath;
    @Expose private ExampleMediaFile exampleMediaFile;

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

    public ExampleMediaFile getExampleMediaFile() {
        if (exampleMediaPath == null) {
            Log.d(this.getClass().getName(), "no example media path for card " + this.getId());
            return null;
        }

        if (exampleMediaFile == null) {
            exampleMediaFile = new ExampleMediaFile(storyPath.buildZipPath(exampleMediaPath), "photo");
        }

        return exampleMediaFile;
    }

    public void setExampleMediaFile(ExampleMediaFile exampleMediaFile) {
        this.exampleMediaFile = exampleMediaFile;
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof IntroCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF IntroCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        IntroCard castCard = (IntroCard)card;

        this.title = castCard.getTitle();
        this.headline = castCard.getHeadline();
        this.level = castCard.getLevel();
        this.time = castCard.getTime();
    }
}
