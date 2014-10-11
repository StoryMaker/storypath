package scal.io.liger.model;

import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by mnbogner on 9/29/14.
 */
public class StoryPathLibrary {

    private String id;
    private String title;
    private String fileLocation;
    private Map<List<String>, String> hookMap;
    private List<String> storyPathTemplateFiles;

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

    public Map<List<String>, String> getHookMap() {
        return hookMap;
    }

    public void setHookMap(Map<List<String>, String> hookMap) {
        this.hookMap = hookMap;
    }

    public List<String> getStoryPathTemplateFiles() {
        return storyPathTemplateFiles;
    }

    public void setStoryPathTemplateFiles(List<String> storyPathTemplateFiles) {
        this.storyPathTemplateFiles = storyPathTemplateFiles;
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
