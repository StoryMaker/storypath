package scal.io.liger.model;

import com.google.gson.annotations.Expose;

/**
 * Relates a String id to a File describing a {@link scal.io.liger.model.StoryPath}.
 * Used by separate StoryPaths to reference each other.
 *
 * Created by mnbogner on 7/14/14.
 */
public class Dependency {

    @Expose private String dependencyId;
    @Expose private String dependencyFile;

    public String toString() {
        return dependencyId + ": " + dependencyFile;
    }

    public Dependency() {
        // required for JSON/GSON
    }

    public Dependency(String dependencyId, String dependencyFile) {
        this.dependencyId = dependencyId;
        this.dependencyFile = dependencyFile;
    }

    public String getDependencyId() {
        return dependencyId;
    }

    public void setDependencyId(String dependencyId) {
        this.dependencyId = dependencyId;
    }

    public String getDependencyFile() {
        return dependencyFile;
    }

    public void setDependencyFile(String dependencyFile) {
        this.dependencyFile = dependencyFile;
    }
}
