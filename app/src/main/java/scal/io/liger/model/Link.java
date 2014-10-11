package scal.io.liger.model;

/**
 * Created by mnbogner on 7/14/14.
 */
public class Link {

    private String linkText;
    private String linkPath;

    public Link() {
        // required for JSON/GSON
    }

    public Link(String linkText, String linkPath) {
        this.linkText = linkText;
        this.linkPath = linkPath;
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    public String getLinkPath() {
        return linkPath;
    }

    public void setLinkPath(String linkPath) {
        this.linkPath = linkPath;
    }
}
