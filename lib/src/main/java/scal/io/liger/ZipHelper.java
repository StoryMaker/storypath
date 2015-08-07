package scal.io.liger;


import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import scal.io.liger.model.ExpansionIndexItem;

/**
 * @author Matt Bogner
 * @author Josh Steiner
 */
public class ZipHelper {
    public static final String TAG = "ZipHelper";

    public static String getExpansionZipFilename(Context ctx, String mainOrPatch, int version) {
        String packageName = ctx.getPackageName();
        String filename = mainOrPatch + "." + version + "." + packageName + ".obb";
        return filename;
    }

    public static String getExpansionZipDirectory(Context ctx, String mainOrPatch, int version) {
        // basically a wrapper for getExpansionFileFolder, but defaults to files directory
        String filePath = getExpansionFileFolder(ctx, mainOrPatch, version);

        if (filePath == null) {
            return getFileFolderName(ctx);
        } else {
            return filePath;
        }
    }

    public static String getObbFolderName(Context ctx) {
        String packageName = ctx.getPackageName();
        File root = Environment.getExternalStorageDirectory();
        return root.toString() + "/Android/obb/" + packageName + "/";
    }

    public static String getFileFolderName(Context ctx) {
        // TODO Why doesn't this use ctx.getExternalFilesDir(null) (like JsonHelper)?

        // String packageName = ctx.getPackageName();
        // File root = Environment.getExternalStorageDirectory();
        // return root.toString() + "/Android/data/" + packageName + "/files/";
        return StorageHelper.getActualStorageDirectory(ctx).getPath() + "/";
    }

    public static String getFileFolderName(Context context, String fileName) {

        // need to account for patch files
        if (fileName.contains(Constants.PATCH)) {
            fileName.replace(Constants.PATCH, Constants.MAIN);
        }

        ExpansionIndexItem expansionIndexItem = IndexManager.loadInstalledFileIndex(context).get(fileName);

        if (expansionIndexItem == null) {
            Log.e("DIRECTORIES", "FAILED TO LOCATE EXPANSION INDEX ITEM FOR " + fileName);
            return null;
        }

        // TODO - switching to the new storage method ignores the value set in the expansion index item
        // File root = Environment.getExternalStorageDirectory();
        // return root.toString() + File.separator + expansionIndexItem.getExpansionFilePath();
        return StorageHelper.getActualStorageDirectory(context).getPath() + "/";
    }

    public static String getFileFolderName(Context context, String fileName, ExpansionIndexItem item) {

        // need to account for patch files
        if (fileName.contains(Constants.PATCH)) {
            fileName.replace(Constants.PATCH, Constants.MAIN);
        }

        // TODO - switching to the new storage method ignores the value set in the expansion index item
        // File root = Environment.getExternalStorageDirectory();
        // return root.toString() + File.separator + item.getExpansionFilePath();
        return StorageHelper.getActualStorageDirectory(context).getPath() + "/";
    }

    // supressing messages for less text during polling
    public static String getExpansionFileFolder(Context ctx, String mainOrPatch, int version) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // check and/or attempt to create obb folder
            String checkPath = getObbFolderName(ctx);
            File checkDir = new File(checkPath);
            if (checkDir.isDirectory() || checkDir.mkdirs()) {
                File checkFile = new File(checkPath + getExpansionZipFilename(ctx, mainOrPatch, version));
                if (checkFile.exists()) {
                    Log.d("DIRECTORIES", "FOUND " + mainOrPatch + " " + version + " IN OBB DIRECTORY: " + checkFile.getPath());
                    return checkPath;
                }
            }

            // check and/or attempt to create files folder
            checkPath = getFileFolderName(ctx);
            checkDir = new File(checkPath);
            if (checkDir.isDirectory() || checkDir.mkdirs()) {
                File checkFile = new File(checkPath + getExpansionZipFilename(ctx, mainOrPatch, version));
                if (checkFile.exists()) {
                    Log.d("DIRECTORIES", "FOUND " + mainOrPatch + " " + version + " IN FILES DIRECTORY: " + checkFile.getPath());
                    return checkPath;
                }
            }
        }

        Log.e("DIRECTORIES", mainOrPatch + " " + version + " NOT FOUND IN OBB DIRECTORY OR FILES DIRECTORY");
        return null;
    }

    // for additional expansion files, check files folder for specified file
    public static String getExpansionFileFolder(Context ctx, String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // check and/or attempt to create files folder
            String checkPath = getFileFolderName(ctx, fileName);

            if (checkPath == null) {
                return null;
            }

            File checkDir = new File(checkPath);
            if (checkDir.isDirectory() || checkDir.mkdirs()) {
                File checkFile = new File(checkPath + fileName);
                if (checkFile.exists()) {
                    Log.d("DIRECTORIES", "FOUND " + fileName + " IN DIRECTORY: " + checkFile.getPath());
                    return checkPath;
                }
            }
        }

        Log.e("DIRECTORIES", fileName + " NOT FOUND");
        return null;
    }

    public static String getExpansionFileFolder(Context ctx, String fileName, ExpansionIndexItem item) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // check and/or attempt to create files folder
            String checkPath = getFileFolderName(ctx, fileName, item);

            if (checkPath == null) {
                return null;
            }

            File checkDir = new File(checkPath);
            if (checkDir.isDirectory() || checkDir.mkdirs()) {
                File checkFile = new File(checkPath + fileName);
                if (checkFile.exists()) {
                    Log.d("DIRECTORIES", "FOUND " + fileName + " IN DIRECTORY: " + checkFile.getPath());
                    return checkPath;
                }
            }
        }

        Log.e("DIRECTORIES", fileName + " NOT FOUND");
        return null;
    }

    public static ZipResourceFile getResourceFile(Context context) {
        try {
            // resource file contains main file and patch file

            ArrayList<String> paths = getExpansionPaths(context);

            ZipResourceFile resourceFile = APKExpansionSupport.getResourceZipFile(paths.toArray(new String[paths.size()]));

            return resourceFile;
        } catch (IOException ioe) {
            Log.e(" *** TESTING *** ", "Could not open resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            return null;
        }
    }

    public static InputStream getFileInputStream(String path, Context context, String language) {

        String localizedFilePath = path;

        // check language setting and insert country code if necessary

        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (path.lastIndexOf("-" + language + path.substring(path.lastIndexOf("."))) < 0) {
                // if not already appended, don't bother to append -en
                if (!"en".equals(language)) {
                    localizedFilePath = path.substring(0, path.lastIndexOf(".")) + "-" + language + path.substring(path.lastIndexOf("."));
                    Log.d("LANGUAGE", "getFileInputStream() - TRYING LOCALIZED PATH: " + localizedFilePath);
                } else {
                    Log.d("LANGUAGE", "getFileInputStream() - TRYING PATH: " + localizedFilePath);
                }
            } else {
                Log.d("LANGUAGE", "getFileInputStream() - TRYING LOCALIZED PATH: " + localizedFilePath);
            }
        } else {
            Log.d("LANGUAGE", "getFileInputStream() - TRYING PATH: " + localizedFilePath);
        }

        InputStream fileStream = getFileInputStream(localizedFilePath, context);

        // if there is no result with the localized path, retry with default path
        if (fileStream == null) {
            if (localizedFilePath.contains("-")) {
                localizedFilePath = localizedFilePath.substring(0, localizedFilePath.lastIndexOf("-")) + localizedFilePath.substring(localizedFilePath.lastIndexOf("."));
                Log.d("LANGUAGE", "getFileInputStream() - NO RESULT WITH LOCALIZED PATH, TRYING DEFAULT PATH: " + localizedFilePath);
                fileStream = ZipHelper.getFileInputStream(localizedFilePath, context);
            }
        } else {
            return fileStream;
        }

        if (fileStream == null) {
            Log.d("LANGUAGE", "getFileInputStream() - NO RESULT WITH DEFAULT PATH: " + localizedFilePath);
        } else {
            return fileStream;
        }

        return null;
    }

    public static @Nullable InputStream getFileInputStreamForExpansionAndPath(@NonNull ExpansionIndexItem expansion,
                                                                              @NonNull String path,
                                                                              @NonNull Context context) {

        return getFileInputStreamFromFile(IndexManager.buildFileAbsolutePath(expansion, Constants.MAIN, context), path, context);
    }

    public static InputStream getThumbnailInputStreamForItem(ExpansionIndexItem item, Context context) {
        ZipResourceFile resourceFile = null;
        try {
            resourceFile = APKExpansionSupport.getResourceZipFile(new String[]{IndexManager.buildFileAbsolutePath(item, Constants.MAIN, context)});
            return resourceFile.getInputStream(item.getThumbnailPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ArrayList<String> expansionPaths;

    /**
     * @return an absolute path to an expansion file with the given expansionId, or null if no
     * match could be made.
     */
    public static @Nullable String getExpansionPathForExpansionId(Context context, String expansionId) {
        String targetExpansionPath = null;
        ArrayList<String> expansionPaths = getExpansionPaths(context);
        for (String expansionPath : expansionPaths) {
            if (expansionPath.contains(expansionId)) {
                targetExpansionPath = expansionPath;
                break;
            }
        }
        return targetExpansionPath;
    }

    public static void clearCache() {
        expansionPaths = null;
    }

    /**
     * @return a list of absolute paths to all available expansion files.
     */
    private static  ArrayList<String> getExpansionPaths(Context context) {
        if (expansionPaths == null) {
            expansionPaths = new ArrayList<>();

            File mainFile = new File(getExpansionFileFolder(context, Constants.MAIN, Constants.MAIN_VERSION) + getExpansionZipFilename(context, Constants.MAIN, Constants.MAIN_VERSION));
            if (mainFile.exists() && (mainFile.length() > 0)) {
                expansionPaths.add(mainFile.getPath());
            } else {
                Log.e("ZIP", mainFile.getPath() + " IS MISSING OR EMPTY, EXCLUDING FROM ZIP RESOURCE");
            }

            if (Constants.PATCH_VERSION > 0) {

                // if the main file is newer than the patch file, do not apply a patch file
                if (Constants.PATCH_VERSION < Constants.MAIN_VERSION) {
                    Log.d("ZIP", "PATCH VERSION " + Constants.PATCH_VERSION + " IS OUT OF DATE (MAIN VERSION IS " + Constants.MAIN_VERSION + ")");
                } else {
                    Log.d("ZIP", "APPLYING PATCH VERSION " + Constants.PATCH_VERSION + " (MAIN VERSION IS " + Constants.MAIN_VERSION + ")");

                    File patchFile = new File(getExpansionFileFolder(context, Constants.PATCH, Constants.PATCH_VERSION) + getExpansionZipFilename(context, Constants.PATCH, Constants.PATCH_VERSION));
                    if (patchFile.exists() && (patchFile.length() > 0)) {
                        expansionPaths.add(patchFile.getPath());
                    }else {
                        Log.e("ZIP", patchFile.getPath() + " IS MISSING OR EMPTY, EXCLUDING FROM ZIP RESOURCE");
                    }
                }

            }

            // add 3rd party stuff
            HashMap<String, ExpansionIndexItem> expansionIndex = IndexManager.loadInstalledOrderIndex(context);

            // need to sort patch order keys, numbers may not be consecutive
            ArrayList<String> orderNumbers = new ArrayList<String>(expansionIndex.keySet());
            Collections.sort(orderNumbers);

            for (String orderNumber : orderNumbers) {
                ExpansionIndexItem item = expansionIndex.get(orderNumber);
                if (item == null) {
                    Log.d("ZIP", "EXPANSION FILE ENTRY MISSING AT PATCH ORDER NUMBER " + orderNumber);
                } else {

                    // construct name
                    String pathName = IndexManager.buildFilePath(item, context);
                    String fileName = IndexManager.buildFileName(item, Constants.MAIN);

                    File checkFile = new File(pathName + fileName);

                    // should be able to do this locally
                    // if (DownloadHelper.checkExpansionFiles(context, fileName)) {
                    if (checkFile.exists() && (checkFile.length() > 0)) {
                        Log.d("ZIP", "EXPANSION FILE " + checkFile.getPath() + " FOUND, ADDING TO ZIP");
                        expansionPaths.add(checkFile.getPath());

                        if ((item.getPatchFileVersion() != null) &&
                                (item.getExpansionFileVersion() != null) &&
                                (Integer.parseInt(item.getPatchFileVersion()) > 0) &&
                                (Integer.parseInt(item.getPatchFileVersion()) >= Integer.parseInt(item.getExpansionFileVersion()))) {
                            // construct name
                            String patchName = IndexManager.buildFileName(item, Constants.PATCH);

                            checkFile = new File(pathName + patchName);

                            // should be able to do this locally
                            // if (DownloadHelper.checkExpansionFiles(context, patchName)) {
                            if (checkFile.exists() && (checkFile.length() > 0)) {
                                Log.d("ZIP", "EXPANSION FILE " + checkFile.getPath() + " FOUND, ADDING TO ZIP");
                                expansionPaths.add(checkFile.getPath());
                            } else {
                                Log.e("ZIP", checkFile.getPath() + " IS MISSING OR EMPTY, EXCLUDING FROM ZIP RESOURCE");
                            }
                        }
                    } else {
                        Log.e("ZIP", checkFile.getPath() + " IS MISSING OR EMPTY, EXCLUDING FROM ZIP RESOURCE");
                    }
                }
            }
        }
        return expansionPaths;
    }

    /**
     * @return the {@link scal.io.liger.model.ExpansionIndexItem} which is likely to contain the
     * given path.
     *
     * This method is useful as an optimization step until StoryPathLibrarys hold reference
     * to the ExpansionIndexItem from which they were created, if applicable. The inspection
     * is performed by searching for installed ExpansionIndexItem expansionId values
     * within the given path parameter, and does not involve inspection of the files belonging to
     * each ExpansionIndexItem.
     *
     */
    public static @Nullable ExpansionIndexItem guessExpansionIndexItemForPath(String path, Context context) {
        HashMap<String, ExpansionIndexItem> expansions = IndexManager.loadInstalledIdIndex(context);

        Set<String> expansionIds = expansions.keySet();
        for(String expansionId : expansionIds) {
            if (path.contains(expansionId)) {
                return expansions.get(expansionId);
            }
        }
        return null;
    }

    /**
     * @return an {@link java.io.InputStream} corresponding to the given path which must reside within
     * one of the installed index items. On disk this corresponds to a zip archive packaged as an .obb file.
     *
     */
    @Deprecated
    public static InputStream getFileInputStream(String path, Context context) {

        // resource file contains main file and patch file
        ArrayList<String> allExpansionPaths = getExpansionPaths(context);
        ArrayList<String> targetExpansionPaths = new ArrayList<>();

        // try to extract expansion id from target path
        // assumes format org.storymaker.app/learning_guide/content_metadata-en.json
        String[] parts = path.split("/");

        for (String expansionPath : allExpansionPaths) {
            if (expansionPath.contains(parts[1])) {
                Log.d("PATCHING", "FOUND MATCH FOR " + parts[1] + " IN PATH " + expansionPath);
                targetExpansionPaths.add(expansionPath);
            }
        }

        // this shouldn't happen...
        if (targetExpansionPaths.size() == 0) {
            Log.d("PATCHING", "NO MATCHES FOR " + parts[1] + ", USING ALL PATHS");
            targetExpansionPaths = allExpansionPaths;
        }

        StringBuilder paths = new StringBuilder();
        for (String expansionPath : targetExpansionPaths) {
            paths.append(expansionPath);
            paths.append(", ");
        }
        paths.delete(paths.length()-2, paths.length());
        Log.d(TAG, String.format("Searching for %s in %s", path, paths));

        return getFileInputStreamFromFiles(targetExpansionPaths, path, context);
    }

    public static InputStream getFileInputStreamFromFile(String zipPath, String filePath, Context context) {

        ArrayList<String> zipPaths = new ArrayList<String>();
        zipPaths.add(zipPath);

        return getFileInputStreamFromFiles(zipPaths, filePath, context);
    }

    public static InputStream getFileInputStreamFromFiles(ArrayList<String> zipPaths, String filePath, Context context) {
        try {
            ZipResourceFile resourceFile = APKExpansionSupport.getResourceZipFile(zipPaths.toArray(new String[zipPaths.size()]));

            if (resourceFile == null) {
                return null;
            }

            // file path must be relative to the root of the resource file
            InputStream resourceStream = resourceFile.getInputStream(filePath);

            if (resourceStream == null) {
                Log.d(" *** TESTING *** ", "Could not find file " + filePath + " within resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            } else {
                Log.d(" *** TESTING *** ", "Found file " + filePath + " within resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            }
            return resourceStream;
        } catch (IOException ioe) {
            Log.e(" *** TESTING *** ", "Could not find file " + filePath + " within resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            return null;
        }
    }

    /**
     * Copy the expansion asset at path to a temporary file within tempPath. If
     * a temp file exists for the given arguments it will be deleted and remade.
     */
    @Deprecated
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
