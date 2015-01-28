package scal.io.liger;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import scal.io.liger.view.AudioLevelView;
import scal.io.liger.view.AudioWaveform;

/**
 * Created by mnbogner on 7/14/14.
 */
public class MediaHelper {

    private static final String LIGER_DIR = "Liger";
    private static File selectedFile = null;
    private static ArrayList<File> fileList = null;
    private static String sdLigerFilePath = null;

    public static File loadFileFromPath(String filePath) {

        // assume initial / indicates a non-relative path
        // (a relative path starting with / will break the code anyway)
        if (filePath.startsWith("/")) {
            File mediaFile = new File(filePath);
            return mediaFile;
        }

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            System.out.println("GOT FILE: " + sdLigerFilePath + filePath);
            File mediaFile = new File(sdLigerFilePath + filePath);
            return mediaFile;
        } else {
            System.err.println("SD CARD NOT FOUND");
        }

        return null;
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

    public static void setupFileStructure(Context context) {
        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            String sdCardFolderPath = Environment.getExternalStorageDirectory().getPath();
            sdLigerFilePath = sdCardFolderPath + File.separator + LIGER_DIR + File.separator;
        } else {
            System.err.println("SD CARD NOT FOUND");
        }
    }

    public static String[] getMediaFileList() {
        //ensure path has been set
        if(null == sdLigerFilePath) {
            return null;
        }

        ArrayList<String> fileNamesList = new ArrayList<String>();
        fileList = new ArrayList<File>();

        // revisit this, perhaps take media type and return corresponding files?
        File ligerDir = new File(sdLigerFilePath);
        if (ligerDir != null) {
            for (File file : ligerDir.listFiles()) {
                if (file.getName().endsWith(".mp4")) {
                    fileNamesList.add(file.getName());
                    fileList.add(file);
                }
            }
        }

        File defaultLigerDir = new File(sdLigerFilePath + "/default/");
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
    public static @Nullable File getAudioStorageDirectory() throws IOException {
        File storageDirectory = new File(sdLigerFilePath, "audio");
        recursiveCreateDirectory(storageDirectory);

        return storageDirectory;
    }

    /**
     * Return a directory where audio waveforms will be stored
     * @throws IOException
     */
    public static @Nullable File getAudioThumbnailDirectory() throws IOException {
        File thumbDirectory = new File(getAudioStorageDirectory(), "waveforms");
        recursiveCreateDirectory(thumbDirectory);

        return thumbDirectory;
    }

    /**
     * Asynchronously load a waveform onto an ImageView. Safe to call from UI thread.
     */
    public static void displayWaveform(@NonNull final File audio, @NonNull final ImageView target) {
        if (!getWaveformFileForAudioFile(audio).exists()) {
            // If we're going to have to generate the waveform
            // set a loading indicator
            target.setImageResource(R.drawable.waveform_loading);
        }

        new AsyncTask<Void, Void, File>() {

            WeakReference<ImageView> weakView = new WeakReference<>(target);

            @Override
            protected File doInBackground(Void... params) {
                ImageView target = weakView.get();
                if (target != null) {
                    try {
                        return getWaveformForAudioFile(target.getContext(), audio);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(File result) {
                ImageView target = weakView.get();
                if (result != null && target != null) {
                    Picasso.with(target.getContext()).load(result).into(target);
                }
            }
        }.execute();
    }

    /**
     * Get or create a waveform bitmap for the given audio file. Should be called on a background thread
     * @throws IOException
     */
    public static File getWaveformForAudioFile(@NonNull Context context, @NonNull File audio) throws IOException {
        File waveFormFile = getWaveformFileForAudioFile(audio);
        if (!waveFormFile.exists()) {
            Bitmap waveform = AudioWaveform.createBitmap(context, audio.getAbsolutePath());

            if (waveform == null) {
                Log.e("getWaveform", "Failed to create audio waveform");
                return null;
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(waveFormFile);
                waveform.compress(Bitmap.CompressFormat.PNG, 100 /* No effect with PNG */, out);
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

    private static File getWaveformFileForAudioFile(@NonNull File audio) {
        return new File(audio.getAbsolutePath() + ".waveform");
    }
}