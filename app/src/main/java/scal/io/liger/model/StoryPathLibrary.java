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
    private String file_location;
    private Map<List<String>, String> hook_map;
    private List<String> story_path_template_files;

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

    public String getFile_location() {
        return file_location;
    }

    public void setFile_location(String file_location) {
        this.file_location = file_location;
    }

    public Map<List<String>, String> getHook_map() {
        return hook_map;
    }

    public void setHook_map(Map<List<String>, String> hook_map) {
        this.hook_map = hook_map;
    }

    public List<String> getStory_path_template_files() {
        return story_path_template_files;
    }

    public void setStory_path_template_files(List<String> story_path_template_files) {
        this.story_path_template_files = story_path_template_files;
    }

    public String buildPath(String originalPath) {
        if (originalPath.startsWith(File.separator)) {
            return originalPath;
        }

        // construct path relative to location of story path
        String relativePath = getFile_location();

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
