package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.PreviewCardView;


public class PreviewCardModel extends CardModel {
    private String mediaPath;
    private String text;

    public PreviewCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new PreviewCardView(context, this);
    }

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }

    public String getText() { return this.text; }

    public void setText(String text) { this.text = text; }
}