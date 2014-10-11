package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.ReferenceHelper;
import scal.io.liger.view.ProgressCardView;

public class ProgressCard extends Card {

    private String text;
    private ArrayList<String> storyMedium; // why is this an array?
    private ArrayList<String> videoClipCards;
    private ArrayList<String> audioClipCards;
    private ArrayList<String> photoClipCards;

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

    public ArrayList<String> getStoryMedium() {
        return storyMedium;
    }

    public void setStoryMedium(ArrayList<String> storyMedium) {
        this.storyMedium = storyMedium;
    }

    public ArrayList<String> getVideoClipCards() {
        return videoClipCards;
    }

    public void setVideoClipCards(ArrayList<String> videoClipCards) {
        this.videoClipCards = videoClipCards;
    }

    public ArrayList<String> getAudioClipCards() {
        return audioClipCards;
    }

    public void setAudioClipCards(ArrayList<String> audioClipCards) {
        this.audioClipCards = audioClipCards;
    }

    public ArrayList<String> getPhotoClipCards() {
        return photoClipCards;
    }

    public void setPhotoClipCards(ArrayList<String> photoClipCards) {
        this.photoClipCards = photoClipCards;
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

        if (storyMedium.size() == 1) {
            mediumReference = storyMedium.get(0);
        }
        else {
            Log.e(this.type, "unexpected number of story medium references: " + storyMedium.size());
            return false;
        }

        String medium = storyPathReference.getReferencedValue(mediumReference);
        ArrayList<String> values = new ArrayList<String>();

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return false;
        }
        else if (medium.equals(Constants.VIDEO)) {
            values = ReferenceHelper.getValues(storyPathReference, videoClipCards);
        }
        else if (medium.equals(Constants.AUDIO)) {
            values = ReferenceHelper.getValues(storyPathReference, audioClipCards);
        }
        else if (medium.equals(Constants.PHOTO)) {
            values = ReferenceHelper.getValues(storyPathReference, photoClipCards);
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

        if (storyMedium.size() == 1) {
            mediumReference = storyMedium.get(0);
        }
        else {
            Log.e(this.type, "unexpected number of story medium references: " + storyMedium.size());
            return 0;
        }

        String medium = storyPathReference.getReferencedValue(mediumReference);
        ArrayList<String> values = new ArrayList<String>();

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return 0;
        }
        else if (medium.equals(Constants.VIDEO)) {
            values = ReferenceHelper.getValues(storyPathReference, videoClipCards);
        }
        else if (medium.equals(Constants.AUDIO)) {
            values = ReferenceHelper.getValues(storyPathReference, audioClipCards);
        }
        else if (medium.equals(Constants.PHOTO)) {
            values = ReferenceHelper.getValues(storyPathReference, photoClipCards);
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

        if (storyMedium.size() == 1) {
            mediumReference = storyMedium.get(0);
        }
        else {
            Log.e(this.type, "unexpected number of story medium references: " + storyMedium.size());
            return 0;
        }

        String medium = storyPathReference.getReferencedValue(mediumReference);

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return 0;
        }
        else if (medium.equals(Constants.VIDEO)) {
            result = videoClipCards.size();
        }
        else if (medium.equals(Constants.AUDIO)) {
            result = audioClipCards.size();
        }
        else if (medium.equals(Constants.PHOTO)) {
            result = photoClipCards.size();
        }

        return result;
    }
}