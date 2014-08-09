package scal.io.liger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
        Log.d("MainActivity", "onCreate");
        initApp();
    }

    private void initApp() {
        SharedPreferences sp = getSharedPreferences("appPrefs", Context.MODE_PRIVATE);
        boolean isFirstStart = sp.getBoolean("isFirstStartFlag", true);

        // if it was the first app start
        if(isFirstStart) {
            //save our flag
            SharedPreferences.Editor e = sp.edit();
            e.putBoolean("isFirstStartFlag", false);
            e.commit();
        }

        JsonHelper.setupFileStructure(this, isFirstStart);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] jsonFiles = JsonHelper.getJSONFileList();

        //should never happen
        if(jsonFiles.length == 0) {
            jsonFiles = new String[1];
            jsonFiles[0] = "Please add JSON files to the 'Liger' Folder and restart app\n(Located on root of SD card)";

            builder.setTitle("No JSON files found")
                .setItems(jsonFiles, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int index) {
                    }
                });
        }
        else {
            builder.setTitle("Choose Story File(SdCard/Liger/)")
                .setItems(jsonFiles, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int index) {
                        JsonHelper.setSelectedJSONFile(index);
                        initCardList();
                    }
                });
        }

        AlertDialog alert = builder.create();
        alert.show();
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

        String json = JsonHelper.loadJSON();
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