package scal.io.liger.model.sqlbrite;

import android.util.Log;

import com.hannesdorfmann.sqlbrite.objectmapper.annotation.ObjectMappable;

/**
 * Created by mnbogner on 8/20/15.
 */

@ObjectMappable
public class InstalledIndexItem extends ExpansionIndexItem {

    public static final String TABLE_NAME = "InstalledIndexItem";


    @Override
    public java.util.Date getCreationDate() { return creationDate; }

    public void setCreationDate(java.util.Date creationDate) { this.creationDate = creationDate; }


    public InstalledIndexItem() {
        super();

    }

    public InstalledIndexItem(ExpansionIndexItem item) {
        super(item.getId(),
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
                item.getPatchDownloadFlag());
    }

    public InstalledIndexItem(long id, String title, String description, String thumbnailPath, String packageName, String expansionId, int autoincrementingId, java.util.Date creationDate, java.util.Date lastModifiedDate, java.util.Date lastOpenedDate, int sortOrder, String patchOrder, String contentType, String expansionFileUrl, String expansionFilePath, String expansionFileVersion, long expansionFileSize, String expansionFileChecksum, String patchFileVersion, long patchFileSize, String patchFileChecksum, String author, String website, String dateUpdated, String languages, String tags, int installedFlag, int mainDownloadFlag, int patchDownloadFlag) {
        super(id,
                title,
                description,
                thumbnailPath,
                packageName,
                expansionId,
                autoincrementingId,
                creationDate,
                lastModifiedDate,
                lastOpenedDate,
                sortOrder,
                patchOrder,
                contentType,
                expansionFileUrl,
                expansionFilePath,
                expansionFileVersion,
                expansionFileSize,
                expansionFileChecksum,
                patchFileVersion,
                patchFileSize,
                patchFileChecksum,
                author,
                website,
                dateUpdated,
                languages,
                tags,
                installedFlag,
                mainDownloadFlag,
                patchDownloadFlag);

        this.creationDate = creationDate;

    }

    @Override
    public int compareTo(Object another) {
            if (another instanceof InstalledIndexItem) {
                    //return new Integer(getSortOrder()).compareTo(new Integer(((AvailableIndexItem) another).getSortOrder()));



                    //java.util.Date thisDate = new java.util.Date(creationDate.toString());
                    //java.util.Date thatDate = new java.util.Date(((InstalledIndexItem) another).getCreationDate().toString());
                    java.util.Date thisDate = getCreationDate();
                    java.util.Date thatDate = ((InstalledIndexItem) another).getCreationDate();
                    Log.d("InstalledIndexItem", "compareTo " + getExpansionId() + " " + thisDate.toString() + " " + ((InstalledIndexItem) another).getExpansionId() + " " + thatDate.toString());
                    return thisDate.compareTo(thatDate);

            } else if (another instanceof AvailableIndexItem) {
                    return -1; // should always appear below installed index items
            } else if (another instanceof InstanceIndexItem) {
                    return -1; // should always appear below instance index items
            } else {
                    return 0; // otherwise don't care
            }
    }

}
