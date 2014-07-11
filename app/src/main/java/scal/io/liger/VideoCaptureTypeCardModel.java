package scal.io.liger;

import java.util.ArrayList;

/**
 * Created by mnbogner on 7/11/14.
 */
public class VideoCaptureTypeCardModel extends CardModel {
    public ArrayList<Object> body;

    public VideoCaptureTypeCardModel() {
        this.type = this.getClass().getName();
    }

    public ArrayList<Object> getBody() {
        return body;
    }

    public void setBody(ArrayList<Object> body) {
        this.body = body;
    }

    public void addBody(Object body) {
        if (this.body == null)
            this.body = new ArrayList<Object>();

        this.body.add(body);
    }
}
