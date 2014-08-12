package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/10/14.
 */
public class QuizCardModel extends CardModel {
    public String description;
    public ArrayList<Object> options;

    public QuizCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new IntroCardView(context, this); //TODO
    }

    public String getDescription() {
        return fillReferences(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Object> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<Object> options) {
        this.options = options;
    }

    public void addOption(Object option) {
        if (this.options == null)
            this.options = new ArrayList<Object>();

        this.options.add(option);
    }
}
