package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.ReferenceHelper;
import scal.io.liger.view.ProgressCardView;

public class ProgressCardModel extends CardModel {
    private String text;

    private ArrayList<String> story_medium;
    private ArrayList<String> video_clip_cards;
    private ArrayList<String> audio_clip_cards;
    private ArrayList<String> photo_clip_cards;

    public ProgressCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new ProgressCardView(context, this);
    }

    public String getText() {
        return fillReferences(this.text);
    }

    public void setText(String time) {
        this.text = time;
    }

    public ArrayList<String> getStory_medium() {
        return story_medium;
    }

    public void setStory_medium(ArrayList<String> story_medium) {
        this.story_medium = story_medium;
    }

    public ArrayList<String> getVideo_clip_cards() {
        return video_clip_cards;
    }

    public void setVideo_clip_cards(ArrayList<String> video_clip_cards) {
        this.video_clip_cards = video_clip_cards;
    }

    public ArrayList<String> getAudio_clip_cards() {
        return audio_clip_cards;
    }

    public void setAudio_clip_cards(ArrayList<String> audio_clip_cards) {
        this.audio_clip_cards = audio_clip_cards;
    }

    public ArrayList<String> getPhoto_clip_cards() {
        return photo_clip_cards;
    }

    public void setPhoto_clip_cards(ArrayList<String> photo_clip_cards) {
        this.photo_clip_cards = photo_clip_cards;
    }

    /*
    private boolean g(Object obj) {
        if (obj instanceof String) {
            String ref = (String)obj;
            String val = storyPathReference.getReferencedValue(ref);
            Log.d("ProgressCardModel", "ref: " + ref + ", val: " + val);
            return (val != null) && !val.equals("");
        }
        else {
            Log.d("TESTING", "REFERENCE IS NOT A STRING");
            return false;
        }
    }
    */

    @Override
    public boolean checkReferencedValues() {
        clearValues();
        addValue("value", (areWeSatisfied() ? "true" : "false"), false); // FIXME this should be in a more general init() method called on each card as the path is bootstrapped

        /*
        boolean val1 = g(references.get(0));

        String sv = storyPathReference.getReferencedValue(references.get(10));
        Log.d("ProgressCardModel", "ref: " + references.get(10) + ", val: " + sv);
        boolean val2 = (sv != null) && sv.equals("true");

        return val1 && val2;
        */

        // no need to check both choose_medium and got_it, since got_it already references choose_medium
        return super.checkReferencedValues();
    }

    public boolean areWeSatisfied() {
        // boolean result = true;

        String mediumReference = "";

        if (story_medium.size() == 1) {
            mediumReference = story_medium.get(0);
        }
        else {
            Log.e(this.type, "unexpected number of story medium references: " + story_medium.size());
            return false;
        }

        String medium = storyPathReference.getReferencedValue(mediumReference);
        ArrayList<String> values = new ArrayList<String>();

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return false;
        }
        else if (medium.equals(Constants.VIDEO)) {
            values = ReferenceHelper.getValues(storyPathReference, video_clip_cards);
        }
        else if (medium.equals(Constants.AUDIO)) {
            values = ReferenceHelper.getValues(storyPathReference, audio_clip_cards);
        }
        else if (medium.equals(Constants.PHOTO)) {
            values = ReferenceHelper.getValues(storyPathReference, photo_clip_cards);
        }

        for (String s : values) {
            if ((s == null) || (s.length() == 0 )) {
                return false;
            }
        }

        return true;

        /*
        if ((references != null) && (references.size() == 11)) { // FIXME hardcoding to 9 refs (+1 ignored for medium) (+1 for got it card) obviously sucks balls
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
        */
    }

    public int getFilledCount() {
        int result = 0;

        String mediumReference = "";

        if (story_medium.size() == 1) {
            mediumReference = story_medium.get(0);
        }
        else {
            Log.e(this.type, "unexpected number of story medium references: " + story_medium.size());
            return 0;
        }

        String medium = storyPathReference.getReferencedValue(mediumReference);
        ArrayList<String> values = new ArrayList<String>();

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return 0;
        }
        else if (medium.equals(Constants.VIDEO)) {
            values = ReferenceHelper.getValues(storyPathReference, video_clip_cards);
        }
        else if (medium.equals(Constants.AUDIO)) {
            values = ReferenceHelper.getValues(storyPathReference, audio_clip_cards);
        }
        else if (medium.equals(Constants.PHOTO)) {
            values = ReferenceHelper.getValues(storyPathReference, photo_clip_cards);
        }

        for (String s : values) {
            if ((s != null) && (s.length() > 0 )) {
                result++;
            }
        }

        /*
        if ((references != null) && (references.size() == 11)) { // FIXME hardcoding to 9 refs (+1 ignored for medium) (+1 for got it card)  obviously sucks balls
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
        */

        return result;
    }

    public int getMaxCount() {
        int result = 0;

        String mediumReference = "";

        if (story_medium.size() == 1) {
            mediumReference = story_medium.get(0);
        }
        else {
            Log.e(this.type, "unexpected number of story medium references: " + story_medium.size());
            return 0;
        }

        String medium = storyPathReference.getReferencedValue(mediumReference);

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return 0;
        }
        else if (medium.equals(Constants.VIDEO)) {
            result = video_clip_cards.size();
        }
        else if (medium.equals(Constants.AUDIO)) {
            result = audio_clip_cards.size();
        }
        else if (medium.equals(Constants.PHOTO)) {
            result = photo_clip_cards.size();
        }

        return result;
    }
}