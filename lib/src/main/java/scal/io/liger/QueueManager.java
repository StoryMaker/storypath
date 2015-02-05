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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import scal.io.liger.model.ExpansionIndexItem;
import scal.io.liger.model.InstanceIndexItem;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;

/**
 * Created by mnbogner on 11/24/14.
 */
public class QueueManager {

    private static String downloadQueueName = "download_queue.json";

    public static HashMap<Long, String> loadQueue(Context context) {

        String queueJson = null;
        HashMap<Long, String> queueList = new HashMap<Long, String>();

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        //Log.d("QUEUE", "READING JSON FILE " + jsonFilePath + downloadQueueName + " FROM SD CARD");

        File jsonFile = new File(jsonFilePath + downloadQueueName);
        if (!jsonFile.exists()) {
            Log.e("QUEUE", jsonFilePath + downloadQueueName + " WAS NOT FOUND");
            return queueList;
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
                queueJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e("QUEUE", "READING JSON FILE " + jsonFilePath + downloadQueueName + " FROM SD CARD FAILED");
                return queueList;
            }
        } else {
            Log.e("QUEUE", "SD CARD WAS NOT FOUND");
            return queueList;
        }

        if ((queueJson != null) && (queueJson.length() > 0)) {
            GsonBuilder gBuild = new GsonBuilder();
            Gson gson = gBuild.create();

            queueList = gson.fromJson(queueJson, new TypeToken<HashMap<Long, String>>() {}.getType());
        }

        return queueList;
    }

    public static void addToQueue(Context context, Long queueId, String queueItem) {

        HashMap<Long, String> queueList = loadQueue(context);
        queueList.put(queueId, queueItem);
        saveQueue(context, queueList, downloadQueueName);

        return;
    }

    public static void removeFromQueue(Context context, Long queueId) {

        HashMap<Long, String> queueList = loadQueue(context);
        queueList.remove(queueId);
        saveQueue(context, queueList, downloadQueueName);

        return;
    }

    private static void saveQueue(Context context, HashMap<Long, String> indexList, String jsonFileName) {

        String queueJson = "";

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        //Log.d("QUEUE", "WRITING JSON FILE " + jsonFilePath + jsonFileName + " TO SD CARD");

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

                queueJson = gson.toJson(indexList);

                byte[] buffer = queueJson.getBytes();
                jsonStream.write(buffer);
                jsonStream.flush();
                jsonStream.close();
                jsonStream = null;

                Process p = Runtime.getRuntime().exec("mv " + jsonFilePath + jsonFileName + ".tmp " + jsonFilePath + jsonFileName);

            } catch (IOException ioe) {
                Log.e("QUEUE", "WRITING JSON FILE " + jsonFilePath + jsonFileName + " TO SD CARD FAILED");
                return;
            }
        } else {
            Log.e("QUEUE", "SD CARD WAS NOT FOUND");
            return;
        }
    }
}
