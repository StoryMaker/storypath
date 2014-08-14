package scal.io.liger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.fima.cardsui.views.CardUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import scal.io.liger.model.CardModel;
import scal.io.liger.model.DependencyModel;
import scal.io.liger.model.StoryPathModel;


public class MainActivity extends Activity {

    Context mContext = this;
    CardUI mCardView;
    StoryPathModel mStoryPathModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MainActivity", "onCreate");
//        initApp();
        if (savedInstanceState == null) {
            initApp();
        } else {
            Log.d("MainActivity", "savedInstanceState not null, check for and load storypath json");
            if (savedInstanceState.containsKey("storyPathJson")) {
                String json = savedInstanceState.getString("storyPathJson");
                initCardList(json);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Gson gson = new Gson();
        mStoryPathModel.clearCardReferences(); // FIXME move this stuff into the model itself so we dont have to worry about it
        mStoryPathModel.context = null;
        String json = gson.toJson(mStoryPathModel);
        outState.putString("storyPathJson", json);
        mStoryPathModel.context = this;
        mStoryPathModel.setCardReferences();
        super.onSaveInstanceState(outState);
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
                        String json = JsonHelper.loadJSON();
                        initCardList(json);
                    }
                });
        }

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initCardList(String json) {
        mCardView = (CardUI) findViewById(R.id.cardsview);
        if (mCardView == null)
            return;

        mCardView.setSwipeable(false);

        initStoryPathModel(json);
        refreshCardView();
    }

    private void initStoryPathModel(String json) {
        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPathModel.class, new StoryPathDeserializer());
        Gson gson = gBuild.create();

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

    public void goToCard(String cardPath) {
        // assumes the format story::card::field::value
        String[] pathParts = cardPath.split("::");

        StoryPathModel story = null;
        boolean newStory = false;
        if (mStoryPathModel.getId().equals(pathParts[0])) {
            // reference targets this story path
            story = mStoryPathModel;
        } else {
            // reference targets a serialized story path
            for (DependencyModel dependency : mStoryPathModel.getDependencies()) {
                if (dependency.getDependencyId().equals(pathParts[0])) {
                    GsonBuilder gBuild = new GsonBuilder();
                    gBuild.registerTypeAdapter(StoryPathModel.class, new StoryPathDeserializer());
                    Gson gson = gBuild.create();

                    String json = JsonHelper.loadJSONFromPath(dependency.getDependencyFile());
                    story = gson.fromJson(json, StoryPathModel.class);

                    story.context = this.mContext;
                    story.setCardReferences();

                    newStory = true;
                }
            }
        }

        if (story == null) {
            System.err.println("STORY PATH ID " + pathParts[0] + " WAS NOT FOUND");
            return;
        }

        CardModel card = story.getCardById(cardPath);

        if (card == null) {
            System.err.println("CARD ID " + pathParts[1] + " WAS NOT FOUND");
            return;
        }

        int cardIndex = story.getValidCards().indexOf(card);

        if (cardIndex < 0) {
            System.err.println("CARD ID " + pathParts[1] + " IS NOT VISIBLE");
            return;
        }

        if (newStory) {

            // TODO: need additional code to save current story path

            mStoryPathModel = story;
            refreshCardView();
        }

        mCardView.scrollToCard(cardIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {

            if(requestCode == Constants.REQUEST_VIDEO_CAPTURE) {

                Uri uri = intent.getData();
                String path = getRealPathFromURI(getApplicationContext(), uri);
                String pathId = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                if (null == pathId || null == uri) {
                    return;
                }

                CardModel cm = mStoryPathModel.getCardById(pathId);
                cm.addValue("value::" + path);
            } else if(requestCode == Constants.REQUEST_IMAGE_CAPTURE) {

                String path = getLastImagePath();
                String pathId = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                if (null == pathId || null == path) {
                    return;
                }

                CardModel cm = mStoryPathModel.getCardById(pathId);
                cm.addValue("value::" + path);
            } else if(requestCode == Constants.REQUEST_AUDIO_CAPTURE) {

                Uri uri = intent.getData();
                String path = getRealPathFromURI(getApplicationContext(), uri);
                String pathId = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                if (null == pathId || null == uri) {
                    return;
                }

                CardModel cm = mStoryPathModel.getCardById(pathId);
                cm.addValue("value::" + path);
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getLastImagePath() {
        final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
        Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
        String imagePath = null;

        if(imageCursor.moveToFirst()){
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            imagePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            imageCursor.close();
        }

        return imagePath;
    }
}