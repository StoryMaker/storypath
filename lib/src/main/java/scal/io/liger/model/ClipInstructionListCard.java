package scal.io.liger.model;

import timber.log.Timber;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import scal.io.liger.view.ClipInstructionListCardView;
import scal.io.liger.view.DisplayableCard;

/**
 * Created by mnbogner on 7/17/14.
 */
public class ClipInstructionListCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String mediaPath;
    @Expose private String header;
    @Expose private ArrayList<String> bulletList;

    public ClipInstructionListCard() {
        super();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new ClipInstructionListCardView(context, this);
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

    public ArrayList<String> getBulletList() {
        ArrayList<String> a = new ArrayList<String>();
        if (bulletList != null) {
            for (String s : bulletList) {
                a.add(fillReferences(s));
            }
        }
        return a;
    }

    public void setBulletList(ArrayList<String> bulletList) {
        this.bulletList = bulletList;
    }

    public void addBulletListItem(String bullet_listItem) {
        if (this.bulletList == null)
            this.bulletList = new ArrayList<String>();

        this.bulletList.add(bullet_listItem);
    }


    @Override
    public void copyText(Card card) {
        if (!(card instanceof ClipInstructionListCard)) {
            Timber.e("CARD " + card.getId() + " IS NOT AN INSTANCE OF ClipInstructionListCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Timber.e("CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        ClipInstructionListCard castCard = (ClipInstructionListCard)card;

        this.title = castCard.getTitle();
        this.header = castCard.getHeader();
        this.bulletList = castCard.getBulletList();
    }
}
