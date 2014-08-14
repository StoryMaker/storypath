package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.OrderMediaCardView;


public class OrderMediaCardModel extends CardModel {
    private String header;
    private ArrayList<String> clips;

    public OrderMediaCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new OrderMediaCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public ArrayList<String> getClips() {
        return clips;
    }

    public void setClips(ArrayList<String> clips) {
        this.clips = clips;
    }

    public void addClips(String clip) {
        if (this.clips == null)
            this.clips = new ArrayList<String>();

        this.clips.add(clip);
    }
}
