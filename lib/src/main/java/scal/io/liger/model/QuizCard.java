package scal.io.liger.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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


    public QuizCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public DisplayableCard getDisplayableCard(@NonNull Context context) {
        return new QuizCardView(context, this); //TODO
    }

    @Nullable
    public int getCorrectRequired() {
        return correct_required;
    }

    /**
     * Return the correct choice ids for this QuizCard or null
     * if any first selection should render the card complete.
     */
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

    public ArrayList<Choice> getChoices() {
        return choices;
    }

    public void setChoices(ArrayList<Choice> choices) {
        this.choices = choices;
    }

    public ArrayList<String> getCorrect_answers() {
        return correct_answers;
    }

    public void setCorrect_answers(ArrayList<String> correct_answers) {
        this.correct_answers = correct_answers;
    }

    public int getCorrect_required() {
        return correct_required;
    }

    public void setCorrect_required(int correct_required) {
        this.correct_required = correct_required;
    }
}
