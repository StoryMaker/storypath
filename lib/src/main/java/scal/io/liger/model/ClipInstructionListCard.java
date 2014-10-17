package scal.io.liger.model;

import android.content.Context;

import java.util.ArrayList;

import scal.io.liger.view.ClipInstructionListCardView;
import scal.io.liger.view.DisplayableCard;

/**
 * Created by mnbogner on 7/17/14.
 */
public class ClipInstructionListCard extends Card {

    private String mediaPath;
    private String header;
    private ArrayList<String> bulletList;

    public ClipInstructionListCard() {
        super();
        this.type = this.getClass().getName();
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
}
