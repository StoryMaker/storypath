package scal.io.liger.model;

import timber.log.Timber;

/**
 * Created by mnbogner on 2/19/15.
 */
public class ContentPackMetadata {

    String contentPackThumbnailPath; // path of actual thumbnail within content pack zip

    public String getContentPackThumbnailPath() {
        return contentPackThumbnailPath;
    }

    public void setContentPackThumbnailPath(String contentPackThumbnailPath) {
        this.contentPackThumbnailPath = contentPackThumbnailPath;
    }
}
