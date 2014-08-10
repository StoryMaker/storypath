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
        clearValues();
        addValue("value::" + (areWeSatisfied() ? "true" : "false"), false); // FIXME this should be in a more general init() method called on each card as the path is bootstrapped

        boolean val = g(references.get(0));
        return val;
    }

    public boolean areWeSatisfied() {
        boolean result = false;

        if ((references != null) && (references.size() == 10)) { // FIXME hardcoding to 9 refs (+1 ignored) obviously sucks balls
//            result = ((g(references.get(1)) && g(references.get(2)) && g(references.get(3)))
//                || (g(references.get(4)) && g(references.get(5)) && g(references.get(6)))
//                || (g(references.get(7)) && g(references.get(8)) && g(references.get(9))));

            String medium = storyPathReference.getReferencedValue(references.get(0));

            if (medium != null) {
                // FIXME this is super fragile, assume the clip type is based on order.  ug.
                if (medium.equals("video")) {
                    result = (g(references.get(1)) && g(references.get(2)) && g(references.get(3)));
                } else if (medium.equals("audio")) {
                    result = (g(references.get(4)) && g(references.get(5)) && g(references.get(6)));
                } else if (medium.equals("photo")) {
                    result = (g(references.get(7)) && g(references.get(8)) && g(references.get(9)));
                }
            }

        }
        Log.d("areWeSatisfied", result ? "true" : "false");
        return result;
    }

    public int getFilledCount() {
        int result = 0;

        if ((references != null) && (references.size() == 10)) { // FIXME hardcoding to 9 refs (+1 ignored) obviously sucks balls
            String medium = storyPathReference.getReferencedValue(references.get(0));
            if (medium != null) {
                // FIXME this is super fragile, assume the clip type is based on order.  ug.
                if (medium.equals("video")) {
                    result += (g(references.get(1)) ? 1 : 0);
                    result += (g(references.get(2)) ? 1 : 0);
                    result += (g(references.get(3)) ? 1 : 0);
                } else if (medium.equals("audio")) {
                    result += (g(references.get(4)) ? 1 : 0);
                    result += (g(references.get(5)) ? 1 : 0);
                    result += (g(references.get(6)) ? 1 : 0);
                } else if (medium.equals("photo")) {
                    result += (g(references.get(7)) ? 1 : 0);
                    result += (g(references.get(8)) ? 1 : 0);
                    result += (g(references.get(9)) ? 1 : 0);
                }
            }

        }
        Log.d("filledCardCount", "" + result);
        return result;
    }
}