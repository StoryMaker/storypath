package scal.io.liger.model;

import android.content.Context;
import android.util.Log;
import android.view.Display;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import scal.io.liger.view.ClipInstructionTypeCardView;
import scal.io.liger.view.DisplayableCard;

/**
 * Created by mnbogner on 7/17/14.
 */
public class ClipInstructionTypeCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

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

    @Override
    public void copyText(Card card) {
        if (!(card instanceof ClipInstructionTypeCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF ClipInstructionTypeCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        ClipInstructionTypeCard castCard = (ClipInstructionTypeCard)card;

        this.title = castCard.getTitle();
        this.header = castCard.getHeader();
        this.clipTypes = castCard.getClipTypes();
    }
}
