package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import scal.io.liger.view.DisplayableCard;
import scal.io.liger.view.SelfEvalCardView;

/**
 * Created by mnbogner on 7/17/14.
 */
public class SelfEvalCard extends Card {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private String header;
    @Expose private ArrayList<String> checklist;

    public SelfEvalCard() {
        super();
    }

    @Override
    public DisplayableCard getDisplayableCard(Context context) {
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

    @Override
    public void copyText(Card card) {
        if (!(card instanceof SelfEvalCard)) {
            Log.e(TAG, "CARD " + card.getId() + " IS NOT AN INSTANCE OF SelfEvalCard");
        }
        if (!(this.getId().equals(card.getId()))) {
            Log.e(TAG, "CAN'T COPY STRINGS FROM " + card.getId() + " TO " + this.getId() + " (CARD ID'S MUST MATCH)");
            return;
        }

        SelfEvalCard castCard = (SelfEvalCard)card;

        this.title = castCard.getTitle();
        this.header = castCard.getHeader();
        this.checklist = castCard.getChecklist();
    }
}
