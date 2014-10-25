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
    private ArrayList<String> correctAnswers; // Collection of Choice#id   // FIXME camelCase this
    private int correctRequired; // FIXME camelCase this


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
        return correctRequired;
    }

    /**
     * Return the correct choice ids for this QuizCard or null
     * if any first selection should render the card complete.
     */
    public ArrayList<String> getCorrectAnswers() {
        return correctAnswers;
    } // FIXME we need to filter down correctAnswers to only include choices that passed our filters

    public String getDescription() {
        return fillReferences(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // FIXME rename to getChoices
    public ArrayList<Choice> getOptions() {
        return choices;
    }

    // FIXME a generalized version of this should probably be brought down to base class and used all over the place
    public ArrayList<Choice> getFilteredChoices() {
        ArrayList<Choice> filtered = new ArrayList<Choice>();
        for (Choice choice: choices) {
            if (choice.filter != null) {
                if (checkReferencedValueMatches(choice.filter)) {
                    filtered.add(choice);
                }
            } else if (choice.filters_and != null) {
                boolean andPassed = true;
                for (String filter: choice.filters_and) {
                    if (!checkReferencedValueMatches(filter)) {
                        andPassed = false;
                        break;
                    }
                }
                if (andPassed) filtered.add(choice);
            } else if (choice.filters_or != null) {
                boolean orPassed = false;
                for (String filter: choice.filters_or) {
                    if (checkReferencedValueMatches(filter)) {
                        orPassed = true;
                        break;
                    }
                }
                if (orPassed) filtered.add(choice);
            } else { // if there are no filters, we just add it to the good list
                filtered.add(choice);
            }
        }
        return filtered;
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

    public void setCorrectAnswers(ArrayList<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void setCorrectRequired(int correctRequired) {
        this.correctRequired = correctRequired;
    }
}
