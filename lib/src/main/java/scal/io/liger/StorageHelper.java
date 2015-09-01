package scal.io.liger;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.IOException;

/**
 * Created by mnbogner on 8/6/15.
 */
public class StorageHelper {

    public static final String KEY_USE_INTERNAL = "p_use_internal_storage";

    @Nullable
    public static File getActualStorageDirectory(Context context) {

        // locate actual external storage path if available

        File returnValue = null;

        // values for debugging
        int storageState = 0;
        int storageCount = 0;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // can't use this method for older versions, just use existing method

            // Log.d("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", USING OLD METHOD: " + context.getExternalFilesDir(null).getPath());

            storageState = 1;

            returnValue = context.getExternalFilesDir(null);

        } else {
            // use new method to get all directories, only the first directory should be internal storage
            File[] externalFilesDirs = context.getExternalFilesDirs(null);
            storageCount = externalFilesDirs.length;
            /// FIXME what if internal is null?  try to use external even though they said useIinternal
            returnValue = externalFilesDirs[0]; // FIXME default our returnValue to the internal so if we fail finding a more appropriate place this is our fallback
            storageState = 2; // FIXME just for debugging now

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            boolean useInternal = settings.getBoolean(KEY_USE_INTERNAL, false);
            if (useInternal) {
                storageState = 4;
            } else {
                if (externalFilesDirs.length > 1) {
                    // iterate over storage options in case one or more is unavailable
                    int i = 1;
                    while (i < externalFilesDirs.length) {
                        if (externalFilesDirs[i] != null) {
                            storageState = 3;
                            returnValue = externalFilesDirs[i];
                            break; // FIXME using the first available card, if we detect more than one we should probably let the user pick which one?
                        }
                        i++;
                    }
                }
            }
        }

        if (returnValue == null) {
            Log.e("STORAGE_ERROR", "EXTERNAL FILES DIRECTORY IS NULL (STORAGE IS UNAVAILABLE)");

            switch (storageState) {
                case 1:
                    Log.e("STORAGE_ERROR", "PRE-JELLYBEAN BUILD " + Build.VERSION.SDK_INT + " FOUND SO INTERNAL STORAGE MUST BE USED");
                    break;
                case 2:
                    Log.e("STORAGE_ERROR", storageCount + " EXTERNAL STORAGE OPTIONS FOUND BUT USER SELECTED INTERNAL STORAGE");
                    break;
                case 3:
                    Log.e("STORAGE_ERROR", storageCount + " EXTERNAL STORAGE OPTIONS FOUND AND USER SELECTED EXTERNAL STORAGE");
                    break;
                case 4:
                    Log.e("STORAGE_ERROR", storageCount + " EXTERNAL STORAGE OPTIONS FOUND SO INTERNAL STORAGE MUST BE USED");
                    break;
                default:
                    Log.e("STORAGE_ERROR", "UNEXPECTED STATE");
                    break;
            }
        }

        return returnValue;
    }

    public static String fixPath (String currentPath, Context context) {

        // instances and indexes may store full paths, need to compare and update

        String actualPath = getActualStorageDirectory(context).getPath();

        if (!currentPath.contains(actualPath)) {

            Log.d("SDCARD", currentPath + " MUST BE UPDATED -> " + actualPath);

            return actualPath + currentPath.substring(currentPath.lastIndexOf(File.separator));

        }

        return currentPath;
    }

    public static boolean migrateToExternal(Context context) {

        // migrate files from internal storage to external storage if available

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // can't use this method for older versions, so no point in migrating

            Log.e("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", CAN'T DETERMINE EXTERNAL PATH FOR MIGRATION");

            return false;

        } else {

            // use new method to get all directories, only the first directory should be internal storage

            File[] externalFilesDirs = context.getExternalFilesDirs(null);

            if (externalFilesDirs.length > 1) {

                Log.d("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", MIGRATING FILES TO EXTERNAL PATH " + externalFilesDirs[1].getPath());

                return moveFromHereToThere(externalFilesDirs[0], externalFilesDirs[1]);

            } else {

                // no external directories available, so no point in migrating

                Log.e("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", NO EXTERNAL PATH FOR MIGRATION");

                return false;

            }
        }
    }

    // adding this method to allow the user to switch back to internal storage

    public static boolean migrateFromExternal(Context context) {

        // migrate files from external storage (if available) to internal storage

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // can't use this method for older versions, so no point in migrating

            Log.e("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", CAN'T DETERMINE EXTERNAL PATH FOR MIGRATION");

            return false;

        } else {

            // use new method to get all directories, only the first directory should be internal storage

            File[] externalFilesDirs = context.getExternalFilesDirs(null);

            if (externalFilesDirs.length > 1) {

                // TODO: actually migrate files

                Log.d("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", MIGRATING FILES TO INTERNAL PATH " + externalFilesDirs[0].getPath());

                return moveFromHereToThere(externalFilesDirs[1], externalFilesDirs[0]);

            } else {

                // no external directories available, so no point in migrating

                Log.e("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", NO EXTERNAL PATH FOR MIGRATION");

                return false;

            }
        }
    }

    private static boolean moveFromHereToThere(File source, File destination) {

        if (!source.exists()) {
            Log.e("SDCARD", "SOURCE DIRECTORY " + source.getPath() + " DOES NOT EXIST");
            return false;
        }

        try {
            FileUtils.copyDirectory(source, destination, true);
        } catch (IOException ioe) {
            Log.e("SDCARD", "FAILED TO COPY " + source.getPath() + " - " + ioe.getMessage());
            return false;
        }

        try {
            FileUtils.deleteDirectory(source);
        } catch (IOException ioe) {
            Log.e("SDCARD", "FAILED TO DELETE " + source.getPath() + " - " + ioe.getMessage());
            return false;
        }

        return true;
    }
}
