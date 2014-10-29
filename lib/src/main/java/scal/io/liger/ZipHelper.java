package scal.io.liger;


import android.content.Context;
import android.util.Log;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mnbogner on 10/28/14.
 */
public class ZipHelper {
    // move values into constants
    private static int mainVersion = 1;
    private static int patchVersion = 0;

    public static InputStream getFileInputStream(String path, Context context) {
        try {
            // resource file contains main file and patch file
            ZipResourceFile resourceFile = APKExpansionSupport.getAPKExpansionZipFile(context, mainVersion, patchVersion);

            // file path must be relative to the root of the resource file
            InputStream resourceStream = resourceFile.getInputStream(path);

            Log.d(" *** TESTING *** ", "Found file " + path + " within resource file (main version " + mainVersion + ", patch version " + patchVersion + ")");
            return resourceStream;
        } catch (IOException ioe) {
            Log.e(" *** TESTING *** ", "Could not find file " + path + " within resource file (main version " + mainVersion + ", patch version " + patchVersion + ")");
            return null;
        }
    }
}
