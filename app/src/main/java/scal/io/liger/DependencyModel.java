package scal.io.liger;

/**
 * Created by mnbogner on 7/14/14.
 */
public class DependencyModel {
    public String dependencyType;
    public String dependencyId;
    public String dependencyFile;

    public String getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(String dependencyType) {
        this.dependencyType = dependencyType;
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
