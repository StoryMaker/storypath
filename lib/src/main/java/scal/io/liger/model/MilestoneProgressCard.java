package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Observable;

import scal.io.liger.Constants;
import scal.io.liger.ReferenceHelper;
import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.MilestoneProgressCardView;

public class MilestoneProgressCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String text;
    @Expose private ArrayList<String> storyMedium; // why is this an array?
    @Expose private ArrayList<String> videoClipCards;
    @Expose private ArrayList<String> audioClipCards;
    @Expose private ArrayList<String> photoClipCards;

    public MilestoneProgressCard() {
        super();
    }

    // NEW - SHOULD THESE BE SERIALIZED?
    @Expose private String stateMedium;
    @Expose private int stateVideo;
    @Expose private int stateAudio;
    @Expose private int statePhoto;

    // NEW
    public String getStateMedium() {
        return stateMedium;
    }
    public int getStateVideo() {
        return stateVideo;
    }
    public int getStateAudio() {
        return stateAudio;
    }
    public int getStatePhoto() {
        return statePhoto;
    }

    // no setters, should not be set from outside the class

    // NEW
    @Override
    public void registerObservers() {
        super.registerObservers();

        if (storyMedium != null) {
            for (String reference : storyMedium) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.addObserver(this);
                }
            }
        }
        if (videoClipCards != null) {
            for (String reference : videoClipCards) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.addObserver(this);
                }
            }
        }
        if (audioClipCards != null) {
            for (String reference : audioClipCards) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.addObserver(this);
                }
            }
        }
        if (photoClipCards != null) {
            for (String reference : photoClipCards) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.addObserver(this);
                }
            }
        }
    }

    // NEW
    @Override
    public void removeObservers() {
        super.removeObservers();

        if (storyMedium != null) {
            for (String reference : storyMedium) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.deleteObserver(this);
                }
            }
        }
        if (videoClipCards != null) {
            for (String reference : videoClipCards) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.deleteObserver(this);
                }
            }
        }
        if (audioClipCards != null) {
            for (String reference : audioClipCards) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.deleteObserver(this);
                }
            }
        }
        if (photoClipCards != null) {
            for (String reference : photoClipCards) {
                Card card = storyPath.getCardById(reference);
                // need to filter out cards that are not found (ie: references to external files)
                if (card != null) {
                    card.deleteObserver(this);
                }
            }
        }
    }

    // NEW
    @Override
    public void update(Observable observable, Object o) {
        if (!(observable instanceof Card)) {
            Log.e(this.getClass().getName(), "update notification received from non-card observable");
            return;
        }
        if (storyPath == null) {
            Log.e(this.getClass().getName(), "STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
            return;
        }

        Card card = (Card)observable;

        if (checkVisibilityChanged() ||
            checkStateMedium() ||
            checkStateVideo() ||
            checkStateAudio() ||
            checkStatePhoto()) {
            storyPath.notifyCardChanged(this);
        }
    }

    // NEW
    public boolean checkStateMedium() {
        String mediumReference = "";

        if (storyMedium.size() == 1) {
            mediumReference = storyMedium.get(0);
        } else {
            Log.e(this.type, "unexpected number of story medium references: " + storyMedium.size());
            return false;
        }

        String newState = storyPath.getReferencedValue(mediumReference);

        if ((newState != null) && (newState.equals(Constants.EXTERNAL))) {
            newState = storyPath.getExternalReferencedValue(mediumReference);
        }

        if (stateMedium != newState) {
            stateMedium = newState;
            return true;
        } else {
            return false;
        }
    }

    // NEW
    public boolean checkStateVideo() {
        int newState = 0;

        ArrayList<String> values = storyPath.getValues(videoClipCards);

        if (values == null) {
            newState = 0;
        } else {
            for (String s : values) {
                if ((s != null) && (s.length() > 0)) {
                    newState++;
                }
            }
        }

        if (stateVideo != newState) {
            stateVideo = newState;
            return true;
        } else {
            return false;
        }
    }

    // NEW
    public boolean checkStateAudio() {
        int newState = 0;

        ArrayList<String> values = storyPath.getValues(audioClipCards);

        if (values == null) {
            newState = 0;
        } else {
            for (String s : values) {
                if ((s != null) && (s.length() > 0)) {
                    newState++;
                }
            }
        }

        if (stateAudio != newState) {
            stateAudio = newState;
            return true;
        } else {
            return false;
        }
    }

    // NEW
    public boolean checkStatePhoto() {
        int newState = 0;

        ArrayList<String> values = storyPath.getValues(photoClipCards);

        if (values == null) {
            newState = 0;
        } else {
            for (String s : values) {
                if ((s != null) && (s.length() > 0)) {
                    newState++;
                }
            }
        }

        if (statePhoto != newState) {
            statePhoto = newState;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new MilestoneProgressCardView(context, this);
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
        addValue("value", (areWeSatisfied() ? "true" : "false"), true); // FIXME this should be in a more general init() method called on each card as the path is bootstrapped

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

        String medium = storyPath.getReferencedValue(mediumReference);

        if ((medium != null) && (medium.equals(Constants.EXTERNAL))) {
            medium = storyPath.getExternalReferencedValue(mediumReference);
        }

        ArrayList<String> values = new ArrayList<String>();

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return false;
        }
        else if (medium.equals(Constants.VIDEO)) {
            values = storyPath.getValues(videoClipCards);
        }
        else if (medium.equals(Constants.AUDIO)) {
            values = storyPath.getValues(audioClipCards);
        }
        else if (medium.equals(Constants.PHOTO)) {
            values = storyPath.getValues(photoClipCards);
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

        String medium = storyPath.getReferencedValue(mediumReference);

        if ((medium != null) && (medium.equals(Constants.EXTERNAL))) {
            medium = storyPath.getExternalReferencedValue(mediumReference);
        }

        ArrayList<String> values = new ArrayList<String>();

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return 0;
        }
        else if (medium.equals(Constants.VIDEO)) {
            values = storyPath.getValues(videoClipCards);
        }
        else if (medium.equals(Constants.AUDIO)) {
            values = storyPath.getValues(audioClipCards);
        }
        else if (medium.equals(Constants.PHOTO)) {
            values = storyPath.getValues(photoClipCards);
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

        String medium = storyPath.getReferencedValue(mediumReference);

        if ((medium != null) && (medium.equals(Constants.EXTERNAL))) {
            medium = storyPath.getExternalReferencedValue(mediumReference);
        }

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

    @Override
    public void copyText(Card card) {
        if (!(card instanceof MilestoneProgressCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF MilestoneProgressCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        MilestoneProgressCard castCard = (MilestoneProgressCard)card;

        this.title = castCard.getTitle();
        this.text = castCard.getText();
    }
}