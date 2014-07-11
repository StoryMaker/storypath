package scal.io.liger.widget;

/**
 * Created by mnbogner on 7/11/14.
 */
public class ImageWidget extends Widget {
    public String path;

    public ImageWidget() {
        this.type = this.getClass().getName();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
