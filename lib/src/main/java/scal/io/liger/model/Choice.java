package scal.io.liger.model;

import timber.log.Timber;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

/**
 * Created by mnbogner on 10/21/14.
 */
public class Choice {
    @Expose public String id;
    @Expose public String text;
    @Expose String filter;
    @Expose ArrayList<String> filters_or;
    @Expose ArrayList<String> filters_and;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public ArrayList<String> getFilters_or() {
        return filters_or;
    }

    public void setFilters_or(ArrayList<String> filters_or) {
        this.filters_or = filters_or;
    }

    public ArrayList<String> getFilters_and() {
        return filters_and;
    }

    public void setFilters_and(ArrayList<String> filters_and) {
        this.filters_and = filters_and;
    }
}
