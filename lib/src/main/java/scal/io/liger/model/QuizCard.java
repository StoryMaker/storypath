package scal.io.liger.model;

import android.content.Context;

import java.util.ArrayList;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.QuizCardView;

/**
 * Created by josh@scal.io on 10/17/2014.
 */
public class QuizCard extends Card {
    private String question;
    private String caption;
    private String filter;
    private String description;
    private ArrayList<Choice> choices;
    private ArrayList<String> correct_answers; // Collection of Choice#id
    private int correct_required;

    public static class Choice {
        public String id;
        public String text;
        String filter;
        String filters_or;
        String filters_and;
    }

    public QuizCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
        return new QuizCardView(context, this); //TODO
    }

    public int getCorrectRequired() {
        return correct_required;
    }

    public ArrayList<String> getCorrectAnswers() {
        return correct_answers;
    }

    public String getDescription() {
        return fillReferences(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Choice> getOptions() {
        return choices;
    }

    public void setOptions(ArrayList<Choice> choices) {
        this.choices = choices;
    }

    public void addChoice(Choice choice) {
        if (this.choices == null)
            this.choices = new ArrayList<Choice>();

        this.choices.add(choice);
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
