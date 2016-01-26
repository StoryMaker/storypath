package scal.io.liger.model.sqlbrite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.hannesdorfmann.sqlbrite.dao.Dao;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Created by mnbogner on 8/20/15.
 */
public class InstalledIndexItemDao extends Dao {

    Random r = new Random();

    @Override
    public void createTable(SQLiteDatabase sqLiteDatabase) {

        CREATE_TABLE(InstalledIndexItem.TABLE_NAME,
                InstalledIndexItem.COLUMN_ID + " INTEGER", // change to auto-increment?
                InstalledIndexItem.COLUMN_TITLE + " TEXT",
                InstalledIndexItem.COLUMN_DESCRIPTION + " TEXT",
                InstalledIndexItem.COLUMN_THUMBNAILPATH + " TEXT",
                InstalledIndexItem.COLUMN_PACKAGENAME + " TEXT",
                InstalledIndexItem.COLUMN_EXPANSIONID + " TEXT PRIMARY KEY NOT NULL",
                InstalledIndexItem.COLUMN_AUTOINCREMENTINGID + " INTEGER",
                InstalledIndexItem.COLUMN_CREATIONDATE + " TEXT",
                InstalledIndexItem.COLUMN_LASTMODIFIEDDATE + " TEXT",
                InstalledIndexItem.COLUMN_LASTOPENEDDATE + " TEXT",
                InstalledIndexItem.COLUMN_SORTORDER + " INTEGER",
                InstalledIndexItem.COLUMN_PATCHORDER + " TEXT",
                InstalledIndexItem.COLUMN_CONTENTTYPE + " TEXT",
                InstalledIndexItem.COLUMN_EXPANSIONFILEURL + " TEXT",
                InstalledIndexItem.COLUMN_EXPANSIONFILEPATH + " TEXT",
                InstalledIndexItem.COLUMN_EXPANSIONFILEVERSION + " TEXT",
                InstalledIndexItem.COLUMN_EXPANSIONFILESIZE + " INTEGER",
                InstalledIndexItem.COLUMN_EXPANSIONFILECHECKSUM + " TEXT",
                InstalledIndexItem.COLUMN_PATCHFILEVERSION + " TEXT",
                InstalledIndexItem.COLUMN_PATCHFILESIZE + " INTEGER",
                InstalledIndexItem.COLUMN_PATCHFILECHECKSUM + " TEXT",
                InstalledIndexItem.COLUMN_AUTHOR + " TEXT",
                InstalledIndexItem.COLUMN_WEBSITE + " TEXT",
                InstalledIndexItem.COLUMN_DATEUPDATED + " TEXT",
                InstalledIndexItem.COLUMN_LANGUAGES + " TEXT",
                InstalledIndexItem.COLUMN_TAGS + " TEXT",
                InstalledIndexItem.COLUMN_INSTALLEDFLAG + " INTEGER",
                InstalledIndexItem.COLUMN_MAINDOWNLOADFLAG + " INTEGER",
                InstalledIndexItem.COLUMN_PATCHDOWNLOADFLAG + " INTEGER")
                .execute(sqLiteDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // foo

        if (oldVersion == 1 && newVersion == 2){
            ALTER_TABLE(InstalledIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstalledIndexItem.COLUMN_AUTOINCREMENTINGID + " INTEGER")
                    .execute(db);
            ALTER_TABLE(InstalledIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstalledIndexItem.COLUMN_CREATIONDATE + " TEXT")
                    .execute(db);
            ALTER_TABLE(InstalledIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstalledIndexItem.COLUMN_LASTMODIFIEDDATE + " TEXT")
                    .execute(db);
            ALTER_TABLE(InstalledIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstalledIndexItem.COLUMN_LASTOPENEDDATE + " TEXT")
                    .execute(db);
            ALTER_TABLE(InstalledIndexItem.TABLE_NAME)
                    .ADD_COLUMN(InstalledIndexItem.COLUMN_SORTORDER + " INTEGER")
                    .execute(db);
        }

    }

    public int getNextAutoincrementingId() {

        return -1;

    }

    public Observable<List<InstalledIndexItem>> getInstalledIndexItems() {

        // select all rows

        return query(SELECT(InstalledIndexItem.COLUMN_ID,
                InstalledIndexItem.COLUMN_TITLE,
                InstalledIndexItem.COLUMN_DESCRIPTION,
                InstalledIndexItem.COLUMN_THUMBNAILPATH,
                InstalledIndexItem.COLUMN_PACKAGENAME,
                InstalledIndexItem.COLUMN_EXPANSIONID,
                InstalledIndexItem.COLUMN_AUTOINCREMENTINGID,
                InstalledIndexItem.COLUMN_CREATIONDATE,
                InstalledIndexItem.COLUMN_LASTMODIFIEDDATE,
                InstalledIndexItem.COLUMN_LASTOPENEDDATE,
                InstalledIndexItem.COLUMN_SORTORDER,
                InstalledIndexItem.COLUMN_PATCHORDER,
                InstalledIndexItem.COLUMN_CONTENTTYPE,
                InstalledIndexItem.COLUMN_EXPANSIONFILEURL,
                InstalledIndexItem.COLUMN_EXPANSIONFILEPATH,
                InstalledIndexItem.COLUMN_EXPANSIONFILEVERSION,
                InstalledIndexItem.COLUMN_EXPANSIONFILESIZE,
                InstalledIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                InstalledIndexItem.COLUMN_PATCHFILEVERSION,
                InstalledIndexItem.COLUMN_PATCHFILESIZE,
                InstalledIndexItem.COLUMN_PATCHFILECHECKSUM,
                InstalledIndexItem.COLUMN_AUTHOR,
                InstalledIndexItem.COLUMN_WEBSITE,
                InstalledIndexItem.COLUMN_DATEUPDATED,
                InstalledIndexItem.COLUMN_LANGUAGES,
                InstalledIndexItem.COLUMN_TAGS,
                InstalledIndexItem.COLUMN_INSTALLEDFLAG,
                InstalledIndexItem.COLUMN_MAINDOWNLOADFLAG,
                InstalledIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(InstalledIndexItem.TABLE_NAME))
                //.ORDER_BY(InstanceIndexItem.COLUMN_CREATIONDATE + " DESC")
                .map(new Func1<SqlBrite.Query, List<InstalledIndexItem>>() {

                    @Override
                    public List<InstalledIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return InstalledIndexItemMapper.list(cursor);
                    }
                });
    }

    public Observable<List<InstalledIndexItem>> getInstalledIndexItemsByInstalledFlag(boolean installedFlag) {

        int installedInt = 0;

        if (installedFlag) {
            installedInt = 1;
        }

        // select all rows with matching download flag

        return query(SELECT(InstalledIndexItem.COLUMN_ID,
                InstalledIndexItem.COLUMN_TITLE,
                InstalledIndexItem.COLUMN_DESCRIPTION,
                InstalledIndexItem.COLUMN_THUMBNAILPATH,
                InstalledIndexItem.COLUMN_PACKAGENAME,
                InstalledIndexItem.COLUMN_EXPANSIONID,
                InstalledIndexItem.COLUMN_AUTOINCREMENTINGID,
                InstalledIndexItem.COLUMN_CREATIONDATE,
                InstalledIndexItem.COLUMN_LASTMODIFIEDDATE,
                InstalledIndexItem.COLUMN_LASTOPENEDDATE,
                InstalledIndexItem.COLUMN_SORTORDER,
                InstalledIndexItem.COLUMN_PATCHORDER,
                InstalledIndexItem.COLUMN_CONTENTTYPE,
                InstalledIndexItem.COLUMN_EXPANSIONFILEURL,
                InstalledIndexItem.COLUMN_EXPANSIONFILEPATH,
                InstalledIndexItem.COLUMN_EXPANSIONFILEVERSION,
                InstalledIndexItem.COLUMN_EXPANSIONFILESIZE,
                InstalledIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                InstalledIndexItem.COLUMN_PATCHFILEVERSION,
                InstalledIndexItem.COLUMN_PATCHFILESIZE,
                InstalledIndexItem.COLUMN_PATCHFILECHECKSUM,
                InstalledIndexItem.COLUMN_AUTHOR,
                InstalledIndexItem.COLUMN_WEBSITE,
                InstalledIndexItem.COLUMN_DATEUPDATED,
                InstalledIndexItem.COLUMN_LANGUAGES,
                InstalledIndexItem.COLUMN_TAGS,
                InstalledIndexItem.COLUMN_INSTALLEDFLAG,
                InstalledIndexItem.COLUMN_MAINDOWNLOADFLAG,
                InstalledIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(InstalledIndexItem.TABLE_NAME)
                .WHERE(InstalledIndexItem.COLUMN_INSTALLEDFLAG + " = ? "), Integer.toString(installedInt)) // query parameters must be strings?
                .map(new Func1<SqlBrite.Query, List<InstalledIndexItem>>() {

                    @Override
                    public List<InstalledIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return InstalledIndexItemMapper.list(cursor);
                    }
                });
    }

    // need patch download version too?
    // method may be unecessary, all installed items should be checked at startup
    public Observable<List<InstalledIndexItem>> getInstalledIndexItemsByMainDownloadFlag(boolean downloadFlag) {

        int downloadInt = 0;

        if (downloadFlag) {
            downloadInt = 1;
        }

        // select all rows with matching download flag

        return query(SELECT(InstalledIndexItem.COLUMN_ID,
                InstalledIndexItem.COLUMN_TITLE,
                InstalledIndexItem.COLUMN_DESCRIPTION,
                InstalledIndexItem.COLUMN_THUMBNAILPATH,
                InstalledIndexItem.COLUMN_PACKAGENAME,
                InstalledIndexItem.COLUMN_EXPANSIONID,
                InstalledIndexItem.COLUMN_AUTOINCREMENTINGID,
                InstalledIndexItem.COLUMN_CREATIONDATE,
                InstalledIndexItem.COLUMN_LASTMODIFIEDDATE,
                InstalledIndexItem.COLUMN_LASTOPENEDDATE,
                InstalledIndexItem.COLUMN_SORTORDER,
                InstalledIndexItem.COLUMN_PATCHORDER,
                InstalledIndexItem.COLUMN_CONTENTTYPE,
                InstalledIndexItem.COLUMN_EXPANSIONFILEURL,
                InstalledIndexItem.COLUMN_EXPANSIONFILEPATH,
                InstalledIndexItem.COLUMN_EXPANSIONFILEVERSION,
                InstalledIndexItem.COLUMN_EXPANSIONFILESIZE,
                InstalledIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                InstalledIndexItem.COLUMN_PATCHFILEVERSION,
                InstalledIndexItem.COLUMN_PATCHFILESIZE,
                InstalledIndexItem.COLUMN_PATCHFILECHECKSUM,
                InstalledIndexItem.COLUMN_AUTHOR,
                InstalledIndexItem.COLUMN_WEBSITE,
                InstalledIndexItem.COLUMN_DATEUPDATED,
                InstalledIndexItem.COLUMN_LANGUAGES,
                InstalledIndexItem.COLUMN_TAGS,
                InstalledIndexItem.COLUMN_INSTALLEDFLAG,
                InstalledIndexItem.COLUMN_MAINDOWNLOADFLAG,
                InstalledIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(InstalledIndexItem.TABLE_NAME)
                .WHERE(InstalledIndexItem.COLUMN_MAINDOWNLOADFLAG + " = ? "), Integer.toString(downloadInt)) // query parameters must be strings?
                .map(new Func1<SqlBrite.Query, List<InstalledIndexItem>>() {

                    @Override
                    public List<InstalledIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return InstalledIndexItemMapper.list(cursor);
                    }
                });
    }

    public Observable<List<InstalledIndexItem>> getInstalledIndexItemsByType(String contentType) {

        // select all rows with matching content type
        return query(SELECT(InstalledIndexItem.COLUMN_ID,
                InstalledIndexItem.COLUMN_TITLE,
                InstalledIndexItem.COLUMN_DESCRIPTION,
                InstalledIndexItem.COLUMN_THUMBNAILPATH,
                InstalledIndexItem.COLUMN_PACKAGENAME,
                InstalledIndexItem.COLUMN_EXPANSIONID,
                InstalledIndexItem.COLUMN_AUTOINCREMENTINGID,
                InstalledIndexItem.COLUMN_CREATIONDATE,
                InstalledIndexItem.COLUMN_LASTMODIFIEDDATE,
                InstalledIndexItem.COLUMN_LASTOPENEDDATE,
                InstalledIndexItem.COLUMN_SORTORDER,
                InstalledIndexItem.COLUMN_PATCHORDER,
                InstalledIndexItem.COLUMN_CONTENTTYPE,
                InstalledIndexItem.COLUMN_EXPANSIONFILEURL,
                InstalledIndexItem.COLUMN_EXPANSIONFILEPATH,
                InstalledIndexItem.COLUMN_EXPANSIONFILEVERSION,
                InstalledIndexItem.COLUMN_EXPANSIONFILESIZE,
                InstalledIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                InstalledIndexItem.COLUMN_PATCHFILEVERSION,
                InstalledIndexItem.COLUMN_PATCHFILESIZE,
                InstalledIndexItem.COLUMN_PATCHFILECHECKSUM,
                InstalledIndexItem.COLUMN_AUTHOR,
                InstalledIndexItem.COLUMN_WEBSITE,
                InstalledIndexItem.COLUMN_DATEUPDATED,
                InstalledIndexItem.COLUMN_LANGUAGES,
                InstalledIndexItem.COLUMN_TAGS,
                InstalledIndexItem.COLUMN_INSTALLEDFLAG,
                InstalledIndexItem.COLUMN_MAINDOWNLOADFLAG,
                InstalledIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(InstalledIndexItem.TABLE_NAME)
                .WHERE(InstalledIndexItem.COLUMN_CONTENTTYPE + " = ? "), contentType)
                //.WHERE(InstalledIndexItem.COLUMN_CONTENTTYPE + " = ? ")
                //.ORDER_BY(InstalledIndexItem.COLUMN_CREATIONDATE + " DESC"), contentType)
                .map(new Func1<SqlBrite.Query, List<InstalledIndexItem>>() {

                    @Override
                    public List<InstalledIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return InstalledIndexItemMapper.list(cursor);
                    }
                });
    }

    public Observable<Long> addInstalledIndexItem(long id, String title, String description, String thumbnailPath, String packageName, String expansionId, int autoincrementingId, java.util.Date creationDate, java.util.Date lastModifiedDate, java.util.Date lastOpenedDate, int sortOrder, String patchOrder, String contentType, String expansionFileUrl, String expansionFilePath, String expansionFileVersion, long expansionFileSize, String expansionFileChecksum, String patchFileVersion, long patchFileSize, String patchFileChecksum, String author, String website, String dateUpdated, String languages, String tags, int installedFlag, int mainDownloadFlag, int patchDownloadFlag, boolean replace) {

        Timber.d("ADDING ROW FOR " + expansionId + "(MAIN " + mainDownloadFlag + ", PATCH " + patchDownloadFlag + ", REPLACE? " + replace + ")");

        Observable<Long> rowId = null;

        int autoincrementingId_local = getNextAutoincrementingId();

        ContentValues values = InstalledIndexItemMapper.contentValues()
                .id(r.nextLong())
                .title(title)
                .description(description)
                .thumbnailPath(thumbnailPath)
                .packageName(packageName)
                .expansionId(expansionId)
                .autoincrementingId(autoincrementingId)
                .creationDate(creationDate)
                .lastModifiedDate(lastModifiedDate)
                .lastOpenedDate(lastOpenedDate)
                .sortOrder(sortOrder)
                .patchOrder(patchOrder)
                .contentType(contentType)
                .expansionFileUrl(expansionFileUrl)
                .expansionFilePath(expansionFilePath)
                .expansionFileVersion(expansionFileVersion)
                .expansionFileSize(expansionFileSize)
                .expansionFileChecksum(expansionFileChecksum)
                .patchFileVersion(patchFileVersion)
                .patchFileSize(patchFileSize)
                .patchFileChecksum(patchFileChecksum)
                .author(author)
                .website(website)
                .dateUpdated(dateUpdated)
                .languages(languages)
                .tags(tags)
                .installedFlag(installedFlag)
                .mainDownloadFlag(mainDownloadFlag)
                .patchDownloadFlag(patchDownloadFlag)
                .build();

        try {
            if (replace) {
                rowId = insert(InstalledIndexItem.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
            } else {
                rowId = insert(InstalledIndexItem.TABLE_NAME, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
        } catch (SQLiteConstraintException sce) {
            Timber.d("INSERT FAILED: " + sce.getMessage());
        }

        return rowId;
    }

    public Observable<Long> addInstalledIndexItem(scal.io.liger.model.ExpansionIndexItem item, boolean replace) {

        String languageString = null;
        String tagString = null;

        if (item.getLanguages() != null) {
            languageString = item.getLanguages().toString();
            Timber.d("WHAT DOES THIS LOOK LIKE? " + languageString);
        }
        if (item.getTags() != null) {
            tagString = item.getTags().toString();
            Timber.d("WHAT DOES THIS LOOK LIKE? " + tagString);
        }

        int flag = 0;
        if (item.getExpansionId().equals("learning_guide")) {
            Timber.d("SETTING FLAG!");
            flag = 1;
        }

        return addInstalledIndexItem(r.nextLong(),
                item.getTitle(),
                item.getDescription(),
                item.getThumbnailPath(),
                item.getPackageName(),
                item.getExpansionId(),
                item.getAutoincrementingId(),
                item.getCreationDate(),
                item.getLastModifiedDate(),
                item.getLastOpenedDate(),
                item.getSortOrder(),
                item.getPatchOrder(),
                item.getContentType(),
                item.getExpansionFileUrl(),
                item.getExpansionFilePath(),
                item.getExpansionFileVersion(),
                item.getExpansionFileSize(),
                item.getExpansionFileChecksum(),
                item.getPatchFileVersion(),
                item.getPatchFileSize(),
                item.getPatchFileChecksum(),
                item.getAuthor(),
                item.getWebsite(),
                item.getDateUpdated(),
                languageString,
                tagString,
                flag,  // default to false (not installed), no need to update liger ExpansionIndexItem class
                0,  // default to false (not downloading), no need to update liger ExpansionIndexItem class
                0,  // default to false (not downloading), no need to update liger ExpansionIndexItem class
                replace);
    }

    public Observable<Long> addInstalledIndexItem(InstalledIndexItem item, boolean replace) {

        return addInstalledIndexItem(item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getThumbnailPath(),
                item.getPackageName(),
                item.getExpansionId(),
                item.getAutoincrementingId(),
                item.getCreationDate(),
                item.getLastModifiedDate(),
                item.getLastOpenedDate(),
                item.getSortOrder(),
                item.getPatchOrder(),
                item.getContentType(),
                item.getExpansionFileUrl(),
                item.getExpansionFilePath(),
                item.getExpansionFileVersion(),
                item.getExpansionFileSize(),
                item.getExpansionFileChecksum(),
                item.getPatchFileVersion(),
                item.getPatchFileSize(),
                item.getPatchFileChecksum(),
                item.getAuthor(),
                item.getWebsite(),
                item.getDateUpdated(),
                item.getLanguages(),
                item.getTags(),
                item.getInstalledFlag(),
                item.getMainDownloadFlag(),
                item.getPatchDownloadFlag(),
                replace);
    }

    public Observable<Integer> removeInstalledIndexItem(InstalledIndexItem item) {

        // remove an existing record

        return removeInstalledIndexItemByKey(item.getExpansionId());
    }

    public Observable<Integer> removeInstalledIndexItemByKey(String key) {

        // remove an existing record with a matching key

        Timber.d("REMOVE ROW FOR " + key);

        return delete(InstalledIndexItem.TABLE_NAME,
                InstalledIndexItem.COLUMN_EXPANSIONID + " = ? ",
                key);
    }

    public Observable<List<InstalledIndexItem>> getInstalledIndexItem(InstalledIndexItem item) {

        // check current state of an existing record

        return getInstalledIndexItemByKey(item.getExpansionId());
    }

    public java.util.Date getInstalledIndexItemCreationDateByKey (String key) {

        final ArrayList<java.util.Date> returnVals = new ArrayList<java.util.Date>();

        getInstalledIndexItemByKey(key).take(1).subscribe(new Action1<List<InstalledIndexItem>>() {

            @Override
            public void call(List<InstalledIndexItem> expansionIndexItems) {

                // only one item expected

                if (expansionIndexItems.size() == 1) {

                    InstalledIndexItem installedItem = expansionIndexItems.get(0);

                    returnVals.add(installedItem.getCreationDate());
                }
            }
        });

        if (returnVals.size() > 0) {
            return returnVals.get(0);
        } else {
            return null;
        }

    }

    public Observable<List<InstalledIndexItem>> getInstalledIndexItemByKey(String key) {

        // check current state of an existing record with a matching key

        return query(SELECT(InstalledIndexItem.COLUMN_ID,
                InstalledIndexItem.COLUMN_TITLE,
                InstalledIndexItem.COLUMN_DESCRIPTION,
                InstalledIndexItem.COLUMN_THUMBNAILPATH,
                InstalledIndexItem.COLUMN_PACKAGENAME,
                InstalledIndexItem.COLUMN_EXPANSIONID,
                InstalledIndexItem.COLUMN_AUTOINCREMENTINGID,
                InstalledIndexItem.COLUMN_CREATIONDATE,
                InstalledIndexItem.COLUMN_LASTMODIFIEDDATE,
                InstalledIndexItem.COLUMN_LASTOPENEDDATE,
                InstalledIndexItem.COLUMN_SORTORDER,
                InstalledIndexItem.COLUMN_PATCHORDER,
                InstalledIndexItem.COLUMN_CONTENTTYPE,
                InstalledIndexItem.COLUMN_EXPANSIONFILEURL,
                InstalledIndexItem.COLUMN_EXPANSIONFILEPATH,
                InstalledIndexItem.COLUMN_EXPANSIONFILEVERSION,
                InstalledIndexItem.COLUMN_EXPANSIONFILESIZE,
                InstalledIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                InstalledIndexItem.COLUMN_PATCHFILEVERSION,
                InstalledIndexItem.COLUMN_PATCHFILESIZE,
                InstalledIndexItem.COLUMN_PATCHFILECHECKSUM,
                InstalledIndexItem.COLUMN_AUTHOR,
                InstalledIndexItem.COLUMN_WEBSITE,
                InstalledIndexItem.COLUMN_DATEUPDATED,
                InstalledIndexItem.COLUMN_LANGUAGES,
                InstalledIndexItem.COLUMN_TAGS,
                InstalledIndexItem.COLUMN_INSTALLEDFLAG,
                InstalledIndexItem.COLUMN_MAINDOWNLOADFLAG,
                InstalledIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(InstalledIndexItem.TABLE_NAME)
                .WHERE(InstalledIndexItem.COLUMN_EXPANSIONID + " = ? "), key)
                .map(new Func1<SqlBrite.Query, List<InstalledIndexItem>>() {

                    @Override
                    public List<InstalledIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return InstalledIndexItemMapper.list(cursor);
                    }
                });
    }
}
