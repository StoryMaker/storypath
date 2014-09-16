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

        if ((references != null) && (references.size() == 11)) { // FIXME hardcoding to 9 refs (+1 for medium), +1 more for the card setting our vis obviously sucks balls

            medium = storyPathReference.getReferencedValue(references.get(0));

            if (medium != null) {
                // FIXME this is super fragile, assume the clip type is based on order.  ug.
                // CASTING IS A BAD SOLUTION BUT THIS WILL BE REVISED SOON
                if (medium.equals(Constants.VIDEO)) {
                    clipPaths.add((String)references.get(1));
                    clipPaths.add((String)references.get(2));
                    clipPaths.add((String)references.get(3));
                } else if (medium.equals(Constants.AUDIO)) {
                    clipPaths.add((String)references.get(4));
                    clipPaths.add((String)references.get(5));
                    clipPaths.add((String)references.get(6));
                } else if (medium.equals(Constants.PHOTO)) {
                    clipPaths.add((String)references.get(7));
                    clipPaths.add((String)references.get(8));
                    clipPaths.add((String)references.get(9));
                }
            }
        }
        return  clipPaths;
    }



    private boolean g(Object obj) {
        if (obj instanceof String) {
            String ref = (String)obj;
            String val = storyPathReference.getReferencedValue(ref);
            Log.d("ProgressCardModel", "ref: " + ref + ", val: " + val);
            return (val != null) && val.equals("true"); // FIXME refactor checkReferenceValues in teh base class to leverage it instead of this hard coded check
        }
        else {
            Log.d("TESTING", "REFERENCE IS NOT A STRING");
            return false;
        }
    }

    @Override
    public boolean checkReferencedValues() {
        boolean val = g(references.get(10)); // FIXME this is 10 so we can leave teh rest of the magic in getClipPaths intact
        return val;
    }
}