package scal.io.liger.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import scal.io.liger.IndexManager;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;

/**
 * Created by mnbogner on 9/29/14.
 */
public class StoryPathLibrary extends StoryPath {

    public final String TAG = this.getClass().getSimpleName();

    @Expose private HashMap<String, String> storyPathTemplateFiles;
    @Expose private ArrayList<String> storyPathInstanceFiles;
    @Expose private String currentStoryPathFile;
    private StoryPath currentStoryPath; // not serialized
    /** Map of MediaFile UUIDs to MediaFile */
    @Expose private HashMap<String, MediaFile> mediaFiles;

    // additional metadata for publishing
    @Expose private String metaTitle;
    @Expose private String metaDescription;
    @Expose private String metaThumbnail;
    @Expose private String metaSection;
    @Expose private String metaLocation;
    @Expose private ArrayList<String> metaTags;

    /**
     * User preferences set by MainActivity. Delivered to MainActivity via Intent extras
     * from liger library client.
     *
     * TODO : Should these be serialized in StoryPathLibrary?
     */
    public int photoSlideDurationMs;
    public String lang;

    StoryPathLibraryListener mListener;

    public static interface StoryPathLibraryListener {
        public void onCardAdded(Card newCard);
        public void onCardChanged(Card changedCard);
        public void onCardsSwapped(Card cardOne, Card cardTwo);
        public void onCardRemoved(Card removedCard);
        public void onStoryPathLoaded();
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
        save(true);
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

        // update instance index with thumbnail in case thumbnail has changed
        if ((context instanceof MainActivity) && (((MainActivity)context).instanceIndex.containsKey(getSavedFileName())))  {

            InstanceIndexItem item = ((MainActivity)context).instanceIndex.get(getSavedFileName());

            // item.setStoryThumbnailPath(file.getThumbnailFilePath()); <- use existing method instead

            // check current thumbnail to minimize file access
            if ((item.getStoryThumbnailPath() != null) && (item.getStoryThumbnailPath().equals(this.getCoverImageThumbnailPath()))) {
                Log.d(TAG, "can't update index item with thumbnail path (index item found for " + getSavedFileName() + " already has the same path)");
            } else {
                // thumbnail path method only checks story path, will return null if media is somehow
                // captured by a library card, index items with null thumbnail paths shouldn't be an issue
                item.setStoryThumbnailPath(this.getCoverImageThumbnailPath());
                item.setStoryType(this.getMedium());

                IndexManager.updateInstanceIndex(context, item, ((MainActivity) context).instanceIndex);
                Log.d(TAG, "updated index item with thumbnail path " + file.getThumbnailFilePath() + " (index item found for " + getSavedFileName() + ")");
            }
        } else if (!(context instanceof MainActivity)) {
            Log.d(TAG, "can't update index item with thumbnail path outside the context of liger main activity");
        } else {
            // index item must be initialized by a save action
            Log.e(TAG, "can't update index item with thumbnail path (no index item found for " + getSavedFileName() + ")");
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
            Log.e(TAG, "key was not found, cannot delete file");
            return;
        }

        mediaFiles.remove(uuid);

        // delete actual file?
    }

    // additional metadata for publishing
    public String getMetaTitle() {
        return metaTitle;
    }

    public void setMetaTitle(String metaTitle) {
        this.metaTitle = metaTitle;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getMetaThumbnail() {
        return metaThumbnail;
    }

    public void setMetaThumbnail(String metaThumbnail) {
        this.metaThumbnail = metaThumbnail;
    }

    public String getMetaSection() {
        return metaSection;
    }

    public void setMetaSection(String metaSection) {
        this.metaSection = metaSection;
    }

    public String getMetaLocation() {
        return metaLocation;
    }

    public void setMetaLocation(String metaLocation) {
        this.metaLocation = metaLocation;
    }

    public ArrayList<String> getMetaTags() {
        return metaTags;
    }

    public void setMetaTags(ArrayList<String> metaTags) {
        this.metaTags = metaTags;
    }

    public void addMetaTag(String metaTag) {
        if (this.metaTags == null) {
            this.metaTags = new ArrayList<String>();
        }

        this.metaTags.add(metaTag);
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
            Log.e(TAG, "could not file file: " + fnfe.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "other exception: " + e.getMessage());
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

    public void loadStoryPathTemplate(String storyPathTemplateKey, boolean save) {
        String storyPathTemplateFile = null;

        if (storyPathTemplateKey.equals("CURRENT")) { // ADD TO CONSTANTS
            storyPathTemplateFile = getCurrentStoryPathFile();
            Log.d(TAG, "Loading current StoryPath: " + storyPathTemplateFile); // FIXME at least toast the user
        } else {
            storyPathTemplateFile = storyPathTemplateFiles.get(storyPathTemplateKey);
            Log.d(TAG, "Loading template StoryPath: " + storyPathTemplateFile); // FIXME at least toast the user
        }

        if (storyPathTemplateFile == null) {
            Log.e(TAG, "Loading failed. Could not find file: " + storyPathTemplateKey); // FIXME at least toast the user
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
                Log.d(TAG, "Load from saved instance");
                referencedFiles = new ArrayList<String>();
            } else {
                Log.d(TAG, "Load from template");
                referencedFiles = new ArrayList<String>();
                // pass through files referenced by library (which were pulled from intent)
                // does there need to be a way to select references when loading a path?
                // referencedFiles = JsonHelper.getInstancePaths();
                if ((dependencies != null) && (dependencies.size() > 0)) {
                    // support multiple referenced files?
                    Log.d(TAG, "Found " + dependencies.size() + " referenced files in library");
                    for (Dependency dependency : dependencies) {
                        if(dependency.getDependencyFile().contains("-instance")) {
                            referencedFiles.add(dependency.getDependencyFile());
                        }
                    }
                } else {
                    Log.d(TAG, "Found no referenced files in library");
                }
            }

            StoryPath story = null;
            if (checkFile.exists()) {
                story = JsonHelper.loadStoryPath(checkPath, this, referencedFiles, context, lang);
                Log.d(TAG, "Loaded StoryPath from file: " + checkPath);
            } else {
                story = JsonHelper.loadStoryPathFromZip(checkPath, this, referencedFiles, context, lang);
                Log.d(TAG, "Loaded StoryPath from zip: " + checkPath);
            }

            setCurrentStoryPath(story);
            setCurrentStoryPathFile(storyPathTemplateFile);

            // need to prevent saves when opening instances to extract metadata
            if (save) {
                save(false);
            }

            // update instance index with title
            if ((context instanceof MainActivity) && (((MainActivity)context).instanceIndex.containsKey(getSavedFileName())))  {
                InstanceIndexItem item = ((MainActivity)context).instanceIndex.get(getSavedFileName());

                // check current title to minimize file access
                if ((item.getStoryTitle() != null) && (item.getStoryTitle().equals(story.getTitle()))) {
                    Log.d(TAG, "can't update index item with title (index item found for " + getSavedFileName() + " already has the same title)");
                } else {
                    item.setStoryTitle(story.getTitle());

                    IndexManager.updateInstanceIndex(context, item, ((MainActivity) context).instanceIndex);
                    Log.d(TAG, "updated index item with title " + story.getTitle() + " (index item found for " + getSavedFileName() + ")");
                }
            } else if (!(context instanceof MainActivity)) {
                Log.d(TAG, "can't update index item with title outside the context of liger main activity");
            } else {
                // index item must be initialized by a save action
                Log.e(TAG, "can't update index item with title (no index item found for " + getSavedFileName() + ")");
            }

            if (mListener != null) mListener.onStoryPathLoaded();

        } else {
            Log.e(TAG, "app context reference not found, cannot initialize card list for " + storyPathTemplateFile); // FIXME at least toast the user
        }
    }

    @Override
    public void setCoverImageThumbnail(ImageView target) {
        if (getCurrentStoryPath() != null) {
            getCurrentStoryPath().setCoverImageThumbnail(target);
        }
    }

    @Override
    public String getCoverImageThumbnailPath() {
        if (getCurrentStoryPath() != null) {
            return getCurrentStoryPath().getCoverImageThumbnailPath();
        } else {
            return null;
        }
    }

    @Override
    public String getMedium() {
        if (getCurrentStoryPath() != null) {
            return getCurrentStoryPath().getMedium();
        } else {
            return null;
        }
    }

        /**
         * Serialize this object to disk.
         *
         * @param saveCurrentStoryPath whether to also save the StoryPath returned by
         *                             {@link #getCurrentStoryPath()}
         */
    public void save(boolean saveCurrentStoryPath) {
        //Gson gson = new Gson();

        String savedStoryPathLibraryFile = getSavedFileName();
        if (savedStoryPathLibraryFile == null) {
            savedStoryPathLibraryFile = JsonHelper.getStoryPathLibrarySaveFileName(this);
            setSavedFileName(savedStoryPathLibraryFile);
            Log.d(TAG, "Saving to new file: " + savedStoryPathLibraryFile);

            // create new item for instance index
            Date now = new Date();
            InstanceIndexItem newItem = new InstanceIndexItem(savedStoryPathLibraryFile, now.getTime());

            // need source for title/description/type

            IndexManager.updateInstanceIndex(context, newItem, ((MainActivity)context).instanceIndex);

            Log.d(TAG, "Added index item for new instance file : " + savedStoryPathLibraryFile);

        } else {
            Log.d(TAG, "Saving to existing file: " + savedStoryPathLibraryFile);
        }

        if (saveCurrentStoryPath && (getCurrentStoryPath() != null)) {
            getCurrentStoryPath().setStoryPathLibraryFile(savedStoryPathLibraryFile);
            String savedStoryPathFile = getCurrentStoryPath().getSavedFileName();
            if (savedStoryPathFile == null) {
                savedStoryPathFile = JsonHelper.getStoryPathSaveFileName(getCurrentStoryPath());
                getCurrentStoryPath().setSavedFileName(savedStoryPathFile);
                Log.d(TAG, "Saving current StoryPath to new file: " + savedStoryPathFile);
            } else {
                Log.d(TAG, "Saving current StoryPath to existing file: " + savedStoryPathFile);
            }
            setCurrentStoryPathFile(savedStoryPathFile);
            JsonHelper.saveStoryPath(getCurrentStoryPath(), savedStoryPathFile);
            Log.d(TAG, "Current StoryPath with id " + getCurrentStoryPath().getId() + " was saved to file " + savedStoryPathFile);
        } else {
            Log.d(TAG, "Id " + getId() + " has no current StoryPath, but save was explicitly requested. Ignoring.");
        }

        JsonHelper.saveStoryPathLibrary(this, savedStoryPathLibraryFile);
        Log.d(TAG, "Id " + getId() + " was saved to file " + savedStoryPathLibraryFile);

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
}
