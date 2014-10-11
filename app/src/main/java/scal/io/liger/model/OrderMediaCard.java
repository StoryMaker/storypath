package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.view.OrderMediaCardView;


public class OrderMediaCard extends Card {

    private String header;
    private String medium;
    private ArrayList<String> storyMedium; // why is this an array?
    private ArrayList<String> videoClipCards;
    private ArrayList<String> audioClipCards;
    private ArrayList<String> photoClipCards;

    public OrderMediaCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
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

        medium = storyPathReference.getReferencedValue(mediumReference);

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