package scal.io.liger;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;

/**
 * Created by mnbogner on 8/6/15.
 */
public class StorageHelper {

    public static File getActualStorageDirectory(Context context) {

        // locate actual external storage path if available

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // can't use this method for older versions, just use existing method

            // Log.d("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", USING OLD METHOD: " + context.getExternalFilesDir(null).getPath());

            return context.getExternalFilesDir(null);

        } else {

            // use new method to get all directories, only the first directory should be internal storage

            File[] externalFilesDirs = context.getExternalFilesDirs(null);

            if (externalFilesDirs.length > 1) {

                // is there a more intelligent way to make this selection?

                // TODO: still deciding how to handle 4.4, for now just return item 0 (should be the same as getExternalFilesDir)

                // Log.d("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", USING NEW METHOD (" + externalFilesDirs.length + " OPTIONS): " + externalFilesDirs[0].getPath());

                return externalFilesDirs[0];

            } else {

                // no external directories available, use internal storage

                // Log.d("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", USING NEW METHOD (1 OPTION): " + externalFilesDirs[0].getPath());

                return externalFilesDirs[0];

            }
        }
    }

    public static boolean migrate(Context context) {

        // migrate files from internal storage to external storage if available

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // can't use this method for older versions, so no point in migrating

            Log.e("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", CAN'T DETERMINE EXTERNAL PATH FOR MIGRATION");

            return false;

        } else {

            // use new method to get all directories, only the first directory should be internal storage

            File[] externalFilesDirs = context.getExternalFilesDirs(null);

            if (externalFilesDirs.length > 1) {

                // TODO: actually migrate files

                Log.d("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", MIGRATING FILES TO EXTERNAL PATH " + externalFilesDirs[1].getPath());

                return true;

            } else {

                // no external directories available, so no point in migrating

                Log.e("SDCARD", "VERSION " + Build.VERSION.SDK_INT + ", NO EXTERNAL PATH FOR MIGRATION");

                return false;

            }
        }
    }
}
