package scal.io.liger.model;

import android.content.Context;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import scal.io.liger.JsonHelper;

/**
 * Created by mnbogner on 12/8/14.
 */
public class InstanceIndexItem extends BaseIndexItem {

    // only save libraries?

    String instanceFilePath;   // static, key
    // String storyTitle;         // watch/update/persist (what path/library field is this?)
    // String storyDescription;   // watch/update/persist (what path/library field is this?)
    String storyType;          // static? (what path/library field is this?)
    // String storyThumbnailPath; // watch/update/persist
    long storyCreationDate;    // static
    long storySaveDate;        // watch/update/persist? (this field would force an update to the index for every update to a path/library)
    String language;           // set to app language, used to force updates if language changes

    // additional fields for supporting sequences of lessons
    String storyPathId;
    private ArrayList<String> storyPathPrerequisites;
    long storyCompletionDate;

    public InstanceIndexItem() {

    }

    public InstanceIndexItem(String instanceFilePath, long storyCreationDate) {
        this.instanceFilePath = instanceFilePath;
        this.storyCreationDate = storyCreationDate;
    }

    public String getInstanceFilePath() {
        return instanceFilePath;
    }

    public void setInstanceFilePath(String instanceFilePath) {
        this.instanceFilePath = instanceFilePath;
    }

    public String getStoryType() {
        return storyType;
    }

    public void setStoryType(String storyType) {
        this.storyType = storyType;
    }

    public long getStoryCreationDate() {
        return storyCreationDate;
    }

    public void setStoryCreationDate(long storyCreationDate) {
        this.storyCreationDate = storyCreationDate;
    }

    public long getStorySaveDate() {
        return storySaveDate;
    }

    public void setStorySaveDate(long storySaveDate) {
        this.storySaveDate = storySaveDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getLastModifiedTime() {
        if (TextUtils.isEmpty(instanceFilePath)) {
            return 0;
        } else {
            return new File(instanceFilePath).lastModified();
        }
    }

    public String getStoryPathId() {
        return storyPathId;
    }

    public void setStoryPathId(String storyPathId) {
        this.storyPathId = storyPathId;
    }

    public ArrayList<String> getStoryPathPrerequisites() {
        return storyPathPrerequisites;
    }

    public void setStoryPathPrerequisites(ArrayList<String> storyPathPrerequisites) {
        this.storyPathPrerequisites = storyPathPrerequisites;
    }

    public long getStoryCompletionDate() {
        return storyCompletionDate;
    }

    public void setStoryCompletionDate(long storyCompletionDate) {
        this.storyCompletionDate = storyCompletionDate;
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

    public void deleteAssociatedFiles(Context context, boolean deleteMedia) {

        File libraryToDelete = new File(instanceFilePath);

        if (libraryToDelete.exists()) {

            // open library to get associated file(s) to delete
            String jsonString = JsonHelper.loadJSON(libraryToDelete, language);

            // if no string was loaded, cannot continue
            if (jsonString == null) {
                Log.e("INDEX", "json could not be loaded from " + libraryToDelete.getPath());
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
}
