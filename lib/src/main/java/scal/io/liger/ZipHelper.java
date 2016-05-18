package scal.io.liger;

import timber.log.Timber;


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
import scal.io.liger.model.sqlbrite.InstalledIndexItemDao;

/**
 * @author Matt Bogner
 * @author Josh Steiner
 */
public class ZipHelper {
    public static final String TAG = "ZipHelper";

    @NonNull
    public static String getExpansionZipFilename(Context ctx, String mainOrPatch, int version) {
        String packageName = ctx.getPackageName();
        return mainOrPatch + "." + version + "." + packageName + ".obb";
    }

    @Nullable
    public static String getExpansionZipDirectory(Context ctx, String mainOrPatch, int version) {
        // basically a wrapper for getExpansionFileFolder, but defaults to files directory
        String filePath = getExpansionFileFolder(ctx, mainOrPatch, version);

        if (filePath == null) {
            return getFileFolderName(ctx);
        } else {
            return filePath;
        }
    }

    @NonNull
    public static String getObbFolderName(Context ctx) {
        String packageName = ctx.getPackageName();
        File root = Environment.getExternalStorageDirectory();
        return root.toString() + "/Android/obb/" + packageName + "/";
    }

    @Nullable
    public static String getFileFolderName(Context ctx) {
        // TODO Why doesn't this use ctx.getExternalFilesDir(null) (like JsonHelper)?

        // String packageName = ctx.getPackageName();
        // File root = Environment.getExternalStorageDirectory();
        // return root.toString() + "/Android/data/" + packageName + "/files/";
        File file = StorageHelper.getActualStorageDirectory(ctx);
        String path = null;
        if (file != null) {
            path = file.getPath() + "/";
        }
        return path;
    }

    @Nullable
    public static String getFileFolderName(Context context, String fileName) {

        // need to account for patch files
        if (fileName.contains(Constants.PATCH)) {
            fileName = fileName.replace(Constants.PATCH, Constants.MAIN);
        }

        ExpansionIndexItem expansionIndexItem = IndexManager.loadInstalledFileIndex(context).get(fileName);

        if (expansionIndexItem == null) {
            Timber.e("FAILED TO LOCATE EXPANSION INDEX ITEM FOR " + fileName);
            return null;
        }

        // TODO - switching to the new storage method ignores the value set in the expansion index item
        // File root = Environment.getExternalStorageDirectory();
        // return root.toString() + File.separator + expansionIndexItem.getExpansionFilePath();
        File file = StorageHelper.getActualStorageDirectory(context);
        if (file != null) {
            return file.getPath() + "/";
        } else {
            return null;
        }
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
    @Nullable
    public static String getExpansionFileFolder(Context ctx, String mainOrPatch, int version) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // check and/or attempt to create obb folder
            String checkPath = getObbFolderName(ctx);
            File checkDir = new File(checkPath);
            if (checkDir.isDirectory() || checkDir.mkdirs()) {
                File checkFile = new File(checkPath + getExpansionZipFilename(ctx, mainOrPatch, version));
                if (checkFile.exists()) {
                    Timber.d("FOUND " + mainOrPatch + " " + version + " IN OBB DIRECTORY: " + checkFile.getPath());
                    return checkPath;
                }
            }

            // check and/or attempt to create files folder
            checkPath = getFileFolderName(ctx);
            checkDir = new File(checkPath);
            if (checkDir.isDirectory() || checkDir.mkdirs()) {
                File checkFile = new File(checkPath + getExpansionZipFilename(ctx, mainOrPatch, version));
                if (checkFile.exists()) {
                    Timber.d("FOUND " + mainOrPatch + " " + version + " IN FILES DIRECTORY: " + checkFile.getPath());
                    return checkPath;
                }
            }
        }

        Timber.e(mainOrPatch + " " + version + " NOT FOUND IN OBB DIRECTORY OR FILES DIRECTORY");
        return null;
    }

    // for additional expansion files, check files folder for specified file
    @Nullable
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
                    Timber.d("FOUND " + fileName + " IN DIRECTORY: " + checkFile.getPath());
                    return checkPath;
                }
            }
        }

        Timber.e(fileName + " NOT FOUND");
        return null;
    }

    @Nullable
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
                    Timber.d("FOUND " + fileName + " IN DIRECTORY: " + checkFile.getPath());
                    return checkPath;
                }
            }
        }

        Timber.e(fileName + " NOT FOUND");
        return null;
    }

    @Nullable
    public static ZipResourceFile getResourceFile(Context context) {
        try {
            // resource file contains main file and patch file

            ArrayList<String> paths = getExpansionPaths(context);

            ZipResourceFile resourceFile = APKExpansionSupport.getResourceZipFile(paths.toArray(new String[paths.size()]));

            return resourceFile;
        } catch (IOException ioe) {
            Timber.e("Could not open resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            return null;
        }
    }

    @Nullable
    public static InputStream getFileInputStream(String path, Context context, String language) {

        String localizedFilePath = path;

        // check language setting and insert country code if necessary

        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (path.lastIndexOf("-" + language + path.substring(path.lastIndexOf("."))) < 0) {
                // if not already appended, don't bother to append -en
                if (!"en".equals(language)) {
                    localizedFilePath = path.substring(0, path.lastIndexOf(".")) + "-" + language + path.substring(path.lastIndexOf("."));
                    Timber.d("getFileInputStream() - TRYING LOCALIZED PATH: " + localizedFilePath);
                } else {
                    Timber.d("getFileInputStream() - TRYING PATH: " + localizedFilePath);
                }
            } else {
                Timber.d("getFileInputStream() - TRYING LOCALIZED PATH: " + localizedFilePath);
            }
        } else {
            Timber.d("getFileInputStream() - TRYING PATH: " + localizedFilePath);
        }

        InputStream fileStream = getFileInputStream(localizedFilePath, context);

        // if there is no result with the localized path, retry with default path
        if (fileStream == null) {
            if (localizedFilePath.contains("-")) {
                localizedFilePath = localizedFilePath.substring(0, localizedFilePath.lastIndexOf("-")) + localizedFilePath.substring(localizedFilePath.lastIndexOf("."));
                Timber.d("getFileInputStream() - NO RESULT WITH LOCALIZED PATH, TRYING DEFAULT PATH: " + localizedFilePath);
                fileStream = ZipHelper.getFileInputStream(localizedFilePath, context);
            }
        } else {
            return fileStream;
        }

        if (fileStream == null) {
            Timber.d("getFileInputStream() - NO RESULT WITH DEFAULT PATH: " + localizedFilePath);
        } else {
            return fileStream;
        }

        return null;
    }

    @Nullable
    public static InputStream getFileInputStreamForExpansionAndPath(@NonNull ExpansionIndexItem expansion,
                                                                              @NonNull String path,
                                                                              @NonNull Context context) {

        return getFileInputStreamFromFile(IndexManager.buildFileAbsolutePath(expansion, Constants.MAIN, context), path, context);
    }

    @Nullable
    public static InputStream getThumbnailInputStreamForItem(@NonNull ExpansionIndexItem item, @NonNull Context context) {
        ZipResourceFile resourceFile = null;
        try {
            resourceFile = APKExpansionSupport.getResourceZipFile(new String[]{IndexManager.buildFileAbsolutePath(item, Constants.MAIN, context)});
            return resourceFile.getInputStream(item.getThumbnailPath());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

   // private static ArrayList<String> expansionPaths;

    /**
     * @return an absolute path to an expansion file with the given expansionId, or null if no
     * match could be made.
     */
    // unused, removing for now
    /*
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
    */

    public static void clearCache() {
       // expansionPaths = null;
    }

    /**
     * @return a list of absolute paths to all available expansion files.
     */
    @NonNull
    private static ArrayList<String> getExpansionPaths(@NonNull Context context) {
        //if (expansionPaths == null) {

              ArrayList<String> expansionPaths = new ArrayList<>();

            File mainFile = new File(getExpansionFileFolder(context, Constants.MAIN, Constants.MAIN_VERSION) + getExpansionZipFilename(context, Constants.MAIN, Constants.MAIN_VERSION));
            if (mainFile.exists() && (mainFile.length() > 0)) {
                expansionPaths.add(mainFile.getPath());
            } else {
                Timber.e(mainFile.getPath() + " IS MISSING OR EMPTY, EXCLUDING FROM ZIP RESOURCE");
            }

            if (Constants.PATCH_VERSION > 0) {

                // if the main file is newer than the patch file, do not apply a patch file
                if (Constants.PATCH_VERSION < Constants.MAIN_VERSION) {
                    Timber.d("PATCH VERSION " + Constants.PATCH_VERSION + " IS OUT OF DATE (MAIN VERSION IS " + Constants.MAIN_VERSION + ")");
                } else {
                    Timber.d("APPLYING PATCH VERSION " + Constants.PATCH_VERSION + " (MAIN VERSION IS " + Constants.MAIN_VERSION + ")");

                    File patchFile = new File(getExpansionFileFolder(context, Constants.PATCH, Constants.PATCH_VERSION) + getExpansionZipFilename(context, Constants.PATCH, Constants.PATCH_VERSION));
                    if (patchFile.exists() && (patchFile.length() > 0)) {
                        expansionPaths.add(patchFile.getPath());
                    }else {
                        Timber.e(patchFile.getPath() + " IS MISSING OR EMPTY, EXCLUDING FROM ZIP RESOURCE");
                    }
                }

            }

            // need db access to get installed files
            InstalledIndexItemDao dao = null;

            if (context instanceof MainActivity) {
                dao = ((MainActivity)context).getInstalledIndexItemDao(); // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())
            } else {
                Timber.e("NO DAO IN getExpansionPaths");
            }

            // add 3rd party stuff
            HashMap<String, scal.io.liger.model.sqlbrite.ExpansionIndexItem> expansionIndex = StorymakerIndexManager.loadInstalledOrderIndex(context, dao);

            // need to sort patch order keys, numbers may not be consecutive
            ArrayList<String> orderNumbers = new ArrayList<String>(expansionIndex.keySet());
            Collections.sort(orderNumbers);

            for (String orderNumber : orderNumbers) {
                scal.io.liger.model.sqlbrite.ExpansionIndexItem item = expansionIndex.get(orderNumber);
                if (item == null) {
                    Timber.d("EXPANSION FILE ENTRY MISSING AT PATCH ORDER NUMBER " + orderNumber);
                } else {

                    // construct name
                    String pathName = StorymakerIndexManager.buildFilePath(item, context);
                    String fileName = StorymakerIndexManager.buildFileName(item, Constants.MAIN);

                    File checkFile = new File(pathName + fileName);

                    // should be able to do this locally
                    // if (DownloadHelper.checkExpansionFiles(context, fileName)) {
                    if (checkFile.exists() && (checkFile.length() > 0)) {
                        Timber.d("EXPANSION FILE " + checkFile.getPath() + " FOUND, ADDING TO ZIP");
                        expansionPaths.add(checkFile.getPath());

                        if ((item.getPatchFileVersion() != null) &&
                                (item.getExpansionFileVersion() != null) &&
                                (Integer.parseInt(item.getPatchFileVersion()) > 0) &&
                                (Integer.parseInt(item.getPatchFileVersion()) >= Integer.parseInt(item.getExpansionFileVersion()))) {
                            // construct name
                            String patchName = StorymakerIndexManager.buildFileName(item, Constants.PATCH);

                            checkFile = new File(pathName + patchName);

                            // should be able to do this locally
                            // if (DownloadHelper.checkExpansionFiles(context, patchName)) {
                            if (checkFile.exists() && (checkFile.length() > 0)) {
                                Timber.d("EXPANSION FILE " + checkFile.getPath() + " FOUND, ADDING TO ZIP");
                                expansionPaths.add(checkFile.getPath());
                            } else {
                                Timber.e(checkFile.getPath() + " IS MISSING OR EMPTY, EXCLUDING FROM ZIP RESOURCE");
                            }
                        }
                    } else {
                        Timber.e(checkFile.getPath() + " IS MISSING OR EMPTY, EXCLUDING FROM ZIP RESOURCE");
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
    @Nullable
    public static ExpansionIndexItem guessExpansionIndexItemForPath(@NonNull String path, @NonNull Context context) {

        // need db access to get list of installed content packs
        if (context instanceof MainActivity) {
            InstalledIndexItemDao dao = ((MainActivity)context).getInstalledIndexItemDao(); // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())

            HashMap<String, scal.io.liger.model.sqlbrite.ExpansionIndexItem> expansions = StorymakerIndexManager.loadInstalledIdIndex(context, dao);

            Set<String> expansionIds = expansions.keySet();
            for(String expansionId : expansionIds) {
                if (path.contains(expansionId)) {

                    // turning this into an instance of the non sql-brite class to minimize impact
                    ExpansionIndexItem eii = new ExpansionIndexItem((expansions.get(expansionId)));

                    return eii;
                }
            }
        } else {
            Timber.e("could not find a dao to access the installed item list in the db");
        }

        return null;
    }

    /**
     * @return an {@link java.io.InputStream} corresponding to the given path which must reside within
     * one of the installed index items. On disk this corresponds to a zip archive packaged as an .obb file.
     *
     */
    @Deprecated
    @Nullable
    public static InputStream getFileInputStream(@NonNull String path, @NonNull Context context) {

        // resource file contains main file and patch file
        ArrayList<String> allExpansionPaths = getExpansionPaths(context);
        ArrayList<String> targetExpansionPaths = new ArrayList<>();

        // try to extract expansion id from target path
        // assumes format org.storymaker.app/learning_guide/content_metadata-en.json
        String[] parts = path.split("/");

        for (String expansionPath : allExpansionPaths) {
            if (expansionPath.contains(parts[1])) {
                Timber.d("FOUND MATCH FOR " + parts[1] + " IN PATH " + expansionPath);
                targetExpansionPaths.add(expansionPath);
            }
        }

        // this shouldn't happen...
        if (targetExpansionPaths.size() == 0) {
            Timber.d("NO MATCHES FOR " + parts[1] + ", USING ALL PATHS");
            targetExpansionPaths = allExpansionPaths;
        }

        StringBuilder paths = new StringBuilder();
        for (String expansionPath : targetExpansionPaths) {
            paths.append(expansionPath);
            paths.append(", ");
        }
        paths.delete(paths.length()-2, paths.length());
        Timber.d(String.format("Searching for %s in %s", path, paths));

        return getFileInputStreamFromFiles(targetExpansionPaths, path, context);
    }

    @Nullable
    public static InputStream getFileInputStreamFromFile(String zipPath, String filePath, Context context) {

        ArrayList<String> zipPaths = new ArrayList<String>();
        zipPaths.add(zipPath);

        return getFileInputStreamFromFiles(zipPaths, filePath, context);
    }

    @Nullable
    public static InputStream getFileInputStreamFromFiles(ArrayList<String> zipPaths, String filePath, Context context) {
        try {
            ZipResourceFile resourceFile = APKExpansionSupport.getResourceZipFile(zipPaths.toArray(new String[zipPaths.size()]));

            if (resourceFile == null) {
                return null;
            }

            // file path must be relative to the root of the resource file
            InputStream resourceStream = resourceFile.getInputStream(filePath);

            if (resourceStream == null) {
                Timber.d("Could not find file " + filePath + " within resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            } else {
                Timber.d("Found file " + filePath + " within resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            }
            return resourceStream;
        } catch (IOException ioe) {
            Timber.e("Could not find file " + filePath + " within resource file (main version " + Constants.MAIN_VERSION + ", patch version " + Constants.PATCH_VERSION + ")");
            return null;
        }
    }

    /**
     * Copy the expansion asset at path to a temporary file within tempPath. If
     * a temp file exists for the given arguments it will be deleted and remade.
     */
    @Deprecated
    @Nullable
    public static File getTempFile(String path, String tempPath, Context context) {

        String extension = path.substring(path.lastIndexOf("."));

        File tempFile = new File(tempPath + File.separator + "TEMP" + extension);

        try {
            if (tempFile.exists()) {
                tempFile.delete();
                Timber.d("Deleted temp file " + tempFile.getPath());
            }
            tempFile.createNewFile();
            Timber.d("Made temp file " + tempFile.getPath());
        } catch (IOException ioe) {
            Timber.e("Failed to clean up existing temp file " + tempFile.getPath() + ", " + ioe.getMessage());
            return null;
        }

        InputStream zipInput = getFileInputStream(path, context);

        if (zipInput == null) {
            Timber.e("Failed to open input stream for " + path + " in .zip file");
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
            Timber.d("Wrote temp file " + tempFile.getPath());
        } catch (IOException ioe) {
            Timber.e("Failed to write to temp file " + tempFile.getPath() + ", " + ioe.getMessage());
            return null;
        }

        Timber.e("Created temp file " + tempFile.getPath());

        return tempFile;
    }
}
