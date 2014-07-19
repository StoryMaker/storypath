package scal.io.liger;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.view.SelfEvalCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class SelfEvalCardModel extends CardModel {
    public String header;
    public ArrayList<String> checklist;

    public SelfEvalCardModel() {
        this.type = this.getClass().getName();
    }

    @Override
    public Card getCardView(Context context) {
        return new SelfEvalCardView(context, this);
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public ArrayList<String> getChecklist() {
        return checklist;
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
