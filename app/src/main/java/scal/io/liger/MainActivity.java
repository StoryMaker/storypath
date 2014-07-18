package scal.io.liger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;
import com.fima.cardsui.views.CardUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import scal.io.liger.view.CardView;
import scal.io.liger.view.IntroCardView;


public class MainActivity extends Activity {

    Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCardList();
    }

    private void initCardList() {
        CardUI mCardView = (CardUI) findViewById(R.id.cardsview);
        if (mCardView == null)
            return;

        mCardView.clearCards();
        mCardView.setSwipeable(false);

        ArrayList<CardModel> cardModels = getCardModels();

        //add cardlist to view
        for (CardModel model : cardModels) {
            mCardView.addCard(model.getCardView(mContext));
        }

        //draw cards
        mCardView.refresh();
    }

    private ArrayList<CardModel> getCardModels() {
        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPathModel.class, new StoryPathDeserializer());
        Gson gson = gBuild.create();

        String json = JsonHelper.loadJSON(this, "intro_card_test.json");
        StoryPathModel spm = gson.fromJson(json, StoryPathModel.class);

        return spm.getCards();
    }
}
