package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/10/14.
 */
public class ClipTypeCardModel extends CardModel {
    public ArrayList<Object> clip_types;

    public ClipTypeCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new IntroCardView(context, this); //TODO
    }

    public ArrayList<Object> getClip_types() {
        return clip_types;
    }

    public void setClip_types(ArrayList<Object> clip_types) {
        this.clip_types = clip_types;
    }

    public void addClip_type(Object clip_type) {
        if (this.clip_types == null)
            this.clip_types = new ArrayList<Object>();

        this.clip_types.add(clip_type);
    }
}
