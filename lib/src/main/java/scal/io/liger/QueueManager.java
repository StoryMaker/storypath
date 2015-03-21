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

    //public static long NO_MANAGER = -123;

    public static Long DUPLICATE_QUERY = Long.valueOf(0);

    private static String downloadQueueName = "download_queue.json";

    public static long queueTimeout = Long.MAX_VALUE; // user-configurable?  setting to max value, will revisit later

    // caching to avoid file collision issues (cache should ensure correct data is read/written)
    public static HashMap<Long, QueueItem> cachedQueue = new HashMap<Long, QueueItem>();
    public static ArrayList<String> cachedQueries = new ArrayList<String>();

    // need some sort of solution to prevent multiple simultaneous checks from all looking and all finding nothing
    public static synchronized Long checkQueue(Context context, File queueFile) {
        return checkQueue(context, queueFile.getName());
    }

    public static synchronized Long checkQueue(Context context, String queueFile) {

        loadQueue(context); // fills cached queue

        if (cachedQueries.contains(queueFile)) {
            Log.d("QUEUE", "QUEUE ITEM IS " + queueFile + " BUT SOMEONE IS ALREADY LOOKING FOR THAT");

            return DUPLICATE_QUERY;
        } else {
            Log.d("QUEUE", "ADDING CACHED QUERY FOR " + queueFile);

            cachedQueries.add(queueFile);
        }

        for (Long queueId : cachedQueue.keySet()) {

            Log.d("QUEUE", "QUEUE ITEM IS " + cachedQueue.get(queueId).getQueueFile() + " LOOKING FOR " + queueFile);

            if (queueFile.equals(cachedQueue.get(queueId).getQueueFile())) {
                Log.d("QUEUE", "QUEUE ITEM FOR " + queueFile + " FOUND WITH ID " + queueId + " REMOVING CACHED QUERY ");

                cachedQueries.remove(queueFile);

                return queueId;
            }
        }

        Log.d("QUEUE", "QUEUE ITEM FOR " + queueFile + " NOT FOUND");

        return null;
    }

    public static synchronized void checkQueueFinished(Context context, File queueFile) {
        checkQueueFinished(context, queueFile.getName());
    }

    public static synchronized void checkQueueFinished(Context context, String queueFile) {

        Log.d("QUEUE", "LOOKING FOR CACHED QUERY FOR " + queueFile);

        // done checking queue for item, remove temp item
        if (cachedQueries.contains(queueFile)) {
            Log.d("QUEUE", "REMOVING CACHED QUERY FOR " + queueFile);

            cachedQueries.remove(queueFile);
        }
    }

    public static synchronized HashMap<Long, QueueItem> loadQueue(Context context) {

        if (cachedQueue.size() > 0) {
            return cachedQueue;
        }

        String queueJson = null;

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        //Log.d("QUEUE", "READING JSON FILE " + jsonFilePath + downloadQueueName + " FROM SD CARD");

        File jsonFile = new File(jsonFilePath + downloadQueueName);
        if (!jsonFile.exists()) {
            Log.e("QUEUE", jsonFilePath + downloadQueueName + " WAS NOT FOUND");
            return cachedQueue;
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
                return cachedQueue;
            }
        } else {
            Log.e("QUEUE", "SD CARD WAS NOT FOUND");
            return cachedQueue;
        }

        if ((queueJson != null) && (queueJson.length() > 0)) {
            GsonBuilder gBuild = new GsonBuilder();
            Gson gson = gBuild.create();

            // trying to account for issues with old queue files
            try {
                cachedQueue = gson.fromJson(queueJson, new TypeToken<HashMap<Long, QueueItem>>() {
                }.getType());
            } catch (Exception ise) {
                // this will hopefully catch existing Long/String queue files

                Log.d("QUEUE", "JSON QUEUE FILE APPEARS CORRUPT, PURGING FILE, RETURNING EMPTY QUEUE");

                if (jsonFile.exists()) {
                    jsonFile.delete();
                }

                cachedQueue = new HashMap<Long, QueueItem>();
            }
        }

        return cachedQueue;
    }

    // unused
    /*
    public static long checkQueue(String fileName, HashMap<Long, QueueItem> queueMap) {

        for (Long queueNumber : queueMap.keySet()) {
            if (queueMap.get(queueNumber).getQueueFile().contains(fileName)) {
                return queueNumber.longValue();
            }
        }

        return -1;
    }
    */

    public static synchronized void addToQueue(Context context, Long queueId, String queueFile) {

        if (cachedQueue.size() == 0) {
            cachedQueue = loadQueue(context);
        }

        QueueItem queueItem = new QueueItem(queueFile, new Date());

        cachedQueue.put(queueId, queueItem);

        // we have an actual entry for the item now, remove temp item
        if (cachedQueries.contains(queueFile)) {
            Log.d("QUEUE", "REMOVING CACHED QUERY FOR " + queueFile);

            cachedQueries.remove(queueFile);
        }

        Log.d("QUEUE", "PUT " + queueId + " IN QUEUE, NEW QUEUE " + cachedQueue.keySet().toString());

        saveQueue(context, cachedQueue, downloadQueueName);
        return;
    }

    public static synchronized boolean removeFromQueue(Context context, Long queueId) {

        if (cachedQueue.size() == 0) {
            cachedQueue = loadQueue(context);
        }

        if (cachedQueue.keySet().contains(queueId)) {
            QueueItem removedItem = cachedQueue.remove(queueId);

            // check for cached queries
            checkQueueFinished(context, removedItem.getQueueFile());

            Log.d("QUEUE", "REMOVED " + queueId + " FROM QUEUE, NEW QUEUE " + cachedQueue.keySet().toString());

            saveQueue(context, cachedQueue, downloadQueueName);
            return true;
        } else{
            return false;
        }
    }

    // unused
    /*
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
    */

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
