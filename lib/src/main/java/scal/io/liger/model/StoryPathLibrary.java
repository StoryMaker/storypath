package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mnbogner on 9/29/14.
 */
public class StoryPathLibrary {

    private String id;
    private String title;
    private String fileLocation;
    private HashMap<List<String>, String> hookMap;
    private ArrayList<String> storyPathTemplateFiles;
    private ArrayList<String> storyPathInstanceFiles;
    private StoryPath currentStoryPath; // not serialized
    private String currentStoryPathFile;
    private HashMap<String, MediaFile> mediaFiles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public HashMap<List<String>, String> getHookMap() {
        return hookMap;
    }

    public void setHookMap(HashMap<List<String>, String> hookMap) {
        this.hookMap = hookMap;
    }

    public ArrayList<String> getStoryPathTemplateFiles() {
        return storyPathTemplateFiles;
    }

    public void setStoryPathTemplateFiles(ArrayList<String> storyPathTemplateFiles) {
        this.storyPathTemplateFiles = storyPathTemplateFiles;
    }

    public ArrayList<String> getStoryPathInstanceFiles() {
        return storyPathInstanceFiles;
    }

    public void setStoryPathInstanceFiles(ArrayList<String> storyPathInstanceFiles) {
        this.storyPathInstanceFiles = storyPathInstanceFiles;
    }

    public void addStoryPathInstanceFile(String file) {
        if (this.storyPathInstanceFiles == null) {
            this.storyPathInstanceFiles = new ArrayList<String>();
        }

        this.storyPathInstanceFiles.add(file);
    }

    public StoryPath getCurrentStoryPath() {
        return currentStoryPath;
    }

    public void setCurrentStoryPath(StoryPath currentStoryPath) {
        this.currentStoryPath = currentStoryPath;
    }

    public String getCurrentStoryPathFile() {
        return currentStoryPathFile;
    }

    public void setCurrentStoryPathFile(String currentStoryPathFile) {
        this.currentStoryPathFile = currentStoryPathFile;
    }

    public HashMap<String, MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(HashMap<String, MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public void saveMediaFile(String uuid, MediaFile file) {
        if (this.mediaFiles == null) {
            this.mediaFiles = new HashMap<String, MediaFile>();
        }

        this.mediaFiles.put(uuid, file);
    }

    public MediaFile loadMediaFile(String uuid) {
        return mediaFiles.get(uuid);
    }

    // need to determine whether users are allowed to delete files that are referenced by cards
    // need to determine whether to automatically delete files when they are no longer referenced
    public void deleteMediaFile(String uuid) {
        if ((mediaFiles == null) || (!mediaFiles.keySet().contains(uuid))) {
            Log.e(this.getClass().getName(), "key was not found, cannot delete file");
            return;
        }

        mediaFiles.remove(uuid);

        // delete actual file?
    }

    // need to determine where to present path options and deserialize new path
    public void switchPaths(StoryPath newPath) {
        // export clip metadata
        // also may need to export stored values
        StoryPath oldPath = this.getCurrentStoryPath();
        ArrayList<ClipMetadata> metadata = oldPath.exportMetadata();

        // serialize current story path
        Gson gson = new Gson();
        oldPath.setStoryReference(null);
        oldPath.clearObservers();
        oldPath.clearCardReferences(); // FIXME move this stuff into the model itself so we dont have to worry about it
        Context oldContext = oldPath.getContext();
        oldPath.setContext(null);

        String json = gson.toJson(oldPath);

        try {
            File oldPathFile = new File(oldPath.buildPath(oldPath.getId() + ".path"));
            PrintStream ps = new PrintStream(new FileOutputStream(oldPathFile.getPath()));
            ps.print(json);
            // store file path
            // NOT YET SURE HOW TO HANDLE VERSIONS OR DUPLICATES
            this.addStoryPathInstanceFile(oldPathFile.getPath());
        } catch (FileNotFoundException fnfe) {
            Log.e(this.getClass().getName(), "could not file file: " + fnfe.getMessage());
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "other exception: " + e.getMessage());
        }

        // import clip metadata
        newPath.importMetadata(metadata);

        // should this be done externally?
        newPath.setContext(oldContext);
        newPath.setCardReferences();
        newPath.initializeObservers();
        newPath.setStoryReference(this);

        // update current story path
        this.setCurrentStoryPath(newPath);

        // NOTIFY/REFRESH HERE OR LET THAT BE HANDLED BY WHATEVER CALLS THIS?
    }

    public String buildPath(String originalPath) {
        if (originalPath.startsWith(File.separator)) {
            return originalPath;
        }

        // construct path relative to location of story path
        String relativePath = getFileLocation();

        if ((relativePath != null) && (relativePath.length() != 0)) {
            relativePath = relativePath.substring(0, relativePath.lastIndexOf(File.separator));
            relativePath = relativePath + File.separator + originalPath;
            return relativePath;
        } else {
            Log.e(this.getClass().getName(), "NO ROOT TO CONSTRUCT RELATIVE PATH FOR " + originalPath);
            return originalPath;
        }
    }
}
