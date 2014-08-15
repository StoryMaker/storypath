package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.PreviewCardView;


public class PreviewCardModel extends CardModel {
    private ArrayList<String> media_paths;
    private String text;

    public PreviewCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new PreviewCardView(context, this);
    }

    public ArrayList<String> getMedia_paths() {
        ArrayList<String> a = new ArrayList<String>();
        for (String s : media_paths)
        {
            a.add(fillReferences(s));
        }
        return a;
    }

    public void setMedia_paths(ArrayList<String> media_paths) {
        this.media_paths = media_paths;
    }

    public void addMedia_path(String media_path) {
        if (this.media_paths == null)
            this.media_paths = new ArrayList<String>();

        this.media_paths.add(media_path);
    }

    public String getText() { return fillReferences(this.text); }

    public void setText(String text) { this.text = text; }
}