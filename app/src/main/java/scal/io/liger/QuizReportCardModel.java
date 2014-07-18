package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/10/14.
 */
public class QuizReportCardModel extends CardModel {
    public String description;
    public ArrayList<Object> results;

    public QuizReportCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new IntroCardView(context, this); //TODO
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Object> getResults() {
        return results;
    }

    public void setResults(ArrayList<Object> results) {
        this.results = results;
    }

    public void addResult(Object result) {
        if (this.results == null)
            this.results = new ArrayList<Object>();

        this.results.add(results);
    }
}
