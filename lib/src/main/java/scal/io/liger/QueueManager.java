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
import scal.io.liger.model.QueueItem;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;

/**
 * Created by mnbogner on 11/24/14.
 */
public class QueueManager {

    public static long NO_MANAGER = -123;

    private static String downloadQueueName = "download_queue.json";

    public static long queueTimeout = Long.MAX_VALUE; // user-configurable?  setting to max value, will revisit later

    // need to ensure multiple threads don't grab the file at the same time

    public static synchronized HashMap<Long, QueueItem> loadQueue(Context context) {

        String queueJson = null;
        HashMap<Long, QueueItem> queueList = new HashMap<Long, QueueItem>();

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

            // trying to account for issues with old queue files
            try {
                queueList = gson.fromJson(queueJson, new TypeToken<HashMap<Long, QueueItem>>(){}.getType());
            } catch (Exception ise) {
                // this will hopefully catch existing Long/String queue files

                Log.d("QUEUE", "JSON QUEUE FILE APPEARS CORRUPT, PURGING FILE, RETURNING EMPTY QUEUE");

                if (jsonFile.exists()) {
                    jsonFile.delete();
                }

                queueList = new HashMap<Long, QueueItem>();
            }
        }

        return queueList;
    }

    public static long checkQueue(String fileName, HashMap<Long, QueueItem> queueMap) {

        for (Long queueNumber : queueMap.keySet()) {
            if (queueMap.get(queueNumber).getQueueFile().contains(fileName)) {
                return queueNumber.longValue();
            }
        }

        return -1;
    }

    public static synchronized void addToQueue(Context context, Long queueId, String queueFile) {

        HashMap<Long, QueueItem> queueList = loadQueue(context);

        QueueItem queueItem = new QueueItem(queueFile, new Date());

        queueList.put(queueId, queueItem);

        Log.d("QUEUE", "PUT " + queueId + " IN QUEUE, NEW QUEUE " + queueList.keySet().toString());

        saveQueue(context, queueList, downloadQueueName);
        return;
    }

    public static synchronized boolean removeFromQueue(Context context, Long queueId) {

        HashMap<Long, QueueItem> queueList = loadQueue(context);

        if (queueList.keySet().contains(queueId)) {
            queueList.remove(queueId);

            Log.d("QUEUE", "REMOVED " + queueId + " FROM QUEUE, NEW QUEUE " + queueList.keySet().toString());

            saveQueue(context, queueList, downloadQueueName);
            return true;
        } else{
            return false;
        }
    }

    public static synchronized boolean purgeFromQueue(Context context, Long queueId) {

        HashMap<Long, QueueItem> queueMap = loadQueue(context);

        if (queueMap.keySet().contains(queueId)) {

            String fileToPurge = queueMap.get(queueId).getQueueFile();
            fileToPurge = fileToPurge.substring(fileToPurge.lastIndexOf(File.separator) + 1, fileToPurge.lastIndexOf("."));

            Log.d("QUEUE", "REMOVING " + queueId + " AND PURGING ALL OTHER QUEUE ITEMS FOR " + fileToPurge);

            queueMap.remove(queueId);

            for (Long queueNumber : queueMap.keySet()) {
                if (queueMap.get(queueNumber).getQueueFile().contains(fileToPurge)) {
                    queueMap.remove(queueNumber);

                    Log.d("QUEUE", "REMOVED " + queueNumber + " FROM QUEUE, NEW QUEUE " + queueMap.keySet().toString());
                }
            }

            saveQueue(context, queueMap, downloadQueueName);
            return true;
        } else{
            return false;
        }
    }

    private static synchronized void saveQueue(Context context, HashMap<Long, QueueItem> queueMap, String jsonFileName) {

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

                queueJson = gson.toJson(queueMap);

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
