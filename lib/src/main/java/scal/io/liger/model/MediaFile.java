package scal.io.liger.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.google.gson.annotations.Expose;
import com.squareup.picasso.Picasso;

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

    /**
     * Callback to report when a new thumbnail is assigned to this MediaFile
     */
    public static interface MediaFileThumbnailCallback {
        public void newThumbnailAssigned(File newThumbnail);
    }

    public MediaFile() {
        // required for JSON/GSON
    }

    public MediaFile(@NonNull String path, @NonNull String medium) {
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

    public void setThumbnailFilePath(String thumbnailFilePath) {
        this.thumbnailFilePath = thumbnailFilePath;
    }

    /**
     * Load a thumbnail representation of this MediaFile into the target ImageView,
     * creating the thumbnail if necessary.
     * TODO : multiple sizes?
     */
    public void loadThumbnail(@NonNull ImageView target) {
        loadThumbnail(target, null);
    }

    /**
     * Load a thumbnail representation of this MediaFile into the target ImageView,
     * creating the thumbnail if necessary.
     * TODO : multiple sizes?
     */
    public void loadThumbnail(@NonNull ImageView target,
                              @Nullable final MediaFileThumbnailCallback callback) {

        if (TextUtils.isEmpty(thumbnailFilePath)) {

            MediaHelper.displayLoadingIndicator(medium, target);

            MediaHelper.displayMediaThumbnail(medium, getPath(), target,

                    new MediaHelper.ThumbnailCallback() {

                        // we more or less want to do the same thing whether the thumbnail is loaded or created

                        @Override
                        public void newThumbnailGenerated(File thumbnail) {
                            boolean newlyAssigned = thumbnailFilePath == null || !thumbnailFilePath.equals(thumbnail.getAbsolutePath());
                            thumbnailFilePath = thumbnail.getAbsolutePath();

                            if (callback != null && newlyAssigned) callback.newThumbnailAssigned(thumbnail);
                        }

                        @Override
                        public void thumbnailLoaded(File thumbnail) {
                            boolean newlyAssigned = thumbnailFilePath == null || !thumbnailFilePath.equals(thumbnail.getAbsolutePath());
                            thumbnailFilePath = thumbnail.getAbsolutePath();

                            if (callback != null && newlyAssigned) callback.newThumbnailAssigned(thumbnail);
                        }
                    }
            );
        } else {
            Picasso.with(target.getContext())
                   .load(new File(thumbnailFilePath))
                   .into(target);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MediaFile clone = new MediaFile(this.path, this.medium);
        clone.thumbnailFilePath = this.thumbnailFilePath;

        return clone;
    }
}
