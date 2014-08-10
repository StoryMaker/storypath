package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.OrderMediaCardView;


public class OrderMediaCardModel extends CardModel {
    private String header;
    private ArrayList<Object> clips;

    public OrderMediaCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new OrderMediaCardView(context, this);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public ArrayList<Object> getClips() {
        return clips;
    }

    public void setClips(ArrayList<Object> clips) {
        this.clips = clips;
    }

    public void addClips(Object clip) {
        if (this.clips == null)
            this.clips = new ArrayList<Object>();

        this.clips.add(clip);
    }
}
