package scal.io.liger.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.google.gson.annotations.Expose;

import java.io.File;

import scal.io.liger.MediaHelper;

/**
 * Created by mnbogner on 9/29/14.
 */
public class MediaFile implements Cloneable {
    private static final String TAG = "MediaFile";

    @Expose protected String path;
    @Expose protected String medium; // mime type?
    @Expose protected String thumbnailFilePath;

    public MediaFile() {
        // required for JSON/GSON
    }

    public MediaFile(String path, String medium) {
        this.path = path;
        this.medium = medium;

        // check for file existance?
    }

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

    public String getThumbnailFilePath() {
        return thumbnailFilePath;
    }

    /**
     * Load a thumbnail representation of this MediaFile into the target ImageView.
     * TODO : Disk cache, multiple sizes
     */
    public void loadThumbnail(@NonNull ImageView target) {

        MediaHelper.displayMediaThumbnail(medium, getPath(), target,

                new MediaHelper.ThumbnailCallback() {
                    @Override
                    public void newThumbnailGenerated(File thumbnail) {
                        thumbnailFilePath = thumbnail.getAbsolutePath();
                    }
                }
        );
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MediaFile clone = new MediaFile(this.path, this.medium);
        clone.thumbnailFilePath = this.thumbnailFilePath;

        return clone;
    }
}
