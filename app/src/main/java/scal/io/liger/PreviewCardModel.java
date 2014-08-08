package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.PreviewCardView;


public class PreviewCardModel extends CardModel {
    private String media_path;
    private String text;

    public PreviewCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new PreviewCardView(context, this);
    }

    public String getMedia_path() {
        return media_path;
    }

    public void setMedia_path(String media_path) {
        this.media_path = media_path;
    }

    public String getText() { return this.text; }

    public void setText(String text) { this.text = text; }
}