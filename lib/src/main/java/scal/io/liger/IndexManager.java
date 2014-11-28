package scal.io.liger;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import scal.io.liger.model.ExpansionIndexItem;

/**
 * Created by mnbogner on 11/24/14.
 */
public class IndexManager {

    private static String availableIndexName = "available_index.json";
    private static String installedIndexName = "installed_index.json";

    public static void copyAvailableIndex(Context context) {

        copyIndex(context, availableIndexName);

        return;
    }

    public static void copyInstalledIndex(Context context) {

        copyIndex(context, installedIndexName);

        return;
    }

    private static void copyIndex(Context context, String jsonFileName) {

        AssetManager assetManager = context.getAssets();

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        Log.d("INDEX", "COPYING JSON FILE " + jsonFileName + " FROM ASSETS TO " + jsonFilePath);

        File jsonFile = new File(jsonFilePath + jsonFileName);
        if (jsonFile.exists()) {
            jsonFile.delete();
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

        return;
    }

    public static HashMap<String, ExpansionIndexItem> loadAvailableFileIndex(Context context) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, availableIndexName);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getExpansionFileName(), item);
        }

        return indexMap;
    }

    public static HashMap<String, ExpansionIndexItem> loadInstalledFileIndex(Context context) {

        ArrayList<ExpansionIndexItem> indexList  = loadIndex(context, installedIndexName);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getExpansionFileName(), item);
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

        ArrayList<ExpansionIndexItem> indexList  = loadIndex(context, installedIndexName);

        HashMap<String, ExpansionIndexItem> indexMap = new HashMap<String, ExpansionIndexItem>();

        for (ExpansionIndexItem item : indexList) {
            indexMap.put(item.getPatchOrder(), item);
        }

        return indexMap;
    }

    private static ArrayList<ExpansionIndexItem> loadIndex(Context context, String jsonFileName) {

        String indexJson = null;
        ArrayList<ExpansionIndexItem> indexList = new ArrayList<ExpansionIndexItem>();

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        Log.d("INDEX", "READING JSON FILE " + jsonFilePath + jsonFileName + " FROM SD CARD");

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

            indexList = gson.fromJson(indexJson, new TypeToken<ArrayList<ExpansionIndexItem>>() {}.getType());
        }

        return indexList;
    }

    public static void registerAvailableIndexItem (Context context, ExpansionIndexItem indexItem) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, availableIndexName);
        indexList.add(indexItem);
        saveIndex(context, indexList, availableIndexName);
        return;
    }

    public static void registerInstalledIndexItem (Context context, ExpansionIndexItem indexItem) {

        ArrayList<ExpansionIndexItem> indexList = loadIndex(context, installedIndexName);
        indexList.add(indexItem);
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
}
