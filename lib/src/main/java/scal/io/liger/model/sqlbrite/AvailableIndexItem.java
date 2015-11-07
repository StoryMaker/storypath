package scal.io.liger.model.sqlbrite;

import timber.log.Timber;

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

    public AvailableIndexItem(long id, String title, String description, String thumbnailPath, String packageName, String expansionId, String patchOrder, String contentType, String expansionFileUrl, String expansionFilePath, String expansionFileVersion, long expansionFileSize, String expansionFileChecksum, String patchFileVersion, long patchFileSize, String patchFileChecksum, String author, String website, String dateUpdated, String languages, String tags, int installedFlag, int mainDownloadFlag, int patchDownloadFlag) {
        super(id,
                title,
                description,
                thumbnailPath,
                packageName,
                expansionId,
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
}
