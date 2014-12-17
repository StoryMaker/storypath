package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import scal.io.liger.view.ExampleCardView;


public class ExampleCard extends Card implements Cloneable {

    public final String TAG = this.getClass().getSimpleName();

    @Expose protected String header;
    @Expose private String medium;
    @Expose private String exampleMediaPath;
    @Expose private ExampleMediaFile exampleMediaFile;

    public ExampleCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public scal.io.liger.view.DisplayableCard getDisplayableCard(Context context) {
        return new ExampleCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMedium() { return fillReferences(medium); }

    public void setMedium(String medium) { this.medium = medium; }

    public String getExampleMediaPath() { return exampleMediaPath; }

    public void setExampleMediaPath(String example_media_path) { this.exampleMediaPath = example_media_path; }

    public ExampleMediaFile getExampleMediaFile() {
        if (exampleMediaPath == null) {
            Log.d(this.getClass().getName(), "no example media path for card " + this.getId());
            return null;
        }

        if (exampleMediaFile == null) {
            exampleMediaFile = new ExampleMediaFile(storyPath.buildZipPath(exampleMediaPath), medium);
        }

        return exampleMediaFile;
    }

    public void setExampleMediaFile(ExampleMediaFile exampleMediaFile) {
        this.exampleMediaFile = exampleMediaFile;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ExampleCard clone = (ExampleCard) super.clone();
        clone.header = this.header; // Strings are immutable
        clone.medium = this.medium;
        clone.exampleMediaPath = this.exampleMediaPath;
        if (this.exampleMediaFile != null) clone.exampleMediaFile = (ExampleMediaFile) this.exampleMediaFile.clone();
        clone.type = this.getClass().getName();

        return clone;
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof ExampleCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF ExampleCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        ExampleCard castCard = (ExampleCard)card;

        this.title = castCard.getTitle();
        this.header = castCard.getHeader();
    }
}
