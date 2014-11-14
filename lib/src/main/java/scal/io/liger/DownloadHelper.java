package scal.io.liger;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by mnbogner on 11/6/14.
 */
public class DownloadHelper {

    public static boolean checkExpansionFiles(Context context, String mainOrPatch, int version) {
        String expansionFilePath = ZipHelper.getExpansionFileFolder(context, mainOrPatch, version);

        if (expansionFilePath != null) {
            Log.d("DOWNLOAD", "EXPANSION FILE " + ZipHelper.getExpansionZipFilename(context, mainOrPatch, version) + " FOUND IN " + expansionFilePath);
            return true;
        } else {
            Log.d("DOWNLOAD", "EXPANSION FILE " + ZipHelper.getExpansionZipFilename(context, mainOrPatch, version) + " NOT FOUND");
            return false;
        }
    }

    public static void checkAndDownload(Context context) {

        if (checkExpansionFiles(context, Constants.MAIN, Constants.MAIN_VERSION)) {
            Log.d("DOWNLOAD", "MAIN EXPANSION FILE FOUND (NO DOWNLOAD)");
        } else {
            Log.d("DOWNLOAD", "MAIN EXPANSION FILE NOT FOUND (DOWNLOADING)");

            final LigerDownloadManager mainDownload = new LigerDownloadManager(Constants.MAIN, Constants.MAIN_VERSION, context, true);
            Thread mainDownloadThread = new Thread(mainDownload);

            Toast.makeText(context, "Starting download of content pack.", Toast.LENGTH_LONG).show(); // FIXME move to strings

            mainDownloadThread.start();
        }

        if (Constants.PATCH_VERSION > 0) {
            if (checkExpansionFiles(context, Constants.PATCH, Constants.PATCH_VERSION)) {
                Log.d("DOWNLOAD", "PATCH EXPANSION FILE FOUND (NO DOWNLOAD)");
            } else {
                Log.d("DOWNLOAD", "PATCH EXPANSION FILE NOT FOUND (DOWNLOADING)");

                final LigerDownloadManager patchDownload = new LigerDownloadManager(Constants.PATCH, Constants.PATCH_VERSION, context, true);
                Thread patchDownloadThread = new Thread(patchDownload);

                Toast.makeText(context, "Starting download of path for content pack.", Toast.LENGTH_LONG).show(); // FIXME move to strings

                patchDownloadThread.start();
            }
        }
    }
}
