package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;
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

    public void setNarration(MediaFile narrationMediaFile) {
        ClipMetadata cmd = new ClipMetadata(Constants.NARRATION, UUID.randomUUID().toString());

        getStoryPath().saveMediaFile(cmd.getUuid(), narrationMediaFile);
        setNarration(cmd);
    }

    public void setNarration(ClipMetadata narrationClip) {
        setNarration(narrationClip, true);
    }

    public void setNarration(ClipMetadata clip, boolean notify) {
        this.narration = clip;

        // send notification that a narration has been saved so that cards will be refreshed
        if (notify) {
            setChanged();
            notifyObservers();
        }
    }

    public MediaFile getSelectedNarrationFile(){
        if (narration == null) {
            Log.e(this.getClass().getName(), "no narration metadata was found, cannot get file");
            return null;
        }
        return loadMediaFile(narration);
    }

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