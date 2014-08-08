package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.ClipCardView;
import scal.io.liger.view.IntroCardView;


public class ClipCardModel extends CardModel {

    private String header;
    private String media_path;
    private String clipMedium;
    private String clipType;

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

    public String getMedia_path() {
        return media_path;
    }

    public void setMedia_path(String media_path) {
        this.media_path = media_path;
    }

    public String getClipMedium() { return clipMedium; }

    public void setClipMedium(String clipMedium) { this.clipMedium = clipMedium; }

    public String getClipType() { return clipType; }

    public void setClipType(String clipType) { this.clipType = clipType; }
}
