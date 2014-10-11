package scal.io.liger.model;

/**
 * Created by mnbogner on 10/10/14.
 */
public class FullMetadata {

    // class used for export, not serialized

    private int startTime;
    private int stopTime;
    private int volume;
    private String effect;
    private String type;
    private String medium;
    private String filePath;

    public FullMetadata (ClipMetadata cm, MediaFile mf) {
        this.startTime = cm.getStartTime();
        this.stopTime = cm.getStopTime();
        this.volume = cm.getVolume();
        this.effect = cm.getEffect();
        this.type = cm.getType();
        this.medium = mf.getMedium();
        this.filePath = mf.getPath();
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

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
