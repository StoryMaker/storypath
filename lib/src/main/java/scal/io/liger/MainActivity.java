package scal.io.liger;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import scal.io.liger.adapter.CardAdapter;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.Dependency;
import scal.io.liger.model.InstanceIndexItem;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;
import scal.io.liger.view.ScrollLockRecyclerView;


public class MainActivity extends Activity implements StoryPathLibrary.StoryPathLibraryListener{
    private static final String TAG = "MainActivity";

    public static final String INTENT_KEY_WINDOW_TITLE = "window_title";
    public static final String INTENT_KEY_STORYPATH_LIBRARY_ID = "storypath_library_id";
    public static final String INTENT_KEY_STORYPATH_LIBRARY_PATH = "storypath_library_path";
    public static final String INTENT_KEY_STORYPATH_INSTANCE_PATH = "storypath_instance_path";

    ScrollLockRecyclerView mRecyclerView;
    StoryPathLibrary mStoryPathLibrary;
    public CardAdapter mCardAdapter = null;
    String language = null;

    /** Preferences received via launching intent */
    String mRequestedLanguage;
    int mPhotoSlideDuration;
    private String mAppTitle;

    // new, store info to minimize file access
    public HashMap<String, InstanceIndexItem> instanceIndex;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    // added for testing
    public void scroll(int position) {
        Log.d("TEST", "Scrolling to index item " + position);
        mRecyclerView.setCanScroll(true); // is this required?
        mRecyclerView.scrollToPosition(position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // copy index files - now handled in StoryMaker HomeActivity
        // IndexManager.copyAvailableIndex(MainActivity.this);
        // IndexManager.copyInstalledIndex(MainActivity.this);

        // check expansion files, initiate downloads if necessary
        DownloadHelper.checkAndDownload(MainActivity.this);

        setContentView(R.layout.activity_main);
        mRecyclerView = (ScrollLockRecyclerView) findViewById(R.id.recyclerView);
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

        Intent i = getIntent();
        if (i.hasExtra("lang")) {
            language = i.getExtras().getString("lang");
            Log.d("LANGUAGE", "Found language code " + language + " in intent");
        } else {
            language = "en";
            Log.d("LANGUAGE", "Found no language code in intent, defaulting to en");
        }

        Log.d("MainActivity", "onCreate");
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate called with no savedInstanceState");

            JsonHelper.setupFileStructure(this);

            // NEW: load instance index
            //      only fill on startup to minimize disk access
            instanceIndex = IndexManager.loadInstanceIndex(MainActivity.this);
            instanceIndex = IndexManager.fillInstanceIndex(MainActivity.this, instanceIndex, language);

            // TEMP
            if (instanceIndex.size() > 0) {
                Log.d(TAG, "ONCREATE - FOUND INSTANCE INDEX WITH " + instanceIndex.size() + " ITEMS");
            } else {
                Log.d(TAG, "ONCREATE - FOUND INSTANCE INDEX WITH NO ITEMS");
            }

            final ActionBar actionBar = getActionBar();

            if (i.hasExtra(INTENT_KEY_WINDOW_TITLE)) {
                mAppTitle = i.getStringExtra(INTENT_KEY_WINDOW_TITLE);
                getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).edit().putString(Constants.PREFS_APP_TITLE, mAppTitle).apply();
                actionBar.setTitle(mAppTitle);
            }
            actionBar.setDisplayHomeAsUpEnabled(true);

            // TODO : Should these be serialized with StoryPathLibrary?
            mPhotoSlideDuration = i.getIntExtra(Constants.EXTRA_PHOTO_SLIDE_DURATION, 0);
            mRequestedLanguage = i.getStringExtra(Constants.EXTRA_LANG);

            String jsonFilePath = null;
            String json = null;
            if (i.hasExtra(INTENT_KEY_STORYPATH_LIBRARY_ID)) {
                jsonFilePath = JsonHelper.getJsonPathByKey(this, i.getStringExtra(INTENT_KEY_STORYPATH_LIBRARY_ID));
                json = JsonHelper.loadJSONFromZip(jsonFilePath, this, language);
            } else if (i.hasExtra(INTENT_KEY_STORYPATH_LIBRARY_PATH)) {
                jsonFilePath = i.getStringExtra(INTENT_KEY_STORYPATH_LIBRARY_PATH);
                Log.d(TAG, "ONCREATE(NEW) - LOADING " + jsonFilePath + " FROM ZIP FILE");
                json = JsonHelper.loadJSONFromZip(jsonFilePath, this, language);
            } else if (i.hasExtra(INTENT_KEY_STORYPATH_INSTANCE_PATH)) {
                jsonFilePath = i.getStringExtra(INTENT_KEY_STORYPATH_INSTANCE_PATH);
                json = JsonHelper.loadJSON(new File(jsonFilePath), language);
            }

            if (json != null) {
                initFromJson(json, jsonFilePath);
            } else {
                showJsonSelectorPopup();
            }
        } else {

            // NEW: load instance index
            //      if there is no file, this should be an empty hash map
            instanceIndex = IndexManager.loadInstanceIndex(MainActivity.this);

            // TEMP
            if (instanceIndex.size() > 0) {
                Log.d(TAG, "ONCREATE(STATE) - FOUND INSTANCE INDEX WITH " + instanceIndex.size() + " ITEMS");
            } else {
                Log.d(TAG, "ONCREATE(STATE) - FOUND INSTANCE INDEX WITH NO ITEMS");
            }

            if (savedInstanceState.containsKey("storyPathLibraryJson")) {
                Log.d(TAG, "LOAD STORY PATH LIBRARY FROM SAVED INSTANCE STATE");

                String jsonSPL = savedInstanceState.getString("storyPathLibraryJson");

                if (jsonSPL != null) {
                    // fyi: story path (if any) is restored from saved instance, not saved state
                    initFromJson(jsonSPL, "SAVED_STATE");
                } else {
                    Log.e(TAG, "SAVED INSTANCE STATE DOES NOT CONTAIN A VALID STORY PATH LIBRARY");
                }
            } else {
                Log.e(TAG, "SAVED INSTANCE STATE DOES NOT CONTAIN STORY PATH LIBRARY");
            }
        }
    }

    /**
     * Apply user preferences delivered via Intent extras to StoryPathLibrary
     */
    private void configureStoryPathLibrary() {
        mStoryPathLibrary.lang = mRequestedLanguage;
        mStoryPathLibrary.photoSlideDurationMs = mPhotoSlideDuration;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] jsonFiles = JsonHelper.getJSONFileList(this);

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

        ArrayList<String> referencedFiles = null;

        // should not need to insert dependencies into a saved instance or state
        if (jsonPath.contains("instance")) {
            Log.d(TAG, "INIT FROM SAVED INSTANCE");
            referencedFiles = new ArrayList<String>();
        } else if (jsonPath.equals("SAVED_STATE")) {
            Log.d(TAG, "INIT FROM SAVED STATE");
            referencedFiles = new ArrayList<String>();
        } else {
            Log.d(TAG, "INIT FROM TEMPLATE");
            referencedFiles = new ArrayList<String>();
            // referenced instances should be passed in with the intent somehow
            // referencedFiles = JsonHelper.getInstancePaths();
            Intent i = getIntent();
            if (i.hasExtra("referenced_files")) {
                String referencedFilesString = i.getExtras().getString("referenced_files");
                // support multiple referenced files?
                String[] referencedFilesArray = referencedFilesString.split(":");
                Log.d(TAG, "Found " + referencedFilesArray.length + " referenced files in intent");
                for (String referencedFile : referencedFilesArray) {
                    referencedFiles.add(referencedFile);
                }
            } else {
                Log.d(TAG, "Found no referenced files in intent");
            }
        }

        mStoryPathLibrary = JsonHelper.deserializeStoryPathLibrary(json, jsonPath, referencedFiles, MainActivity.this, language);
        configureStoryPathLibrary();
        mStoryPathLibrary.setStoryPathLibraryListener(MainActivity.this);

        setupCardView();

        if ((mStoryPathLibrary != null) && (mStoryPathLibrary.getCurrentStoryPathFile() != null)) {
            mStoryPathLibrary.loadStoryPathTemplate("CURRENT", false);
        }

        // TEMP METADATA CHECK
        /*
        Log.d("METADATA", "TITLE: " + mStoryPathLibrary.getMetaTitle());
        Log.d("METADATA", "DESCRIPTION: " + mStoryPathLibrary.getMetaDescription());
        Log.d("METADATA", "THUMBNAIL: " + mStoryPathLibrary.getMetaThumbnail());
        Log.d("METADATA", "SECTION: " + mStoryPathLibrary.getMetaSection());
        Log.d("METADATA", "LOCATION: " + mStoryPathLibrary.getMetaLocation());
        if (mStoryPathLibrary.getMetaTags() != null) {
            for (String metaTag : mStoryPathLibrary.getMetaTags()) {
                Log.d("METADATA", "TAG: " + metaTag);
            }
        }
        */
    }

    // MNB - IS THIS METHOD NEEDED?
    public void refreshCardList() {
    Log.d(TAG, "refreshCardList called");
        if (mRecyclerView == null)
            return;

        refreshCardViewXXX();
    }

    public void setupCardView () {
        Log.d(TAG, "setupCardView called");
        if (mRecyclerView == null)
            return;

        if (mCardAdapter == null) {

            //add valid cards to view
            ArrayList<Card> cards = new ArrayList<Card>();

            if (mStoryPathLibrary != null) {
                cards = mStoryPathLibrary.getValidCards();
                StoryPath storyPath = mStoryPathLibrary.getCurrentStoryPath();
                if (storyPath != null) {
                    cards.addAll(storyPath.getValidCards());
                }
            }
            mCardAdapter = new CardAdapter(cards);
            mRecyclerView.setAdapter(mCardAdapter);
        }
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
            cards = mStoryPathLibrary.getValidCards();
            StoryPath storyPath = mStoryPathLibrary.getCurrentStoryPath();
            if (storyPath != null) {
                cards.addAll(storyPath.getValidCards());
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

                    // add reference to previous path and gather references from that path
                    // (not sure it makes sense to add reference to previous path automatically?)
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

            if(requestCode == Constants.REQUEST_VIDEO_CAPTURE) {

                Uri uri = intent.getData();
                String path = Utility.getRealPathFromURI(getApplicationContext(), uri);

                if (Utility.isNullOrEmpty(path)) {
                    Log.e(TAG, "onActivityResult got null path");
                    return;
                }

                Log.d(TAG, "onActivityResult, video path:" + path);
                String pathId = this.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

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

                    // confirm mime type
                    String mimeType = URLConnection.guessContentTypeFromName(path);

                    Log.d(TAG, "onActivityResult, media type is " + mimeType);

                    if (mimeType.startsWith(Constants.VIDEO)) {

                        MediaFile mf = new MediaFile(path, Constants.VIDEO);
                        cc.saveMediaFile(mf);

                        // SEEMS LIKE A REASONABLE TIME TO SAVE
                        mStoryPathLibrary.save(true);

                        mCardAdapter.changeCard(cc);
                        scrollRecyclerViewToCard(cc);
                    } else {

                        Utility.toastOnUiThread(this, "Expecting " + Constants.VIDEO + " file but found " + mimeType, true);

                        Log.e(TAG, "onActivityResult, expecting " + Constants.VIDEO + " file but found " + mimeType);
                        return;
                    }
                } else {
                    if (c != null) {
                        Log.e(TAG, "card type " + c.getClass().getName() + " has no method to save " + Constants.VIDEO + " files");
                    } else {
                        Log.e(TAG, "c is null!");
                    }
                }

            } else if(requestCode == Constants.REQUEST_IMAGE_CAPTURE) {

                String path = this.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).getString(Constants.EXTRA_FILE_LOCATION, null);
                Log.d(TAG, "onActivityResult, path:" + path);
                String pathId = this.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread
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

                    // confirm mime type
                    String mimeType = URLConnection.guessContentTypeFromName(path);

                    Log.d(TAG, "onActivityResult, media type is " + mimeType);

                    if (mimeType.startsWith(Constants.IMAGE)) {

                        MediaFile mf = new MediaFile(path, Constants.PHOTO);
                        cc.saveMediaFile(mf);

                        // SEEMS LIKE A REASONABLE TIME TO SAVE
                        mStoryPathLibrary.save(true);

                        mCardAdapter.changeCard(cc);
                        scrollRecyclerViewToCard(cc);
                    } else {

                        Utility.toastOnUiThread(this, "Expecting " + Constants.PHOTO + " file but found " + mimeType, true);

                        Log.e(TAG, "onActivityResult, expecting " + Constants.IMAGE + " file but found " + mimeType);
                        return;
                    }
                } else {
                    Log.e(TAG, "card type " + c.getClass().getName() + " has no method to save " + Constants.PHOTO + " files");
                }

            } else if(requestCode == Constants.REQUEST_AUDIO_CAPTURE) {

                Uri uri = intent.getData();
                String path = Utility.getRealPathFromURI(getApplicationContext(), uri);
                Log.d(TAG, "onActivityResult, audio path:" + path);
                String pathId = this.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

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

                    // confirm mime type
                    String mimeType = URLConnection.guessContentTypeFromName(path);

                    Log.d(TAG, "onActivityResult, media type is " + mimeType);

                    if (mimeType.startsWith(Constants.AUDIO)) {

                        MediaFile mf = new MediaFile(path, Constants.AUDIO);
                        cc.saveMediaFile(mf);

                        // SEEMS LIKE A REASONABLE TIME TO SAVE
                        mStoryPathLibrary.save(true);

                        mCardAdapter.changeCard(cc);
                        scrollRecyclerViewToCard(cc);
                    } else {

                        Utility.toastOnUiThread(this, "Expecting " + Constants.AUDIO + " file but found " + mimeType, true);

                        Log.e(TAG, "onActivityResult, expecting " + Constants.AUDIO + " file but found " + mimeType);
                        return;
                    }
                } else {
                    Log.e(TAG, "card class " + c.getClass().getName() + " has no method to save " + Constants.AUDIO + " files");
                }

            } else if (requestCode == Constants.REQUEST_FILE_IMPORT) {
                Uri uri = intent.getData();
                // Will only allow stream-based access to files
                if (Build.VERSION.SDK_INT >= 19) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                // FIXME this can get a file:// uri, e.g. from facebook: https://rink.hockeyapp.net/manage/apps/30627/app_versions/62/crash_reasons/24334871

                // String path = Utility.getRealPathFromURI(getApplicationContext(), uri);
                String path = FileUtils.getPath(getApplicationContext(), uri);

                Log.d(TAG, "onActivityResult, imported file path:" + path);
                String pathId = this.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).getString(Constants.PREFS_CALLING_CARD_ID, null); // FIXME should be done off the ui thread

                Card c = mStoryPathLibrary.getCurrentStoryPath().getCardById(pathId);

                if (c instanceof ClipCard) {
                    ClipCard cc = (ClipCard) c;

                    String checkType = cc.getMedium();

                    // adjust target value if needed
                    if (checkType.equals(Constants.PHOTO)) {
                        checkType = Constants.IMAGE;
                    }

                    // confirm mime type
                    String mimeType = "";

                    if (path != null) {
                        mimeType = URLConnection.guessContentTypeFromName(path); // TODO: a null path causes failure, is that ok?
                    }
                    
                    Log.d(TAG, "onActivityResult, media type is " + mimeType);

                    if (mimeType.startsWith(checkType)) {

                        MediaFile mf = new MediaFile(uri.toString(), cc.getMedium());
                        cc.saveMediaFile(mf);

                        // SEEMS LIKE A REASONABLE TIME TO SAVE
                        mStoryPathLibrary.save(true);

                        mCardAdapter.changeCard(cc);
                        scrollRecyclerViewToCard(cc);
                    } else {

                        Utility.toastOnUiThread(this, "Expecting " + cc.getMedium() + " file but found " + mimeType, true);

                        Log.e(TAG, "onActivityResult, expecting " + checkType + " file but found " + mimeType);
                        return;
                    }
                } else {
                    Log.e(TAG, "card type " + c.getClass().getName() + " has no method to save " + Constants.VIDEO + " files");
                }

            }
        }
    }

    /**
     * Deprecated. Remove after testing that we have no issues with devices not storing
     * image files where specified via the EXTRA_OUTPUT extra of the ACTION_IMAGE_CAPTURE intent.
     */
    @Deprecated
    private String getLastImagePath() {
        final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
        Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
        String imagePath = null;

        try {
            if (imageCursor.moveToFirst()) {
                int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
                imagePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
        } finally {
            if (imageCursor != null) {
                imageCursor.close();
            }
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
    public void onStoryPathLoaded() {
        refreshCardList();
    }

    @Override
    public void onScrollLockRequested(boolean scrollLockRequested, Card hostCard) {
        scrollRecyclerViewToCard(hostCard);
        mRecyclerView.setCanScroll(!scrollLockRequested);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return true;
    }
}
