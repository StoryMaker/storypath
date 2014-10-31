package scal.io.liger.model;

import com.google.gson.annotations.Expose;

/**
 * Created by mnbogner on 9/29/14.
 */
public class ClipMetadata {

    @Expose private int startTime;
    @Expose private int stopTime; // TODO Possible to initialize this to the clip duration? It's a pain to do checks for stopTime == 0 all over the place to check if the value is valid.
    @Expose private int volume;
    @Expose private String effect;
    @Expose private String type; // e.g: "action", "person"
    @Expose private String uuid; // key to mediaFiles map in StoryModel

    public ClipMetadata() {
        // required for JSON/GSON
    }

    public ClipMetadata(String type, String uuid) {
        this.type = type;
        this.uuid = uuid;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getStopTime() {
        return stopTime;
    }

    public void setStopTime(int stopTime) {
        this.stopTime = stopTime;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
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
