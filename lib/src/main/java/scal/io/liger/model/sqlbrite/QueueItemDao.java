package scal.io.liger.model.sqlbrite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hannesdorfmann.sqlbrite.dao.Dao;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by mnbogner on 8/28/15.
 */
public class QueueItemDao extends Dao {

    Random r = new Random();

    @Override
    public void createTable(SQLiteDatabase sqLiteDatabase) {

        CREATE_TABLE(QueueItem.TABLE_NAME,
                QueueItem.COLUMN_ID + " INTEGER", // change to auto-increment?
                QueueItem.COLUMN_QUEUEFILE + " TEXT PRIMARY KEY NOT NULL",
                QueueItem.COLUMN_QUEUETIME + " INTEGER")
                .execute(sqLiteDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // foo

    }

    public Observable<List<QueueItem>> getQueueItems() {

        // select all rows

        return query(SELECT(QueueItem.COLUMN_ID,
                QueueItem.COLUMN_QUEUEFILE,
                QueueItem.COLUMN_QUEUETIME)
                .FROM(QueueItem.TABLE_NAME))
                .map(new Func1<SqlBrite.Query, List<QueueItem>>() {

                    @Override
                    public List<QueueItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return QueueItemMapper.list(cursor);
                    }
                });
    }

    public Observable<Long> addQueueItem(long id, String queueFile, long queueTime, boolean replace) {

        Log.d("DB_ADD", "ADDING ROW FOR " + queueFile + ", REPLACE? " + replace);

        Observable<Long> rowId = null;

        ContentValues values = QueueItemMapper.contentValues()
                .id(id)
                .queueFile(queueFile)
                .queueTime(queueTime)
                .build();

        try {
            if (replace) {
                rowId = insert(QueueItem.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
            } else {
                rowId = insert(QueueItem.TABLE_NAME, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
        } catch (SQLiteConstraintException sce) {
            Log.d("RX_DB", "INSERT FAILED: " + sce.getMessage());
        }

        return rowId;
    }

    public Observable<Long> addQueueItem(QueueItem item, boolean replace) {
        return addQueueItem(item.getId(),
                item.queueFile,
                item.queueTime,
                replace);
    }

    public Observable<Integer> removeQueueItem(QueueItem item) {

        // remove an existing record

        return removeQueueItemByKey(item.getQueueFile());
    }

    public Observable<Integer> removeQueueItemByKey(String key) {

        // remove an existing record with a matching key

        Log.d("DB_REMOVE", "REMOVE ROW FOR " + key);

        return delete(QueueItem.TABLE_NAME,
                QueueItem.COLUMN_QUEUEFILE + " = ? ",
                key);
    }
}
