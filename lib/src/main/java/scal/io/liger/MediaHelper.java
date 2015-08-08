package scal.io.liger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;
import android.widget.ImageView;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import scal.io.liger.model.ExpansionIndexItem;
import scal.io.liger.view.AudioWaveform;

import static org.apache.commons.io.IOUtils.copy;

/**
 * Created by mnbogner on 7/14/14.
 */
public class MediaHelper {

    private static final String TAG = "MediaHelper";
    private static final String LIGER_DIR = "Liger";
    private static final boolean VERBOSE = false;

    private static final int DEFAULT_THUMB_WIDTH = 640;
    private static final int DEFAULT_THUMB_HEIGHT = 480;

    private static File selectedFile = null;
    private static ArrayList<File> fileList = null;
    // private static String sdLigerFilePath = null;

    /** Use @MediaType annotation to limit String argument to one of
     * Constants.VIDEO / AUDIO / PHOTO
     */
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Constants.VIDEO, Constants.AUDIO, Constants.PHOTO})
    public @interface MediaType {}

    /*
    static {
        setupFileStructure();
    }
    */

    /**
     * Callback to report when thumbnail requests resolve, as well as
     * when the request resulted in the generation of a new thumbnail file
     */
    public static abstract class ThumbnailCallback {
        /** Called when a thumbnail request requires generation of a new thumbnail */
        public void newThumbnailGenerated(File thumbnail) {}

        /** Called when a thumbnail request successfully resolves to a thumbnail file */
        public void thumbnailLoaded(File thumbnail) {}
    }

    public static File loadFileFromPath(String filePath, Context context) {

        // assume initial / indicates a non-relative path
        // (a relative path starting with / will break the code anyway)
        if (filePath.startsWith("/")) {
            File mediaFile = new File(filePath);
            return mediaFile;
        }

        // file location now handled by helper class

        /*
        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            System.out.println("GOT FILE: " + sdLigerFilePath + filePath);
            File mediaFile = new File(sdLigerFilePath + filePath);
            return mediaFile;
        } else {
            System.err.println("SD CARD NOT FOUND");
        }

        return null;
        */

        String sdLigerFilePath = getLigerFilePath(context);
        File mediaFile = new File(sdLigerFilePath + filePath);
        return mediaFile;
    }

    public static File loadFile() {
        if(null == selectedFile) {
            return null;
        }

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            return selectedFile;
        } else {
            System.err.println("SD CARD NOT FOUND");
        }

        return null;
    }

    private static String getLigerFilePath(Context context) {

        // file location now handled by helper class

        /*
        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            String sdCardFolderPath = Environment.getExternalStorageDirectory().getPath();
            sdLigerFilePath = sdCardFolderPath + File.separator + LIGER_DIR + File.separator;
        } else {
            System.err.println("SD CARD NOT FOUND");
        }
        */

        return StorageHelper.getActualStorageDirectory(context).getPath() + File.separator + LIGER_DIR + File.separator;
    }

    public static String[] getMediaFileList(Context context) {

        //ensure path has been set
        /*
        if(null == sdLigerFilePath) {
            return null;
        }
        */

        ArrayList<String> fileNamesList = new ArrayList<String>();
        fileList = new ArrayList<File>();

        // revisit this, perhaps take media type and return corresponding files?

        // File ligerDir = new File(sdLigerFilePath);
        File ligerDir = new File(getLigerFilePath(context));

        if (ligerDir != null) {
            for (File file : ligerDir.listFiles()) {
                if (file.getName().endsWith(".mp4")) {
                    fileNamesList.add(file.getName());
                    fileList.add(file);
                }
            }
        }

        // File defaultLigerDir = new File(sdLigerFilePath + "/default/");
        File defaultLigerDir = new File(getLigerFilePath(context) + "/default/");

        if (defaultLigerDir != null) {
            for (File file : defaultLigerDir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    fileNamesList.add(file.getName());
                    fileList.add(file);
                }
            }
        }

        return fileNamesList.toArray(new String[fileNamesList.size()]);
    }

    public static void setSelectedFile(int index) {
        selectedFile = fileList.get(index);
    }

    /**
     * Return a directory where audio recordings will be stored
     * @throws IOException
     * TODO Allow this to be configured by Liger client application
     */
    public static @Nullable File getAudioDirectory(Context context) throws IOException {
        File storageDirectory = new File(getLigerFilePath(context), "audio");
        recursiveCreateDirectory(storageDirectory);

        return storageDirectory;
    }

    /**
     * Return a directory where media thumbnails will be stored
     * @throws IOException
     */
    public static @NonNull File getThumbnailDirectory(Context context) throws IOException {
        File thumbDirectory = new File(getLigerFilePath(context), "thumbs");
        recursiveCreateDirectory(thumbDirectory);

        return thumbDirectory;
    }

    /**
     * Display a thumbnail for a given media asset contained in an {@link scal.io.liger.model.ExpansionIndexItem}.
     * These assets require special treatment because they are stored within a zipped archive.
     */
    public static void displayExpansionMediaThumbnail(@NonNull @MediaType final String mediaType,
                                                      @NonNull final String relativeExpansionPath,
                                                      @Nullable ExpansionIndexItem expansionItem,
                                                      @NonNull final ImageView target,
                                                      @Nullable final ThumbnailCallback callback) {

        final Context context = target.getContext();
        final ExpansionIndexItem targetExpansion = expansionItem == null ?
                ZipHelper.guessExpansionIndexItemForPath(relativeExpansionPath, context) :
                expansionItem;

        if (targetExpansion == null) {
            Log.w(TAG, String.format("Cannot display thumbnail for path %s. No ExpansionIndexItem provided and none could be guessed", relativeExpansionPath));
            return;
        }

        try {
            File thumbnail = getThumbnailFileForPathInExpansion(relativeExpansionPath, targetExpansion, target.getContext());
            if (thumbnail.exists()) displayImage(thumbnail, target);
            else {
                new AsyncTask<File, Void, File>() {

                    @Override
                    protected File doInBackground(File... params) {

                        File thumbFile = params[0];

                        switch (mediaType) {
                            case Constants.AUDIO:
                            case Constants.VIDEO:
                                // We can't currently generate video and audio thumbnails
                                // directly from the zip archive (N.B : We only have access to InputStream,
                                // not FileInputStream, from the zip archive. FileInputStream would
                                // give us access to a FileDescriptor, which we could use to generate
                                // a video / audio thumbnail without first copying the stream to file.

                                //File tempDirectory = context.getExternalFilesDir(null);
                                File tempDirectory = StorageHelper.getActualStorageDirectory(context);

                                File tempFile = ZipHelper.getTempFile(relativeExpansionPath,
                                                                      tempDirectory.getAbsolutePath(),
                                                                      context);
                                try {
                                    return generateMediaThumbnail(context, tempFile, thumbFile, mediaType);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case Constants.PHOTO:

                                InputStream thumbStream = ZipHelper.getFileInputStreamForExpansionAndPath(targetExpansion,
                                        relativeExpansionPath,
                                        context);
                                if (thumbStream != null) {
                                    Bitmap newThumbBitmap = decodeSampledBitmapFromInputStream(thumbStream,
                                            DEFAULT_THUMB_WIDTH,
                                            DEFAULT_THUMB_HEIGHT);

                                    if (newThumbBitmap == null) {
                                        Log.e(TAG, "Unable to generate thumbnail for " + relativeExpansionPath);
                                        return null;
                                    }

                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(thumbFile);
                                        newThumbBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                                        fos.close();
                                        return thumbFile;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(File result) {
                        if (result != null) displayImage(result, target);
                    }
                }.execute(thumbnail);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void displayImage(File image, ImageView target) {
        Picasso.with(target.getContext()).load(image).into(target);
    }

    public static void displayExpansionIndexItemThumbnail(@NonNull ExpansionIndexItem item,
                                                          @NonNull ImageView target) {

        try {
            File thumbnail = getThumbnailFileForExpansionItem(item, target.getContext());
            if (!thumbnail.exists()) {

                // get inpustream efficiently for ExpansionIndexItem thumbnail
                // TODO BG thread
                InputStream inputStream = ZipHelper.getThumbnailInputStreamForItem(item, target.getContext());
                if (inputStream == null) {
                    Log.w(TAG, "Unable to get inputstream for expansion item thumb: " + item.getThumbnailPath());
                    return;
                }

                Bitmap bitmap = decodeSampledBitmapFromInputStream(inputStream, DEFAULT_THUMB_WIDTH, DEFAULT_THUMB_HEIGHT);
                if (bitmap == null) {
                    Log.w(TAG, "Unable to generate thumbnail for expansion item thumb: " + item.getThumbnailPath());
                    return;
                }

                FileOutputStream fos = new FileOutputStream(thumbnail);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
            }

            Picasso.with(target.getContext()).load(thumbnail).into(target);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asynchronously load a a media file or content provider URI into an ImageView,
     * creating and saving the thumbnail if necessary.
     *
     * Development Note:
     * So long as Content Provider Uris are translatable to File addresses
     * let's maintain only the file-based thumnbail generation logic.
     * When the time comes that we have to deal with Streams, convert
     * {@link #getFileThumbnail(String, java.io.File, android.widget.ImageView, scal.io.liger.MediaHelper.ThumbnailCallback)} )}
     * to take an InputStream, instead of File, argument.
     *
     * Safe to call from the UI thread.
     */
    public static void displayMediaThumbnail(@NonNull @MediaType final String mediaType,
                                             @NonNull final String path,
                                             @NonNull final ImageView target,
                                             @Nullable final ThumbnailCallback callback) {

        new AsyncTask<Void, Void, File>() {

            private WeakReference<ImageView> weakView = new WeakReference<>(target);
            final Context context = target.getContext();

            @Override
            protected File doInBackground(Void... params) {
                String filePath = null;

                if (path.contains("://") && !path.contains("file://")) {

                    // path is a ContentProvider URI
                    Uri uri         = Uri.parse(path);
                    String mimeType = FileUtils.getMimeType(context, uri);
                    filePath        = FileUtils.getPath(context, uri);
                    if (VERBOSE) Log.d(TAG, String.format("media uri mime type %s path %s", mimeType, filePath));
                    // WARNING .mp4 audio files report mimetype video
                    if (!mimeType.contains("image") &&
                            !mimeType.contains("video") &&
                            !mimeType.contains("audio"))
                        Log.w(TAG, "Cannot display thumbnail. Unknown content url type " + path);

                } else {

                    // path is a file path
                    filePath = path.replace("file://", "");

                }

                if (filePath == null) {
                    Log.e(TAG, "Unable to get file path for " + path);
                    return null;
                }

                File mediaFile = new File(filePath);

                if (mediaFile.exists())
                    return getFileThumbnail(mediaType, mediaFile, target, callback);
                else
                    Log.w(TAG, "path appears to be a file, but it cannot be found on disk " + filePath);

                return null;
            }

            @Override
            protected void onPostExecute(File result) {
                ImageView target = weakView.get();
                if (target != null && result != null) {
                    Picasso.with(context).load(result).into(target);
                }
            }
        }.execute();
    }

    /**
     * Synchronously load a media file thumbnail into an ImageView, creating the thumbnail if necessary.
     * Must be called from background thread.
     */
    private static @Nullable File getFileThumbnail(@NonNull @MediaType final String mediaType,
                                                   @NonNull final File media,
                                                   @NonNull final ImageView target,
                                                   @Nullable final ThumbnailCallback callback) {
        try {
            final File thumbnailFile = getThumbnailFileForMediaFile(media, target.getContext());
            if (thumbnailFile.exists()) {
                if (callback != null) {
                    target.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.thumbnailLoaded(thumbnailFile);
                        }
                    });
                }
                return thumbnailFile;
            } else {
                final File newThumbnail = generateMediaThumbnail(target.getContext(), media, null, mediaType);
                if (callback != null) {
                    target.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.newThumbnailGenerated(newThumbnail);
                        }
                    });
                }
                return newThumbnail;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get or create a waveform bitmap for the given audio file.
     * Must be called on a background thread
     * @throws IOException
     */
    private static File getWaveformForAudioFile(@NonNull Context context, @NonNull File audio) throws IOException {
        File waveFormFile = getThumbnailFileForMediaFile(audio, context);
        if (!waveFormFile.exists()) {
            Bitmap waveform = AudioWaveform.createBitmap(context, audio.getAbsolutePath());

            if (waveform == null) {
                Log.e(TAG, "Failed to create audio waveform");
                return null;
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(waveFormFile);
                waveform.compress(Bitmap.CompressFormat.PNG, 100 /* No effect with PNG */, out);
                Log.d(TAG, "Generated waveform thumb " + waveFormFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return waveFormFile;
    }

    private static void recursiveCreateDirectory(@NonNull File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            // Directory does not exist and could not be created
            throw new IOException("Unable to create " + directory.getAbsolutePath());
        }
    }

    public static File getThumbnailFileForMediaFile(@NonNull File media, @NonNull Context context) throws IOException {
        return new File(getThumbnailDirectory(context), media.getName() + ".thumb");
    }

    public static File getThumbnailFileForExpansionItem(@NonNull ExpansionIndexItem item, @NonNull Context context) throws IOException {
        return new File(getThumbnailDirectory(context),
                        item.getExpansionId() + '_' + item.getExpansionFileVersion() + '_' +
                                item.getThumbnailPath().replace(File.separatorChar, '_') + ".thumb");
    }

    public static File getThumbnailFileForPathInExpansion(@NonNull String path,
                                                          @NonNull ExpansionIndexItem item,
                                                          @NonNull Context context) throws IOException {
        return new File(getThumbnailDirectory(context),
                item.getExpansionId() + '_' + item.getExpansionFileVersion() + '_' +
                        path.replace(File.separatorChar, '_') + ".thumb");
    }

    public static File getThumbnailFileForMediaUri(@NonNull Uri media, Context context) throws IOException {
        // Convert last Uri path segmenet to file name, allowing only alphanumeric and underscores.
        return new File(getThumbnailDirectory(context), media.getLastPathSegment().replaceAll("\\W+", "") + ".thumb");
    }

    /**
     * Display an appropriate loading image for the given media type
     */
    public static void displayLoadingIndicator(@NonNull @MediaType String mediaType,
                                                @NonNull ImageView target) {

        int resId = -1;
        switch (mediaType) {
            case Constants.AUDIO:
                resId = R.drawable.waveform_loading;
                break;
            case Constants.VIDEO:
            case Constants.PHOTO:
                resId = R.drawable.media_loading;
                break;
        }

        Picasso.with(target.getContext())
               .load(resId)
               .into(target);
    }

    /**
     * Generate a thumbnail for the given media file.
     * Must be called from a background thread.
     * TODO Allow custom sizes
     *
     * @param thumbnailFile an optional parameter specifying where a thumbnail should be generated.
     *                      Useful if the passed media file is only a temporary file representing an
     *                      asset availale only via an InputStream. In this case we should not locate
     *                      the thumbnail based on the location of the media file.
     */
    private static @Nullable File generateMediaThumbnail(@NonNull Context context,
                                                         @NonNull File media,
                                                         @Nullable File thumbnailFile,
                                                         @NonNull @MediaType String mediaType)
                                                         throws IOException {

        Bitmap thumbnail = null;

        switch (mediaType) {
            case Constants.AUDIO:
                return getWaveformForAudioFile(context, media);
            case Constants.VIDEO:
                thumbnail = ThumbnailUtils.createVideoThumbnail(media.getAbsolutePath(),
                                                                MediaStore.Images.Thumbnails.MINI_KIND);
                break;
            case Constants.PHOTO:
                thumbnail = decodeSampledBitmapFromFile(media, DEFAULT_THUMB_WIDTH, DEFAULT_THUMB_HEIGHT);
                break;
        }

        if (thumbnail != null) {
            if (thumbnailFile == null) thumbnailFile = getThumbnailFileForMediaFile(media, context);
            FileOutputStream thumbnailStream = new FileOutputStream(thumbnailFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 75, thumbnailStream); // FIXME make compression level configurable
            thumbnailStream.flush();
            thumbnailStream.close();
            Log.d(TAG, "Generated thumbnail at " + thumbnailFile.getAbsolutePath());
            return thumbnailFile;
        } else {
            Log.w(TAG, "Unable to generate thumbnail for " + media.getAbsolutePath());
        }
        return null;
    }


    private static @Nullable Bitmap decodeSampledBitmapFromFile(File media,
                                                                int reqWidth,
                                                                int reqHeight)
                                                                throws FileNotFoundException {

        // First decode with inJustDecodeBounds=true to check dimensions
        // this does not allocate any memory for image data
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(media.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(media.getAbsolutePath(), options);
    }

    private static @Nullable Bitmap decodeSampledBitmapFromInputStream(@NonNull InputStream media,
                                                                       int reqWidth,
                                                                       int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();

        // First decode with inJustDecodeBounds=true to check dimensions
        // this does not allocate any memory for image data
        if (media.markSupported()) {
            media.mark(Integer.MAX_VALUE);
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(media, null, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            try {
                media.reset();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to rewind InputStream. Could not generate thumbnail");
                return null;
            }
        } else {
            Log.w(TAG, "InputStream does not support marking. Thumbnail will be full size");
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(media, null, options);
    }

    /**
     * Calculating a scaling factor for loading a downsampled
     * Bitmap to be at least (reWidth x reqHeight)
     */
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}