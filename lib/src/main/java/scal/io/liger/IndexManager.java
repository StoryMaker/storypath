package scal.io.liger;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import scal.io.liger.model.ContentPackMetadata;
import scal.io.liger.model.ExpansionIndexItem;
import scal.io.liger.model.InstanceIndexItem;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;

/**
 * Created by mnbogner on 11/24/14.
 */
public class IndexManager {

    private static String availableIndexName = "available_index.json";
    private static String installedIndexName = "installed_index.json";
    private static String instanceIndexName = "instance_index.json";
    private static String contentIndexName = "content_index.json";
    private static String contentMetadataName = "content_metadata.json";

    public static String noPatchFile = "NOPATCH";

    public static String buildFileName(ExpansionIndexItem item, String mainOrPatch) {
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

    public static String buildFilePath(ExpansionIndexItem item) {

        String checkPath = Environment.getExternalStorageDirectory().toString() + File.separator + item.getExpansionFilePath();

        File checkDir = new File(checkPath);
        if (checkDir.isDirectory() || checkDir.mkdirs()) {
            return checkPath;
        } else {
            Log.d("INDEX", "CAN'T CONSTRUCT PATH FOR " + item.getExpansionId() + ", PATH " + item.getExpansionFilePath() + " DOES NOT EXIST AND COULD NOT BE CREATED");
            return null;
        }
    }

    public static void copyAvailableIndex(Context context) {

        copyIndex(context, availableIndexName);

        return;
    }

    public static void copyInstalledIndex(Context context) {

        copyIndex(context, installedIndexName);

        return;
    }

    // instance and content index shouldn't be part of assets

    private static void copyIndex(Context context, String jsonFileName) {

        AssetManager assetManager = context.getAssets();

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        Log.d("INDEX", "COPYING JSON FILE " + jsonFileName + " FROM ASSETS TO " + jsonFilePath);

        File jsonFile = new File(jsonFilePath + jsonFileName);
        // do not replace installed index
        if (jsonFileName.equals(availableIndexName) && jsonFile.exists()) {
            Log.d("INDEX", "JSON FILE " + jsonFileName + " ALREADY EXISTS IN " + jsonFilePath + ", DELETING");
            jsonFile.delete();
        }
        if (jsonFileName.equals(installedIndexName) && jsonFile.exists()) {
            Log.d("INDEX", "JSON FILE " + jsonFileName + " ALREADY EXISTS IN " + jsonFilePath + ", NOT COPYING");
            return;
        }

        InputStream assetIn = null;
        OutputStream assetOut = null;

        try {
            assetIn = assetManager.open(jsonFileName);

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
            Log.e("INDEX", "COPYING JSON FILE " + jsonFileName + " FROM ASSETS TO " + jsonFilePath + " FAILED");
            return;
        }

        // check for zero-byte files
        if (jsonFile.exists() && (jsonFile.length() == 0)) {
            Log.e("INDEX", "COPYING JSON FILE " + jsonFileName + " FROM ASSETS TO " + jsonFilePath + " FAILED (FILE WAS ZERO BYTES)");
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

    public static HashMap<String, ExpansionIndexItem> loadAvailableFileIndex(Context context) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, availableIndexName);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            // construct name (index by main file names)
            String fileName = buildFileName(item, Constants.MAIN);
            indexMap.put(fileName, item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadInstalledFileIndex(Context context) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, installedIndexName);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            // construct name (index by main file names)
            String fileName = buildFileName(item, Constants.MAIN);
            indexMap.put(fileName, item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadAvailableOrderIndex(Context context) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, availableIndexName);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getPatchOrder(), item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadInstalledOrderIndex(Context context) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, installedIndexName);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getPatchOrder(), item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadAvailableIdIndex(Context context) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, availableIndexName);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getExpansionId(), item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadInstalledIdIndex(Context context) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, installedIndexName);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getExpansionId(), item);
        }

        return indexMap;
    }

    // supressing messages for less text during polling
    private static ArrayList<ExpansionIndexItem> loadIndex(Context context, String jsonFileName) {

        String indexJson = null;
        ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        // Log.d("INDEX", "READING JSON FILE " + jsonFilePath + jsonFileName + " FROM SD CARD");

        File jsonFile = new File(jsonFilePath + jsonFileName);
        if (!jsonFile.exists()) {
            Log.e("INDEX", jsonFilePath + jsonFileName + " WAS NOT FOUND");
            return indexList;
        }

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                InputStream jsonStream = new FileInputStream(jsonFile);

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonStream = null;
                indexJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e("INDEX", "READING JSON FILE " + jsonFilePath + jsonFileName + " FROM SD CARD FAILED");
                return indexList;
            }
        } else {
            Log.e("INDEX", "SD CARD WAS NOT FOUND");
            return indexList;
        }

        if ((indexJson != null) && (indexJson.length() > 0)) {
            GsonBuilder gBuild = new GsonBuilder();
            Gson gson = gBuild.create();

            indexList = gson.fromJson(indexJson, new TypeToken<ArrayList<ExpansionIndexItem>>() {
            }.getType());
        }

        return indexList;
    }

    // only one key option for instance index
    public static HashMap<String, InstanceIndexItem> loadInstanceIndex(Context context) {

        HashMap<String, InstanceIndexItem> indexMap = new HashMap<String, InstanceIndexItem>();

        String indexJson = null;
        ArrayList<InstanceIndexItem> indexList = new ArrayList<InstanceIndexItem>();

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        Log.d("INDEX", "READING JSON FILE " + jsonFilePath + instanceIndexName + " FROM SD CARD");

        File jsonFile = new File(jsonFilePath + instanceIndexName);
        if (!jsonFile.exists()) {
            Log.d("INDEX", jsonFilePath + instanceIndexName + " WAS NOT FOUND");
        } else {

            String sdCardState = Environment.getExternalStorageState();

            if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
                try {
                    InputStream jsonStream = new FileInputStream(jsonFile);

                    int size = jsonStream.available();
                    byte[] buffer = new byte[size];
                    jsonStream.read(buffer);
                    jsonStream.close();
                    jsonStream = null;
                    indexJson = new String(buffer);
                } catch (IOException ioe) {
                    Log.e("INDEX", "READING JSON FILE " + jsonFilePath + instanceIndexName + " FROM SD CARD FAILED");
                }
            } else {
                Log.e("INDEX", "SD CARD WAS NOT FOUND");
                return indexMap; // if there's no card, there's nowhere to read instance files from, so just stop here
            }

            if ((indexJson != null) && (indexJson.length() > 0)) {
                GsonBuilder gBuild = new GsonBuilder();
                Gson gson = gBuild.create();

                indexList = gson.fromJson(indexJson, new TypeToken<ArrayList<InstanceIndexItem>>() {
                }.getType());
            }

            for (InstanceIndexItem item : indexList) {
                indexMap.put(item.getInstanceFilePath(), item);
            }
        }

        return indexMap;
    }

    // only one key option for content index, file is loaded from a zipped content pack
    // content index is read only, no register/update/save methods
    // TODO this should leverage the loadContentIndexAsList to avoid dupliction
    public static HashMap<String, InstanceIndexItem> loadContentIndex(Context context, String packageName, String expansionId, String language) {

        String contentJson = null;
        ArrayList<InstanceIndexItem> contentList = new ArrayList<InstanceIndexItem>();
        HashMap<String, InstanceIndexItem> contentMap = new HashMap<String, InstanceIndexItem>();

        String contentPath = packageName + File.separator + expansionId + File.separator + contentIndexName;

        Log.d("INDEX", "READING JSON FILE " + contentPath + " FROM ZIP FILE");

        try {
            InputStream jsonStream = ZipHelper.getFileInputStream(contentPath, context, language);

            if (jsonStream == null) {
                Log.e("INDEX", "READING JSON FILE " + contentPath + " FROM ZIP FILE FAILED (STREAM WAS NULL)");
                return contentMap;
            }

            int size = jsonStream.available();
            byte[] buffer = new byte[size];
            jsonStream.read(buffer);
            jsonStream.close();
            contentJson = new String(buffer);

            if ((contentJson != null) && (contentJson.length() > 0)) {
                GsonBuilder gBuild = new GsonBuilder();
                Gson gson = gBuild.create();

                contentList = gson.fromJson(contentJson, new TypeToken<ArrayList<InstanceIndexItem>>() {
                }.getType());
            }

            for (InstanceIndexItem item : contentList) {
                contentMap.put(item.getInstanceFilePath(), item);
            }
        } catch (IOException ioe) {
            Log.e("INDEX", "READING JSON FILE " + contentPath + " FROM ZIP FILE FAILED: " + ioe.getMessage());
            return contentMap;
        }

        return contentMap;
    }


    // only one key option for content index, file is loaded from a zipped content pack
    // content index is read only, no register/update/save methods
    public static ArrayList<InstanceIndexItem> loadContentIndexAsList(Context context, String packageName, String expansionId, String language) {

        String contentJson = null;
        ArrayList<InstanceIndexItem> contentList = new ArrayList<InstanceIndexItem>();

        String contentPath = packageName + File.separator + expansionId + File.separator + contentIndexName;

        Log.d("INDEX", "READING JSON FILE " + contentPath + " FROM ZIP FILE");

        try {
            InputStream jsonStream = ZipHelper.getFileInputStream(contentPath, context, language);

            if (jsonStream == null) {
                Log.e("INDEX", "READING JSON FILE " + contentPath + " FROM ZIP FILE FAILED (STREAM WAS NULL)");
                return contentList;
            }

            int size = jsonStream.available();
            byte[] buffer = new byte[size];
            jsonStream.read(buffer);
            jsonStream.close();
            contentJson = new String(buffer);

            if ((contentJson != null) && (contentJson.length() > 0)) {
                GsonBuilder gBuild = new GsonBuilder();
                Gson gson = gBuild.create();

                contentList = gson.fromJson(contentJson, new TypeToken<ArrayList<InstanceIndexItem>>() {
                }.getType());
            }
        } catch (IOException ioe) {
            Log.e("INDEX", "READING JSON FILE " + contentPath + " FROM ZIP FILE FAILED: " + ioe.getMessage());
        }

        return contentList;
    }

    // not strictly an index, but including here because code is similar
    public static ContentPackMetadata loadContentMetadata(Context context, String packageName, String expansionId, String language) {

        String metadataJson = null;
        ContentPackMetadata metadata = null;

        String metadataPath = packageName + File.separator + expansionId + File.separator + contentMetadataName;

        Log.d("INDEX", "READING JSON FILE " + metadataPath + " FROM ZIP FILE");

        try {
            InputStream jsonStream = ZipHelper.getFileInputStream(metadataPath, context, language);

            if (jsonStream == null) {
                Log.e("INDEX", "READING JSON FILE " + metadataPath + " FROM ZIP FILE FAILED (STREAM WAS NULL)");
                return null;
            }

            int size = jsonStream.available();
            byte[] buffer = new byte[size];
            jsonStream.read(buffer);
            jsonStream.close();
            metadataJson = new String(buffer);

            if ((metadataJson != null) && (metadataJson.length() > 0)) {
                GsonBuilder gBuild = new GsonBuilder();
                Gson gson = gBuild.create();

                metadata = gson.fromJson(metadataJson, new TypeToken<ContentPackMetadata>() {
                }.getType());
            }
        } catch (IOException ioe) {
            Log.e("INDEX", "READING JSON FILE " + metadataPath + " FROM ZIP FILE FAILED: " + ioe.getMessage());
            return null;
        }

        return metadata;
    }


    public static HashMap<String, String> loadTempateIndex (Context context) {
        HashMap<String, String> templateMap = new HashMap<String, String>();

        ZipResourceFile zrf = ZipHelper.getResourceFile(context);
        ArrayList<ZipResourceFile.ZipEntryRO> zipEntries = new ArrayList<ZipResourceFile.ZipEntryRO>(Arrays.asList(zrf.getAllEntries()));
        for (ZipResourceFile.ZipEntryRO zipEntry : zipEntries) {
            // Log.d("INDEX", "GOT ITEM: " + zipEntry.mFileName);
            templateMap.put(zipEntry.mFileName.substring(zipEntry.mFileName.lastIndexOf(File.separator) + 1), zipEntry.mFileName);
        }

        return templateMap;
    }

    public static HashMap<String, InstanceIndexItem> fillInstanceIndex(Context context, HashMap<String, InstanceIndexItem> indexList, String language) {

        ArrayList<File> instanceFiles = JsonHelper.getLibraryInstanceFiles(context);

        boolean forceSave = false; // need to resolve issue of unset language in existing record preventing update to index

        int initialSize = indexList.size();

        for (final File f : instanceFiles) {
            if (indexList.containsKey(f.getAbsolutePath()) && language.equals(indexList.get(f.getAbsolutePath()).getLanguage())) {
                Log.d("INDEX", "FOUND INDEX ITEM FOR INSTANCE FILE " + f.getAbsolutePath());
            } else {
                Log.d("INDEX", "ADDING INDEX ITEM FOR INSTANCE FILE " + f.getAbsolutePath());

                forceSave = true;

                String[] parts = FilenameUtils.removeExtension(f.getName()).split("-");
                String datePart = parts[parts.length - 1]; // FIXME make more robust
                Date date = new Date(Long.parseLong(datePart));

                InstanceIndexItem newItem = new InstanceIndexItem(f.getAbsolutePath(), date.getTime());

                String jsonString = JsonHelper.loadJSON(f, "en"); // FIXME don't hardcode "en"
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

        // persist updated index (if necessary)
        if ((indexList.size() == initialSize) && !forceSave) {
            Log.d("INDEX", "NOTHING ADDED TO INSTANCE INDEX, NO SAVE");
        } else {
            Log.d("INDEX", (indexList.size() - initialSize) + " ITEMS ADDED TO INSTANCE INDEX, SAVING");
            ArrayList<InstanceIndexItem> indexArray = new ArrayList<InstanceIndexItem>(indexList.values());
            saveInstanceIndex(context, indexArray, instanceIndexName);
        }

        return indexList;
    }

    public static void registerAvailableIndexItem(Context context, ExpansionIndexItem indexItem) {

        HashMap<String, ExpansionIndexItem> indexMap = loadAvailableIdIndex(context);
        indexMap.put(indexItem.getExpansionId(), indexItem);
        ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();
        for (ExpansionIndexItem eii : indexMap.values()) {
            indexList.add(eii);
        }
        saveIndex(context, indexList, availableIndexName);
        return;
    }

    public static void registerInstalledIndexItem(Context context, ExpansionIndexItem indexItem) {

        HashMap<String, ExpansionIndexItem> indexMap = loadInstalledIdIndex(context);
        indexMap.put(indexItem.getExpansionId(), indexItem);
        ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();
        for (ExpansionIndexItem eii : indexMap.values()) {
            indexList.add(eii);
        }
        saveIndex(context, indexList, installedIndexName);
        return;
    }

    public static void unregisterAvailableIndexItem(Context context, ExpansionIndexItem indexItem) {

        HashMap<String, ExpansionIndexItem> indexMap = loadAvailableIdIndex(context);
        indexMap.remove(indexItem.getExpansionId());
        ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();
        for (ExpansionIndexItem eii : indexMap.values()) {
            indexList.add(eii);
        }
        saveIndex(context, indexList, availableIndexName);
        return;
    }

    public static void unregisterInstalledIndexItem(Context context, ExpansionIndexItem indexItem) {

        HashMap<String, ExpansionIndexItem> indexMap = loadInstalledIdIndex(context);
        indexMap.remove(indexItem.getExpansionId());
        ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();
        for (ExpansionIndexItem eii : indexMap.values()) {
            indexList.add(eii);
        }
        saveIndex(context, indexList, installedIndexName);
        return;
    }

    public static void unregisterAvailableIndexItem(Context context, String fileName) {

        HashMap<String, ExpansionIndexItem> indexMap = loadAvailableFileIndex(context);
        indexMap.remove(fileName);
        ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();
        for (ExpansionIndexItem eii : indexMap.values()) {
            indexList.add(eii);
        }
        saveIndex(context, indexList, availableIndexName);
        return;
    }

    public static void unregisterInstalledIndexItem(Context context, String fileName) {

        HashMap<String, ExpansionIndexItem> indexMap = loadInstalledFileIndex(context);
        indexMap.remove(fileName);
        ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();
        for (ExpansionIndexItem eii : indexMap.values()) {
            indexList.add(eii);
        }
        saveIndex(context, indexList, installedIndexName);
        return;
    }

    public static void saveAvailableIndex(Context context, HashMap<String, ExpansionIndexItem> indexMap) {

        saveIndex(context, new ArrayList(indexMap.values()), availableIndexName);

        return;
    }

    public static void saveInstalledIndex(Context context, HashMap<String, ExpansionIndexItem> indexMap) {

        saveIndex(context, new ArrayList(indexMap.values()), installedIndexName);

        return;
    }

    private static void saveIndex(Context context, ArrayList<ExpansionIndexItem> indexList, String jsonFileName) {

        String indexJson = "";

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        Log.d("INDEX", "WRITING JSON FILE " + jsonFilePath + jsonFileName + " TO SD CARD");

        File jsonFile = new File(jsonFilePath + jsonFileName + ".tmp"); // write to temp and rename
        if (jsonFile.exists()) {
            jsonFile.delete();
        }

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                jsonFile.createNewFile();

                FileOutputStream jsonStream = new FileOutputStream(jsonFile);

                GsonBuilder gBuild = new GsonBuilder();
                Gson gson = gBuild.create();

                indexJson = gson.toJson(indexList);

                byte[] buffer = indexJson.getBytes();
                jsonStream.write(buffer);
                jsonStream.flush();
                jsonStream.close();
                jsonStream = null;

                Process p = Runtime.getRuntime().exec("mv " + jsonFilePath + jsonFileName + ".tmp " + jsonFilePath + jsonFileName);

            } catch (IOException ioe) {
                Log.e("INDEX", "WRITING JSON FILE " + jsonFilePath + jsonFileName + " TO SD CARD FAILED");
                return;
            }
        } else {
            Log.e("INDEX", "SD CARD WAS NOT FOUND");
            return;
        }
    }

    public static void updateInstanceIndex(Context context, InstanceIndexItem newItem, HashMap<String, InstanceIndexItem> indexList) {

        indexList.put(newItem.getInstanceFilePath(), newItem);

        ArrayList<InstanceIndexItem> indexArray = new ArrayList<InstanceIndexItem>(indexList.values());

        saveInstanceIndex(context, indexArray, instanceIndexName);

    }

    public static void saveInstanceIndex(Context context, ArrayList<InstanceIndexItem> indexList, String jsonFileName) {

        String indexJson = "";

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        Log.d("INDEX", "WRITING JSON FILE " + jsonFilePath + jsonFileName + " TO SD CARD");

        File jsonFile = new File(jsonFilePath + jsonFileName + ".tmp"); // write to temp and rename
        if (jsonFile.exists()) {
            jsonFile.delete();
        }

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                jsonFile.createNewFile();

                FileOutputStream jsonStream = new FileOutputStream(jsonFile);

                GsonBuilder gBuild = new GsonBuilder();
                Gson gson = gBuild.create();

                indexJson = gson.toJson(indexList);

                byte[] buffer = indexJson.getBytes();
                jsonStream.write(buffer);
                jsonStream.flush();
                jsonStream.close();
                jsonStream = null;

                Process p = Runtime.getRuntime().exec("mv " + jsonFilePath + jsonFileName + ".tmp " + jsonFilePath + jsonFileName);

            } catch (IOException ioe) {
                Log.e("INDEX", "WRITING JSON FILE " + jsonFilePath + jsonFileName + " TO SD CARD FAILED");
                return;
            }
        } else {
            Log.e("INDEX", "SD CARD WAS NOT FOUND");
            return;
        }
    }
}
