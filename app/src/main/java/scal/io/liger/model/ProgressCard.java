package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.ReferenceHelper;
import scal.io.liger.view.ProgressCardView;

public class ProgressCard extends Card {

    private String text;
    private ArrayList<String> story_medium; // why is this an array?
    private ArrayList<String> video_clip_cards;
    private ArrayList<String> audio_clip_cards;
    private ArrayList<String> photo_clip_cards;

    public ProgressCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
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

    @Override
    public boolean checkReferencedValues() {
        clearValues();
        addValue("value", (areWeSatisfied() ? "true" : "false"), false); // FIXME this should be in a more general init() method called on each card as the path is bootstrapped

        // no need to check both choose_medium and got_it, since got_it already references choose_medium
        return super.checkReferencedValues();
    }

    public boolean areWeSatisfied() {
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