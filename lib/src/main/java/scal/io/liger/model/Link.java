package scal.io.liger.model;

import com.google.gson.annotations.Expose;

/**
 * Created by mnbogner on 7/14/14.
 */
public class Link {

    @Expose private String linkText;
    @Expose private String linkPath;

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
