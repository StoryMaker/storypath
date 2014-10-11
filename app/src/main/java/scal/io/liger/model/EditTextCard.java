package scal.io.liger.model;

import android.content.Context;

import scal.io.liger.view.EditTextCardView;

public class EditTextCard extends Card {

    private String hintText;
    private String header;

    public EditTextCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
        return new EditTextCardView(context, this);
    }

    public String getHintText() {
        return fillReferences(hintText);
    }

    public void setHintText(String text) {
        this.hintText = text;
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
