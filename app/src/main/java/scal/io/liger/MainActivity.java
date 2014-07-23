package scal.io.liger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.fima.cardsui.views.CardUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class MainActivity extends Activity {

    Context mContext = this;
    CardUI mCardView;
    StoryPathModel mStoryPathModel;

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

        initStoryPathModel();
        refreshCardView();
    }

    private void initStoryPathModel() {
        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPathModel.class, new StoryPathDeserializer());
        Gson gson = gBuild.create();

        String json = JsonHelper.loadJSON(this, "misc_card_test.json");
        mStoryPathModel = gson.fromJson(json, StoryPathModel.class);
        mStoryPathModel.context = this.mContext;
        mStoryPathModel.setCardReferences();
    }

    public void refreshCardView () {
        if (mCardView == null)
            return;

        mCardView.clearCards();

        //add cardlist to view
        for (CardModel model : mStoryPathModel.getValidCards()) {
            mCardView.addCard(model.getCardView(mContext));
        }

        mCardView.refresh();
    }
}