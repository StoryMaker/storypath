package scal.io.liger.model;

/**
 * Created by mnbogner on 7/14/14.
 */
public class Link {

    private String link_text;
    private String link_path;

    public Link() {
        // required for JSON/GSON
    }

    public Link(String link_text, String link_path) {
        this.link_text = link_text;
        this.link_path = link_path;
    }

    public String getLink_text() {
        return link_text;
    }

    public void setLink_text(String link_text) {
        this.link_text = link_text;
    }

    public String getLink_path() {
        return link_path;
    }

    public void setLink_path(String link_path) {
        this.link_path = link_path;
    }
}
