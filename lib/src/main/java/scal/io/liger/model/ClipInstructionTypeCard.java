package scal.io.liger.model;

import android.content.Context;
import android.view.Display;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import scal.io.liger.view.ClipInstructionTypeCardView;
import scal.io.liger.view.DisplayableCard;

/**
 * Created by mnbogner on 7/17/14.
 */
public class ClipInstructionTypeCard extends Card {

    @Expose private String mediaPath;
    @Expose private String header;
    @Expose private ArrayList<String> clipTypes;

    public ClipInstructionTypeCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new ClipInstructionTypeCardView(context, this);
    }

    public String getMediaPath() {
        return fillReferences(mediaPath);
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public ArrayList<String> getClipTypes() {
        ArrayList<String> a = new ArrayList<String>();
        if (clipTypes != null) {
            for (String s : clipTypes) {
                a.add(fillReferences(s));
            }
        }
        return a;
    }

    public void setClipTypes(ArrayList<String> clipTypes) {
        this.clipTypes = clipTypes;
    }

    public void addClipType(String clip_type) {
        if (this.clipTypes == null)
            this.clipTypes = new ArrayList<String>();

        this.clipTypes.add(clip_type);
    }
}
