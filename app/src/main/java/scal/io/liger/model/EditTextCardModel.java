package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.EditTextCardView;

public class EditTextCardModel extends CardModel {
    private String hint_text;
    private String header;

    public EditTextCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new EditTextCardView(context, this);
    }

    public String getHint_text() {
        return hint_text;
    }

    public void setHint_text(String text) {
        this.hint_text = text;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
