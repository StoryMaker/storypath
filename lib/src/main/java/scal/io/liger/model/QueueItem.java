package scal.io.liger.model;

import timber.log.Timber;

import java.util.Date;

/**
 * Created by mnbogner on 2/23/15.
 */
public class QueueItem {

    String queueFile;
    long queueTime;

    public QueueItem() {

    }

    public QueueItem(String queueFile, long queueTime) {
        this.queueFile = queueFile;
        this.queueTime = queueTime;
    }

    public QueueItem(String queueFile, Date queueDate) {
        this.queueFile = queueFile;
        this.queueTime = queueDate.getTime();
    }

    public String getQueueFile() {
        return queueFile;
    }

    public void setQueueFile(String queueFile) {
        this.queueFile = queueFile;
    }

    public long getQueueTime() {
        return queueTime;
    }

    public void setQueueTime(long queueTime) {
        this.queueTime = queueTime;
    }
}
