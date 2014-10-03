package scal.io.liger.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mnbogner on 9/29/14.
 */
public class Story {

    public StoryPathLibrary storyPathLibrary;
    public StoryPath currentStoryPath;
    public ArrayList<String> storyPathInstanceFiles; // story path instance files
    public HashMap<String, MediaFile> mediaFiles;

    public StoryPathLibrary getStoryPathLibrary() {
        return storyPathLibrary;
    }

    public void setStoryPathLibrary(StoryPathLibrary storyPathLibrary) {
        this.storyPathLibrary = storyPathLibrary;
    }

    public StoryPath getCurrentStoryPath() {
        return currentStoryPath;
    }

    public void setCurrentStoryPath(StoryPath currentStoryPath) {
        this.currentStoryPath = currentStoryPath;
    }

    public ArrayList<String> getStoryPathInstanceFiles() {
        return storyPathInstanceFiles;
    }

    public void setStoryPathInstanceFiles(ArrayList<String> storyPathInstanceFiles) {
        this.storyPathInstanceFiles = storyPathInstanceFiles;
    }

    public void addStoryPathFile(String file) {
        if (storyPathInstanceFiles == null) {
            storyPathInstanceFiles = new ArrayList<String>();
        }

        storyPathInstanceFiles.add(file);
    }

    public HashMap<String, MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(HashMap<String, MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public void saveMediaFile(String uuid, MediaFile file) {

        if (mediaFiles == null) {
            mediaFiles = new HashMap<String, MediaFile>();
        }
        mediaFiles.put(uuid, file);

    }

    public MediaFile loadMediaFile(String uuid) {
        return mediaFiles.get(uuid);
    }
}
