package scal.io.liger.model.sqlbrite;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hannesdorfmann.sqlbrite.objectmapper.annotation.Column;
import com.hannesdorfmann.sqlbrite.objectmapper.annotation.ObjectMappable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import scal.io.liger.JsonHelper;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPathLibrary;

/**
 * Created by mnbogner on 8/20/15.
 */

@ObjectMappable
public class InstanceIndexItem extends BaseIndexItem {

    public static final String TABLE_NAME = "InstanceIndexItem";
    public static final String COLUMN_INSTANCEFILEPATH = "instanceFilePath";
    public static final String COLUMN_STORYCREATIONDATE = "storyCreationDate";
    public static final String COLUMN_STORYSAVEDATE = "storySaveDate";
    public static final String COLUMN_STORYTYPE = "storyType";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_STORYPATHID = "storyPathId";
    public static final String COLUMN_STORYPATHPREREQUISITES = "storyPathPrerequisites";
    public static final String COLUMN_STORYCOMPLETIONDATE = "storyCompletionDate";

    @Column(COLUMN_INSTANCEFILEPATH) public String instanceFilePath;
    @Column(COLUMN_STORYCREATIONDATE) public long storyCreationDate;
    @Column(COLUMN_STORYSAVEDATE) public long storySaveDate;
    @Column(COLUMN_STORYTYPE) public String storyType;
    @Column(COLUMN_LANGUAGE) public String language;

    // additional fields for supporting sequences of lessons
    @Column(COLUMN_STORYPATHID) public String storyPathId;
    @Column(COLUMN_STORYPATHPREREQUISITES) public String storyPathPrerequisites; // comma-delimited list, need access methods that will construct an ArrayList<String>
    @Column(COLUMN_STORYCOMPLETIONDATE) public long storyCompletionDate;

    public InstanceIndexItem() {
        super();

    }

    public InstanceIndexItem(long id, String title, String description, String thumbnailPath, String instanceFilePath, long storyCreationDate, long storySaveDate, String storyType, String language, String storyPathId, String storyPathPrerequisites, long storyCompletionDate) {
        super(id, title, description, thumbnailPath);
        this.instanceFilePath = instanceFilePath;
        this.storyCreationDate = storyCreationDate;
        this.storySaveDate = storySaveDate;
        this.storyType = storyType;
        this.language = language;
        this.storyPathId = storyPathId;
        this.storyPathPrerequisites = storyPathPrerequisites;
        this.storyCompletionDate = storyCompletionDate;
    }

    public String getInstanceFilePath() {
        return instanceFilePath;
    }

    public long getStoryCreationDate() {
        return storyCreationDate;
    }

    public long getStorySaveDate() {
        return storySaveDate;
    }

    public String getStoryType() {
        return storyType;
    }

    public String getLanguage() {
        return language;
    }

    public String getStoryPathId() {
        return storyPathId;
    }

    public String getStoryPathPrerequisites() {
        return storyPathPrerequisites;
    }

    public long getStoryCompletionDate() {
        return storyCompletionDate;
    }

    public void setInstanceFilePath(String instanceFilePath) {
        this.instanceFilePath = instanceFilePath;
    }

    public void setStoryCreationDate(long storyCreationDate) {
        this.storyCreationDate = storyCreationDate;
    }

    public void setStorySaveDate(long storySaveDate) {
        this.storySaveDate = storySaveDate;
    }

    public void setStoryType(String storyType) {
        this.storyType = storyType;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setStoryPathId(String storyPathId) {
        this.storyPathId = storyPathId;
    }

    public void setStoryPathPrerequisites(String storyPathPrerequisites) {
        this.storyPathPrerequisites = storyPathPrerequisites;
    }

    public void setStoryCompletionDate(long storyCompletionDate) {
        this.storyCompletionDate = storyCompletionDate;
    }

    public void deleteAssociatedFiles(Context context, boolean deleteMedia) {

        File libraryToDelete = new File(instanceFilePath);

        if (libraryToDelete.exists()) {

            // open library to get associated file(s) to delete
            String jsonString = JsonHelper.loadJSON(libraryToDelete, language);

            // if no string was loaded, cannot continue
            if (jsonString == null) {
                Log.e("INDEX", "json could not be loaded from " + libraryToDelete);
                // delete existing file anyway
                Log.d("INDEX", "DELETING STORY LIBRARY INSTANCE " + libraryToDelete.getName());
                libraryToDelete.delete();
                return;
            }

            ArrayList<String> referencedFiles = new ArrayList<String>(); // no need to insert dependencies to open for checking file path
            StoryPathLibrary spl = JsonHelper.deserializeStoryPathLibrary(jsonString, libraryToDelete.getAbsolutePath(), referencedFiles, context, language);

            // if specified, delete all media files associated with this story
            // (this may cause issues if another story imported these files)
            if (deleteMedia) {
                HashMap<String, MediaFile> mediaMap = spl.getMediaFiles();
                if (mediaMap != null) {
                    Collection<MediaFile> mediaFiles = mediaMap.values();
                    for (MediaFile mediaFile : mediaFiles) {
                        File f1 = new File(mediaFile.getPath());

                        if (f1.exists()) {
                            Log.d("INDEX", "DELETING STORY MEDIA FILE " + f1.getPath());
                            f1.delete();
                        }

                        if (mediaFile.getThumbnailFilePath() != null) {

                            File f2 = new File(mediaFile.getThumbnailFilePath());

                            if (f2.exists()) {
                                Log.d("INDEX", "DELETING STORY MEDIA THUMBNAIL " + f2.getPath());
                                f2.delete();
                            }
                        }

                    }
                }

                // audio clips appear to be metadata + a uuid to a media file so the previous block should delete them
            }

            if (spl.getCurrentStoryPathFile() != null) {

                File pathToDelete = new File(spl.getCurrentStoryPathFile());

                if (pathToDelete.exists()) {
                    Log.d("INDEX", "DELETING STORY PATH INSTANCE " + pathToDelete.getName());
                    pathToDelete.delete();
                }
            }

            Log.d("INDEX", "DELETING STORY LIBRARY INSTANCE " + libraryToDelete.getName());
            libraryToDelete.delete();
        }
    }

    public long getLastModifiedTime() {
        if (TextUtils.isEmpty(instanceFilePath)) {
            return 0;
        } else {
            return new File(instanceFilePath).lastModified();
        }
    }

    @Override
    public int compareTo(Object another) {
        if (another instanceof  ExpansionIndexItem) {
            return 1; // should always appear above expansion index items
        } else if (another instanceof InstanceIndexItem) {
            return new Date(getLastModifiedTime()).compareTo(new Date(((InstanceIndexItem)another).getLastModifiedTime())); // compare file dates for other instance index items
        } else {
            return 0; // otherwise don't care
        }
    }
}
