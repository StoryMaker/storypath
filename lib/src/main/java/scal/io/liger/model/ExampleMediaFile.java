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
