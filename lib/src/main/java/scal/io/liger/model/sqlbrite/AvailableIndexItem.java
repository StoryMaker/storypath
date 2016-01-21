package scal.io.liger.model.sqlbrite;

import com.hannesdorfmann.sqlbrite.objectmapper.annotation.ObjectMappable;

/**
 * Created by mnbogner on 8/20/15.
 */

@ObjectMappable
public class AvailableIndexItem extends ExpansionIndexItem {

    public static final String TABLE_NAME = "AvailableIndexItem";

    public AvailableIndexItem() {
        super();

    }

    public AvailableIndexItem(ExpansionIndexItem item) {
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

    public AvailableIndexItem(long id, String title, String description, String thumbnailPath, String packageName, String expansionId, int autoincrementingId, java.util.Date creationDate, java.util.Date lastModifiedDate, java.util.Date lastOpenedDate, int sortOrder, String patchOrder, String contentType, String expansionFileUrl, String expansionFilePath, String expansionFileVersion, long expansionFileSize, String expansionFileChecksum, String patchFileVersion, long patchFileSize, String patchFileChecksum, String author, String website, String dateUpdated, String languages, String tags, int installedFlag, int mainDownloadFlag, int patchDownloadFlag) {
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
    }

    @Override
    public int compareTo(Object another) {
        if (another instanceof AvailableIndexItem) {


            //Log.d("ExpansionIndexItem", "compare 3 available " + getExpansionId() + " " + ((AvailableIndexItem) another).getExpansionId());


            return new Integer(getSortOrder()).compareTo(new Integer(((AvailableIndexItem) another).getSortOrder()));
                //return new Date(getLastModifiedTime()).compareTo(new Date(((AvailableIndexItem) another).getLastModifiedTime())); // compare file dates for other available index items
        } else if (another instanceof InstalledIndexItem) {
            //Log.d("AvailableIndexItem", "compare installed " + getExpansionId() + " " + ((InstalledIndexItem) another).getExpansionId());


            return -1; // should always appear below installed index items
        } else if (another instanceof InstanceIndexItem) {
            //Log.d("AvailableIndexItem", "compare instance " + getExpansionId() + " " + ((InstanceIndexItem) another).getInstanceFilePath());

            return -1; // should always appear below instance index items
        } else {
                return 0; // otherwise don't care
        }
    }

}
