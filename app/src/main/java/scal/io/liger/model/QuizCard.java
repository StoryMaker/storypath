package scal.io.liger.model;

import android.content.Context;

import java.util.ArrayList;

import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/10/14.
 */
public class QuizCard extends Card {

    private String description;
    private ArrayList<Object> options;

    public QuizCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
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
