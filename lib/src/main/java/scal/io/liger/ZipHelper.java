package scal.io.liger;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mnbogner on 10/28/14.
 */
public class ZipHelper {
    // move values into constants
    private static int mainVersion = 1;
    private static int patchVersion = 1;

    public static InputStream getFileInputStream(String path, Context context) {
        try {
            // resource file contains main file and patch file
            //ZipResourceFile resourceFile = APKExpansionSupport.getAPKExpansionZipFile(context, mainVersion, patchVersion);

            // multi-patch attempt
            String packageName = context.getPackageName();
            File root = Environment.getExternalStorageDirectory();
            String expPath_1 = root.toString() + "/Android/obb/" + packageName + "/main.1.scal.io.liger.sample.obb";
            String expPath_2 = root.toString() + "/Android/obb/" + packageName + "/patch.1.scal.io.liger.sample.obb";
            String expPath_3 = root.toString() + "/Android/obb/" + packageName + "/patch.2.scal.io.liger.sample.obb";

            String[] paths = new String[3];
            paths[0] = expPath_1;
            paths[1] = expPath_2;
            paths[2] = expPath_3;

            ZipResourceFile resourceFile = APKExpansionSupport.getResourceZipFile(paths);

            // file path must be relative to the root of the resource file
            InputStream resourceStream = resourceFile.getInputStream(path);

            Log.d(" *** TESTING *** ", "Found file " + path + " within resource file (main version " + mainVersion + ", patch version " + patchVersion + ")");
            return resourceStream;
        } catch (IOException ioe) {
            Log.e(" *** TESTING *** ", "Could not find file " + path + " within resource file (main version " + mainVersion + ", patch version " + patchVersion + ")");
            return null;
        }
    }

    public static File getTempFile(String path, String tempPath, Context context) {

        String extension = path.substring(path.lastIndexOf("."));

        File tempFile = new File(tempPath + File.separator + "TEMP" + extension);

        try {
            if (tempFile.exists()) {
                tempFile.delete();
                Log.d(" *** TESTING *** ", "Deleted temp file " + tempFile.getPath());
            }
            tempFile.createNewFile();
            Log.d(" *** TESTING *** ", "Made temp file " + tempFile.getPath());
        } catch (IOException ioe) {
            Log.e(" *** TESTING *** ", "Failed to clean up existing temp file " + tempFile.getPath() + ", " + ioe.getMessage());
            return null;
        }

        InputStream zipInput = getFileInputStream(path, context);

        try {
            FileOutputStream tempOutput = new FileOutputStream(tempFile);
            byte[] buf = new byte[1024];
            int i;
            while((i = zipInput.read(buf)) > 0) {
                tempOutput.write(buf, 0, i);
            }
            tempOutput.close();
            zipInput.close();
            Log.d(" *** TESTING *** ", "Wrote temp file " + tempFile.getPath());
        } catch (IOException ioe) {
            Log.e(" *** TESTING *** ", "Failed to write to temp file " + tempFile.getPath() + ", " + ioe.getMessage());
            return null;
        }

        Log.e(" *** TESTING *** ", "Created temp file " + tempFile.getPath());

        return tempFile;
    }
}
