package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.ClipCardView;
import scal.io.liger.view.IntroCardView;


public class ClipCardModel extends CardModel {
    private enum eClipMedium { VIDEO, AUDIO, PHOTO };
    private enum eClipType { CHARACTER, ACTION, RESULT };

    private String mediaPath;
    private eClipMedium clipMedium;
    private eClipType clipType;

    public ClipCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new ClipCardView(context, this);
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public eClipMedium getClipMedium() { return clipMedium; }

    public void setClipMedium(eClipMedium clipMedium) { this.clipMedium = clipMedium; }

    public eClipType getClipType() { return clipType; }

    public void setClipType(eClipType clipType) { this.clipType = clipType; }
}
