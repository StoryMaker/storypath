package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.UUID;

import scal.io.liger.Constants;
import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.ReviewCardView;

public class ReviewCard extends GenericCard {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private ClipMetadata narration;

    public ReviewCard() {
        super();
        this.type = this.getClass().getName();
    }

    /**
     * If no references have been specified by the json model,
     * assume that this ReviewCard should monitor all ClipCards
     * in its StoryPath.
     */
    @Override
    public void registerObservers() {
        if (references == null) references = new ArrayList<>();
        if (references.size() == 0 && getStoryPath() != null) {
            List<ClipCard> clipCards = getStoryPath().gatherCardsOfClass(ClipCard.class);
            for (ClipCard card : clipCards) {
                references.add(
                        String.format("%s::%s::%s", getStoryPath().getId(),
                                                    card.getId(), "clips"));
                //Log.d(TAG, "Adding reference to clipcard: " + references.get(references.size()-1));
            }
        }
        super.registerObservers();
    }

    @Override
    public void update(Observable observable, Object o) {
        if (!(observable instanceof Card)) {
            Log.e(TAG, "update notification received from non-card observable");
            return;
        }
        if (storyPath == null) {
            Log.e(TAG, "STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
            return;
        }

        if (checkVisibilityChanged() ||
            observable instanceof ClipCard) {
            // ReviewCard needs to update its view if any ClipCards are changed
            // e.g: Clip added, deleted, or new primary clip selected
            storyPath.notifyCardChanged(this);
        }
    }

    public boolean checkReferencedValues() {
        // This prescribes visibility, and ReviewCard should be visible
        // even when no Clips are added to any ClipCards it references
        return true;
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new ReviewCardView(context, this);
    }

    public String getMediaPath() { return fillReferences(mediaPath); }

    public void setMediaPath(String mediaPath) { this.mediaPath = mediaPath; }

    /**
     * Use {@link StoryPathLibrary#saveNarrationAudioClip(AudioClip, MediaFile)}
     * to register a new Narration AudioClip with the StoryPathLibrary
     */
    @Deprecated
    public void setNarration(MediaFile narrationMediaFile) {
        ClipMetadata cmd = new ClipMetadata(Constants.NARRATION, UUID.randomUUID().toString());

        getStoryPath().saveMediaFile(cmd.getUuid(), narrationMediaFile);
        setNarration(cmd);
    }

    /**
     * Use {@link StoryPathLibrary#saveNarrationAudioClip(AudioClip, MediaFile)}
     * to register a new Narration AudioClip with the StoryPathLibrary
     */
    @Deprecated
    public void setNarration(ClipMetadata narrationClip) {
        setNarration(narrationClip, true);
    }

    /**
     * Use {@link StoryPathLibrary#saveNarrationAudioClip(AudioClip, MediaFile)}
     * to register a new Narration AudioClip with the StoryPathLibrary
     */
    @Deprecated
    public void setNarration(ClipMetadata clip, boolean notify) {
        this.narration = clip;

        // send notification that a narration has been saved so that cards will be refreshed
        if (notify) {
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Use {@link scal.io.liger.model.StoryPathLibrary#getAudioClips()}
     * to get Narration AudioClip with the StoryPathLibrary
     */
    @Deprecated
    public MediaFile getSelectedNarrationFile(){
        if (narration == null) {
            Log.e(this.getClass().getName(), "no narration metadata was found, cannot get file");
            return null;
        }
        return loadMediaFile(narration);
    }

    /**
     * Use {@link scal.io.liger.model.StoryPathLibrary#getAudioClips()}
     * to get Narration AudioClip with the StoryPathLibrary
     */
    @Deprecated
    public ClipMetadata getSelectedNarrationClip() {
        if (narration == null) {
            Log.e(this.getClass().getName(), "no narration metadata was found, cannot get file");
            return null;
        }

        return narration;
    }

    public MediaFile loadMediaFile(ClipMetadata cmd) {
        return getStoryPath().loadMediaFile(cmd.getUuid());
    }

    @Override
    public void copyText(Card card) {
        if (!(card instanceof ReviewCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF ReviewCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        ReviewCard castCard = (ReviewCard)card;

        this.title = castCard.getTitle();
        this.header = castCard.getHeader();
        this.text = castCard.getText();
    }
}