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
import android.widget.Toast;

import com.fima.cardsui.views.CardUI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.MalformedJsonException;
import com.twotoasters.android.support.v7.widget.LinearLayoutManager;
import com.twotoasters.android.support.v7.widget.RecyclerView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import scal.io.liger.adapter.CardAdapter;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.Dependency;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.Story;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    Context mContext = this;
    RecyclerView mRecyclerView;
    //CardUI mCardView;
    StoryPathLibrary mStoryPathLibrary;
    //Story mStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d("MainActivity", "onCreate");
//        initApp();
        if (savedInstanceState == null) {
            Log.d(TAG, "onSaveInstanceState called with savedInstanceState");
            initApp();
        } else {
            Log.d(TAG, "onSaveInstanceState called with no saved state");
            Log.d("MainActivity", "savedInstanceState not null, check for and load storypath json");
            if (savedInstanceState.containsKey("storyPathJson")) {

                //String json1 = savedInstanceState.getString("storyJson");
                //initStory(json1);

                String json2 = savedInstanceState.getString("storyPathLibraryJson");
                initHook(json2);
                // maybe just initStoryPathLibraryModel?

                String json3 = savedInstanceState.getString("storyPathJson");
                initCardList(json3);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState called");

        if (mStoryPathLibrary == null) {
            Log.d(TAG, "data not yet loaded, no state to save");
        } else {
            //Gson gson = new Gson();
            //mStoryPathLibrary.getCurrentStoryPath().setStoryReference(null);
            //mStoryPathLibrary.getCurrentStoryPath().clearObservers();
            //mStoryPathLibrary.getCurrentStoryPath().clearCardReferences(); // FIXME move this stuff into the model itself so we dont have to worry about it
            //mStoryPathLibrary.getCurrentStoryPath().setContext(null);

            // need to serialize Story as well?

            //String json = gson.toJson(mStoryPathLibrary.getCurrentStoryPath());
            //outState.putString("storyPathJson", json);

            outState.putString("storyPathJson", JsonHelper.serializeStoryPath(mStoryPathLibrary.getCurrentStoryPath()));

            //StoryPath sp = mStoryPathLibrary.getCurrentStoryPath();
            //mStoryPathLibrary.setCurrentStoryPath(null);
            //mStory.setStoryPathLibrary(null);

            //String json2 = gson.toJson(mStory);
            //outState.putString("storyJson", json2);

            //String json3 = gson.toJson(mStoryPathLibrary);
            //outState.putString("storyPathLibraryJson", json3);
            outState.putString("storyPathLibraryJson", JsonHelper.serializeStoryPathLibrary(mStoryPathLibrary));

            //mStory.setStoryPathLibrary(mStoryPathLibrary);
            //mStoryPathLibrary.setCurrentStoryPath(sp);

            //mStoryPathLibrary.getCurrentStoryPath().setContext(this);
            //mStoryPathLibrary.getCurrentStoryPath().setCardReferences();
            //mStoryPathLibrary.getCurrentStoryPath().initializeObservers();
            //mStoryPathLibrary.getCurrentStoryPath().setStoryReference(mStoryPathLibrary);

        }

        super.onSaveInstanceState(outState);
    }

    private void initApp() {
        SharedPreferences sp = getSharedPreferences("appPrefs", Context.MODE_PRIVATE);

        /*
        boolean isFirstStart = sp.getBoolean("isFirstStartFlag", true);

        // if it was the first app start
        if(isFirstStart) {
            // save our flag
            SharedPreferences.Editor e = sp.edit();
            e.putBoolean("isFirstStartFlag", false);
            e.commit();
        }
        */

        JsonHelper.setupFileStructure(this);
        MediaHelper.setupFileStructure(this);

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
                        File jsonFile = JsonHelper.setSelectedJSONFile(index);

                        // TEMP - unsure how to best determine new story vs. existing story
                        if (jsonFile.getPath().endsWith("TEST_STORY.json")) {

                            // loadStoryFile(jsonFile);

                        } else {
                            String json = JsonHelper.loadJSON();

                            initHook(json, jsonFile);

                            // need to implement selection of story path based on hook

                            /*
                            jsonFile = new File(mStoryPathLibrary.buildPath(mStoryPathLibrary.getStoryPathTemplateFiles().get("NAME_1")));
                            json = JsonHelper.loadJSONFromPath(jsonFile.getPath());

                            initCardList(json, jsonFile);
                            */

                            mStoryPathLibrary.loadStoryPathTemplate("NAME_1");

                        }
                    }
                });
        }

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initHook(String json) {
        initHook(json, null);
    }

    private void initHook(String json, File jsonFile) {
        Log.d(TAG, "initHook called");

        // unsure what needs to be set up for the hook interface

        try {
            initStoryPathLibraryModel(json, jsonFile);
        } catch (com.google.gson.JsonSyntaxException e) {
            Toast.makeText(MainActivity.this, "JSON syntax error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (MalformedJsonException e) {
            Toast.makeText(MainActivity.this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /*
    private void initStory(String json) {
        Log.d(TAG, "initStory called");
        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.create();

        try {
            mStory = gson.fromJson(json, Story.class);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "JSON parsing error: " + e.getMessage().substring(e.getMessage().indexOf(":") + 2), Toast.LENGTH_LONG).show();
        }
    }
    */

    private void initStoryPathLibraryModel(String json, File jsonFile) throws MalformedJsonException {
        Log.d(TAG, "initStoryPathLibraryModel called");

        if (jsonFile != null) {
            mStoryPathLibrary = JsonHelper.deserializeStoryPathLibrary(json, jsonFile.getPath(), this.mContext);
        } else {
            mStoryPathLibrary = JsonHelper.deserializeStoryPathLibrary(json, null, this.mContext);
        }

        /*
        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.create();

        mStoryPathLibrary = gson.fromJson(json, StoryPathLibrary.class);

        // a story path library model must have a file location to manage relative paths
        // if it is loaded from a saved state, the location should already be set
        if ((jsonFile == null) || (jsonFile.length() == 0)) {
            if ((mStoryPathLibrary.getFileLocation() == null) || (mStoryPathLibrary.getFileLocation().length() == 0)) {
                Log.e(TAG, "file location for story path library " + mStoryPathLibrary.getId() + " could not be determined");
            }
        } else {
            mStoryPathLibrary.setFileLocation(jsonFile.getPath());
        }

        //if (mStory == null) {
        //    mStory = new Story(mStoryPathLibrary);
        //}
        */
    }

    private void initCardList(String json) {
        initCardList(json, null);
    }

    public void initCardList(String json, File jsonFile) {
        Log.d(TAG, "initCardList called");
        if (mRecyclerView == null)
            return;

        //mCardView.setSwipeable(false);

        try {
            initStoryPathModel(json, jsonFile);
            refreshCardView();
        } catch (com.google.gson.JsonSyntaxException e) {
            Toast.makeText(MainActivity.this, "JSON syntax error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (MalformedJsonException e) {
            Toast.makeText(MainActivity.this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initStoryPathModel(String json, File jsonFile) throws MalformedJsonException {
        Log.d(TAG, "initStoryPathModel called");

        if (jsonFile != null) {
            mStoryPathLibrary.setCurrentStoryPath(JsonHelper.deserializeStoryPath(json, jsonFile.getPath(), mStoryPathLibrary, this.mContext));
        } else {
            mStoryPathLibrary.setCurrentStoryPath(JsonHelper.deserializeStoryPath(json, null, mStoryPathLibrary, this.mContext));
        }

        /*
        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
        Gson gson = gBuild.create();

        StoryPath sp = gson.fromJson(json, StoryPath.class);
        sp.setContext(this.mContext);
        sp.setCardReferences();
        sp.initializeObservers();

        // a story path model must have a file location to manage relative paths
        // if it is loaded from a saved state, the location should already be set
        if ((jsonFile == null) || (jsonFile.length() == 0)) {
            if ((sp.getFileLocation() == null) || (sp.getFileLocation().length() == 0)) {
                Log.e(TAG, "file location for story path " + sp.getId() + " could not be determined");
            }
        } else {
            sp.setFileLocation(jsonFile.getPath());
        }

        sp.setStoryReference(mStoryPathLibrary);


        mStoryPathLibrary.setCurrentStoryPath(sp);
        */
    }

    public void refreshCardView () {
        Log.d(TAG, "refreshCardview called");
        if (mRecyclerView == null)
            return;

        //add cardlist to view
        List<Card> cards = mStoryPathLibrary.getCurrentStoryPath().getValidCards();
        mRecyclerView.setAdapter(new CardAdapter(this, cards));
    }

    public void goToCard(String cardPath) throws MalformedJsonException {
        Log.d(TAG, "goToCard: " + cardPath);
        // assumes the format story::card::field::value
        String[] pathParts = cardPath.split("::");

        StoryPath story = null;
        boolean newStory = false;

        /*
        // TEMP CODE FOR TESTING
        if (cardPath == "SWITCH") {
            GsonBuilder gBuild = new GsonBuilder();
            gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
            Gson gson = gBuild.create();

            String jsonFile = "learning_guide_v2.json";

            String json = JsonHelper.loadJSONFromPath(mStory.getStoryPathLibrary().buildPath(jsonFile));
            story = gson.fromJson(json, StoryPath.class);
            story.context = this.mContext;
            story.setCardReferences();
            story.setFileLocation(mStory.getCurrentStoryPath().buildPath(jsonFile));

            mStory.switchPaths(story);
            refreshCardView();
            mCardView.scrollToCard(0);

            return;
        }
        */

        if (mStoryPathLibrary.getCurrentStoryPath().getId().equals(pathParts[0])) {
            // reference targets this story path
            story = mStoryPathLibrary.getCurrentStoryPath();
        } else {
            // reference targets a serialized story path
            for (Dependency dependency : mStoryPathLibrary.getCurrentStoryPath().getDependencies()) {
                if (dependency.getDependencyId().equals(pathParts[0])) {

                    story = JsonHelper.loadStoryPath(dependency.getDependencyFile(), mStoryPathLibrary, this.mContext);

                    /*
                    GsonBuilder gBuild = new GsonBuilder();
                    gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
                    Gson gson = gBuild.create();

                    String jsonFile = dependency.getDependencyFile();
                    String json = JsonHelper.loadJSONFromPath(mStoryPathLibrary.getCurrentStoryPath().buildPath(jsonFile));
                    story = gson.fromJson(json, StoryPath.class);

                    story.setContext(this.mContext);
                    story.setCardReferences();
                    story.setFileLocation(mStoryPathLibrary.getCurrentStoryPath().buildPath(jsonFile));
                    */

                    newStory = true;
                }
            }
        }

        if (story == null) {
            System.err.println("STORY PATH ID " + pathParts[0] + " WAS NOT FOUND");
            return;
        }

        Card card = story.getCardById(cardPath);

        if (card == null) {
            System.err.println("CARD ID " + pathParts[1] + " WAS NOT FOUND");
            return;
        }

        int cardIndex = story.getValidCardIndex(card);

        if (cardIndex < 0) {
            System.err.println("CARD ID " + pathParts[1] + " IS NOT VISIBLE");
            return;
        }

        if (newStory) {

            // TODO: need additional code to save current story path

            // serialize current story path
            // add to story path files

            mStoryPathLibrary.setCurrentStoryPath(story);
            refreshCardView();
        }
        // TODO: Scroll to card
        //mCardView.scrollToCard(cardIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult, requestCode:" + requestCode + ", resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {

            if(requestCode == Constants.REQUEST_VIDEO_CAPTURE) {

                Uri uri = intent.getData();
                String path = getRealPathFromURI(getApplicationContext(), uri);
                Log.d(TAG, "onActivityResult, video path:" + path);
                String pathId = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                if (null == pathId || null == uri) {
                    return;
                }

                Card c = mStoryPathLibrary.getCurrentStoryPath().getCardById(pathId);

                if (c instanceof ClipCard) {
                    ClipCard cc = (ClipCard)c;
                    MediaFile mf = new MediaFile(path, Constants.VIDEO);
                    cc.saveMediaFile(mf);
                } else {
                    Log.e(TAG, "card type " + c.getClass().getName() + " has no method to save " + Constants.VIDEO + " files");
                }

            } else if(requestCode == Constants.REQUEST_IMAGE_CAPTURE) {

                String path = getLastImagePath();
                Log.d(TAG, "onActivityResult, path:" + path);
                String pathId = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                if (null == pathId || null == path) {
                    return;
                }

                Card c = mStoryPathLibrary.getCurrentStoryPath().getCardById(pathId);

                if (c instanceof ClipCard) {
                    ClipCard cc = (ClipCard)c;
                    MediaFile mf = new MediaFile(path, Constants.PHOTO);
                    cc.saveMediaFile(mf);
                } else {
                    Log.e(TAG, "card type " + c.getClass().getName() + " has no method to save " + Constants.PHOTO + " files");
                }

            } else if(requestCode == Constants.REQUEST_AUDIO_CAPTURE) {

                Uri uri = intent.getData();
                String path = getRealPathFromURI(getApplicationContext(), uri);
                Log.d(TAG, "onActivityResult, audio path:" + path);
                String pathId = mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                if (null == pathId || null == uri) {
                    return;
                }

                Card c = mStoryPathLibrary.getCurrentStoryPath().getCardById(pathId);

                if (c instanceof ClipCard) {
                    ClipCard cc = (ClipCard)c;
                    MediaFile mf = new MediaFile(path, Constants.AUDIO);
                    cc.saveMediaFile(mf);
                } else {
                    Log.e(TAG, "card class " + c.getClass().getName() + " has no method to save " + Constants.AUDIO + " files");
                }

            }
        }
    }

    public void saveStoryFile() {
        Gson gson = new Gson();

        /*
        // prep and serialize story path library
        String json3 = gson.toJson(mStoryPathLibrary);

        // write to file, store path
        try {
            File storyPathLibraryFile = new File("/storage/emulated/0/Liger/default/TEST_LIB.json"); // need file naming plan
            FileOutputStream fos = new FileOutputStream(storyPathLibraryFile);
            if (!storyPathLibraryFile.exists()) {
                storyPathLibraryFile.createNewFile();
            }
            byte data[] = json3.getBytes();
            fos.write(data);
            fos.flush();
            fos.close();
            mStory.setStoryPathLibrary(null);
            mStory.setStoryPathLibraryFile(storyPathLibraryFile.getPath());
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        }
        */

        // prep and serialize current story path
        mStoryPathLibrary.getCurrentStoryPath().setStoryReference(null);
        mStoryPathLibrary.getCurrentStoryPath().clearObservers();
        mStoryPathLibrary.getCurrentStoryPath().clearCardReferences(); // FIXME move this stuff into the model itself so we dont have to worry about it
        mStoryPathLibrary.getCurrentStoryPath().setContext(null);
        String json1 = gson.toJson(mStoryPathLibrary.getCurrentStoryPath());

        StoryPath sp = mStoryPathLibrary.getCurrentStoryPath();

        // write to file, store path
        try {
            File currentStoryPathFile = new File("/storage/emulated/0/Liger/default/TEST_PATH.json"); // need file naming plan
            FileOutputStream fos = new FileOutputStream(currentStoryPathFile);
            if (!currentStoryPathFile.exists()) {
                currentStoryPathFile.createNewFile();
            }
            byte data[] = json1.getBytes();
            fos.write(data);
            fos.flush();
            fos.close();
            mStoryPathLibrary.setCurrentStoryPath(null);
            mStoryPathLibrary.setCurrentStoryPathFile(currentStoryPathFile.getPath());
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        }

        // prep and serialize top level story
        String json2 = gson.toJson(mStoryPathLibrary);

        // write to file
        try {
            File storyFile = new File("/storage/emulated/0/Liger/default/TEST_STORY.json");  // need file naming plan
            FileOutputStream fos = new FileOutputStream(storyFile);
            if (!storyFile.exists()) {
                storyFile.createNewFile();
            }
            byte data[] = json2.getBytes();
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
        }

        // restore links and continue
        // mStory.setStoryPathLibrary(mStoryPathLibrary);
        mStoryPathLibrary.setCurrentStoryPath(sp);

        mStoryPathLibrary.getCurrentStoryPath().setContext(this);
        mStoryPathLibrary.getCurrentStoryPath().setCardReferences();
        mStoryPathLibrary.getCurrentStoryPath().initializeObservers();
        mStoryPathLibrary.getCurrentStoryPath().setStoryReference(mStoryPathLibrary);
    }

    /*
    public void loadStoryFile(File jsonFile) {
        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.create();

        // String storyJson = JsonHelper.loadJSONFromPath(jsonFile.getPath());
        // mStory = gson.fromJson(storyJson, Story.class);

        String libraryJson = JsonHelper.loadJSONFromPath(jsonFile.getPath());
        mStoryPathLibrary = gson.fromJson(libraryJson, StoryPathLibrary.class);

        // mStory.setStoryPathLibrary(mStoryPathLibrary);

        String pathJson = JsonHelper.loadJSONFromPath(mStoryPathLibrary.getCurrentStoryPathFile());
        initCardList(pathJson);
    }
    */

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
        Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
        String imagePath = null;

        if(imageCursor.moveToFirst()){
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            imagePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            imageCursor.close();
            imageCursor = null;
        }

        return imagePath;
    }
}