package scal.io.liger.model;

import android.content.Context;

import java.util.ArrayList;

import scal.io.liger.view.IntroCardView;

/**
 * Created by mnbogner on 7/10/14.
 */
public class QuizReportCard extends Card {

    private String description;
    private ArrayList<Object> results;

    public QuizReportCard() {
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
