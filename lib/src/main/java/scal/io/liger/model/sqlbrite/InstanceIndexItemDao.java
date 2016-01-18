package scal.io.liger.model.sqlbrite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.hannesdorfmann.sqlbrite.dao.Dao;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Created by mnbogner on 8/20/15.
 */
public class InstanceIndexItemDao extends Dao {

    Random r = new Random();

    @Override
    public void createTable(SQLiteDatabase sqLiteDatabase) {

        CREATE_TABLE(InstanceIndexItem.TABLE_NAME,
                InstanceIndexItem.COLUMN_ID + " INTEGER", // change to auto-increment?
                InstanceIndexItem.COLUMN_TITLE + " TEXT",
                InstanceIndexItem.COLUMN_DESCRIPTION + " TEXT",
                InstanceIndexItem.COLUMN_THUMBNAILPATH + " TEXT",
                InstanceIndexItem.COLUMN_INSTANCEFILEPATH + " TEXT PRIMARY KEY NOT NULL",
                InstanceIndexItem.COLUMN_AUTOINCREMENTINGID + " INTEGER",
                InstanceIndexItem.COLUMN_CREATIONDATE + " TEXT",
                InstanceIndexItem.COLUMN_LASTMODIFIEDDATE + " TEXT",
                InstanceIndexItem.COLUMN_LASTOPENEDDATE + " TEXT",
                InstanceIndexItem.COLUMN_SORTORDER + " INTEGER",
                InstanceIndexItem.COLUMN_STORYCREATIONDATE + " INTEGER",
                InstanceIndexItem.COLUMN_STORYSAVEDATE + " INTEGER",
                InstanceIndexItem.COLUMN_STORYTYPE + " TEXT",
                InstanceIndexItem.COLUMN_LANGUAGE + " TEXT",
                InstanceIndexItem.COLUMN_STORYPATHID + " TEXT",
                InstanceIndexItem.COLUMN_STORYPATHPREREQUISITES + " TEXT",
                InstanceIndexItem.COLUMN_STORYCOMPLETIONDATE + " INTEGER")
                .execute(sqLiteDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // foo

        if (oldVersion == 1 && newVersion == 2){
            ALTER_TABLE(InstanceIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstanceIndexItem.COLUMN_AUTOINCREMENTINGID + " INTEGER")
                    .execute(db);
            ALTER_TABLE(InstanceIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstanceIndexItem.COLUMN_CREATIONDATE + " TEXT")
                    .execute(db);
            ALTER_TABLE(InstanceIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstanceIndexItem.COLUMN_LASTMODIFIEDDATE + " TEXT")
                    .execute(db);
            ALTER_TABLE(InstanceIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstanceIndexItem.COLUMN_LASTOPENEDDATE + " TEXT")
                    .execute(db);
            ALTER_TABLE(InstanceIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstanceIndexItem.COLUMN_SORTORDER + " INTEGER")
                    .execute(db);
        }

    }

    public int getNextAutoincrementingId() {

        return -1;

    }

    public Observable<List<InstanceIndexItem>> getInstanceIndexItems() {

        // select all rows

        return query(SELECT(InstanceIndexItem.COLUMN_ID,
                InstanceIndexItem.COLUMN_TITLE,
                InstanceIndexItem.COLUMN_DESCRIPTION,
                InstanceIndexItem.COLUMN_THUMBNAILPATH,
                InstanceIndexItem.COLUMN_INSTANCEFILEPATH,
                InstanceIndexItem.COLUMN_AUTOINCREMENTINGID,
                InstanceIndexItem.COLUMN_CREATIONDATE,
                InstanceIndexItem.COLUMN_LASTMODIFIEDDATE,
                InstanceIndexItem.COLUMN_LASTOPENEDDATE,
                InstanceIndexItem.COLUMN_SORTORDER,
                InstanceIndexItem.COLUMN_STORYCREATIONDATE,
                InstanceIndexItem.COLUMN_STORYSAVEDATE,
                InstanceIndexItem.COLUMN_STORYTYPE,
                InstanceIndexItem.COLUMN_LANGUAGE,
                InstanceIndexItem.COLUMN_STORYPATHID,
                InstanceIndexItem.COLUMN_STORYPATHPREREQUISITES,
                InstanceIndexItem.COLUMN_STORYCOMPLETIONDATE)
                .FROM(InstanceIndexItem.TABLE_NAME))
                .map(new Func1<SqlBrite.Query, List<InstanceIndexItem>>() {

                    @Override
                    public List<InstanceIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return InstanceIndexItemMapper.list(cursor);
                    }
                });
    }

    public Observable<Long> addInstanceIndexItem(long id, String title, String description, String thumbnailPath, String instanceFilePath, int autoincrementingId, java.util.Date creationDate, java.util.Date lastModifiedDate, java.util.Date lastOpenedDate, int sortOrder, long storyCreationDate, long storySaveDate, String storyType, String language, String storyPathId, String storyPathPrerequisites, long storyCompletionDate, boolean replace) {

        Observable<Long> rowId = null;

        int autoincrementingId_local = getNextAutoincrementingId();
        java.util.Date creationDate_local = new java.util.Date();

        ContentValues values = InstanceIndexItemMapper.contentValues()
                .id(r.nextLong())
                .title(title)
                .description(description)
                .thumbnailPath(thumbnailPath)
                .instanceFilePath(instanceFilePath)
                .autoincrementingId(autoincrementingId_local)
                .creationDate(creationDate_local)
                .lastModifiedDate(creationDate_local)
                .lastOpenedDate(creationDate_local)
                .sortOrder(sortOrder)
                .storyCreationDate(storyCreationDate)
                .storySaveDate(storySaveDate)
                .storyType(storyType)
                .language(language)
                .storyPathId(storyPathId)
                .storyPathPrerequisites(storyPathPrerequisites)
                .storyCompletionDate(storyCompletionDate)
                .build();

        try {
            if (replace) {
                rowId = insert(InstanceIndexItem.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
            } else {
                rowId = insert(InstanceIndexItem.TABLE_NAME, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
        } catch (SQLiteConstraintException sce) {
            Timber.d("INSERT FAILED: " + sce.getMessage());
        }

        return rowId;
    }

    public Observable<Long> addInstanceIndexItem(scal.io.liger.model.InstanceIndexItem item, boolean replace) {

        String sppString = null;

        if (item.getStoryPathPrerequisites() != null) {
            sppString = item.getStoryPathPrerequisites().toString();
            Timber.d("WHAT DOES THIS LOOK LIKE? " + sppString);
        }

        return addInstanceIndexItem(r.nextLong(),
                item.getTitle(),
                item.getDescription(),
                item.getThumbnailPath(),
                item.getInstanceFilePath(),
                item.getAutoincrementingId(),
                item.getCreationDate(),
                item.getLastModifiedDate(),
                item.getLastOpenedDate(),
                item.getSortOrder(),
                item.getStoryCreationDate(),
                item.getStorySaveDate(),
                item.getStoryType(),
                item.getLanguage(),
                item.getStoryPathId(),
                sppString,
                item.getStoryCompletionDate(),
                replace);
    }

    public Observable<Long> addInstanceIndexItem(InstanceIndexItem item, boolean replace) {

        return addInstanceIndexItem(item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getThumbnailPath(),
                item.getInstanceFilePath(),
                item.getAutoincrementingId(),
                item.getCreationDate(),
                item.getLastModifiedDate(),
                item.getLastOpenedDate(),
                item.getSortOrder(),
                item.getStoryCreationDate(),
                item.getStorySaveDate(),
                item.getStoryType(),
                item.getLanguage(),
                item.getStoryPathId(),
                item.getStoryPathPrerequisites(),
                item.getStoryCompletionDate(),
                replace);
    }

    public Observable<Integer> removeInstanceIndexItem(InstanceIndexItem item) {
        // remove an existing record
        return removeInstanceIndexItemByKey(item.getInstanceFilePath());
    }

    public Observable<Integer> removeInstanceIndexItemByKey(String key) {
        // remove an existing record with a matching key
        Timber.d("REMOVE ROW FOR " + key);
        return delete(InstanceIndexItem.TABLE_NAME,
                InstanceIndexItem.COLUMN_INSTANCEFILEPATH + " = ? ",
                key);
    }

}
