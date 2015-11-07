package scal.io.liger.model;

import timber.log.Timber;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

/**
 * Created by mnbogner on 10/27/14.
 */
public class Tip {
    @Expose protected String text;
    @Expose protected ArrayList<String> tags;

    public Tip(String text, ArrayList<String> tags) {
        this.text = text;
        this.tags = tags;
    }
}
