package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import scal.io.liger.view.ExampleCardView;


public class ExampleCard extends Card {

    private String header;
    private String clipMedium;
    private String exampleMediaPath;
    private MediaFile exampleMediaFile;

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

    public String getClipMedium() { return fillReferences(clipMedium); }

    public void setClipMedium(String clip_medium) { this.clipMedium = clip_medium; }

    public String getExampleMediaPath() { return exampleMediaPath; }

    public void setExampleMediaPath(String example_media_path) { this.exampleMediaPath = example_media_path; }

    public MediaFile getExampleMediaFile() {
        if (exampleMediaPath == null) {
            Log.e(this.getClass().getName(), "no example media path for card " + this.getId());
            return null;
        }

        if (exampleMediaFile == null) {
            exampleMediaFile = new MediaFile(storyPathReference.buildPath(exampleMediaPath), clipMedium);
        }

        return exampleMediaFile;
    }

    public void setExampleMediaFile(MediaFile exampleMediaFile) {
        this.exampleMediaFile = exampleMediaFile;
    }
}
