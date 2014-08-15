package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.view.OrderMediaCardView;


public class OrderMediaCardModel extends CardModel {
    private String header;
    private String medium;
    private ArrayList<String> clips;

    public OrderMediaCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new OrderMediaCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public ArrayList<String> getClipPaths() {
        ArrayList<String> clipPaths = new ArrayList<String>();

        if ((references != null) && (references.size() == 10)) { // FIXME hardcoding to 9 refs (+1 ignored) obviously sucks balls

            medium = storyPathReference.getReferencedValue(references.get(0));

            if (medium != null) {
                // FIXME this is super fragile, assume the clip type is based on order.  ug.
                if (medium.equals(Constants.VIDEO)) {
                    clipPaths.add(references.get(1));
                    clipPaths.add(references.get(2));
                    clipPaths.add(references.get(3));
                } else if (medium.equals(Constants.AUDIO)) {
                    clipPaths.add(references.get(4));
                    clipPaths.add(references.get(5));
                    clipPaths.add(references.get(6));
                } else if (medium.equals(Constants.PHOTO)) {
                    clipPaths.add(references.get(7));
                    clipPaths.add(references.get(8));
                    clipPaths.add(references.get(9));
                }
            }
        }
        return  clipPaths;
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
}
