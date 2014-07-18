package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class IntroCardModel extends CardModel {
    public String headline;
    public String level;
    public String time;

    public IntroCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new IntroCardView(context, this); //TODO
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
