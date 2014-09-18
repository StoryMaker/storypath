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

    private ArrayList<String> story_medium;
    private ArrayList<String> video_clip_cards;
    private ArrayList<String> audio_clip_cards;
    private ArrayList<String> photo_clip_cards;

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

    public ArrayList<String> getClipPaths() {
        ArrayList<String> clipPaths = new ArrayList<String>();

        String mediumReference = "";

        if (story_medium.size() == 1) {
            mediumReference = story_medium.get(0);
        }
        else {
            Log.e(this.type, "unexpected number of story medium references: " + story_medium.size());
            return clipPaths;
        }

        medium = storyPathReference.getReferencedValue(mediumReference);

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return clipPaths;
        }
        else if (medium.equals(Constants.VIDEO)) {
            clipPaths.addAll(video_clip_cards);
        }
        else if (medium.equals(Constants.AUDIO)) {
            clipPaths.addAll(audio_clip_cards);
        }
        else if (medium.equals(Constants.PHOTO)) {
            clipPaths.addAll(photo_clip_cards);
        }

        /*
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
        */

        return  clipPaths;
    }

    /*
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
    */

    /*
    @Override
    public boolean checkReferencedValues() {
        boolean val = g(references.get(10)); // FIXME this is 10 so we can leave teh rest of the magic in getClipPaths intact
        return val;
    }
    */
}