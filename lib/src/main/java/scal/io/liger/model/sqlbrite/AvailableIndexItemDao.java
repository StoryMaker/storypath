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
public class AvailableIndexItemDao extends Dao {

    Random r = new Random();

    @Override
    public void createTable(SQLiteDatabase sqLiteDatabase) {

        CREATE_TABLE(AvailableIndexItem.TABLE_NAME,
                AvailableIndexItem.COLUMN_ID + " INTEGER", // change to auto-increment?
                AvailableIndexItem.COLUMN_TITLE + " TEXT",
                AvailableIndexItem.COLUMN_DESCRIPTION + " TEXT",
                AvailableIndexItem.COLUMN_THUMBNAILPATH + " TEXT",
                AvailableIndexItem.COLUMN_PACKAGENAME + " TEXT",
                AvailableIndexItem.COLUMN_EXPANSIONID + " TEXT PRIMARY KEY NOT NULL",
                AvailableIndexItem.COLUMN_AUTOINCREMENTINGID + " INTEGER",
                AvailableIndexItem.COLUMN_CREATIONDATE + " TEXT",
                AvailableIndexItem.COLUMN_LASTMODIFIEDDATE + " TEXT",
                AvailableIndexItem.COLUMN_LASTOPENEDDATE + " TEXT",
                AvailableIndexItem.COLUMN_SORTORDER + " INTEGER",
                AvailableIndexItem.COLUMN_PATCHORDER + " TEXT",
                AvailableIndexItem.COLUMN_CONTENTTYPE + " TEXT",
                AvailableIndexItem.COLUMN_EXPANSIONFILEURL + " TEXT",
                AvailableIndexItem.COLUMN_EXPANSIONFILEPATH + " TEXT",
                AvailableIndexItem.COLUMN_EXPANSIONFILEVERSION + " TEXT",
                AvailableIndexItem.COLUMN_EXPANSIONFILESIZE + " INTEGER",
                AvailableIndexItem.COLUMN_EXPANSIONFILECHECKSUM + " TEXT",
                AvailableIndexItem.COLUMN_PATCHFILEVERSION + " TEXT",
                AvailableIndexItem.COLUMN_PATCHFILESIZE + " INTEGER",
                AvailableIndexItem.COLUMN_PATCHFILECHECKSUM + " TEXT",
                AvailableIndexItem.COLUMN_AUTHOR + " TEXT",
                AvailableIndexItem.COLUMN_WEBSITE + " TEXT",
                AvailableIndexItem.COLUMN_DATEUPDATED + " TEXT",
                AvailableIndexItem.COLUMN_LANGUAGES + " TEXT",
                AvailableIndexItem.COLUMN_TAGS + " TEXT",
                AvailableIndexItem.COLUMN_INSTALLEDFLAG + " INTEGER",
                AvailableIndexItem.COLUMN_MAINDOWNLOADFLAG + " INTEGER",
                AvailableIndexItem.COLUMN_PATCHDOWNLOADFLAG + " INTEGER")
                .execute(sqLiteDatabase);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // foo

        if (oldVersion == 1 && newVersion == 2){
            ALTER_TABLE(AvailableIndexItem.TABLE_NAME)
                    .ADD_COLUMN(AvailableIndexItem.COLUMN_AUTOINCREMENTINGID + " INTEGER")
                    .execute(db);
            ALTER_TABLE(AvailableIndexItem.TABLE_NAME)
                    .ADD_COLUMN(AvailableIndexItem.COLUMN_CREATIONDATE + " TEXT")
                    .execute(db);
            ALTER_TABLE(AvailableIndexItem.TABLE_NAME)
                    .ADD_COLUMN(AvailableIndexItem.COLUMN_LASTMODIFIEDDATE + " TEXT")
                    .execute(db);
            ALTER_TABLE(AvailableIndexItem.TABLE_NAME)
                    .ADD_COLUMN(AvailableIndexItem.COLUMN_LASTOPENEDDATE + " TEXT")
                    .execute(db);
            ALTER_TABLE(AvailableIndexItem.TABLE_NAME)
                    .ADD_COLUMN(AvailableIndexItem.COLUMN_SORTORDER + " INTEGER")
                    .execute(db);
        }

    }

    public int getNextAutoincrementingId() {

        return -1;

    }

    public Observable<List<AvailableIndexItem>> getAvailableIndexItems() {

        // select all rows

        return query(SELECT(AvailableIndexItem.COLUMN_ID,
                AvailableIndexItem.COLUMN_TITLE,
                AvailableIndexItem.COLUMN_DESCRIPTION,
                AvailableIndexItem.COLUMN_THUMBNAILPATH,
                AvailableIndexItem.COLUMN_PACKAGENAME,
                AvailableIndexItem.COLUMN_EXPANSIONID,
                AvailableIndexItem.COLUMN_AUTOINCREMENTINGID,
                AvailableIndexItem.COLUMN_CREATIONDATE,
                AvailableIndexItem.COLUMN_LASTMODIFIEDDATE,
                AvailableIndexItem.COLUMN_LASTOPENEDDATE,
                AvailableIndexItem.COLUMN_SORTORDER,
                AvailableIndexItem.COLUMN_PATCHORDER,
                AvailableIndexItem.COLUMN_CONTENTTYPE,
                AvailableIndexItem.COLUMN_EXPANSIONFILEURL,
                AvailableIndexItem.COLUMN_EXPANSIONFILEPATH,
                AvailableIndexItem.COLUMN_EXPANSIONFILEVERSION,
                AvailableIndexItem.COLUMN_EXPANSIONFILESIZE,
                AvailableIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                AvailableIndexItem.COLUMN_PATCHFILEVERSION,
                AvailableIndexItem.COLUMN_PATCHFILESIZE,
                AvailableIndexItem.COLUMN_PATCHFILECHECKSUM,
                AvailableIndexItem.COLUMN_AUTHOR,
                AvailableIndexItem.COLUMN_WEBSITE,
                AvailableIndexItem.COLUMN_DATEUPDATED,
                AvailableIndexItem.COLUMN_LANGUAGES,
                AvailableIndexItem.COLUMN_TAGS,
                AvailableIndexItem.COLUMN_INSTALLEDFLAG,
                AvailableIndexItem.COLUMN_MAINDOWNLOADFLAG,
                AvailableIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(AvailableIndexItem.TABLE_NAME))
                .map(new Func1<SqlBrite.Query, List<AvailableIndexItem>>() {

                    @Override
                    public List<AvailableIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return AvailableIndexItemMapper.list(cursor);
                    }
                });
    }

    public Observable<List<AvailableIndexItem>> getAvailableIndexItemsByInstalledFlag(boolean installedFlag) {

        int installedInt = 0;

        if (installedFlag) {
            installedInt = 1;
        }

        // select all rows with matching download flag

        return query(SELECT(AvailableIndexItem.COLUMN_ID,
                AvailableIndexItem.COLUMN_TITLE,
                AvailableIndexItem.COLUMN_DESCRIPTION,
                AvailableIndexItem.COLUMN_THUMBNAILPATH,
                AvailableIndexItem.COLUMN_PACKAGENAME,
                AvailableIndexItem.COLUMN_EXPANSIONID,
                AvailableIndexItem.COLUMN_AUTOINCREMENTINGID,
                AvailableIndexItem.COLUMN_CREATIONDATE,
                AvailableIndexItem.COLUMN_LASTMODIFIEDDATE,
                AvailableIndexItem.COLUMN_LASTOPENEDDATE,
                AvailableIndexItem.COLUMN_SORTORDER,
                AvailableIndexItem.COLUMN_PATCHORDER,
                AvailableIndexItem.COLUMN_CONTENTTYPE,
                AvailableIndexItem.COLUMN_EXPANSIONFILEURL,
                AvailableIndexItem.COLUMN_EXPANSIONFILEPATH,
                AvailableIndexItem.COLUMN_EXPANSIONFILEVERSION,
                AvailableIndexItem.COLUMN_EXPANSIONFILESIZE,
                AvailableIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                AvailableIndexItem.COLUMN_PATCHFILEVERSION,
                AvailableIndexItem.COLUMN_PATCHFILESIZE,
                AvailableIndexItem.COLUMN_PATCHFILECHECKSUM,
                AvailableIndexItem.COLUMN_AUTHOR,
                AvailableIndexItem.COLUMN_WEBSITE,
                AvailableIndexItem.COLUMN_DATEUPDATED,
                AvailableIndexItem.COLUMN_LANGUAGES,
                AvailableIndexItem.COLUMN_TAGS,
                AvailableIndexItem.COLUMN_INSTALLEDFLAG,
                AvailableIndexItem.COLUMN_MAINDOWNLOADFLAG,
                AvailableIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(AvailableIndexItem.TABLE_NAME)
                .WHERE(AvailableIndexItem.COLUMN_INSTALLEDFLAG + " = ? "), Integer.toString(installedInt)) // query parameters must be strings?
                .map(new Func1<SqlBrite.Query, List<AvailableIndexItem>>() {

                    @Override
                    public List<AvailableIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return AvailableIndexItemMapper.list(cursor);
                    }
                });
    }

    // need patch download version too?
    // method may be unecessary, all installed items should be checked at startup
    public Observable<List<AvailableIndexItem>> getAvailableIndexItemsByMainDownloadFlag(boolean downloadFlag) {

        int downloadInt = 0;

        if (downloadFlag) {
            downloadInt = 1;
        }

        // select all rows with matching download flag

        return query(SELECT(AvailableIndexItem.COLUMN_ID,
                AvailableIndexItem.COLUMN_TITLE,
                AvailableIndexItem.COLUMN_DESCRIPTION,
                AvailableIndexItem.COLUMN_THUMBNAILPATH,
                AvailableIndexItem.COLUMN_PACKAGENAME,
                AvailableIndexItem.COLUMN_EXPANSIONID,
                AvailableIndexItem.COLUMN_AUTOINCREMENTINGID,
                AvailableIndexItem.COLUMN_CREATIONDATE,
                AvailableIndexItem.COLUMN_LASTMODIFIEDDATE,
                AvailableIndexItem.COLUMN_LASTOPENEDDATE,
                AvailableIndexItem.COLUMN_SORTORDER,
                AvailableIndexItem.COLUMN_PATCHORDER,
                AvailableIndexItem.COLUMN_CONTENTTYPE,
                AvailableIndexItem.COLUMN_EXPANSIONFILEURL,
                AvailableIndexItem.COLUMN_EXPANSIONFILEPATH,
                AvailableIndexItem.COLUMN_EXPANSIONFILEVERSION,
                AvailableIndexItem.COLUMN_EXPANSIONFILESIZE,
                AvailableIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                AvailableIndexItem.COLUMN_PATCHFILEVERSION,
                AvailableIndexItem.COLUMN_PATCHFILESIZE,
                AvailableIndexItem.COLUMN_PATCHFILECHECKSUM,
                AvailableIndexItem.COLUMN_AUTHOR,
                AvailableIndexItem.COLUMN_WEBSITE,
                AvailableIndexItem.COLUMN_DATEUPDATED,
                AvailableIndexItem.COLUMN_LANGUAGES,
                AvailableIndexItem.COLUMN_TAGS,
                AvailableIndexItem.COLUMN_INSTALLEDFLAG,
                AvailableIndexItem.COLUMN_MAINDOWNLOADFLAG,
                AvailableIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(AvailableIndexItem.TABLE_NAME)
                .WHERE(AvailableIndexItem.COLUMN_MAINDOWNLOADFLAG + " = ? "), Integer.toString(downloadInt)) // query parameters must be strings?
                .map(new Func1<SqlBrite.Query, List<AvailableIndexItem>>() {

                    @Override
                    public List<AvailableIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return AvailableIndexItemMapper.list(cursor);
                    }
                });
    }

    public Observable<List<AvailableIndexItem>> getAvailableIndexItemsByType(String contentType) {

        // select all rows with matching content type

        return query(SELECT(AvailableIndexItem.COLUMN_ID,
                AvailableIndexItem.COLUMN_TITLE,
                AvailableIndexItem.COLUMN_DESCRIPTION,
                AvailableIndexItem.COLUMN_THUMBNAILPATH,
                AvailableIndexItem.COLUMN_PACKAGENAME,
                AvailableIndexItem.COLUMN_EXPANSIONID,
                AvailableIndexItem.COLUMN_AUTOINCREMENTINGID,
                AvailableIndexItem.COLUMN_CREATIONDATE,
                AvailableIndexItem.COLUMN_LASTMODIFIEDDATE,
                AvailableIndexItem.COLUMN_LASTOPENEDDATE,
                AvailableIndexItem.COLUMN_SORTORDER,
                AvailableIndexItem.COLUMN_PATCHORDER,
                AvailableIndexItem.COLUMN_CONTENTTYPE,
                AvailableIndexItem.COLUMN_EXPANSIONFILEURL,
                AvailableIndexItem.COLUMN_EXPANSIONFILEPATH,
                AvailableIndexItem.COLUMN_EXPANSIONFILEVERSION,
                AvailableIndexItem.COLUMN_EXPANSIONFILESIZE,
                AvailableIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                AvailableIndexItem.COLUMN_PATCHFILEVERSION,
                AvailableIndexItem.COLUMN_PATCHFILESIZE,
                AvailableIndexItem.COLUMN_PATCHFILECHECKSUM,
                AvailableIndexItem.COLUMN_AUTHOR,
                AvailableIndexItem.COLUMN_WEBSITE,
                AvailableIndexItem.COLUMN_DATEUPDATED,
                AvailableIndexItem.COLUMN_LANGUAGES,
                AvailableIndexItem.COLUMN_TAGS,
                AvailableIndexItem.COLUMN_INSTALLEDFLAG,
                AvailableIndexItem.COLUMN_MAINDOWNLOADFLAG,
                AvailableIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(AvailableIndexItem.TABLE_NAME)
                .WHERE(AvailableIndexItem.COLUMN_CONTENTTYPE + " = ? "), contentType)
                .map(new Func1<SqlBrite.Query, List<AvailableIndexItem>>() {

                    @Override
                    public List<AvailableIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return AvailableIndexItemMapper.list(cursor);
                    }
                });
    }

    public Observable<Long> addAvailableIndexItem(long id, String title, String description, String thumbnailPath, String packageName, String expansionId, int autoincrementingId, java.util.Date creationDate, java.util.Date lastModifiedDate, java.util.Date lastOpenedDate, int sortOrder, String patchOrder, String contentType, String expansionFileUrl, String expansionFilePath, String expansionFileVersion, long expansionFileSize, String expansionFileChecksum, String patchFileVersion, long patchFileSize, String patchFileChecksum, String author, String website, String dateUpdated, String languages, String tags, int installedFlag, int mainDownloadFlag, int patchDownloadFlag, boolean replace) {

        Timber.d("ADDING ROW FOR " + expansionId + "(MAIN " + mainDownloadFlag + ", PATCH " + patchDownloadFlag + ", REPLACE? " + replace + ")");

        Observable<Long> rowId = null;

        int autoincrementingId_local = getNextAutoincrementingId();
        java.util.Date creationDate_local = new java.util.Date();

        ContentValues values = AvailableIndexItemMapper.contentValues()
                .id(r.nextLong())
                .title(title)
                .description(description)
                .thumbnailPath(thumbnailPath)
                .packageName(packageName)
                .expansionId(expansionId)
                .autoincrementingId(autoincrementingId_local)
                .creationDate(creationDate_local)
                .lastModifiedDate(creationDate_local)
                .lastOpenedDate(creationDate_local)
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
                rowId = insert(AvailableIndexItem.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
            } else {
                rowId = insert(AvailableIndexItem.TABLE_NAME, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
        } catch (SQLiteConstraintException sce) {
            Timber.d("INSERT FAILED: " + sce.getMessage());
        }

        return rowId;
    }

    public Observable<Long> addAvailableIndexItem(scal.io.liger.model.ExpansionIndexItem item, boolean replace) {

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

        return addAvailableIndexItem(r.nextLong(),
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
                0,  // default to false (not installed), no need to update liger ExpansionIndexItem class
                0,  // default to false (not downloading), no need to update liger ExpansionIndexItem class
                0,  // default to false (not downloading), no need to update liger ExpansionIndexItem class
                replace);
    }

    public Observable<Long> addAvailableIndexItem(AvailableIndexItem item, boolean replace) {

        return addAvailableIndexItem(item.getId(),
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

    public Observable<Integer> removeAvailableIndexItem(AvailableIndexItem item) {

        // remove an existing record

        return removeAvailableIndexItemByKey(item.getExpansionId());
    }

    public Observable<Integer> removeAvailableIndexItemByKey(String key) {

        // remove an existing record with a matching key

        Timber.d("REMOVE ROW FOR " + key);

        return delete(AvailableIndexItem.TABLE_NAME,
                AvailableIndexItem.COLUMN_EXPANSIONID + " = ? ",
                key);
    }

    public Observable<List<AvailableIndexItem>> getAvailableIndexItem(AvailableIndexItem item) {

        // check current state of an existing record

        return getAvailableIndexItemByKey(item.getExpansionId());
    }

    public Observable<List<AvailableIndexItem>> getAvailableIndexItemByKey(String key) {

        // check current state of an existing record with a matching key

        return query(SELECT(AvailableIndexItem.COLUMN_ID,
                AvailableIndexItem.COLUMN_TITLE,
                AvailableIndexItem.COLUMN_DESCRIPTION,
                AvailableIndexItem.COLUMN_THUMBNAILPATH,
                AvailableIndexItem.COLUMN_PACKAGENAME,
                AvailableIndexItem.COLUMN_EXPANSIONID,
                AvailableIndexItem.COLUMN_AUTOINCREMENTINGID,
                AvailableIndexItem.COLUMN_CREATIONDATE,
                AvailableIndexItem.COLUMN_LASTMODIFIEDDATE,
                AvailableIndexItem.COLUMN_LASTOPENEDDATE,
                AvailableIndexItem.COLUMN_SORTORDER,
                AvailableIndexItem.COLUMN_PATCHORDER,
                AvailableIndexItem.COLUMN_CONTENTTYPE,
                AvailableIndexItem.COLUMN_EXPANSIONFILEURL,
                AvailableIndexItem.COLUMN_EXPANSIONFILEPATH,
                AvailableIndexItem.COLUMN_EXPANSIONFILEVERSION,
                AvailableIndexItem.COLUMN_EXPANSIONFILESIZE,
                AvailableIndexItem.COLUMN_EXPANSIONFILECHECKSUM,
                AvailableIndexItem.COLUMN_PATCHFILEVERSION,
                AvailableIndexItem.COLUMN_PATCHFILESIZE,
                AvailableIndexItem.COLUMN_PATCHFILECHECKSUM,
                AvailableIndexItem.COLUMN_AUTHOR,
                AvailableIndexItem.COLUMN_WEBSITE,
                AvailableIndexItem.COLUMN_DATEUPDATED,
                AvailableIndexItem.COLUMN_LANGUAGES,
                AvailableIndexItem.COLUMN_TAGS,
                AvailableIndexItem.COLUMN_INSTALLEDFLAG,
                AvailableIndexItem.COLUMN_MAINDOWNLOADFLAG,
                AvailableIndexItem.COLUMN_PATCHDOWNLOADFLAG)
                .FROM(AvailableIndexItem.TABLE_NAME)
                .WHERE(AvailableIndexItem.COLUMN_EXPANSIONID + " = ? "), key)
                .map(new Func1<SqlBrite.Query, List<AvailableIndexItem>>() {

                    @Override
                    public List<AvailableIndexItem> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        return AvailableIndexItemMapper.list(cursor);
                    }
                });
    }
}
