package scal.io.liger.model;

/**
 * Created by mnbogner on 9/29/14.
 */
public class ClipMetadata {

    public int startTime;
    public int stopTime;
    public int volume;
    public String effect;
    public String type;
    public String uuid; // key to mediaFiles map in StoryModel

}
