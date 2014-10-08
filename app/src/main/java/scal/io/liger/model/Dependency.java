package scal.io.liger.model;

/**
 * Created by mnbogner on 7/14/14.
 */
public class Dependency {

    private String dependency_id;
    private String dependency_file;

    public Dependency() {
        // required for JSON/GSON
    }

    public Dependency(String dependency_id, String dependency_file) {
        this.dependency_id = dependency_id;
        this.dependency_file = dependency_file;
    }

    public String getDependency_id() {
        return dependency_id;
    }

    public void setDependency_id(String dependency_id) {
        this.dependency_id = dependency_id;
    }

    public String getDependency_file() {
        return dependency_file;
    }

    public void setDependency_file(String dependency_file) {
        this.dependency_file = dependency_file;
    }
}
