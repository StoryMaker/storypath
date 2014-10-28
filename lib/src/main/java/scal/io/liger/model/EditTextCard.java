package scal.io.liger.model;

import android.content.Context;

import com.google.gson.annotations.Expose;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.EditTextCardView;

public class EditTextCard extends Card {

    @Expose private String hintText;
    @Expose private String header;

    public EditTextCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
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
