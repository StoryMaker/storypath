package scal.io.liger.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import scal.io.liger.Constants;
import scal.io.liger.MediaHelper;
import scal.io.liger.ZipHelper;

/**
 * Created by mnbogner on 9/29/14.
 */
public class ExampleMediaFile extends MediaFile implements Cloneable {

    private String exampleUri;

    public ExampleMediaFile() {
      // required for JSON/GSON
    }

    public ExampleMediaFile(String path, String medium) {
        this.path = path;
        this.medium = medium;

        // check for file existance?
    }

    public void loadThumbnail(@NonNull ImageView target) {
        loadThumbnail(target, null);
    }

    @Override
    public void loadThumbnail(@NonNull ImageView target,
                              @Nullable final MediaFileThumbnailCallback callback) {
        if (TextUtils.isEmpty(thumbnailFilePath)) {
            MediaHelper.displayExpansionMediaThumbnail(medium, // media type
                                                       path,   // media path within expansion item
                                                       null,   // Expansion item. null indicates we will guess based on media path
                                                       target, // ImageView to load into
                                                       new MediaHelper.ThumbnailCallback() {
                                                           @Override
                                                           public void thumbnailLoaded(File thumbnail) {
                                                               boolean newlyAssigned = thumbnailFilePath == null || !thumbnailFilePath.equals(thumbnail.getAbsolutePath());
                                                               thumbnailFilePath = thumbnail.getAbsolutePath();
                                                               if (callback != null && newlyAssigned) callback.newThumbnailAssigned(thumbnail);
                                                           }
                                                       });
        } else {
            Picasso.with(target.getContext())
                    .load(new File(thumbnailFilePath))
                    .into(target);
        }

    }

    // method to handle fetching thumbnails for example media files from zipped expansion file
//    public Bitmap getExampleThumbnail(Card card) { // TODO: disk cache, multiple sizes
//        Bitmap thumbnail = null;
//
//        Log.d(" *** TESTING *** ", "Get example thumbnail for " + path);
//
//        if (thumbnailFilePath == null) {
//            if (medium.equals(Constants.VIDEO)) {
//                try {
//                    Log.d(" *** TESTING *** ", "CREATING NEW THUMBNAIL FILE FOR " + path);
//
//                    // FIXME should not be stored in the source location, but a cache dir in our app folder on the sd or internal cache if there is no SD
//                    // FIXME need to check datestamp on original file to check if our thumbnail is up to date
//                    // FIXME this should be run from a background thread as it does disk access
//
//                    // local path will be relative
//                    // must build a complete path for the new thumbnail file
//                    // unsure of intent -> String[] tokens = path.split("\\.(?=[^\\.]+$)");
//                    //                     String newThumbnailName = tokens[0] + "_thumbnail.png";
//                    String newThumbnailName = path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf(".")) + "_thumbnail.png";
//
//                    Log.d(" *** TESTING *** ", "New thumbnail name " + newThumbnailName);
//
//                    String newThumbnailPath = card.getStoryPath().buildTargetPath(path.substring(0, path.lastIndexOf(File.separator) + 1) + newThumbnailName);
//                    File newThumbnailFile = new File(newThumbnailPath);
//
//                    Log.d(" *** TESTING *** ", "New thumbnail path " + newThumbnailFile.getPath());
//
//                    if (newThumbnailFile.exists()) {
//                        Log.d(" *** TESTING *** ", "Thumbnail file exists at " + newThumbnailFile.getPath());
//                        thumbnail = BitmapFactory.decodeFile(newThumbnailFile.getPath());
//                    } else {
//
//                        // need to create a temp file with content from zip file to pass to ThumbnailUtils
//                        String tempThumbnailPath = newThumbnailPath.substring(0, newThumbnailPath.lastIndexOf(File.separator));
//
//                        Log.d(" *** TESTING *** ", "Requesting temp file for " + path + " at " + tempThumbnailPath);
//
//                        File tempThumbnailFile = ZipHelper.getTempFile(path, tempThumbnailPath, card.getStoryPath().getContext());
//
//                        if (tempThumbnailFile == null) {
//                            return null;
//                        }
//
//                        FileOutputStream thumbnailStream = new FileOutputStream(newThumbnailFile);
//
//                        thumbnail = ThumbnailUtils.createVideoThumbnail(tempThumbnailFile.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
//                        thumbnail.compress(Bitmap.CompressFormat.PNG, 75, thumbnailStream); // FIXME make compression level configurable
//                        thumbnailStream.flush();
//                        thumbnailStream.close();
//                    }
//
//                    thumbnailFilePath = newThumbnailFile.getPath();
//
//                    //Log.d(" *** TESTING *** ", "THUMBNAIL FILE SAVED AS " + thumbnailFilePath);
//                } catch (IOException ioe) {
//                    //Log.d(" *** TESTING *** ", "EXCEPTION: " + ioe.getMessage());
//                    return null;
//                }
//            } else if (medium.equals(Constants.AUDIO)) {
//                // TODO create audio thumbnails
//            } else if (medium.equals(Constants.PHOTO)) {
//                // TODO return the image directly
//            } else {
//                Log.e(this.getClass().getName(), "can't create thumbnail file for " + path + ", unsupported medium: " + medium);
//            }
//        } else {
//            Log.d(" *** TESTING *** ", "Thumbnail already exists at " + thumbnailFilePath);
//            thumbnail = BitmapFactory.decodeFile(thumbnailFilePath);
//        }
//
//        return thumbnail;
//    }

    // method to handle fetching example media files from zipped expansion file
    @Deprecated
    public String getExampleURI(Card card) {
        if (exampleUri == null) {
            File media = null;

            Log.d(" *** TESTING *** ", "CREATING TEMP FILE FOR " + path);

            // local path will be relative
            // need to create a temp file with content from zip file to pass to ThumbnailUtils

            String tempMediaFile = card.getStoryPath().buildTargetPath(path);
            String tempMediaPath = tempMediaFile.substring(0, tempMediaFile.lastIndexOf(File.separator));

            Log.d(" *** TESTING *** ", "Temp file path " + tempMediaPath);

            media = ZipHelper.getTempFile(path, tempMediaPath, card.getStoryPath().getContext());

            if (media == null) {
                Log.e(" *** TESTING *** ", "No temp file for " + path);
                return null;
            } else {
                exampleUri = media.getPath();
            }
        }

        return exampleUri;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        ExampleMediaFile clone = (ExampleMediaFile) super.clone();

        return clone;
    }
}
