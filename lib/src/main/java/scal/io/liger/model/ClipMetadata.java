package scal.io.liger.model;

import timber.log.Timber;

import com.google.gson.annotations.Expose;

/**
 * Created by mnbogner on 9/29/14.
 */
public class ClipMetadata {

    @Expose private int startTime;
    @Expose private int stopTime; // TODO Possible to initialize this to the clip duration? It's a pain to do checks for stopTime == 0 all over the place to check if the value is valid.
    @Expose private float volume;
    @Expose private String effect;
    @Expose private String type; // e.g: "action", "person"
    @Expose private String uuid; // key to mediaFiles map in StoryModel

    public ClipMetadata() {
        // required for JSON/GSON
        this.volume = 1f; // Sensible default
    }

    public ClipMetadata(String type, String uuid) {
        this.type = type;
        this.uuid = uuid;
        this.volume = 1f;
    }

    public ClipMetadata(ClipMetadata right) {
        this.startTime = right.startTime;
        this.stopTime = right.stopTime;
        this.volume = right.volume;
        this.effect = right.effect;
        this.type = right.type;
        this.uuid = right.uuid;
    }

    public Object clone() throws CloneNotSupportedException {
        return new ClipMetadata( this );
    }

    /**
     * @return the Clip start time in milliseconds
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * Set the Clip start time in milliseconds
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the Clip stop time in milliseconds
     */
    public int getStopTime() {
        return stopTime;
    }

    /**
     * Set the Clip stop time in milliseconds
     */
    public void setStopTime(int stopTime) {
        this.stopTime = stopTime;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
