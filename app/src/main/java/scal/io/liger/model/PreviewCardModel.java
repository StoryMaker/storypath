package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.view.PreviewCardView;


public class PreviewCardModel extends CardModel {
//    private ArrayList<String> media_paths;
    private String text;

    public PreviewCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new PreviewCardView(context, this);
    }

//    public ArrayList<String> getMedia_paths() {
//        ArrayList<String> a = new ArrayList<String>();
//        for (String s : media_paths)
//        {
//            a.add(fillReferences(s));
//        }
//        return a;
//    }
//
//    public void setMedia_paths(ArrayList<String> media_paths) {
//        this.media_paths = media_paths;
//    }
//
//    public void addMedia_path(String media_path) {
//        if (this.media_paths == null)
//            this.media_paths = new ArrayList<String>();
//
//        this.media_paths.add(media_path);
//    }

    public String getText() { return fillReferences(this.text); }

    public void setText(String text) { this.text = text; }


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

    public ArrayList<String> getClipPaths() {
        ArrayList<String> clipPaths = new ArrayList<String>();

        if ((references != null) && (references.size() == 11)) { // FIXME hardcoding to 9 refs (+1 for medium), +1 more for the card setting our vis obviously sucks balls

            String medium = storyPathReference.getReferencedValue(references.get(0));

            if (medium != null) {
                // FIXME this is super fragile, assume the clip type is based on order.  ug.
                // CASTING IS A BAD SOLUTION, BUT THIS WILL BE REVISED SOON
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
}