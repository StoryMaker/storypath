package scal.io.liger.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;

/**
 * Created by mnbogner on 9/29/14.
 */
public class StoryPathLibrary extends StoryPath {

    @Expose private HashMap<String, String> storyPathTemplateFiles;
    @Expose private ArrayList<String> storyPathInstanceFiles;
    @Expose private String currentStoryPathFile;
    private StoryPath currentStoryPath; // not serialized
    @Expose private HashMap<String, MediaFile> mediaFiles;

    /**
     * User preferences set by MainActivity. Delivered to MainActivity via Intent extras
     * from liger library client.
     *
     * TODO : Should these be serialized in StoryPathLibrary?
     */
    public int photoSlideDurationMs;
    public String language;

    StoryPathLibraryListener mListener;

    public static interface StoryPathLibraryListener {
        public void onCardAdded(Card newCard);
        public void onCardChanged(Card changedCard);
        public void onCardsSwapped(Card cardOne, Card cardTwo);
        public void onCardRemoved(Card removedCard);
    }

    @Override
    public void notifyCardChanged(@NonNull Card firstCard) {
        Log.i(TAG, "(LIBRARY) notifyCardChanged of update to card " + firstCard.getId());
        if (mListener == null) {
            return;
        }

        String action = ((MainActivity)context).checkCard(firstCard);

        if (action.equals("ADD")) {
            mListener.onCardAdded(firstCard);
        }
        if (action.equals("UPDATE")) {
            mListener.onCardChanged(firstCard);
        }
        if (action.equals("DELETE")) {
            mListener.onCardRemoved(firstCard);
        }

        // SEEMS LIKE A REASONABLE TIME TO SAVE
        ((MainActivity)context).saveStoryPathLibrary(true);
    }

    public void setStoryPathLibraryListener(StoryPathLibraryListener listener) {
        mListener = listener;
    }

    public HashMap<String, String> getStoryPathTemplateFiles() {
        return storyPathTemplateFiles;
    }

    public void setStoryPathTemplateFiles(HashMap<String, String> storyPathTemplateFiles) {
        this.storyPathTemplateFiles = storyPathTemplateFiles;
    }

    public ArrayList<String> getStoryPathInstanceFiles() {
        return storyPathInstanceFiles;
    }

    public void setStoryPathInstanceFiles(ArrayList<String> storyPathInstanceFiles) {
        this.storyPathInstanceFiles = storyPathInstanceFiles;
    }

    public void addStoryPathInstanceFile(String file) {
        if (this.storyPathInstanceFiles == null) {
            this.storyPathInstanceFiles = new ArrayList<String>();
        }

        this.storyPathInstanceFiles.add(file);
    }

    public String getCurrentStoryPathFile() {
        return currentStoryPathFile;
    }

    public void setCurrentStoryPathFile(String currentStoryPathFile) {
        this.currentStoryPathFile = currentStoryPathFile;
    }

    public StoryPath getCurrentStoryPath() {
        return currentStoryPath;
    }

    public void setCurrentStoryPath(StoryPath currentStoryPath) {
        this.currentStoryPath = currentStoryPath;
    }

    public HashMap<String, MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(HashMap<String, MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    @Override
    public void saveMediaFile(String uuid, MediaFile file) {
        if (this.mediaFiles == null) {
            this.mediaFiles = new HashMap<String, MediaFile>();
        }

        this.mediaFiles.put(uuid, file);
    }

    @Override
    public MediaFile loadMediaFile(String uuid) {
        return mediaFiles.get(uuid);
    }

    // need to determine whether users are allowed to delete files that are referenced by cards
    // need to determine whether to automatically delete files when they are no longer referenced
    public void deleteMediaFile(String uuid) {
        if ((mediaFiles == null) || (!mediaFiles.keySet().contains(uuid))) {
            Log.e(this.getClass().getName(), "key was not found, cannot delete file");
            return;
        }

        mediaFiles.remove(uuid);

        // delete actual file?
    }

    // need to determine where to present path options and deserialize new path
    public void switchPaths(StoryPath newPath) {
        // export clip metadata
        // also may need to export stored values
        StoryPath oldPath = this.getCurrentStoryPath();
        ArrayList<ClipMetadata> metadata = oldPath.exportMetadata();

        // serialize current story path
        Gson gson = new Gson();
        oldPath.setStoryPathLibrary(null);
        oldPath.clearObservers();
        oldPath.clearCardReferences(); // FIXME move this stuff into the model itself so we dont have to worry about it
        Context oldContext = oldPath.getContext();
        oldPath.setContext(null);

        String json = gson.toJson(oldPath);

        try {
            File oldPathFile = new File(oldPath.buildZipPath(oldPath.getId() + ".path"));
            PrintStream ps = new PrintStream(new FileOutputStream(oldPathFile.getPath()));
            ps.print(json);
            // store file path
            // NOT YET SURE HOW TO HANDLE VERSIONS OR DUPLICATES
            this.addStoryPathInstanceFile(oldPathFile.getPath());
        } catch (FileNotFoundException fnfe) {
            Log.e(this.getClass().getName(), "could not file file: " + fnfe.getMessage());
        } catch (Exception e) {
            Log.e(this.getClass().getName(), "other exception: " + e.getMessage());
        }

        // import clip metadata
        newPath.importMetadata(metadata);

        // should this be done externally?
        newPath.setContext(oldContext);
        newPath.setCardReferences();
        newPath.initializeObservers();
        newPath.setStoryPathLibrary(this);

        // update current story path
        this.setCurrentStoryPath(newPath);

        // NOTIFY/REFRESH HERE OR LET THAT BE HANDLED BY WHATEVER CALLS THIS?
    }

    public void loadStoryPathTemplate(String storyPathTemplateKey) {
        String storyPathTemplateFile = null;

        if (storyPathTemplateKey.equals("CURRENT")) { // ADD TO CONSTANTS
            storyPathTemplateFile = getCurrentStoryPathFile();
            Log.e("FILES", "CURRENT STORY PATH: " + storyPathTemplateFile); // FIXME at least toast the user
        } else {
            storyPathTemplateFile = storyPathTemplateFiles.get(storyPathTemplateKey);
            Log.e("FILES", "STORY PATH TEMPLATE: " + storyPathTemplateFile); // FIXME at least toast the user
        }

        if (storyPathTemplateFile == null) {
            Log.e(this.getClass().getName(), "could not find file name corresponding to " + storyPathTemplateKey); // FIXME at least toast the user
            return;
        }

        if (context != null) {
            //File jsonTemplateFile = new File(buildZipPath(storyPathTemplateFile));
            //String jsonTemplate = JsonHelper.loadJSONFromPath(jsonTemplateFile.getPath());


            MainActivity mainActivity = null;
            String lang = "en"; // FIXME defaulting to en
            if (context instanceof MainActivity) {
                mainActivity = (MainActivity) context; // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())
                lang = mainActivity.getLanguage();
            }

            // check for file
            // paths to actual files should fully qualified
            // paths within zip files should be relative
            // (or at least not resolve to actual files)
            String checkPath = buildZipPath(storyPathTemplateFile);
            File checkFile = new File(checkPath);

            ArrayList<String> referencedFiles = null;

            // should not need to insert dependencies into a saved instance file
            if (checkPath.contains("instance")) {
                referencedFiles = new ArrayList<String>();
            } else {
                referencedFiles = JsonHelper.getInstancePaths();
            }

            StoryPath story = null;
            if (checkFile.exists()) {
                story = JsonHelper.loadStoryPath(checkPath, this, referencedFiles, context, lang);
                Log.e("FILES", "LOADED FROM FILE: " + checkPath);
            } else {
                story = JsonHelper.loadStoryPathFromZip(checkPath, this, referencedFiles, context, lang);
                Log.e("FILES", "LOADED FROM ZIP: " + checkPath);
            }

            setCurrentStoryPath(story);
            setCurrentStoryPathFile(storyPathTemplateFile);

            if (mainActivity != null) {
                mainActivity.saveStoryPathLibrary(false);  // FIXME refactor this stuff out, the loader stuff shouldn't touch UI at all
                mainActivity.refreshCardList();
            }
        } else {
            Log.e(this.getClass().getName(), "app context reference not found, cannot initialize card list for " + storyPathTemplateFile); // FIXME at least toast the user
        }
    }

    @Override
    public Bitmap getCoverImageThumbnail() {
        if (getCurrentStoryPath() != null) {
            return getCurrentStoryPath().getCoverImageThumbnail();
        } else {
            return null;
        }
    }
}
