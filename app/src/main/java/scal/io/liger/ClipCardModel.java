package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.ClipCardView;
import scal.io.liger.view.IntroCardView;


public class ClipCardModel extends CardModel {

    private String header;
    private String clip_medium;
    private String clip_type;

    public ClipCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new ClipCardView(context, this);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getClipMedium() { return clip_medium; }

    public void setClipMedium(String clip_medium) { this.clip_medium = clip_medium; }

    public String getClipType() { return clip_type; }

    public void setClipType(String clipType) { this.clip_type = clipType; }
}
