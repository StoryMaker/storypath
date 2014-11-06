package scal.io.liger;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.stream.MalformedJsonException;

import java.io.File;
import java.util.ArrayList;

import scal.io.liger.adapter.CardAdapter;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.Dependency;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;


public class MainActivity extends Activity implements StoryPathLibrary.StoryPathLibraryListener{
    private static final String TAG = "MainActivity";

    public static final String INTENT_KEY_WINDOW_TITLE = "window_title";
    public static final String INTENT_KEY_STORYPATH_LIBRARY_ID = "storypath_library_id";
    public static final int INTENT_CODE = 16328;

    RecyclerView mRecyclerView;
    //CardUI mCardView;
    StoryPathLibrary mStoryPathLibrary;
    //Story mStory;
    CardAdapter mCardAdapter = null;
    String language = null;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

////        if (DEVELOPER_MODE) {
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()   // or .detectAll() for all detectable problems
//                    .penaltyLog()
//                    .build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    .penaltyDeath()
//                    .build());
////        }

        Log.d("MainActivity", "onCreate");
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate called with no savedInstanceState");

            JsonHelper.setupFileStructure(this);
            MediaHelper.setupFileStructure(this);

            Intent i = getIntent();
            if (i.hasExtra("lang")) {
                language = i.getExtras().getString("lang");
                Log.d("LANGUAGE", "Found language code " + language + " in intent");
            } else {
                Log.d("LANGUAGE", "Found no language code in intent");
            }

            final ActionBar actionBar = getActionBar();

            if (i.hasExtra(INTENT_KEY_WINDOW_TITLE)) {
                actionBar.setTitle(i.getStringExtra(INTENT_KEY_WINDOW_TITLE));
            }
            actionBar.setDisplayHomeAsUpEnabled(true);

            if (i.hasExtra(INTENT_KEY_STORYPATH_LIBRARY_ID)) {
                String jsonFilePath = JsonHelper.getJsonPathByKey(i.getStringExtra(INTENT_KEY_STORYPATH_LIBRARY_ID));
                String json = JsonHelper.loadJSONFromZip(jsonFilePath, this, language);
                initFromJson(json, jsonFilePath);
            } else {
                showJsonSelectorPopup();
            }

//            Intent i = getIntent();
//            if (i.hasExtra("storypathlibrary_json")) {
//                String splJsonFilename = i.getExtras().getString("storypathlibrary_json");
//                File jsonFile = new File(JsonHelper.getSdLigerFilePath() + splJsonFilename);
//                String json = JsonHelper.loadJSON(jsonFile);
//                initHook(json, jsonFile);
//            } else {
//                showJsonSelectorPopup();
//            }

        } else {
            Log.d(TAG, "onCreate called with valid savedInstanceState");
            Log.d("MainActivity", "savedInstanceState not null, check for and load storypath json");
            if (savedInstanceState.containsKey("storyPathLibraryJson")) {
                String jsonSPL = savedInstanceState.getString("storyPathLibraryJson");

                // should not need to insert dependencies into a saved instance state
                ArrayList<String> referencedFiles = new ArrayList<String>();

                mStoryPathLibrary = JsonHelper.deserializeStoryPathLibrary(jsonSPL, null, referencedFiles, this);

                mStoryPathLibrary.setStoryPathLibraryListener(this);

                setupCardView();

                //initHook(jsonSPL);

                if (savedInstanceState.containsKey("storyPathJson")) {
                    String jsonSP = savedInstanceState.getString("storyPathJson");
                    initCardList(jsonSP);
                }
            }
        }
    }

    public void activateCard(Card card) {
        mCardAdapter.addCardAtPosition(card, findSpot(card));
    }

    public void inactivateCard(Card card) {
        mCardAdapter.removeCard(card);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState called");

        if (mStoryPathLibrary == null) {
            Log.d(TAG, "data not yet loaded, no state to save");
        } else {
            outState.putString("storyPathLibraryJson", JsonHelper.serializeStoryPathLibrary(mStoryPathLibrary));

            if (mStoryPathLibrary.getCurrentStoryPath() != null) {
                outState.putString("storyPathJson", JsonHelper.serializeStoryPath(mStoryPathLibrary.getCurrentStoryPath()));
            }
        }

        super.onSaveInstanceState(outState);
    }

    private void showJsonSelectorPopup() {
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
            builder.setTitle("Choose Story File(SdCard/Liger/)").setItems(jsonFiles, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int index) {
                    File jsonFile = JsonHelper.setSelectedJSONFile(index);
                    String jsonPath = JsonHelper.setSelectedJSONPath(index);

                    // TEMP - unsure how to best determine new story vs. existing story

                    String json = JsonHelper.loadJSON(MainActivity.this, language);
                    //String json = JsonHelper.loadJSONFromZip(MainActivity.this, language);

                    initFromJson(json, jsonPath);

                }
            });
        }

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initFromJson(String json, String jsonPath) {
        if (json == null || json.equals("")) {
            Toast.makeText(MainActivity.this, "Was not able to load this lesson, content was missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

                    //initHook(json, jsonPath);

                    ArrayList<String> referencedFiles = null;

                    // should not need to insert dependencies into a saved instance
                    if (jsonPath.contains("instance")) {
                        referencedFiles = new ArrayList<String>();
                    } else {
                        referencedFiles = JsonHelper.getInstancePaths();
                    }

                    mStoryPathLibrary = JsonHelper.deserializeStoryPathLibrary(json, jsonPath, referencedFiles, MainActivity.this);

                    mStoryPathLibrary.setStoryPathLibraryListener(MainActivity.this);

                    setupCardView();

        // need to implement selection of story path based on hook

                    /*
                    jsonFile = new File(mStoryPathLibrary.buildZipPath(mStoryPathLibrary.getStoryPathTemplateFiles().get("NAME_1")));
                    json = JsonHelper.loadJSONFromPath(jsonFile.getPath());

                    initCardList(json, jsonFile);
                    */

//                            mStoryPathLibrary.loadStoryPathTemplate("NAME_1");

        if ((mStoryPathLibrary != null) && (mStoryPathLibrary.getCurrentStoryPathFile() != null)) {
            mStoryPathLibrary.loadStoryPathTemplate("CURRENT");
        }
    }

    // FIXME rename this to init initial cards or something
    private void initHookDELETE(String json) {
        //initHook(json, null);
    }

    private void initHookDELETE(String json, String jsonPath) {
        Log.d(TAG, "initHook called");

        // unsure what needs to be set up for the hook interface




        try {
            //initStoryPathLibraryModel(json, jsonPath);



            mStoryPathLibrary.setStoryPathLibraryListener(this);
            setupCardView();

        } catch (com.google.gson.JsonSyntaxException e) {
            Toast.makeText(MainActivity.this, "JSON syntax error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } //catch (MalformedJsonException e) {
       //     Toast.makeText(MainActivity.this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
      //      e.printStackTrace();
      //  }
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

    private void initStoryPathLibraryModelDELETE(String json, String jsonPath) throws MalformedJsonException {
        Log.d(TAG, "initStoryPathLibraryModel called");

        if (jsonPath != null) {
            //mStoryPathLibrary = JsonHelper.deserializeStoryPathLibrary(json, jsonPath, this);
        } else {
            //mStoryPathLibrary = JsonHelper.deserializeStoryPathLibrary(json, null, this);
        }

        mStoryPathLibrary.setStoryPathLibraryListener(this);

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

        //try {
            //initStoryPathModel(json, jsonFile);
            setupCardView();
        //} catch (com.google.gson.JsonSyntaxException e) {
        //    Toast.makeText(MainActivity.this, "JSON syntax error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        //} catch (MalformedJsonException e) {
        //    Toast.makeText(MainActivity.this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        //}
    }

    /*
    private void refreshCardList(String json) {
        refreshCardList(json, null);
    }
    */

    //public void refreshCardList(String json, File jsonFile) {
    public void refreshCardList() {
    Log.d(TAG, "refreshCardList called");
        if (mRecyclerView == null)
            return;

        //mCardView.setSwipeable(false);

        //try {
            //initStoryPathModel(json, jsonFile);
            refreshCardViewXXX();
        //} catch (com.google.gson.JsonSyntaxException e) {
        //    Toast.makeText(MainActivity.this, "JSON syntax error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        //} catch (MalformedJsonException e) {
        //    Toast.makeText(MainActivity.this, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        //}
    }

    /*
    private void initStoryPathModel(String json, File jsonFile) throws MalformedJsonException {

            Log.d(TAG, "initStoryPathModel called");

        if (jsonFile != null) {
            mStoryPathLibrary.setCurrentStoryPath(JsonHelper.deserializeStoryPath(json, jsonFile.getPath(), mStoryPathLibrary, this));
        } else {
            mStoryPathLibrary.setCurrentStoryPath(JsonHelper.deserializeStoryPath(json, null, mStoryPathLibrary, this));
        }


        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
        Gson gson = gBuild.create();

        StoryPath sp = gson.fromJson(json, StoryPath.class);
        sp.setContext(this);
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

        sp.setStoryPathLibrary(mStoryPathLibrary);


        mStoryPathLibrary.setCurrentStoryPath(sp);

    }
    */

    public void setupCardView () {
        Log.d(TAG, "setupCardView called");
        if (mRecyclerView == null)
            return;

        if (mCardAdapter == null) {

            //add valid cards to view
            ArrayList<Card> cards = new ArrayList<Card>();

            if (mStoryPathLibrary != null) {
//                cards.addAll(mStoryPathLibrary.getValidCards());
                cards = mStoryPathLibrary.getValidCards();
                StoryPath storyPath = mStoryPathLibrary.getCurrentStoryPath();
                if (storyPath != null) {
                    cards.addAll(storyPath.getValidCards());
                    //storyPath.setValidCards(cards);
                }
            }
            mCardAdapter = new CardAdapter(cards);
            mRecyclerView.setAdapter(mCardAdapter);
        }

//        if (mCardAdapter == null) {
//
//            //add valid cards to view
//            ArrayList<Card> cards = new ArrayList<Card>();
//
//            if (mStoryPathLibrary != null) {
//                cards.addAll(mStoryPathLibrary.getValidCards());
//
//                if (mStoryPathLibrary.getCurrentStoryPath() != null) {
//                    cards.addAll(mStoryPathLibrary.getCurrentStoryPath().getValidCards());
//                }
//            }
//            mCardAdapter = new CardAdapter(this, cards);
//            mRecyclerView.setAdapter(mCardAdapter);
//        }
    }

    public void refreshCardViewXXX () {
        Log.d(TAG, "refreshCardViewXXX called");
        if (mRecyclerView == null) {
            return;
        }

        if (mCardAdapter == null) {
            setupCardView();
            return;
        }

        //add valid cards to view
        ArrayList<Card> cards = new ArrayList<Card>();

        if (mStoryPathLibrary != null) {
//                cards.addAll(mStoryPathLibrary.getValidCards());
            cards = mStoryPathLibrary.getValidCards();
            StoryPath storyPath = mStoryPathLibrary.getCurrentStoryPath();
            if (storyPath != null) {
                cards.addAll(storyPath.getValidCards());
                //storyPath.setValidCards(cards);
            }
        }
        mCardAdapter = new CardAdapter(cards);
        mRecyclerView.setAdapter(mCardAdapter);
    }

    public void goToCard(StoryPath currentPath, String cardPath) throws MalformedJsonException {
        Log.d(TAG, "goToCard: " + cardPath);
        // assumes the format story::card::field::value
        String[] pathParts = cardPath.split("::");

        StoryPathLibrary storyPathLibrary = null;
        StoryPath storyPath = null;
        boolean newStoryPath = false;

        /*
        // TEMP CODE FOR TESTING
        if (cardPath == "SWITCH") {
            GsonBuilder gBuild = new GsonBuilder();
            gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
            Gson gson = gBu'ild.create();

            String jsonFile = "foo";

            String json = JsonHelper.loadJSONFromPath(mStory.getStoryPathLibrary().buildZipPath(jsonFile));
            story = gson.fromJson(json, StoryPath.class);
            story.context = this;
            story.setCardReferences();
            story.setFileLocation(mStory.getCurrentStoryPath().buildZipPath(jsonFile));

            mStory.switchPaths(story);
            refreshCardView();
            mCardView.scrollToCard(0);

            return;
        }
        */

        if ((mStoryPathLibrary.getId().equals(pathParts[0])) ||
           ((mStoryPathLibrary.getCurrentStoryPath() != null) &&
            (mStoryPathLibrary.getCurrentStoryPath().getId().equals(pathParts[0])))) {
            // reference targets this story path or library
            storyPathLibrary = mStoryPathLibrary;
            storyPath = mStoryPathLibrary.getCurrentStoryPath();
        } else {
            // reference targets a serialized story path

            for (Dependency dependency : currentPath.getDependencies()) {
                if (dependency.getDependencyId().equals(pathParts[0])) {

                    // ASSUMES DEPENDENCIES ARE CORRECT RELATIVE TO PATH OF CURRENT LIBRARY

                    // check for file
                    // paths to actual files should fully qualified
                    // paths within zip files should be relative
                    // (or at least not resolve to actual files)
                    String checkPath = currentPath.buildZipPath(dependency.getDependencyFile());
                    File checkFile = new File(checkPath);

                    // ArrayList<String> referencedFiles = JsonHelper.getInstancePaths();
                    // add reference to previous path and gather references from that path
                    ArrayList<String> referencedFiles = new ArrayList<String>();
                    if (currentPath.getSavedFileName() != null) {
                        Log.d("DEPENDENCIES", "ADDING REFERENCE TO CURRENT PATH " + currentPath.getSavedFileName());
                        referencedFiles.add(currentPath.getSavedFileName());
                    }

                    if (currentPath.getDependencies() != null) {
                        for (Dependency currentDependency : currentPath.getDependencies()) {
                            if (currentDependency.getDependencyId().contains("instance")) {
                                Log.d("DEPENDENCIES", "ADDING REFERENCE TO CURRENT PATH DEPENDENCY " + currentDependency.getDependencyFile());
                                referencedFiles.add(currentDependency.getDependencyFile());
                            }
                        }
                    }

                    if (checkFile.exists()) {
                        if (dependency.getDependencyFile().contains("-library-instance")) {
                            storyPath = JsonHelper.loadStoryPathLibrary(checkPath, referencedFiles, this, language);
                        } else {
                            storyPath = JsonHelper.loadStoryPath(checkPath, mStoryPathLibrary, referencedFiles, this, language);
                        }
                        Log.d("FILES", "LOADED FROM FILE: " + dependency.getDependencyFile());
                    } else {
                        if (dependency.getDependencyFile().contains("-library-instance")) {
                            storyPath = JsonHelper.loadStoryPathLibraryFromZip(checkPath, referencedFiles, this, language);
                        } else {
                            storyPath = JsonHelper.loadStoryPathFromZip(checkPath, mStoryPathLibrary, referencedFiles, this, language);
                        }
                        Log.d("FILES", "LOADED FROM ZIP: " + dependency.getDependencyFile());
                    }

                    // need to account for references pointing to either a path or a library
                    if (storyPath instanceof StoryPath) {
                        Log.d("REFERENCES", "LOADED A PATH, NOW LOADING A LIBRARY");

                        checkPath = storyPath.buildZipPath(storyPath.getStoryPathLibraryFile());
                        checkFile = new File(checkPath);

                        if (checkFile.exists()) {
                            storyPathLibrary = JsonHelper.loadStoryPathLibrary(checkPath, referencedFiles, this, language);
                            Log.d("FILES", "LOADED FROM FILE: " + storyPath.getStoryPathLibraryFile());
                        } else {
                            storyPathLibrary = JsonHelper.loadStoryPathLibraryFromZip(checkPath, referencedFiles, this, language);
                            Log.d("FILES", "LOADED FROM ZIP: " + storyPath.getStoryPathLibraryFile());
                        }
                    } else {
                        storyPathLibrary = (StoryPathLibrary)storyPath;

                        if (storyPathLibrary.getCurrentStoryPathFile() == null) {
                            Log.d("REFERENCES", "LOADED A LIBRARY, NO PATH");
                            storyPath = null;
                        } else {
                            Log.d("REFERENCES", "LOADED A LIBRARY, NOW LOADING A PATH");
                            checkPath = storyPathLibrary.buildZipPath(storyPathLibrary.getCurrentStoryPathFile());
                            checkFile = new File(checkPath);

                            if (checkFile.exists()) {
                                storyPath = JsonHelper.loadStoryPath(checkPath, storyPathLibrary, referencedFiles, this, language);
                                Log.d("FILES", "LOADED FROM FILE: " + storyPathLibrary.getCurrentStoryPathFile());
                            } else {
                                storyPath = JsonHelper.loadStoryPathFromZip(checkPath, storyPathLibrary, referencedFiles, this, language);
                                Log.d("FILES", "LOADED FROM ZIP: " + storyPathLibrary.getCurrentStoryPathFile());
                            }
                        }
                    }

                    // loaded in reverse order, so need to set these references
                    if (storyPath != null) {
                        storyPath.setStoryPathLibrary(storyPathLibrary);
                        storyPath.setStoryPathLibraryFile(storyPathLibrary.getFileLocation());
                        storyPathLibrary.setCurrentStoryPath(storyPath);
                        storyPathLibrary.setCurrentStoryPathFile(storyPath.getFileLocation()); // VERIFY THIS
                    }

                    /*
                    GsonBuilder gBuild = new GsonBuilder();
                    gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
                    Gson gson = gBuild.create();

                    String jsonFile = dependency.getDependencyFile();
                    String json = JsonHelper.loadJSONFromPath(mStoryPathLibrary.getCurrentStoryPath().buildZipPath(jsonFile));
                    story = gson.fromJson(json, StoryPath.class);

                    story.setContext(this);
                    story.setCardReferences();
                    story.setFileLocation(mStoryPathLibrary.getCurrentStoryPath().buildZipPath(jsonFile));
                    */

                    newStoryPath = true;
                    break;
                }
            }
        }

        Card card = null;

        if ((storyPathLibrary != null) && storyPathLibrary.getId().equals(pathParts[0])) {
            card = storyPathLibrary.getCardById(cardPath);
        }
        if ((storyPath != null) && storyPath.getId().equals(pathParts[0])) {
            card = storyPath.getCardById(cardPath);
        }

        if (card == null) {
            Log.e("REFERENCES", "CARD ID " + pathParts[1] + " WAS NOT FOUND");
            return;
        }

        if (newStoryPath) {

            // TODO: need additional code to save current story path

            // serialize current story path
            // add to story path files

            //mStoryPathLibrary.setCurrentStoryPath(storyPath);
            mStoryPathLibrary = storyPathLibrary;
            refreshCardViewXXX();
        }

        int cardIndex = mCardAdapter.mDataset.indexOf(card);

        if (cardIndex < 0) {
            System.err.println("CARD ID " + pathParts[1] + " IS NOT VISIBLE");
            return;
        }

        mRecyclerView.scrollToPosition(cardIndex);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "onActivityResult, requestCode:" + requestCode + ", resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {
            // TODO : Remove this and allow Card View Controllers to be notified of data changes
//            setupCardView();

            if(requestCode == Constants.REQUEST_VIDEO_CAPTURE) {

                Uri uri = intent.getData();
                String path = getRealPathFromURI(getApplicationContext(), uri);
                Log.d(TAG, "onActivityResult, video path:" + path);
                String pathId = this.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                if (null == pathId || null == uri) {
                    return;
                }

                Card c = null;
                if (mStoryPathLibrary.getCurrentStoryPath() != null) {
                    c = mStoryPathLibrary.getCurrentStoryPath().getCardById(pathId);
                }
                if (c == null) {
                    c = mStoryPathLibrary.getCardById(pathId); // FIXME temporarily routing around this to test clipcard
                }

                if (c instanceof ClipCard) {
                    ClipCard cc = (ClipCard)c;
                    MediaFile mf = new MediaFile(path, Constants.VIDEO);
                    cc.saveMediaFile(mf);

                    // SEEMS LIKE A REASONABLE TIME TO SAVE
                    saveStoryPathLibrary(true);

                    mCardAdapter.changeCard(cc);
                    scrollRecyclerViewToCard(cc);
                } else {
                    Log.e(TAG, "card type " + c.getClass().getName() + " has no method to save " + Constants.VIDEO + " files");
                }

            } else if(requestCode == Constants.REQUEST_IMAGE_CAPTURE) {

                String path = getLastImagePath();
                Log.d(TAG, "onActivityResult, path:" + path);
                String pathId = this.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                if (null == pathId || null == path) {
                    return;
                }

                Card c = null;
                if (mStoryPathLibrary.getCurrentStoryPath() != null) {
                    c = mStoryPathLibrary.getCurrentStoryPath().getCardById(pathId);
                }
                if (c == null) {
                    c = mStoryPathLibrary.getCardById(pathId); // FIXME temporarily routing around this to test clipcard
                }

                if (c instanceof ClipCard) {
                    ClipCard cc = (ClipCard)c;
                    MediaFile mf = new MediaFile(path, Constants.PHOTO);
                    cc.saveMediaFile(mf);

                    // SEEMS LIKE A REASONABLE TIME TO SAVE
                    saveStoryPathLibrary(true);

                    mCardAdapter.changeCard(cc);
                    scrollRecyclerViewToCard(cc);
                } else {
                    Log.e(TAG, "card type " + c.getClass().getName() + " has no method to save " + Constants.PHOTO + " files");
                }

            } else if(requestCode == Constants.REQUEST_AUDIO_CAPTURE) {

                Uri uri = intent.getData();
                String path = getRealPathFromURI(getApplicationContext(), uri);
                Log.d(TAG, "onActivityResult, audio path:" + path);
                String pathId = this.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                if (null == pathId || null == uri) {
                    return;
                }

                Card c = null;
                if (mStoryPathLibrary.getCurrentStoryPath() != null) {
                    c = mStoryPathLibrary.getCurrentStoryPath().getCardById(pathId);
                }
                if (c == null) {
                    c = mStoryPathLibrary.getCardById(pathId); // FIXME temporarily routing around this to test clipcard
                }

                if (c instanceof ClipCard) {
                    ClipCard cc = (ClipCard)c;
                    MediaFile mf = new MediaFile(path, Constants.AUDIO);
                    cc.saveMediaFile(mf);

                    // SEEMS LIKE A REASONABLE TIME TO SAVE
                    saveStoryPathLibrary(true);

                    mCardAdapter.changeCard(cc);
                    scrollRecyclerViewToCard(cc);
                } else {
                    Log.e(TAG, "card class " + c.getClass().getName() + " has no method to save " + Constants.AUDIO + " files");
                }

            } else if (requestCode == Constants.REQUEST_FILE_IMPORT) {
                Uri uri = intent.getData();
                // Will only allow stream-based access to files
                if (Build.VERSION.SDK_INT >= 19) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                String path = getRealPathFromURI(getApplicationContext(), uri);
                Log.d(TAG, "onActivityResult, imported file path:" + path);
                String pathId = this.getSharedPreferences("prefs", Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                Card c = mStoryPathLibrary.getCurrentStoryPath().getCardById(pathId);

                // TODO Support photo, audio import
                if (c instanceof ClipCard) {
                    ClipCard cc = (ClipCard)c;
                    MediaFile mf = new MediaFile(uri.toString(), Constants.VIDEO);
                    cc.saveMediaFile(mf);

                    // SEEMS LIKE A REASONABLE TIME TO SAVE
                    saveStoryPathLibrary(true);

                    mCardAdapter.changeCard(cc);
                    scrollRecyclerViewToCard(cc);
                } else {
                    Log.e(TAG, "card type " + c.getClass().getName() + " has no method to save " + Constants.VIDEO + " files");
                }

            }
        }
    }

    public void saveStoryPathLibrary(boolean savePath) {
        //Gson gson = new Gson();

        if (mStoryPathLibrary == null) {
            Log.d("FILES", "NOTHING TO SAVE YET");
            return;
        }

        String savedStoryPathLibraryFile = mStoryPathLibrary.getSavedFileName();
        if (savedStoryPathLibraryFile == null) {
            savedStoryPathLibraryFile = JsonHelper.getStoryPathLibrarySaveFileName(mStoryPathLibrary);
            mStoryPathLibrary.setSavedFileName(savedStoryPathLibraryFile);
            Log.d("FILES", "NEW NAME: " + savedStoryPathLibraryFile);
        } else {
            Log.d("FILES", "EXISTING NAME: " + savedStoryPathLibraryFile);
        }

        if (savePath && (mStoryPathLibrary.getCurrentStoryPath() != null)) {
            mStoryPathLibrary.getCurrentStoryPath().setStoryPathLibraryFile(savedStoryPathLibraryFile);
            String savedStoryPathFile = mStoryPathLibrary.getCurrentStoryPath().getSavedFileName();
            if (savedStoryPathFile == null) {
                savedStoryPathFile = JsonHelper.getStoryPathSaveFileName(mStoryPathLibrary.getCurrentStoryPath());
                mStoryPathLibrary.getCurrentStoryPath().setSavedFileName(savedStoryPathFile);
                Log.d("FILES", "NEW NAME: " + savedStoryPathFile);
            } else {
                Log.d("FILES", "EXISTING NAME: " + savedStoryPathFile);
            }
            mStoryPathLibrary.setCurrentStoryPathFile(savedStoryPathFile);
            JsonHelper.saveStoryPath(mStoryPathLibrary.getCurrentStoryPath(), savedStoryPathFile);
            Log.d("FILES", mStoryPathLibrary.getCurrentStoryPath().getId() + " WAS SAVED TO FILE " + savedStoryPathFile);
        } else {
            Log.d("FILES", mStoryPathLibrary.getId() + " HAS NO STORY PATH TO SAVE YET");
        }

        JsonHelper.saveStoryPathLibrary(mStoryPathLibrary, savedStoryPathLibraryFile);
        Log.d("FILES", mStoryPathLibrary.getId() + " WAS SAVED TO FILE " + savedStoryPathLibraryFile);

        //String savedFilePath = JsonHelper.saveStoryPath(mStoryPathLibrary.getCurrentStoryPath());
        //mStoryPathLibrary.setCurrentStoryPathFile(savedFilePath);
        //JsonHelper.saveStoryPathLibrary(mStoryPathLibrary);

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

        /*
        // prep and serialize current story path
        mStoryPathLibrary.getCurrentStoryPath().setStoryPathLibrary(null);
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
        mStoryPathLibrary.getCurrentStoryPath().setStoryPathLibrary(mStoryPathLibrary);
        */
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

    public int findSpot(Card card) {
        int newIndex = 0;

        if (mStoryPathLibrary.getCards().contains(card)) {
            int baseIndex = mStoryPathLibrary.getCards().indexOf(card);
            for (int i = (baseIndex - 1); i >= 0; i--) {
                Card previousCard = mStoryPathLibrary.getCards().get(i);
                if (mCardAdapter.mDataset.contains(previousCard)) {
                    newIndex = mCardAdapter.mDataset.indexOf(previousCard) + 1;

                    break;
                }
            }
        }

        if ((mStoryPathLibrary.getCurrentStoryPath() != null) && (mStoryPathLibrary.getCurrentStoryPath().getCards().contains(card))) {
            int baseIndex = mStoryPathLibrary.getCurrentStoryPath().getCards().indexOf(card);
            for (int i = (baseIndex - 1); i >= 0; i--) {
                Card previousCard = mStoryPathLibrary.getCurrentStoryPath().getCards().get(i);
                if (mCardAdapter.mDataset.contains(previousCard)) {
                    newIndex = mCardAdapter.mDataset.indexOf(previousCard) + 1;

                    break;
                }
            }
        }

        return newIndex;
    }

    public String checkCard(Card updatedCard) {

        if (updatedCard.getStateVisiblity()) {
            // new or updated

            if (mCardAdapter.mDataset.contains(updatedCard)) {
                return "UPDATE";
            } else {
                return "ADD";
            }
        } else {
            // deleted

            if (mCardAdapter.mDataset.contains(updatedCard)) {
                return "DELETE";
            }
        }

        return "ERROR";
    }

    /**
     * Scroll {@link #mRecyclerView} so that card is the
     * first visible item
     */
    public void scrollRecyclerViewToCard(Card card) {
        int position = mCardAdapter.getPositionForCard(card);
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    public void onCardAdded(Card newCard) {
        Log.i(TAG, "Card added " + newCard.getId());
        mCardAdapter.appendCard(newCard);
    }

    @Override
    public void onCardChanged(Card changedCard) {
        Log.i(TAG, "Card changed " + changedCard.getId());
        mCardAdapter.changeCard(changedCard);
    }

    @Override
    public void onCardsSwapped(Card cardOne, Card cardTwo) {
        Log.i(TAG, String.format("Cards swapped %s <-> %s ", cardOne.getId(), cardTwo.getId()));
        mCardAdapter.swapCards(cardOne, cardTwo);
    }

    @Override
    public void onCardRemoved(Card removedCard) {
        Log.i(TAG, "Card removed " + removedCard.getId());
        mCardAdapter.removeCard(removedCard);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishActivity(INTENT_CODE);
                return true;
        }
        return true;
    }
}
