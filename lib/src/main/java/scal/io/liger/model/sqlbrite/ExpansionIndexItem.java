package scal.io.liger.model.sqlbrite;

import android.util.Log;

import com.hannesdorfmann.sqlbrite.objectmapper.annotation.Column;
import com.hannesdorfmann.sqlbrite.objectmapper.annotation.ObjectMappable;

import scal.io.liger.Constants;
import timber.log.Timber;

/**
 * Created by mnbogner on 8/20/15.
 */

@ObjectMappable
public class ExpansionIndexItem extends BaseIndexItem {

    public static final String COLUMN_PACKAGENAME = "packageName";
    public static final String COLUMN_EXPANSIONID = "expansionId";
    public static final String COLUMN_AUTOINCREMENTINGID = "autoincrementingId";
    public static final String COLUMN_CREATIONDATE = "creationDate";
    public static final String COLUMN_LASTMODIFIEDDATE = "lastModifiedDate";
    public static final String COLUMN_LASTOPENEDDATE = "lastOpenedDate";
    public static final String COLUMN_SORTORDER = "sortOrder";
    public static final String COLUMN_PATCHORDER = "patchOrder";
    public static final String COLUMN_CONTENTTYPE = "contentType";
    public static final String COLUMN_EXPANSIONFILEURL = "expansionFileUrl";
    public static final String COLUMN_EXPANSIONFILEPATH = "expansionFilePath";
    public static final String COLUMN_EXPANSIONFILEVERSION = "expansionFileVersion";
    public static final String COLUMN_EXPANSIONFILESIZE = "expansionFileSize";
    public static final String COLUMN_EXPANSIONFILECHECKSUM = "expansionFileChecksum";
    public static final String COLUMN_PATCHFILEVERSION = "patchFileVersion";
    public static final String COLUMN_PATCHFILESIZE = "patchFileSize";
    public static final String COLUMN_PATCHFILECHECKSUM = "patchFileChecksum";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_WEBSITE = "website";
    public static final String COLUMN_DATEUPDATED = "dateUpdated";
    public static final String COLUMN_LANGUAGES = "languages";
    public static final String COLUMN_TAGS = "tags";
    public static final String COLUMN_INSTALLEDFLAG = "installedFlag";
    public static final String COLUMN_MAINDOWNLOADFLAG = "mainDownloadFlag";
    public static final String COLUMN_PATCHDOWNLOADFLAG = "patchDownloadFlag";


    // required
    @Column(COLUMN_PACKAGENAME) public String packageName;
    @Column(COLUMN_EXPANSIONID) public String expansionId;
    @Column(COLUMN_PATCHORDER) public String patchOrder;
    @Column(COLUMN_CONTENTTYPE) public String contentType;
    @Column(COLUMN_EXPANSIONFILEURL) public String expansionFileUrl;
    @Column(COLUMN_EXPANSIONFILEPATH) public String expansionFilePath; // relative to Context.getExternalFilesDirs()

    // not optional, but need to handle nulls
    @Column(COLUMN_EXPANSIONFILEVERSION) public String expansionFileVersion;
    @Column(COLUMN_EXPANSIONFILESIZE) public long expansionFileSize;
    @Column(COLUMN_EXPANSIONFILECHECKSUM) public String expansionFileChecksum;

    // patch stuff, optional
    @Column(COLUMN_PATCHFILEVERSION) public String patchFileVersion;
    @Column(COLUMN_PATCHFILESIZE) public long patchFileSize;
    @Column(COLUMN_PATCHFILECHECKSUM) public String patchFileChecksum;

    // optional
    @Column(COLUMN_AUTHOR) public String author;
    @Column(COLUMN_WEBSITE) public String website;
    @Column(COLUMN_DATEUPDATED) public String dateUpdated;
    @Column(COLUMN_LANGUAGES) public String languages; // comma-delimited list, need access methods that will construct an ArrayList<String>
    @Column(COLUMN_TAGS) public String tags; // comma-delimited list, need access methods that will construct an ArrayList<String>

    // HashMap<String, String> extras; <- dropping this, don't know a good way to handle hash maps

    // for internal use
    @Column(COLUMN_INSTALLEDFLAG) public int installedFlag;
    @Column(COLUMN_MAINDOWNLOADFLAG) public int mainDownloadFlag;
    @Column(COLUMN_PATCHDOWNLOADFLAG) public int patchDownloadFlag;

    //db version 2 stuff
    @Column(COLUMN_AUTOINCREMENTINGID) public int autoincrementingId;
    @Column(COLUMN_CREATIONDATE) public java.util.Date creationDate;
    @Column(COLUMN_LASTMODIFIEDDATE) public java.util.Date lastModifiedDate;
    @Column(COLUMN_LASTOPENEDDATE) public java.util.Date lastOpenedDate;
    @Column(COLUMN_SORTORDER) public int sortOrder;

    public ExpansionIndexItem() {
        super();

    }

    public ExpansionIndexItem(long id, String title, String description, String thumbnailPath, String packageName, String expansionId, int autoincrementingId, java.util.Date creationDate, java.util.Date lastModifiedDate, java.util.Date lastOpenedDate, int sortOrder, String patchOrder, String contentType, String expansionFileUrl, String expansionFilePath, String expansionFileVersion, long expansionFileSize, String expansionFileChecksum, String patchFileVersion, long patchFileSize, String patchFileChecksum, String author, String website, String dateUpdated, String languages, String tags, int installedFlag, int mainDownloadFlag, int patchDownloadFlag) {
        super(id, title, description, thumbnailPath);
        this.packageName = packageName;
        this.expansionId = expansionId;
        this.autoincrementingId = autoincrementingId;
        this.creationDate = creationDate;
        this.lastModifiedDate = lastModifiedDate;
        this.lastOpenedDate = lastOpenedDate;
        this.sortOrder = sortOrder;
        this.patchOrder = patchOrder;
        this.contentType = contentType;
        this.expansionFileUrl = expansionFileUrl;
        this.expansionFilePath = expansionFilePath;
        this.expansionFileVersion = expansionFileVersion;
        this.expansionFileSize = expansionFileSize;
        this.expansionFileChecksum = expansionFileChecksum;
        this.patchFileVersion = patchFileVersion;
        this.patchFileSize = patchFileSize;
        this.patchFileChecksum = patchFileChecksum;
        this.author = author;
        this.website = website;
        this.dateUpdated = dateUpdated;
        this.languages = languages;
        this.tags = tags;
        this.installedFlag = installedFlag;
        this.mainDownloadFlag = mainDownloadFlag;
        this.patchDownloadFlag = patchDownloadFlag;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getExpansionId() {
        return expansionId;
    }

    public int getAutoincrementingId() { return autoincrementingId; }

    public java.util.Date getCreationDate() { return creationDate; }

    public java.util.Date getLastModifiedDate() { return lastModifiedDate; }

    public java.util.Date getLastOpenedDate() {
        return lastOpenedDate;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getPatchOrder() {
        return patchOrder;
    }

    public String getContentType() {
        return contentType;
    }

    public String getExpansionFileUrl() {
        return expansionFileUrl;
    }

    public String getExpansionFilePath() {
        return expansionFilePath;
    }

    public String getExpansionFileVersion() {
        return expansionFileVersion;
    }

    public long getExpansionFileSize() {
        return expansionFileSize;
    }

    public String getExpansionFileChecksum() {
        return expansionFileChecksum;
    }

    public String getPatchFileVersion() {
        return patchFileVersion;
    }

    public long getPatchFileSize() {
        return patchFileSize;
    }

    public String getPatchFileChecksum() {
        return patchFileChecksum;
    }

    public String getAuthor() {
        return author;
    }

    public String getWebsite() {
        return website;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public String getLanguages() {
        return languages;
    }

    public String getTags() {
        return tags;
    }

    public int getInstalledFlag() {
        return installedFlag;
    }

    public int getMainDownloadFlag() {
        return mainDownloadFlag;
    }

    public int getPatchDownloadFlag() {
        return patchDownloadFlag;
    }

    // sqlite doesn't support boolean columns, so provide an interface to fake it

    public void setInstalledFlag(boolean installedFlag) {
        if (installedFlag) {
            this.installedFlag = 1;
        } else {
            this.installedFlag = 0;
        }
    }

    public boolean isInstalled() {
        if (installedFlag > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setMainDownloadFlag(boolean mainDownloadFlag) {
        if (mainDownloadFlag) {
            this.mainDownloadFlag = 1;
        } else {
            this.mainDownloadFlag = 0;
        }
    }

    public boolean isDownloadingMain() {
        if (mainDownloadFlag > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void setPatchDownloadFlag(boolean patchDownloadFlag) {
        if (patchDownloadFlag) {
            this.patchDownloadFlag = 1;
        } else {
            this.patchDownloadFlag = 0;
        }
    }

    public boolean isDownloadingPatch() {
        if (patchDownloadFlag > 0) {
            return true;
        } else {
            return false;
        }
    }

    // methods added for convenience
    public void setDownloadFlag(boolean downloadFlag, String fileName) {

        Timber.d("SETTING FLAG FOR " + fileName + " TO " + downloadFlag);

        if (fileName.contains(Constants.MAIN)) {
            setMainDownloadFlag(downloadFlag);
        } else if (fileName.contains(Constants.PATCH)) {
            setPatchDownloadFlag(downloadFlag);
        } else {
            Timber.e("CANNOT SET DOWNLOAD FLAG STATE FOR " + fileName);
        }
    }

    public boolean isDownloading(String fileName) {
        if (fileName.contains(Constants.MAIN)) {
            return isDownloadingMain();
        } else if (fileName.contains(Constants.PATCH)) {
            return isDownloadingPatch();
        } else {
            Timber.e("CANNOT DETERMINE DOWNLOAD FLAG STATE FOR " + fileName);
            return false;
        }
    }

    public void update(scal.io.liger.model.ExpansionIndexItem item) {

        // update db item with values from available index item

        // leave id alone

        this.title = item.getTitle();
        this.description = item.getDescription();
        this.thumbnailPath = item.getThumbnailPath();
        this.packageName = item.getPackageName();
        this.expansionId = item.getExpansionId();
        this.autoincrementingId = item.getAutoincrementingId();
        this.creationDate = item.getCreationDate();
        this.lastModifiedDate = item.getLastModifiedDate();
        this.lastOpenedDate = item.getLastOpenedDate();
        this.sortOrder = item.getSortOrder();
        this.patchOrder = item.getPatchOrder();
        this.contentType = item.getContentType();
        this.expansionFileUrl = item.getExpansionFileUrl();
        this.expansionFilePath = item.getExpansionFilePath();
        this.expansionFileVersion = item.getExpansionFileVersion();
        this.expansionFileSize = item.getExpansionFileSize();
        this.expansionFileChecksum = item.getExpansionFileChecksum();
        this.patchFileVersion = item.getPatchFileVersion();
        this.patchFileSize = item.getPatchFileSize();
        this.patchFileChecksum = item.getPatchFileChecksum();
        this.author = item.getAuthor();
        this.website = item.getWebsite();
        this.dateUpdated = item.getDateUpdated();

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

        this.languages = languageString;
        this.tags = tagString;

        // leave installed flag alone
        // leave download flags alone

    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setExpansionId(String expansionId) {
        this.expansionId = expansionId;
    }

    public void setAutoincrementingId(int autoincrementingId) {
        this.autoincrementingId = autoincrementingId;
    }

    public void setCreationDate(java.util.Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setLastModifiedDate(java.util.Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setLastOpenedDate(java.util.Date lastOpenedDate) {
        this.lastOpenedDate = lastOpenedDate;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setPatchOrder(String patchOrder) {
        this.patchOrder = patchOrder;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setExpansionFileUrl(String expansionFileUrl) {
        this.expansionFileUrl = expansionFileUrl;
    }

    public void setExpansionFilePath(String expansionFilePath) {
        this.expansionFilePath = expansionFilePath;
    }

    public void setExpansionFileVersion(String expansionFileVersion) {
        this.expansionFileVersion = expansionFileVersion;
    }

    public void setExpansionFileSize(long expansionFileSize) {
        this.expansionFileSize = expansionFileSize;
    }

    public void setExpansionFileChecksum(String expansionFileChecksum) {
        this.expansionFileChecksum = expansionFileChecksum;
    }

    public void setPatchFileVersion(String patchFileVersion) {
        this.patchFileVersion = patchFileVersion;
    }

    public void setPatchFileSize(long patchFileSize) {
        this.patchFileSize = patchFileSize;
    }

    public void setPatchFileChecksum(String patchFileChecksum) {
        this.patchFileChecksum = patchFileChecksum;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public int compareTo(Object another) {

        Log.d("ExpansionIndexItem", "compare 2");

        if (another instanceof InstanceIndexItem) {
            //Timber.d(title + " COMPARED TO INSTANCE ITEM: -1");
            return -1; // should always appear below instance index items
        } else if (another instanceof ExpansionIndexItem){

            // if this date is later or null, appear below
            // -1

            // if that date is later or null, appear above
            // 1

            if (dateUpdated == null) {
                //Timber.d(title + " HAS NO DATE: -1");
                return -1;
            }

            if (((ExpansionIndexItem)another).getDateUpdated() == null) {
                //Timber.d(title + " HAS A DATE BUT " + ((ExpansionIndexItem)another).getTitle() + " DOES NOT: 1");
                return 1;
            }

            //Timber.d("COMPARING DATE OF " + title + " TO DATE OF " + ((ExpansionIndexItem)another).getTitle() + ": " + dateUpdated.compareTo(((ExpansionIndexItem)another).getDateUpdated()));
            return dateUpdated.compareTo(((ExpansionIndexItem)another).getDateUpdated());

        } else {
            //Timber.d(title + " HAS NO POINT OF COMPARISON: 0");
            return 0; // otherwise don't care
        }
    }
}
