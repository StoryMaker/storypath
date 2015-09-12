package scal.io.liger.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.util.Date;

/**
 * Created by mnbogner on 2/19/15.
 */
public class BaseIndexItem  implements Comparable {

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

    @Override
    public int compareTo(@NonNull Object another) {
        return 0; // no basis for comparison
    }
}
