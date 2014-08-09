package scal.io.liger;

import android.content.Context;
import android.util.Log;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.ProgressCardView;

public class ProgressCardModel extends CardModel {
    private String text;

    public ProgressCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new ProgressCardView(context, this);
    }

    public String getText() {
        return this.text;
    }

    public void setText(String time) {
        this.text = time;
    }

    private boolean g(String ref) {
        String val = storyPathReference.getReferencedValue(ref);
        Log.d("ProgressCardModel", "ref: " + ref + ", val: " + val);
        return (val != null) && !val.equals("");
    }

    @Override
    public boolean checkReferencedValues() {
        boolean result = true;

        if ((references != null) && (references.size() == 9)) { // FIXME hardcoding to 9 refs obviously sucks balls
            return ((g(references.get(0)) && g(references.get(1)) && g(references.get(2)))
                || (g(references.get(3)) && g(references.get(4)) && g(references.get(5)))
                || (g(references.get(6)) && g(references.get(7)) && g(references.get(8))));
        }

        return result;
    }
}