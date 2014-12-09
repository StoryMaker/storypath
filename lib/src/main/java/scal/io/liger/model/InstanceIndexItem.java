package scal.io.liger.model;

/**
 * Created by mnbogner on 12/8/14.
 */
public class InstanceIndexItem {

    // only save libraries?

    String instanceFilePath;   // static, key
    String storyTitle;         // watch/update/persist (what path/library field is this?)
    String storyDescription;   // watch/update/persist (what path/library field is this?)
    String storyType;          // static? (what path/library field is this?)
    String storyThumbnailPath; // watch/update/persist
    long storyCreationDate;    // static
    long storySaveDate;        // watch/update/persist? (this field would force an update to the index for every update to a path/library)

    public InstanceIndexItem() {

    }

    public InstanceIndexItem(String instanceFilePath, long storyCreationDate) {
        this.instanceFilePath = instanceFilePath;
        this.storyCreationDate = storyCreationDate;
    }

    public String getInstanceFilePath() {
        return instanceFilePath;
    }

    public void setInstanceFilePath(String instanceFilePath) {
        this.instanceFilePath = instanceFilePath;
    }

    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public String getStoryDescription() {
        return storyDescription;
    }

    public void setStoryDescription(String storyDescription) {
        this.storyDescription = storyDescription;
    }

    public String getStoryType() {
        return storyType;
    }

    public void setStoryType(String storyType) {
        this.storyType = storyType;
    }

    public String getStoryThumbnailPath() {
        return storyThumbnailPath;
    }

    public void setStoryThumbnailPath(String storyThumbnailPath) {
        this.storyThumbnailPath = storyThumbnailPath;
    }

    public long getStoryCreationDate() {
        return storyCreationDate;
    }

    public void setStoryCreationDate(long storyCreationDate) {
        this.storyCreationDate = storyCreationDate;
    }

    public long getStorySaveDate() {
        return storySaveDate;
    }

    public void setStorySaveDate(long storySaveDate) {
        this.storySaveDate = storySaveDate;
    }
}
