package scal.io.liger;

import timber.log.Timber;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import rx.functions.Action1;
import scal.io.liger.model.sqlbrite.QueueItem;
import scal.io.liger.model.sqlbrite.QueueItemDao;

/**
 * Created by mnbogner on 8/28/15.
 */
public class StorymakerQueueManager {

    //public static long NO_MANAGER = -123;

    public static Long DUPLICATE_QUERY = Long.valueOf(0);

    private static String downloadQueueName = "download_queue.json";

    public static long queueTimeout = Long.MAX_VALUE; // user-configurable?  setting to max value, will revisit later

    // caching to avoid file collision issues (cache should ensure correct data is read/written)
    public static HashMap<Long, QueueItem> cachedQueue = new HashMap<Long, QueueItem>();
    public static ArrayList<String> cachedQueries = new ArrayList<String>();

    // need some sort of solution to prevent multiple simultaneous checks from all looking and all finding nothing
    public static synchronized Long checkQueue(Context context, File queueFile, QueueItemDao dao) {
        return checkQueue(context, queueFile.getName(), dao);
    }

    public static synchronized Long checkQueue(Context context, String queueFile, QueueItemDao dao) {

        loadQueue(context, dao); // fills cached queue

        if (cachedQueries.contains(queueFile)) {
            Timber.d("QUEUE ITEM IS " + queueFile + " BUT SOMEONE IS ALREADY LOOKING FOR THAT");

            return DUPLICATE_QUERY;
        } else {
            Timber.d("ADDING CACHED QUERY FOR " + queueFile);

            cachedQueries.add(queueFile);
        }

        for (Long queueId : cachedQueue.keySet()) {

            Timber.d("QUEUE ITEM IS " + cachedQueue.get(queueId).getQueueFile() + " LOOKING FOR " + queueFile);

            if (queueFile.equals(cachedQueue.get(queueId).getQueueFile())) {
                Timber.d("QUEUE ITEM FOR " + queueFile + " FOUND WITH ID " + queueId + " REMOVING CACHED QUERY ");

                cachedQueries.remove(queueFile);

                return queueId;
            }
        }

        Timber.d("QUEUE ITEM FOR " + queueFile + " NOT FOUND");

        return null;
    }

    public static synchronized void checkQueueFinished(Context context, File queueFile) {
        checkQueueFinished(context, queueFile.getName());
    }

    public static synchronized void checkQueueFinished(Context context, String queueFile) {

        Timber.d("LOOKING FOR CACHED QUERY FOR " + queueFile);

        // done checking queue for item, remove temp item
        if (cachedQueries.contains(queueFile)) {
            Timber.d("REMOVING CACHED QUERY FOR " + queueFile);

            cachedQueries.remove(queueFile);
        }
    }

    public static synchronized HashMap<Long, QueueItem> loadQueue(Context context, QueueItemDao dao) {

        if (cachedQueue.size() > 0) {
            return cachedQueue;
        }

        // REPLACING FILE ACCESS WITH DB ACCESS

        dao.getQueueItems().subscribe(new Action1<List<QueueItem>>() {

            @Override
            public void call(List<QueueItem> queueItems) {

                cachedQueue = new HashMap<Long, QueueItem>();

                for (QueueItem item : queueItems) {
                    cachedQueue.put(item.getId(), item);
                }
            }
        });

        return cachedQueue;
    }

    public static synchronized void addToQueue(Context context, Long queueId, String queueFile, QueueItemDao dao) {

        if (cachedQueue.size() == 0) {
            cachedQueue = loadQueue(context, dao);
        }

        QueueItem queueItem = new QueueItem();
        queueItem.setId(queueId);
        queueItem.setQueueFile(queueFile);
        queueItem.setQueueTime(new Date().getTime());

        cachedQueue.put(queueItem.getId(), queueItem);

        // we have an actual entry for the item now, remove temp item
        if (cachedQueries.contains(queueFile)) {
            Timber.d("REMOVING CACHED QUERY FOR " + queueFile);

            cachedQueries.remove(queueFile);
        }

        Timber.d("PUT " + queueId + " IN QUEUE, NEW QUEUE " + cachedQueue.keySet().toString());

        saveQueue(context, cachedQueue, downloadQueueName, dao);
        return;
    }

    public static synchronized boolean removeFromQueue(Context context, Long queueId, QueueItemDao dao) {

        if (cachedQueue.size() == 0) {
            cachedQueue = loadQueue(context, dao);
        }

        if (cachedQueue.keySet().contains(queueId)) {
            QueueItem removedItem = cachedQueue.remove(queueId);

            // check for cached queries
            checkQueueFinished(context, removedItem.getQueueFile());

            // need to actually delete item from db (saving updated list will not remove it)
            dao.removeQueueItem(removedItem);

            Timber.d("REMOVED " + queueId + " FROM QUEUE, NEW QUEUE " + cachedQueue.keySet().toString());

            saveQueue(context, cachedQueue, downloadQueueName, dao);
            return true;
        } else{
            return false;
        }
    }

    private static synchronized void saveQueue(Context context, HashMap<Long, QueueItem> queueMap, String jsonFileName, QueueItemDao dao) {

        // REPLACING FILE ACCESS WITH DB ACCESS

        for (QueueItem item : queueMap.values()) {
            dao.addQueueItem(item, true);
        }
    }
}
