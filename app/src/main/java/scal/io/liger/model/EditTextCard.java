package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.EditTextCardView;

public class EditTextCard extends Card {
    private String hint_text;
    private String header;

    public EditTextCard() {
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
        return new EditTextCardView(context, this);
    }

    public String getHint_text() {
        return fillReferences(hint_text);
    }

    public void setHint_text(String text) {
        this.hint_text = text;
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
