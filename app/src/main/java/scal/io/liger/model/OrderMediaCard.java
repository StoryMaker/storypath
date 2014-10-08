package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.view.OrderMediaCardView;


public class OrderMediaCard extends Card {

    private String header;
    private String medium;
    private ArrayList<String> story_medium; // why is this an array?
    private ArrayList<String> video_clip_cards;
    private ArrayList<String> audio_clip_cards;
    private ArrayList<String> photo_clip_cards;

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

        return  clipPaths;
    }
}