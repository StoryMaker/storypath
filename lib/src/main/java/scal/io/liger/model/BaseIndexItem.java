package scal.io.liger.model;

import android.text.TextUtils;

import java.io.File;
import java.util.Date;

/**
 * Created by mnbogner on 2/19/15.
 */
public class BaseIndexItem implements Comparable {

    String title;
    String description;
    String thumbnailPath;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public long getLastModifiedTime() {
        return 0;
    }

    @Override
    public int compareTo(Object another) {
        return -1; // Return "older" if no date available
    }
}
