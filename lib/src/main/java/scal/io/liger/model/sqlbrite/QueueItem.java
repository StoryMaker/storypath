package scal.io.liger.model.sqlbrite;

import timber.log.Timber;

import com.hannesdorfmann.sqlbrite.objectmapper.annotation.Column;
import com.hannesdorfmann.sqlbrite.objectmapper.annotation.ObjectMappable;

/**
 * Created by mnbogner on 8/28/15.
 */

@ObjectMappable
public class QueueItem {

    public static final String COLUMN_ID = "id";
    public static final String TABLE_NAME = "QueueItem";
    public static final String COLUMN_QUEUEFILE = "queueFile";
    public static final String COLUMN_QUEUETIME = "queueTime";

    public long id;
    @Column(COLUMN_QUEUEFILE) public String queueFile;
    @Column(COLUMN_QUEUETIME) public long queueTime;

    @Column(COLUMN_ID)
    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getQueueFile() {
        return queueFile;
    }

    public long getQueueTime() {
        return queueTime;
    }

    public QueueItem() {

    }

    public QueueItem(long id, String queueFile, long queueTime) {
        this.id = id;
        this.queueFile = queueFile;
        this.queueTime = queueTime;
    }

    public void setQueueFile(String queueFile) {
        this.queueFile = queueFile;
    }

    public void setQueueTime(long queueTime) {
        this.queueTime = queueTime;
    }
}
