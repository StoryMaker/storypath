package scal.io.liger.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.annotations.Expose;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import scal.io.liger.Constants;
import scal.io.liger.MediaHelper;
import scal.io.liger.R;
import scal.io.liger.Utility;
import scal.io.liger.view.AudioWaveform;

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

    public boolean loadThumbnail(Context context, @Nullable ImageView target) { // TODO: disk cache, multiple sizes
        try {

            if (thumbnailFilePath == null) {
            Uri uri = Uri.parse(getPath());
            String lastSegment = uri.getLastPathSegment();
            boolean isDocumentProviderUri = getPath().contains("content:/") && (lastSegment.contains(":"));
            if (medium.equals(Constants.VIDEO)) {
                // path of form : content://com.android.providers.media.documents/document/video:183
                if (isDocumentProviderUri) {
                    // An Android Document Provider URI. Thumbnail already generated

                    long id = 0;
                    id = Long.parseLong(lastSegment.split(":")[1]);
                    if (target != null) {
                        target.setImageBitmap(MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, null));
                        return true;
                    }
                } else {
                    // Regular old File path
                        //Log.d(" *** TESTING *** ", "CREATING NEW THUMBNAIL FILE FOR " + path);

                        // FIXME should not be stored in the source location, but a cache dir in our app folder on the sd or internal cache if there is no SD
                        // FIXME need to check datestamp on original file to check if our thumbnail is up to date
                        // FIXME this should be run from a background thread as it does disk access
                        if (path.contains("content:/")) {
                            path = Utility.getRealPathFromURI(context, uri);
                        }
                        File originalFile = new File(path);
                        String fileName = originalFile.getName();
                        String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                        thumbnailFilePath = path.substring(0, path.lastIndexOf(File.separator) + 1) + tokens[0] + "_thumbnail.png";
                        File thumbnailFile = new File(thumbnailFilePath);
                        if (thumbnailFile.exists()) {
                            if (target != null) {
                                Picasso.with(context).load(thumbnailFile).into(target);
                                return true;
                            }
                        } else {
                            FileOutputStream thumbnailStream = new FileOutputStream(thumbnailFile);

                            Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
                            if (thumbnail != null) {
                                thumbnail.compress(Bitmap.CompressFormat.PNG, 75, thumbnailStream); // FIXME make compression level configurable
                                thumbnailStream.flush();
                                thumbnailStream.close();
                                if (target != null) {
                                    Picasso.with(context).load(thumbnailFile).into(target);
                                    return true;
                                }
                            }
                        }

                        //Log.d(" *** TESTING *** ", "THUMBNAIL FILE SAVED AS " + thumbnailFilePath);
                }
            } else if (medium.equals(Constants.AUDIO)) {
                // For now don't allow pre-caching of thumbnails via calls to this method where target is null
                if (target != null) {
                    MediaHelper.displayWaveform(new File(getPath()), target);
                    return true;
                }
            } else if (medium.equals(Constants.PHOTO)) {
                if (isDocumentProviderUri) {
                    // path of form : content://com.android.providers.media.documents/document/video:183
                    // An Android Document Provider URI. Thumbnail already generated
                    // TODO Because we need Context we can't yet override this behavior at MediaFile#loadThumbnail
                    long id = Long.parseLong(Uri.parse(getPath()).getLastPathSegment().split(":")[1]);
                    if (target != null) {
                        target.setImageBitmap(MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, null));
                        return true;
                    }
                } else {
                    if (path.contains("content:/")) {
                        path = Utility.getRealPathFromURI(context, uri);
                    }
                    File originalFile = new File(path);
                    String fileName = originalFile.getName();
                    String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
                    thumbnailFilePath = path.substring(0, path.lastIndexOf(File.separator) + 1) + tokens[0] + "_thumbnail.png";
                    File thumbnailFile = new File(thumbnailFilePath);
                    if (thumbnailFile.exists()) {
                        Picasso.with(context).load(thumbnailFile).into(target);
                        return true;
                    } else {
                        Bitmap bitMap = BitmapFactory.decodeFile(path);
                        Picasso.with(context).load(thumbnailFile).into(target);

                        FileOutputStream thumbnailStream = new FileOutputStream(thumbnailFile);
                        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitMap, 400, 300); // FIXME figure out the real aspect ratio and size needed
                        thumbnail.compress(Bitmap.CompressFormat.PNG, 75, thumbnailStream); // FIXME make compression level configurable
                        thumbnailStream.flush();
                        thumbnailStream.close();
                        if (target != null) {
                            Picasso.with(context).load(thumbnailFile).into(target);
                            return true;
                        }
                    }
                }
            } else {
                Log.e(this.getClass().getName(), "can't create thumbnail file for " + path + ", unsupported medium: " + medium);
                if (target != null) {
                    Picasso.with(context).load(R.drawable.no_thumbnail).into(target);
                    return true;
                }
            }
        } else {
            //Log.d(" *** TESTING *** ", "LOADING THUMBNAIL FILE FOR " + path);
            if (target != null) {
                // If not protocol in String url, assume file.
                if (!thumbnailFilePath.contains("://")) thumbnailFilePath = "file://" + thumbnailFilePath;
                Picasso.with(context).load(thumbnailFilePath).into(target);
                return true;
            }
                //Log.d(" *** TESTING *** ", "LOADED THUMBNAIL FROM FILE " + thumbnailFilePath);
        }

        } catch (IOException ioe) {
            Log.w(TAG, "Error retrieving thumnail");
            //Log.d(" *** TESTING *** ", "EXCEPTION: " + ioe.getMessage());
        }
        return false;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MediaFile clone = new MediaFile(this.path, this.medium);
        clone.thumbnailFilePath = this.thumbnailFilePath;

        return clone;
    }
}
