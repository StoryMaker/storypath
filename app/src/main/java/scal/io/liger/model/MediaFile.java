package scal.io.liger.model;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import scal.io.liger.Constants;

/**
 * Created by mnbogner on 9/29/14.
 */
public class MediaFile {

    private String path;
    private String medium; // mime type?
    private Bitmap thumbnail;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public Bitmap getThumbnail() { // todo: disk cache, multiple sizes
        if (thumbnail == null) {
            if (medium == Constants.VIDEO) {
                thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
            } else {
                Log.e(this.getClass().getName(), "unsupported medium: " + medium);
            }
        }

        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

}
