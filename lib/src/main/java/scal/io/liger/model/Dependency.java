package scal.io.liger.model;

/**
 * Created by mnbogner on 7/14/14.
 */
public class Dependency {

    private String dependencyId;
    private String dependencyFile;

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
