package scal.io.liger;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import scal.io.liger.model.ExpansionIndexItem;

/**
 * Created by mnbogner on 11/6/14.
 */
public class DownloadHelper {

    // TODO use HTTPS
    // TODO pickup Tor settings
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

    // for additional expansion files, check files folder for specified file
    public static boolean checkExpansionFiles(Context context, String fileName) {
        String expansionFilePath = ZipHelper.getExpansionFileFolder(context, fileName);

        if (expansionFilePath != null) {
            Log.d("DOWNLOAD", "EXPANSION FILE " + fileName + " FOUND IN " + expansionFilePath);
            return true;
        } else {
            Log.d("DOWNLOAD", "EXPANSION FILE " + fileName + " NOT FOUND");
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

            // if the main file is newer than the patch file, remove the patch file rather than downloading
            if (Constants.PATCH_VERSION < Constants.MAIN_VERSION) {
                File obbDirectory = new File(ZipHelper.getObbFolderName(context));
                File fileDirectory = new File(ZipHelper.getFileFolderName(context));

                String nameFilter = Constants.PATCH + ".*." + context.getPackageName() + ".obb";

                Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + obbDirectory.getPath());

                WildcardFileFilter obbFileFilter = new WildcardFileFilter(nameFilter);
                for (File obbFile : FileUtils.listFiles(obbDirectory, obbFileFilter, null)) {
                    Log.d("DOWNLOAD", "CLEANUP: FOUND " + obbFile.getPath() + ", DELETING");
                    FileUtils.deleteQuietly(obbFile);
                }

                Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + fileDirectory.getPath());

                WildcardFileFilter fileFileFilter = new WildcardFileFilter(nameFilter);
                for (File fileFile : FileUtils.listFiles(fileDirectory, fileFileFilter, null)) {
                    Log.d("DOWNLOAD", "CLEANUP: FOUND " + fileFile.getPath() + ", DELETING");
                    FileUtils.deleteQuietly(fileFile);
                }
            } else {
                if (checkExpansionFiles(context, Constants.PATCH, Constants.PATCH_VERSION)) {
                    Log.d("DOWNLOAD", "PATCH EXPANSION FILE FOUND (NO DOWNLOAD)");
                } else {
                    Log.d("DOWNLOAD", "PATCH EXPANSION FILE NOT FOUND (DOWNLOADING)");

                    final LigerDownloadManager patchDownload = new LigerDownloadManager(Constants.PATCH, Constants.PATCH_VERSION, context, true);
                    Thread patchDownloadThread = new Thread(patchDownload);

                    Toast.makeText(context, "Starting download of patch for content pack.", Toast.LENGTH_LONG).show(); // FIXME move to strings

                    patchDownloadThread.start();
                }
            }

        }

        HashMap<String, ExpansionIndexItem> installedIndex = IndexManager.loadInstalledIdIndex(context);
        HashMap<String, ExpansionIndexItem> availableIndex = IndexManager.loadAvailableIdIndex(context);

        for (String id : installedIndex.keySet()) {

            ExpansionIndexItem installedItem = installedIndex.get(id);
            ExpansionIndexItem availableItem = availableIndex.get(id);

            // need to compare main and patch versions
            // update installed index for consistency and to minimize code impact
            if ((installedItem.getExpansionFileVersion() != null) &&
                (availableItem.getExpansionFileVersion() != null) &&
                (Integer.parseInt(availableItem.getExpansionFileVersion()) > Integer.parseInt(installedItem.getExpansionFileVersion()))) {
                Log.d("DOWNLOAD", "FOUND NEWER VERSION OF MAIN EXPANSION ITEM " + id + " (" + availableItem.getExpansionFileVersion() + " vs. " + installedItem.getExpansionFileVersion() + ") UPDATING");
                installedItem.setExpansionFileVersion(availableItem.getExpansionFileVersion());
                IndexManager.registerInstalledIndexItem(context, installedItem);
            }

            // need to account for case where installed item has no defined patch version
            if (availableItem.getPatchFileVersion() != null) {
                if (installedItem.getPatchFileVersion() != null) {
                    if (Integer.parseInt(availableItem.getPatchFileVersion()) > Integer.parseInt(installedItem.getPatchFileVersion())) {
                        Log.d("DOWNLOAD", "FOUND NEWER VERSION OF PATCH EXPANSION ITEM " + id + " (" + availableItem.getPatchFileVersion() + " vs. " + installedItem.getPatchFileVersion() + ") UPDATING");
                        installedItem.setPatchFileVersion(availableItem.getPatchFileVersion());
                        IndexManager.registerInstalledIndexItem(context, installedItem);
                    }
                } else {
                    Log.d("DOWNLOAD", "FOUND NEWER VERSION OF PATCH EXPANSION ITEM " + id + " (" + availableItem.getPatchFileVersion() + " vs. " + installedItem.getPatchFileVersion() + ") UPDATING");
                    installedItem.setPatchFileVersion(availableItem.getPatchFileVersion());
                    IndexManager.registerInstalledIndexItem(context, installedItem);
                }
            }

            String fileName = IndexManager.buildFileName(installedItem, Constants.MAIN);

            if (checkExpansionFiles(context, fileName)) {
                Log.d("DOWNLOAD", "MAIN EXPANSION FILE " + fileName + " FOUND (NO DOWNLOAD)");
            } else {
                Log.d("DOWNLOAD", "MAIN EXPANSION FILE " + fileName + " NOT FOUND (DOWNLOADING)");

                // flag item with pending download
                installedItem.addExtra(IndexManager.pendingDownloadKey, IndexManager.pendingDownloadValue);
                IndexManager.registerInstalledIndexItem(context, installedItem);

                final LigerAltDownloadManager expansionDownload = new LigerAltDownloadManager(fileName, context, true);
                Thread expansionDownloadThread = new Thread(expansionDownload);

                Toast.makeText(context, "Starting download of expansion file.", Toast.LENGTH_LONG).show(); // FIXME move to strings

                expansionDownloadThread.start();
            }

            // if the main file is newer than the patch file, remove the patch file rather than downloading
            if (installedItem.getPatchFileVersion() != null) {
                if ((installedItem.getExpansionFileVersion() != null) &&
                    (Integer.parseInt(installedItem.getPatchFileVersion()) < Integer.parseInt(installedItem.getExpansionFileVersion()))) {

                    File obbDirectory = new File(ZipHelper.getObbFolderName(context));
                    File fileDirectory = new File(ZipHelper.getFileFolderName(context));

                    String nameFilter = installedItem.getExpansionId() + "." + Constants.PATCH + "*" + ".obb";

                    Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + obbDirectory.getPath());

                    WildcardFileFilter obbFileFilter = new WildcardFileFilter(nameFilter);
                    for (File obbFile : FileUtils.listFiles(obbDirectory, obbFileFilter, null)) {
                        Log.d("DOWNLOAD", "CLEANUP: FOUND " + obbFile.getPath() + ", DELETING");
                        FileUtils.deleteQuietly(obbFile);
                    }

                    Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + fileDirectory.getPath());

                    WildcardFileFilter fileFileFilter = new WildcardFileFilter(nameFilter);
                    for (File fileFile : FileUtils.listFiles(fileDirectory, fileFileFilter, null)) {
                        Log.d("DOWNLOAD", "CLEANUP: FOUND " + fileFile.getPath() + ", DELETING");
                        FileUtils.deleteQuietly(fileFile);
                    }
                } else {

                    String patchName = IndexManager.buildFileName(installedItem, Constants.PATCH);

                    if (checkExpansionFiles(context, patchName)) {
                        Log.d("DOWNLOAD", "EXPANSION FILE PATCH " + patchName + " FOUND (NO DOWNLOAD)");
                    } else {
                        Log.d("DOWNLOAD", "EXPANSION FILE PATCH " + patchName + " NOT FOUND (DOWNLOADING)");

                        // in case only a patch is downloading, flag item with pending download
                        installedItem.addExtra(IndexManager.pendingDownloadKey, IndexManager.pendingDownloadValue);
                        IndexManager.registerInstalledIndexItem(context, installedItem);

                        final LigerAltDownloadManager expansionDownload = new LigerAltDownloadManager(patchName, context, true);
                        Thread expansionDownloadThread = new Thread(expansionDownload);

                        Toast.makeText(context, "Starting download of expansion file patch.", Toast.LENGTH_LONG).show(); // FIXME move to strings

                        expansionDownloadThread.start();
                    }
                }
            }
        }
    }
}
