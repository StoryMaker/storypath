package scal.io.liger.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import scal.io.liger.Constants;

/**
 * Created by mnbogner on 9/29/14.
 */
public class MediaFile {

    private String path;
    private String medium; // mime type?
    private String thumbnailFilePath;

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

    public void setThumbnailFilePath(String thumbnailFilePath) {
        this.thumbnailFilePath = thumbnailFilePath;
    }

    public Bitmap getThumbnail() { // todo: disk cache, multiple sizes
        Bitmap thumbnail = null;

        if (thumbnailFilePath == null) {
            if (medium.equals(Constants.VIDEO)) {
                try {
                    thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
                    //Log.d(" *** TESTING *** ", "CREATING NEW THUMBNAIL FILE FOR " + path);

                    String thumbnailString = path.substring(0, path.lastIndexOf(File.separator) + 1) + UUID.randomUUID().toString() + ".png";
                    File thumbnailFile = new File(thumbnailString);
                    FileOutputStream thumbnailStream = new FileOutputStream(thumbnailFile);

                    thumbnail.compress(Bitmap.CompressFormat.PNG, 75, thumbnailStream);
                    thumbnailStream.flush();
                    thumbnailStream.close();

                    thumbnailFilePath = thumbnailString;
                    //Log.d(" *** TESTING *** ", "THUMBNAIL FILE SAVED AS " + thumbnailFilePath);
                } catch (IOException ioe) {
                    //Log.d(" *** TESTING *** ", "EXCEPTION: " + ioe.getMessage());
                    return null;
                }
            } else {
                Log.e(this.getClass().getName(), "can't create thumbnail file for " + path + ", unsupported medium: " + medium);
            }
        } else {
            //Log.d(" *** TESTING *** ", "LOADING THUMBNAIL FILE FOR " + path);
            thumbnail = BitmapFactory.decodeFile(thumbnailFilePath);
            //Log.d(" *** TESTING *** ", "LOADED THUMBNAIL FROM FILE " + thumbnailFilePath);
        }

        return thumbnail;
    }
}
