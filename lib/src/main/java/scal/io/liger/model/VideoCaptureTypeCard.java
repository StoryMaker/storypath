package scal.io.liger.model;

import android.content.Context;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/11/14.
 */
public class VideoCaptureTypeCard extends Card {

    @Expose private ArrayList<Object> body;

    public VideoCaptureTypeCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new IntroCardView(context, this); //TODO
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
