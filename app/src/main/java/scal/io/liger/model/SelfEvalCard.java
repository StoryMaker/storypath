package scal.io.liger.model;

import android.content.Context;

import java.util.ArrayList;

import scal.io.liger.view.SelfEvalCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class SelfEvalCard extends Card {

    private String header;
    private ArrayList<String> checklist;

    public SelfEvalCard() {
        super();
        this.type = this.getClass().getName();
    }

    @Override
    public com.fima.cardsui.objects.Card getCardView(Context context) {
        return new SelfEvalCardView(context, this);
    }

    public String getHeader() {
        return fillReferences(header);
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public ArrayList<String> getChecklist() {
        ArrayList<String> a = new ArrayList<String>();
        if (checklist != null) {
            for (String s : checklist) {
                a.add(fillReferences(s));
            }
        }
        return a;
    }

    public void setChecklist(ArrayList<String> checklist) {
        this.checklist = checklist;
    }

    public void addChecklistItem(String checklistItem) {
        if (this.checklist == null)
            this.checklist = new ArrayList<String>();

        this.checklist.add(checklistItem);
    }
}
