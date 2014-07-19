package scal.io.liger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.fima.cardsui.views.CardUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;


public class MainActivity extends Activity {

    Context mContext = this;
    CardUI mCardView;
    ArrayList<CardModel> cardModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCardList();
    }

    private void initCardList() {
        mCardView = (CardUI) findViewById(R.id.cardsview);
        if (mCardView == null)
            return;

        mCardView.setSwipeable(false);

        cardModels = getCardModels();

        refreshCardView();
    }

    private ArrayList<CardModel> getCardModels() {
        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPathModel.class, new StoryPathDeserializer());
        Gson gson = gBuild.create();

        String json = JsonHelper.loadJSON(this, "misc_card_test.json");
        StoryPathModel spm = gson.fromJson(json, StoryPathModel.class);

        return spm.getCards();
    }

    public void addCardModel(CardModel model) {
        cardModels.add(model);
        refreshCardView();
    }

    public void refreshCardView () {
        mCardView.clearCards();

        //add cardlist to view
        for (CardModel model : cardModels) {
            mCardView.addCard(model.getCardView(mContext));
        }

        mCardView.refresh();
    }
}