package scal.io.liger;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.hannesdorfmann.sqlbrite.dao.Dao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rx.functions.Action1;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;
import scal.io.liger.model.sqlbrite.AvailableIndexItem;
import scal.io.liger.model.sqlbrite.AvailableIndexItemDao;
import scal.io.liger.model.sqlbrite.ExpansionIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItem;
import scal.io.liger.model.sqlbrite.InstalledIndexItemDao;
import scal.io.liger.model.sqlbrite.InstanceIndexItem;
import scal.io.liger.model.sqlbrite.InstanceIndexItemDao;

/**
 * Created by mnbogner on 8/28/15.
 */
public class StorymakerIndexManager {

    private static String availableIndexName = "available_index";
    private static String installedIndexName = "installed_index.json";
    private static String instanceIndexName = "instance_index.json";
    private static String contentIndexName = "content_index.json";
    private static String contentMetadataName = "content_metadata.json";

    public static String noPatchFile = "NOPATCH";

    // TODO Temporarily public for debugging convenience
    public static HashMap<String, ArrayList<ExpansionIndexItem>> cachedIndexes = new HashMap<>();
    public static ArrayList<InstanceIndexItem> cachedInstances = new ArrayList<>();

    public static String buildFileAbsolutePath(ExpansionIndexItem item,
                                               String mainOrPatch,
                                               Context context) {

        // Use File constructor to avoid duplicate or missing file path separators after concatenation
        return new File(buildFilePath(item, context) + buildFileName(item, mainOrPatch)).getAbsolutePath();

    }

    public static String buildFileName(ExpansionIndexItem item,
                                       String mainOrPatch) {
        if (Constants.MAIN.equals(mainOrPatch)) {
            return item.getExpansionId() + "." + mainOrPatch + "." + item.getExpansionFileVersion() + ".obb";
        } else if (Constants.PATCH.equals(mainOrPatch)) {
            if (item.getPatchFileVersion() == null) {
                // not really an error, removing message
                // Log.d("INDEX", "CAN'T CONSTRUCT FILENAME FOR " + item.getExpansionId() + ", PATCH VERSION IS NULL");
                return noPatchFile;
            } else {
                return item.getExpansionId() + "." + mainOrPatch + "." + item.getPatchFileVersion() + ".obb";
            }
        } else {
            Log.d("INDEX", "CAN'T CONSTRUCT FILENAME FOR " + item.getExpansionId() + ", DON'T UNDERSTAND " + mainOrPatch);
            // this is not the same as having no patch
            // return noPatchFile;
            return "FOO";
        }
    }



    // adding these to smooth transition to storymaker ExpansionIndexItem class

    public static String buildFileAbsolutePath(String expansionId,
                                               String mainOrPatch,
                                               String version,
                                               Context context) {

        String filePath = buildFilePath(context);

        String fileName = buildFileName(expansionId, mainOrPatch, version);

        return new File(filePath, fileName).getAbsolutePath();
    }

    public static String buildFilePath(Context context) {

        return StorageHelper.getActualStorageDirectory(context).getPath();
    }

    public static String buildFileName(String expansionId,
                                       String mainOrPatch,
                                       String version) {

        return expansionId + "." + mainOrPatch + "." + version + ".obb";
    }



    public static String buildFilePath(ExpansionIndexItem item, Context context) {

        // TODO - switching to the new storage method ignores the value set in the expansion index item
        // String checkPath = Environment.getExternalStorageDirectory().toString() + File.separator + item.getExpansionFilePath();
        String checkPath = StorageHelper.getActualStorageDirectory(context).getPath() + File.separator;

        File checkDir = new File(checkPath);
        if (checkDir.isDirectory() || checkDir.mkdirs()) {
            return checkPath;
        } else {
            Log.d("INDEX", "CAN'T CONSTRUCT PATH FOR " + item.getExpansionId() + ", PATH " + item.getExpansionFilePath() + " DOES NOT EXIST AND COULD NOT BE CREATED");
            return null;
        }
    }

    // only available index should be copied, so collapsing methods

    public static void copyAvailableIndex(Context context, boolean forceCopy) {

        AssetManager assetManager = context.getAssets();

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        Log.d("INDEX", "COPYING JSON FILE " + availableIndexName + ".json" + " FROM ASSETS TO " + jsonFilePath);

        // only replace file if version is different
        File jsonFile = new File(jsonFilePath + getAvailableVersionName());
        if (jsonFile.exists() && !forceCopy) {
            Log.d("INDEX", "JSON FILE " + jsonFile.getName() + " ALREADY EXISTS IN " + jsonFilePath + ", NOT COPYING");
            return;
        } else {

            // delete old patch versions
            String nameFilter = availableIndexName + "." + "*" + ".json";

            Log.d("INDEX", "CLEANUP: DELETING " + nameFilter + " FROM " + jsonFilePath);

            WildcardFileFilter indexFileFilter = new WildcardFileFilter(nameFilter);
            File indexDirectory = new File(jsonFilePath);
            for (File indexFile : FileUtils.listFiles(indexDirectory, indexFileFilter, null)) {
                Log.d("INDEX", "CLEANUP: FOUND " + indexFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(indexFile);
            }

            // delete old un-numbered files
            File oldFile = new File(jsonFilePath + availableIndexName + ".json");
            if (oldFile.exists()) {
                Log.d("INDEX", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(oldFile);
            }
        }

        InputStream assetIn = null;
        OutputStream assetOut = null;

        try {
            assetIn = assetManager.open(availableIndexName + ".json");

            assetOut = new FileOutputStream(jsonFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = assetIn.read(buffer)) != -1) {
                assetOut.write(buffer, 0, read);
            }
            assetIn.close();
            assetIn = null;
            assetOut.flush();
            assetOut.close();
            assetOut = null;
        } catch (IOException ioe) {
            Log.e("INDEX", "COPYING JSON FILE " + availableIndexName + ".json" + " FROM ASSETS TO " + jsonFilePath + " FAILED");
            return;
        }

        // check for zero-byte files
        if (jsonFile.exists() && (jsonFile.length() == 0)) {
            Log.e("INDEX", "COPYING JSON FILE " + availableIndexName + ".json" + " FROM ASSETS TO " + jsonFilePath + " FAILED (FILE WAS ZERO BYTES)");
            jsonFile.delete();
        }

        return;
    }

    // need to move this elsewhere
    public static File copyThumbnail(Context context, String thumbnailFileName) {

        AssetManager assetManager = context.getAssets();

        String thumbnailFilePath = ZipHelper.getFileFolderName(context);

        Log.d("INDEX", "COPYING THUMBNAIL FILE " + thumbnailFileName + " FROM ASSETS TO " + thumbnailFilePath);

        File thumbnailFile = new File(thumbnailFilePath + thumbnailFileName);

        if (thumbnailFile.exists()) {
            Log.d("INDEX", "THUMBNAIL FILE " + thumbnailFileName + " ALREADY EXISTS IN " + thumbnailFilePath + ", DELETING");
            thumbnailFile.delete();
        }

        File thumbnailDirectory = new File(thumbnailFile.getParent());
        if (!thumbnailDirectory.exists()) {
            thumbnailDirectory.mkdirs();
        }

        InputStream assetIn = null;
        OutputStream assetOut = null;

        try {
            assetIn = assetManager.open(thumbnailFileName);

            assetOut = new FileOutputStream(thumbnailFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = assetIn.read(buffer)) != -1) {
                assetOut.write(buffer, 0, read);
            }
            assetIn.close();
            assetIn = null;
            assetOut.flush();
            assetOut.close();
            assetOut = null;
        } catch (IOException ioe) {
            Log.e("INDEX", "COPYING THUMBNAIL FILE " + thumbnailFileName + " FROM ASSETS TO " + thumbnailFilePath + " FAILED: " + ioe.getLocalizedMessage());
            return null;
        }

        // check for zero-byte files
        if (thumbnailFile.exists() && (thumbnailFile.length() == 0)) {
            Log.e("INDEX", "COPYING THUMBNAIL FILE " + thumbnailFileName + " FROM ASSETS TO " + thumbnailFilePath + " FAILED (FILE WAS ZERO BYTES)");
            thumbnailFile.delete();
            return null;
        }

        return thumbnailFile;
    }

    public static HashMap<String, ExpansionIndexItem> loadAvailableFileIndex(Context context, Dao dao) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, getAvailableVersionName(), dao);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            // construct name (index by main file names)
            String fileName = buildFileName(item, Constants.MAIN);
            indexMap.put(fileName, item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadInstalledFileIndex(Context context, Dao dao) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, installedIndexName, dao);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            // construct names (index by main and patch file names)
            String mainName = buildFileName(item, Constants.MAIN);
            indexMap.put(mainName, item);
            String patchName = buildFileName(item, Constants.MAIN);
            indexMap.put(patchName, item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadAvailableOrderIndex(Context context, Dao dao) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, getAvailableVersionName(), dao);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getPatchOrder(), item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadInstalledOrderIndex(Context context, Dao dao) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, installedIndexName, dao);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getPatchOrder(), item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadAvailableIdIndex(Context context, Dao dao) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, getAvailableVersionName(), dao);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getExpansionId(), item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadInstalledIdIndex(Context context, Dao dao) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, installedIndexName, dao);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getExpansionId(), item);
        }

        return indexMap;
    }

    // supressing messages for less text during polling

    /**
     * This method does an unacceptable amount of work for synchronous use from the main thread
     */
    @Deprecated
    private static ArrayList<ExpansionIndexItem> loadIndex(Context context, final String jsonFileName, Dao dao) {

        if (!cachedIndexes.containsKey(jsonFileName)) {

            // REPLACING FILE ACCESS WITH DB ACCESS

            if (jsonFileName.contains(Constants.AVAILABLE)) {
                if (dao instanceof AvailableIndexItemDao) {

                    AvailableIndexItemDao availableDao = (AvailableIndexItemDao)dao;

                    availableDao.getAvailableIndexItems().subscribe(new Action1<List<AvailableIndexItem>>() {

                        @Override
                        public void call(List<AvailableIndexItem> availableIndexItems) {

                            ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();

                            for (AvailableIndexItem item : availableIndexItems) {
                                indexList.add(item);
                            }

                            cachedIndexes.put(jsonFileName, indexList);
                        }
                    });
                } else {
                    //error
                }
            } else if (jsonFileName.contains(Constants.INSTALLED)) {
                if (dao instanceof InstalledIndexItemDao) {

                    InstalledIndexItemDao installedDao = (InstalledIndexItemDao)dao;

                    installedDao.getInstalledIndexItems().subscribe(new Action1<List<InstalledIndexItem>>() {

                        @Override
                        public void call(List<InstalledIndexItem> installedIndexItems) {

                            ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();

                            for (InstalledIndexItem item : installedIndexItems) {
                                indexList.add(item);
                            }

                            cachedIndexes.put(jsonFileName, indexList);
                        }
                    });
                } else {
                    //error
                }
            } else {
                //error
            }
        }

        ArrayList<ExpansionIndexItem> returnList = cachedIndexes.get(jsonFileName);

        // code downstream is not expecting a null
        if (returnList == null) {
            returnList = new ArrayList<ExpansionIndexItem>();
        }

        //Log.d("loadIndex", String.format("%d index items loaded for %s in %d ms", indexList.size(), jsonFileName, System.currentTimeMillis() - startTime));
        return returnList;
    }

    // only one key option for instance index
    public static HashMap<String, InstanceIndexItem> loadInstanceIndex(Context context, Dao dao) {

        // REPLACING FILE ACCESS WITH DB ACCESS

        if (dao instanceof InstanceIndexItemDao) {

            InstanceIndexItemDao instanceDao = (InstanceIndexItemDao) dao;

            instanceDao.getInstanceIndexItems().subscribe(new Action1<List<InstanceIndexItem>>() {

                @Override
                public void call(List<InstanceIndexItem> instanceIndexItems) {

                    cachedInstances = new ArrayList<InstanceIndexItem>();

                    for (InstanceIndexItem item : instanceIndexItems) {
                        cachedInstances.add(item);
                    }
                }
            });
        } else {
            //error
        }

        // code downstream is not expecting a null
        HashMap<String, InstanceIndexItem> returnMap = new HashMap<String, InstanceIndexItem>();

        if (cachedInstances.size() > 0) {
            for (InstanceIndexItem item : cachedInstances) {
                returnMap.put(item.getInstanceFilePath(), item);
            }
        }

        //Log.d("loadIndex", String.format("%d index items loaded for %s in %d ms", indexList.size(), jsonFileName, System.currentTimeMillis() - startTime));
        return returnMap;
    }

    // only one key option for instance index
    public static ArrayList<InstanceIndexItem> loadInstanceIndexAsList(Context context, Dao dao) {

        // REPLACING FILE ACCESS WITH DB ACCESS

        if (dao instanceof InstanceIndexItemDao) {

            InstanceIndexItemDao instanceDao = (InstanceIndexItemDao) dao;

            instanceDao.getInstanceIndexItems().subscribe(new Action1<List<InstanceIndexItem>>() {

                @Override
                public void call(List<InstanceIndexItem> instanceIndexItems) {

                    cachedInstances = new ArrayList<InstanceIndexItem>();

                    for (InstanceIndexItem item : instanceIndexItems) {
                        cachedInstances.add(item);
                    }
                }
            });
        } else {
            //error
        }

        // code downstream is not expecting a null

        if (cachedInstances.size() > 0) {
            return cachedInstances;
        } else {
            return new ArrayList<InstanceIndexItem>();
        }
    }

    // REMOVED CONTENT METHODS AS THEY ARE NOT DATABASE RELATED

    public static HashMap<String, InstanceIndexItem> fillInstanceIndex(Context context, HashMap<String, InstanceIndexItem> indexList, String language, Dao dao) {

        ArrayList<File> instanceFiles = JsonHelper.getLibraryInstanceFiles(context);

        boolean forceSave = false; // need to resolve issue of unset language in existing record preventing update to index

        int initialSize = indexList.size();

        // make a pass to remove deleted files from the index

        ArrayList<String> keys = new ArrayList<String>();

        for (String key : indexList.keySet()) {
            InstanceIndexItem item = indexList.get(key);
            File checkFile = new File(item.getInstanceFilePath());
            if (!checkFile.exists()) {
                // Log.d("INDEX", "REMOVING INDEX ITEM FOR MISSING INSTANCE FILE " + item.getInstanceFilePath());
                keys.add(key);
            }
        }

        for (String key: keys) {
            // NO!  (need to determine how to deal with iocipher virtual files)
            // indexList.remove(key);
        }

        // check for changes
        if (indexList.size() != initialSize) {
            Log.d("INDEX", Math.abs(indexList.size() - initialSize) + " ITEMS REMOVED FROM INSTANCE INDEX, FORCING SAVE");
            // update flag
            forceSave = true;
            // update initial size
            initialSize = indexList.size();
        }

        // make a pass to add non-indexed files

        for (final File f : instanceFiles) {
            if (indexList.containsKey(f.getAbsolutePath()) && language.equals(indexList.get(f.getAbsolutePath()).getLanguage())) {
                Log.d("INDEX", "FOUND INDEX ITEM FOR INSTANCE FILE " + f.getAbsolutePath());
            } else {
                Log.d("INDEX", "ADDING INDEX ITEM FOR INSTANCE FILE " + f.getAbsolutePath());

                forceSave = true;

                String[] parts = FilenameUtils.removeExtension(f.getName()).split("-");
                String datePart = parts[parts.length - 1]; // FIXME make more robust
                Date date = new Date(Long.parseLong(datePart));

                InstanceIndexItem newItem = new InstanceIndexItem();
                newItem.setInstanceFilePath(f.getAbsolutePath());
                newItem.setStoryCreationDate(date.getTime());

                String jsonString = JsonHelper.loadJSON(f.getPath(), context, "en"); // FIXME don't hardcode "en"

                // if no string was loaded, cannot continue
                if (jsonString == null) {
                    Log.e("INDEX", "json could not be loaded from " + f.getPath());
                    // handle the same way as null spl case below
                    return indexList;
                }

                ArrayList<String> referencedFiles = new ArrayList<String>(); // should not need to insert dependencies to check metadata
                StoryPathLibrary spl = JsonHelper.deserializeStoryPathLibrary(jsonString, f.getAbsolutePath(), referencedFiles, context, language);

                if (spl == null) {
                    return indexList;
                }

                // set language
                newItem.setLanguage(language);

                // first check local metadata fields
                newItem.setTitle(spl.getMetaTitle());
                newItem.setStoryType(spl.getMetaDescription()); // this seems more useful than medium
                newItem.setThumbnailPath(spl.getMetaThumbnail());

                // unsure where to put additional fields

                // if anything is missing, open story path
                if ((newItem.getTitle() == null) ||
                        (newItem.getStoryType() == null) ||
                        (newItem.getThumbnailPath() == null)) {
                    Log.d("INDEX", "MISSING METADATA, OPENING STORY PATH FOR INSTANCE FILE " + f.getAbsolutePath());

                    if (spl.getCurrentStoryPathFile() != null) {
                        spl.loadStoryPathTemplate("CURRENT", false);
                    }

                    StoryPath currentStoryPath = spl.getCurrentStoryPath();

                    if (currentStoryPath != null) {
                        // null values will be handled by the index card builder
                        if (newItem.getTitle() == null) {
                            newItem.setTitle(currentStoryPath.getTitle());
                        }
                        if (newItem.getStoryType() == null) {
                            newItem.setStoryType(currentStoryPath.getMedium());
                        }
                        if (newItem.getThumbnailPath() == null) {
                            newItem.setThumbnailPath(spl.getMetaThumbnail());
                        }
                    }
                } else {
                    Log.d("INDEX", "METADATA COMPLETE FOR INSTANCE FILE " + f.getAbsolutePath());
                }

                indexList.put(newItem.getInstanceFilePath(), newItem);
            }
        }

        // check for changes again
        if (indexList.size() != initialSize) {
            Log.d("INDEX", Math.abs(indexList.size() - initialSize) + " ITEMS ADDED TO INSTANCE INDEX, FORCING SAVE");
            // update flag
            forceSave = true;
            // update initial size
            initialSize = indexList.size();
        }

        // persist updated index (if necessary)
        if (forceSave) {
            ArrayList<InstanceIndexItem> indexArray = new ArrayList<InstanceIndexItem>(indexList.values());
            saveInstanceIndex(context, indexArray, instanceIndexName, dao);
        } else {
            Log.d("INDEX", "NOTHING ADDED TO/REMOVED FROM INSTANCE INDEX, NO SAVE");
        }

        return indexList;
    }

    public static void installedIndexAdd(Context context, InstalledIndexItem indexItem, Dao dao) {

        HashMap<String, ExpansionIndexItem> indexMap = loadInstalledIdIndex(context, dao);

        indexMap.put(indexItem.getExpansionId(), indexItem);
        ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();
        for (ExpansionIndexItem eii : indexMap.values()) {
            indexList.add(eii);
        }
        saveIndex(context, indexList, installedIndexName, dao);
        return;
    }

    public static void installedIndexRemove(Context context, InstalledIndexItem indexItem, Dao dao) {

        HashMap<String, ExpansionIndexItem> indexMap = loadInstalledIdIndex(context, dao);

        if (indexMap.keySet().contains(indexItem.getExpansionId())) {
            indexMap.remove(indexItem.getExpansionId());
            ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();
            for (ExpansionIndexItem eii : indexMap.values()) {
                indexList.add(eii);
            }

            // need to actually delete item from db (saving updated list will not remove it)
            if (dao instanceof InstalledIndexItemDao) {

                InstalledIndexItemDao installedDao = (InstalledIndexItemDao) dao;
                installedDao.removeInstalledIndexItem(indexItem);

                Log.d("INDEX", "UN-INSTALLED " + indexItem.getExpansionId() + " FROM INDEX");

            } else {

                Log.e("INDEX", "FAILED TO UN-INSTALL " + indexItem.getExpansionId() + " (DAO CASTING ISSUE)");

            }

            saveIndex(context, indexList, installedIndexName, dao);
            return;
        }
    }

    public static void saveAvailableIndex(Context context, HashMap<String, ExpansionIndexItem> indexMap, Dao dao) {

        saveIndex(context, new ArrayList(indexMap.values()), getAvailableVersionName(), dao);

        return;
    }

    public static void saveInstalledIndex(Context context, HashMap<String, ExpansionIndexItem> indexMap, Dao dao) {

        saveIndex(context, new ArrayList(indexMap.values()), installedIndexName, dao);

        return;
    }

    private static void saveIndex(Context context, ArrayList<ExpansionIndexItem> indexList, String jsonFileName, Dao dao) {

        // need to update cached index
        if (cachedIndexes.containsKey(jsonFileName)) {
            cachedIndexes.put(jsonFileName, indexList);
        }

        // need to purge ZipHelper cache to force update
        ZipHelper.clearCache();

        // REPLACING FILE ACCESS WITH DB ACCESS

        if (jsonFileName.contains(Constants.AVAILABLE)) {
            if (dao instanceof AvailableIndexItemDao) {

                Log.d("INDEX", "SAVING AVAILABLE INDEX");

                AvailableIndexItemDao availableDao = (AvailableIndexItemDao)dao;

                for (ExpansionIndexItem item : indexList) {
                    if (item instanceof AvailableIndexItem) {
                        availableDao.addAvailableIndexItem((AvailableIndexItem) item, true);
                    } else {
                        // error

                        Log.e("INDEX", "ITEM MISMATCH? " + item.getExpansionId());

                    }
                }
            } else {
                //error

                Log.e("INDEX", "DAO MISMATCH? " + jsonFileName);

            }
        } else if (jsonFileName.contains(Constants.INSTALLED)) {
            if (dao instanceof InstalledIndexItemDao) {

                Log.d("INDEX", "SAVING INSTALLED INDEX");

                InstalledIndexItemDao installedDao = (InstalledIndexItemDao)dao;

                for (ExpansionIndexItem item : indexList) {
                    if (item instanceof InstalledIndexItem) {
                        installedDao.addInstalledIndexItem((InstalledIndexItem) item, true);
                    } else {
                        // error

                        Log.e("INDEX", "ITEM MISMATCH? " + item.getExpansionId());

                    }
                }
            } else {
                //error

                Log.e("INDEX", "DAO MISMATCH? " + jsonFileName);

            }
        } else {
            //error
        }
    }

    public static void instanceIndexAdd(Context context, InstanceIndexItem addItem, HashMap<String, InstanceIndexItem> indexList, Dao dao) {

        indexList.put(addItem.getInstanceFilePath(), addItem);

        ArrayList<InstanceIndexItem> indexArray = new ArrayList<InstanceIndexItem>(indexList.values());

        saveInstanceIndex(context, indexArray, instanceIndexName, dao);

    }

    public static void instanceIndexRemove(Context context, InstanceIndexItem removeItem, HashMap<String, InstanceIndexItem> indexList, boolean deleteFiles, boolean deleteMedia, Dao dao) {

        indexList.remove(removeItem.getInstanceFilePath());

        ArrayList<InstanceIndexItem> indexArray = new ArrayList<InstanceIndexItem>(indexList.values());

        saveInstanceIndex(context, indexArray, instanceIndexName, dao);

        if (deleteFiles) {
            removeItem.deleteAssociatedFiles(context, deleteMedia);
        }
    }

    public static void saveInstanceIndex(Context context, ArrayList<InstanceIndexItem> indexList, String jsonFileName, Dao dao) {

        // REPLACING FILE ACCESS WITH DB ACCESS

        if (dao instanceof InstanceIndexItemDao) {

            InstanceIndexItemDao instanceDao = (InstanceIndexItemDao) dao;

            for (InstanceIndexItem item : indexList) {
                instanceDao.addInstanceIndexItem(item, true);
            }
        } else {
            //error
        }
    }

    public static String getAvailableVersionName() {
        return availableIndexName + "." + Constants.AVAILABLE_INDEX_VERSION + ".json";
    }
}
