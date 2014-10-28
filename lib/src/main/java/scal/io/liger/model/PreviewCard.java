package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Observable;

import scal.io.liger.Constants;
import scal.io.liger.ReferenceHelper;
import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.PreviewCardView;

public class PreviewCard extends Card {

    @Expose private String text;
    @Expose private ArrayList<String> storyMedium; // why is this an array?
    @Expose private ArrayList<String> videoClipCards;
    @Expose private ArrayList<String> audioClipCards;
    @Expose private ArrayList<String> photoClipCards;

    public PreviewCard() {
        super();
        this.type = this.getClass().getName();
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
                Card card = storyPathReference.getCardById(reference);
                card.addObserver(this);
            }
        }
        if (videoClipCards != null) {
            for (String reference : videoClipCards) {
                Card card = storyPathReference.getCardById(reference);
                card.addObserver(this);
            }
        }
        if (audioClipCards != null) {
            for (String reference : audioClipCards) {
                Card card = storyPathReference.getCardById(reference);
                card.addObserver(this);
            }
        }
        if (photoClipCards != null) {
            for (String reference : photoClipCards) {
                Card card = storyPathReference.getCardById(reference);
                card.addObserver(this);
            }
        }
    }

    // NEW
    @Override
    public void removeObservers() {
        super.removeObservers();

        if (storyMedium != null) {
            for (String reference : storyMedium) {
                Card card = storyPathReference.getCardById(reference);
                card.deleteObserver(this);
            }
        }
        if (videoClipCards != null) {
            for (String reference : videoClipCards) {
                Card card = storyPathReference.getCardById(reference);
                card.deleteObserver(this);
            }
        }
        if (audioClipCards != null) {
            for (String reference : audioClipCards) {
                Card card = storyPathReference.getCardById(reference);
                card.deleteObserver(this);
            }
        }
        if (photoClipCards != null) {
            for (String reference : photoClipCards) {
                Card card = storyPathReference.getCardById(reference);
                card.deleteObserver(this);
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
        if (storyPathReference == null) {
            Log.e(this.getClass().getName(), "STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
            return;
        }

        Card card = (Card)observable;

        if (checkVisibilityChanged() ||
            checkStateMedium() ||
            checkStateVideo() ||
            checkStateAudio() ||
            checkStatePhoto()) {
            storyPathReference.notifyCardChanged(this);
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

        String newState = storyPathReference.getReferencedValue(mediumReference);

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

        ArrayList<String> values = ReferenceHelper.getValues(storyPathReference, videoClipCards);

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

        ArrayList<String> values = ReferenceHelper.getValues(storyPathReference, audioClipCards);

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

        ArrayList<String> values = ReferenceHelper.getValues(storyPathReference, photoClipCards);

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
        return new PreviewCardView(context, this);
    }

    public String getText() { return fillReferences(this.text); }

    public void setText(String text) { this.text = text; }

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

    public ArrayList<String> getClipPaths() {
        ArrayList<String> clipPaths = new ArrayList<String>();

        String mediumReference = "";

        if (storyMedium.size() == 1) {
            mediumReference = storyMedium.get(0);
        }
        else {
            Log.e(this.type, "unexpected number of story medium references: " + storyMedium.size());
            return clipPaths;
        }

        String medium = storyPathReference.getReferencedValue(mediumReference);

        if ((medium == null) || (medium.length() == 0 )) {
            Log.e(this.type, "no value found for story medium referenced by " + mediumReference);
            return clipPaths;
        }
        else if (medium.equals(Constants.VIDEO)) {
            clipPaths.addAll(videoClipCards);
        }
        else if (medium.equals(Constants.AUDIO)) {
            clipPaths.addAll(audioClipCards);
        }
        else if (medium.equals(Constants.PHOTO)) {
            clipPaths.addAll(photoClipCards);
        }

        return  clipPaths;
    }
}