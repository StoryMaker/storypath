package scal.io.liger.widget;

/**
 * Created by mnbogner on 7/11/14.
 */
public class VideoCaptureWidget extends Widget {
    public String camera_type;

    public VideoCaptureWidget() {
        this.type = this.getClass().getName();
    }

    public String getCamera_type() {
        return camera_type;
    }

    public void setCamera_type(String camera_type) {
        this.camera_type = camera_type;
    }
}
