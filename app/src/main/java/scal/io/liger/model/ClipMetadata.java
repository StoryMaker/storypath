package scal.io.liger.model;

/**
 * Created by mnbogner on 9/29/14.
 */
public class ClipMetadata {

    private int start_time;
    private int stop_time;
    private int volume;
    private String effect;
    private String type;
    private String uuid; // key to mediaFiles map in StoryModel

    public ClipMetadata() {
        // required for JSON/GSON
    }

    public ClipMetadata(String type, String uuid) {
        this.type = type;
        this.uuid = uuid;
    }

    public int getStart_time() {
        return start_time;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    public int getStop_time() {
        return stop_time;
    }

    public void setStop_time(int stop_time) {
        this.stop_time = stop_time;
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
