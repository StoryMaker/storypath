package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.ChooseMediaCardView;

/**
 * Created by micahlucas on 8/4/14.
 */
public class ChooseMediaCardModel extends CardModel {
    private String header;

    public ChooseMediaCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new ChooseMediaCardView(context, this);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
