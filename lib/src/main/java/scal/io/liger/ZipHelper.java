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
import java.util.ArrayList;
import java.util.Vector;

/**
 * @author Matt Bogner
 * @author Josh Steiner
 */
public class ZipHelper {

    public static String getExtensionZipFilename(Context ctx, String mainOrPatch, int version) {
        String packageName = ctx.getPackageName();
        String filename = mainOrPatch + "." + version + "." + packageName + ".obb";
        return filename;
    }

    public static String getExtensionFolderPath(Context ctx) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Build the full path to the app's expansion files
            String packageName = ctx.getPackageName();
            File root = Environment.getExternalStorageDirectory();

            // check and/or attempt to create obb folder
            String checkPath = root.toString() + "/Android/obb/" + packageName + "/";
            File checkDir = new File(checkPath);
            if (checkDir.isDirectory() || checkDir.mkdirs()) {
                Log.d("DIRECTORIES", "GOT OBB DIRECTORY: " + checkPath);
                return checkPath;
            }

            // check and/or attempt to create files folder
            checkPath = root.toString() + "/Android/data/" + packageName + "/files/";
            checkDir = new File(checkPath);
            if (checkDir.isDirectory() || checkDir.mkdirs()) {
                Log.d("DIRECTORIES", "GOT FILES DIRECTORY: " + checkPath);
                return checkPath;
            }
        }

        Log.e("DIRECTORIES", "NO OBB DIRECTORY AND NO FILES DIRECTORY");
        return null;
    }

    public static InputStream getFileInputStream(String path, Context context) {
        try {
            // resource file contains main file and patch file

            ArrayList<String> paths = new ArrayList<String>();
            paths.add(getExtensionFolderPath(context) + getExtensionZipFilename(context, Constants.MAIN, Constants.MAIN_VERSION));
            if (Constants.PATCH_VERSION > 0) {
                paths.add(getExtensionFolderPath(context) + getExtensionZipFilename(context, Constants.PATCH, Constants.PATCH_VERSION));
            }

            ZipResourceFile resourceFile = APKExpansionSupport.getResourceZipFile(paths.toArray(new String[paths.size()]));

            if (resourceFile == null) {
                return null;
            }

            // file path must be relative to the root of the resource file
            InputStream resourceStream = resourceFile.getInputStream(path);

            if (resourceStream == null) {
                Log.d(" *** TESTING *** ", "Could not find file " + path + " within resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            } else {
                Log.d(" *** TESTING *** ", "Found file " + path + " within resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            }
            return resourceStream;
        } catch (IOException ioe) {
            Log.e(" *** TESTING *** ", "Could not find file " + path + " within resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
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

        if (zipInput == null) {
            Log.e(" *** TESTING *** ", "Failed to open input stream for " + path + " in .zip file");
            return null;
        }

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
