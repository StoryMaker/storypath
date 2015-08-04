package scal.io.liger.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

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

    public void deleteAssociatedFiles(Context context) {

        File libraryToDelete = new File(instanceFilePath);

        if (libraryToDelete.exists()) {

            // open library to get associated file(s) to delete
            String jsonString = JsonHelper.loadJSON(libraryToDelete, language);
            ArrayList<String> referencedFiles = new ArrayList<String>(); // no need to insert dependencies to open for checking file path
            StoryPathLibrary spl = JsonHelper.deserializeStoryPathLibrary(jsonString, libraryToDelete.getAbsolutePath(), referencedFiles, context, language);

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
