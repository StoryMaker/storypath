package scal.io.liger.model;

import timber.log.Timber;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import java.util.List;
import java.util.UUID;

import scal.io.liger.Constants;
//import scal.io.liger.IndexManager;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.StorymakerIndexManager;

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
    @Nullable @Expose private HashMap<String, MediaFile> mediaFiles;
    @Expose private ArrayList<AudioClip> audioClips;

    // additional metadata for publishing
    @Expose private String metaTitle;
    @Expose private String metaDescription;
    @Expose private String metaThumbnail;
    @Expose private String metaSection;
    @Expose private String metaLocation;
    @Expose private ArrayList<String> metaTags;

    @Expose PublishProfile publishProfile;

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

        /** Called when scrolling or seeking through the StoryPath content should be prohibited.
         *  This may occur when media recording or playback is occurring */
        public void onScrollLockRequested(boolean scrollLockRequested, Card hostCard);
    }

    public void notifyScrollLockRequested(boolean scrollLockRequested, Card hostCard) {
        if (mListener != null)
            mListener.onScrollLockRequested(scrollLockRequested, hostCard);
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
    }

    public PublishProfile getPublishProfile() {
        return publishProfile;
    }

    public void setPublishProfile(PublishProfile publishProfile) {
        this.publishProfile = publishProfile;
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

    public MediaFile getMediaFile(String uuid) {
        return mediaFiles.get(uuid);
    }

    public void setMediaFiles(HashMap<String, MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public ArrayList<AudioClip> getAudioClips() {
        return audioClips;
    }

    public void setAudioClips(ArrayList<AudioClip> audioClips) {
        this.audioClips = audioClips;
    }

    /**
     * Remove this AudioClip from the passed ClipCard, if this
     * AudioClip's span includes it.
     *
     * @param audioClip the audio whose span currently includes clipCard
     * @param clipCard the target ClipCard to remove audioClip from
     */
    public void removeAudioClipFromClipCard(@NonNull List<ClipCard> clipCards,
                                            @NonNull AudioClip audioClip,
                                            @NonNull ClipCard clipCard) {

        // Find span of ClipCards
        ClipCard firstCard = getFirstClipCardForAudioClip(audioClip, clipCards);

        if (firstCard == null) {
            Timber.e("Unable to remove AudioClip from ClipCard. Could not find audioClip's first ClipCard");
            return;
        }

        int firstIdx = clipCards.indexOf(firstCard);
        if (audioClip.getClipSpan() == 1) {
            deleteAudioClip(audioClip.getUuid());
            return; // So we don't risk invoking save() twice
        }

        for (int idx = firstIdx; idx < audioClip.getClipSpan(); idx++) {
            if (clipCards.get(idx).getId().equals(clipCard.getId())) {
                Timber.d("Found ClipCard to remove from AudioClip with uuid " + audioClip.getUuid());
                // We found the ClipCard to remove from this AudioClip
                if (idx == firstIdx) {
                    // The ClipCard leads the AudioClip. Advance head to
                    // next card.
                    audioClip.setPositionIndex(idx + 1);
                    audioClip.setClipSpan(audioClip.getClipSpan()-1);
                } else {
                    audioClip.setClipSpan(audioClip.getClipSpan() - (idx - firstIdx));
                }
                break;
            }
        }
    }

    /** AudioClip convenience functions */

    /**
     * Convenience function to find the first ClipCard within a collection
     * during which an AudioTrack is present.
     *
     * clipCards serves merely as an optional optimization
     * around calling {@link #getClipCardsWithAttachedMedia()} on every call
     */
    @Nullable
    public ClipCard getFirstClipCardForAudioClip(@NonNull AudioClip audioClip,
                                                 @Nullable List<ClipCard> clipCards) {

        if (clipCards == null) clipCards = getClipCardsWithAttachedMedia();

        ClipCard firstCard = null;
        if (!TextUtils.isEmpty(audioClip.getPositionClipId())) {
            firstCard = (ClipCard) getCardByIdOnly(audioClip.getPositionClipId());
        } else {
            firstCard = clipCards.get(audioClip.getPositionIndex());
        }

        if (firstCard == null) {
            Timber.e("Could not find audioClip's first ClipCard");
        }
        return firstCard;
    }

    /**
     * Convenience function to find the last ClipCard within a collection
     * during which an AudioTrack is present.
     *
     * clipCards serves merely as an optional optimization
     * around calling {@link #getClipCardsWithAttachedMedia()} on every call
     */
    public ClipCard getLastClipCardForAudioClip(@NonNull AudioClip audioClip,
                                                @Nullable List<ClipCard> clipCards) {

        if (clipCards == null) clipCards = getClipCardsWithAttachedMedia();

        int lastIdx = clipCards.indexOf(getFirstClipCardForAudioClip(audioClip, clipCards)) + audioClip.getClipSpan() - 1;
        return clipCards.get(lastIdx);
    }

    /**
     * Convenience function to determine whether clipCard is within the set of ClipCards
     * within clipCards during which AudioClip
     */
    public boolean isClipCardWithinAudioClipRange(@NonNull ClipCard clipCard,
                                                  @NonNull AudioClip audioClip,
                                                  @Nullable List<ClipCard> clipCards) {

        if (clipCards == null) clipCards = getClipCardsWithAttachedMedia();

        int startIdx = clipCards.indexOf(getFirstClipCardForAudioClip(audioClip, clipCards));
        int endIdx = clipCards.indexOf(getLastClipCardForAudioClip(audioClip, clipCards));

        int targetClipIndex = clipCards.indexOf(clipCard);

        return startIdx <= targetClipIndex && targetClipIndex <= endIdx;
    }

    /** End AudioClip convenience functions */

    public void saveNarrationAudioClip(AudioClip audioClip, MediaFile mediaFile) {
        if (audioClips == null) {
            audioClips = new ArrayList<>();
        }

        Timber.d(String.format("Added %s to audioClips. Total audio clips %d", audioClip.getUuid().substring(0,3), audioClips.size()));
        audioClips.add(audioClip);
        saveMediaFile(audioClip.getUuid(), mediaFile);
    }

    @Override
    public void saveMediaFile(@NonNull String uuid, @NonNull MediaFile file) {
        if (mediaFiles == null) {
            mediaFiles = new HashMap<>();
        }

        // update instance index with thumbnail in case thumbnail has changed
        if ((context instanceof MainActivity) && (((MainActivity)context).instanceIndex.containsKey(getSavedFileName())))  {

            scal.io.liger.model.sqlbrite.InstanceIndexItem item = ((MainActivity)context).instanceIndex.get(getSavedFileName());

            // item.setStoryThumbnailPath(file.getThumbnailFilePath()); <- use existing method instead

            // check current thumbnail to minimize file access
            if ((item.getThumbnailPath() != null) && (item.getThumbnailPath().equals(this.getCoverImageThumbnailPath()))) {
                Timber.d("can't update index item with thumbnail path (index item found for " + getSavedFileName() + " already has the same path)");
            } else {
                // thumbnail path method only checks story path, will return null if media is somehow
                // captured by a library card, index items with null thumbnail paths shouldn't be an issue
                item.setThumbnailPath(this.getCoverImageThumbnailPath());
                item.setStoryType(this.getMedium());

                StorymakerIndexManager.instanceIndexAdd(context, item, ((MainActivity) context).instanceIndex, ((MainActivity) context).getInstanceIndexItemDao());
                Timber.d("updated index item with thumbnail path " + file.getThumbnailFilePath() + " (index item found for " + getSavedFileName() + ")");
            }
        } else if (!(context instanceof MainActivity)) {
            Timber.d("can't update index item with thumbnail path outside the context of liger main activity");
        } else {
            // index item must be initialized by a save action
            Timber.e("can't update index item with thumbnail path (no index item found for " + getSavedFileName() + ")");
        }

        Timber.d(String.format("Added %s to mediaFiles", uuid.substring(0,3)));
        this.mediaFiles.put(uuid, file);
    }

    @Override
    @Nullable
    public MediaFile loadMediaFile(@NonNull String uuid) {
        return mediaFiles.get(uuid);
    }

    // need to determine whether users are allowed to delete files that are referenced by cards
    // need to determine whether to automatically delete files when they are no longer referenced
    public void deleteMediaFile(String uuid) {
        if ((mediaFiles == null) || (!mediaFiles.keySet().contains(uuid))) {
            Timber.e("key was not found, cannot delete file");
            return;
        }

        mediaFiles.remove(uuid);

        // delete actual file?
        save(false);
    }

    /**
     * Delete an AudioClip with the corresponding uuid. Will also
     * delete the associated MediaFile
     */
    public void deleteAudioClip(String uuid) {
        if (audioClips == null) {
            Timber.e("No AudioClips to delete");
            return;
        }

        AudioClip toDelete = null;
        for (AudioClip clip : audioClips) {
            if (clip.getUuid().equals(uuid)) {
                toDelete = clip;
            }
        }

        if (toDelete != null) {
            audioClips.remove(toDelete);
        } else
            Timber.d("Could not Delete AudioClip with uuid " + uuid);

        deleteMediaFile(uuid);
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
            Timber.e("could not file file: " + fnfe.getMessage());
        } catch (Exception e) {
            Timber.e("other exception: " + e.getMessage());
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
            Timber.d("Loading current StoryPath: " + storyPathTemplateFile); // FIXME at least toast the user
        } else {
            storyPathTemplateFile = storyPathTemplateFiles.get(storyPathTemplateKey);
            Timber.d("Loading template StoryPath: " + storyPathTemplateFile); // FIXME at least toast the user
        }

        if (storyPathTemplateFile == null) {
            Timber.e("Loading failed. Could not find file: " + storyPathTemplateKey); // FIXME at least toast the user
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
                Timber.d("Load from saved instance");
                referencedFiles = new ArrayList<String>();
            } else {
                Timber.d("Load from template");
                referencedFiles = new ArrayList<String>();
                // pass through files referenced by library (which were pulled from intent)
                // does there need to be a way to select references when loading a path?
                // referencedFiles = JsonHelper.getInstancePaths();
                if ((dependencies != null) && (dependencies.size() > 0)) {
                    // support multiple referenced files?
                    Timber.d("Found " + dependencies.size() + " referenced files in library");
                    for (Dependency dependency : dependencies) {
                        if(dependency.getDependencyFile().contains("-instance")) {
                            referencedFiles.add(dependency.getDependencyFile());
                        }
                    }
                } else {
                    Timber.d("Found no referenced files in library");
                }
            }

            StoryPath story = null;
            if (checkFile.exists() || checkFile.getPath().contains("instance")) { // need to handle virtual files, also instance files should never be in a zip
                story = JsonHelper.loadStoryPath(checkPath, this, referencedFiles, context, lang);
                Timber.d("Loaded StoryPath from file: " + checkPath);
            } else {
                story = JsonHelper.loadStoryPathFromZip(checkPath, this, referencedFiles, context, lang);
                Timber.d("Loaded StoryPath from zip: " + checkPath);
            }

            setCurrentStoryPath(story);
            setCurrentStoryPathFile(storyPathTemplateFile);

            // need to prevent saves when opening instances to extract metadata
            if (save) {
                save(false);
            }

            // update instance index with title
            if ((context instanceof MainActivity) && (((MainActivity)context).instanceIndex.containsKey(getSavedFileName())))  {
                scal.io.liger.model.sqlbrite.InstanceIndexItem item = ((MainActivity)context).instanceIndex.get(getSavedFileName());

                // check current title to minimize file access
                if ((item.getTitle() != null) && (item.getTitle().equals(story.getTitle()))) {
                    Timber.d("can't update index item with title (index item found for " + getSavedFileName() + " already has the same title)");
                } else {
                    item.setTitle(story.getTitle());

<<<<<<< HEAD
                    StorymakerIndexManager.instanceIndexAdd(context, item, ((MainActivity) context).instanceIndex, ((MainActivity) context).getInstanceIndexItemDao());
                    Log.d(TAG, "updated index item with title " + story.getTitle() + " (index item found for " + getSavedFileName() + ")");
=======
                    IndexManager.instanceIndexAdd(context, item, ((MainActivity) context).instanceIndex);
                    Timber.d("updated index item with title " + story.getTitle() + " (index item found for " + getSavedFileName() + ")");
>>>>>>> dev
                }
            } else if (!(context instanceof MainActivity)) {
                Timber.d("can't update index item with title outside the context of liger main activity");
            } else {
                // index item must be initialized by a save action
                Timber.e("can't update index item with title (no index item found for " + getSavedFileName() + ")");
            }

            if (mListener != null) mListener.onStoryPathLoaded();

        } else {
            Timber.e("app context reference not found, cannot initialize card list for " + storyPathTemplateFile); // FIXME at least toast the user
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
            Timber.d("Saving to new file: " + savedStoryPathLibraryFile);

            // create new item for instance index
            Date now = new Date();
            scal.io.liger.model.sqlbrite.InstanceIndexItem newItem = new scal.io.liger.model.sqlbrite.InstanceIndexItem(savedStoryPathLibraryFile, now.getTime());

            // need source for title/description/type

            StorymakerIndexManager.instanceIndexAdd(context, newItem, ((MainActivity)context).instanceIndex, ((MainActivity) context).getInstanceIndexItemDao());

            Timber.d("Added index item for new instance file : " + savedStoryPathLibraryFile);

        } else {
            Timber.d("Saving to existing file: " + savedStoryPathLibraryFile);
        }

        if (saveCurrentStoryPath && (getCurrentStoryPath() != null)) {
            getCurrentStoryPath().setStoryPathLibraryFile(savedStoryPathLibraryFile);
            String savedStoryPathFile = getCurrentStoryPath().getSavedFileName();
            if (savedStoryPathFile == null) {
                savedStoryPathFile = JsonHelper.getStoryPathSaveFileName(getCurrentStoryPath());
                getCurrentStoryPath().setSavedFileName(savedStoryPathFile);
                Timber.d("Saving current StoryPath to new file: " + savedStoryPathFile);
            } else {
                Timber.d("Saving current StoryPath to existing file: " + savedStoryPathFile);
            }
            setCurrentStoryPathFile(savedStoryPathFile);
            JsonHelper.saveStoryPath(getCurrentStoryPath(), savedStoryPathFile);
            Timber.d("Current StoryPath with id " + getCurrentStoryPath().getId() + " was saved to file " + savedStoryPathFile);
        } else {
            Timber.d("Id " + getId() + " has no current StoryPath, but save was explicitly requested. Ignoring.");
        }

        JsonHelper.saveStoryPathLibrary(this, savedStoryPathLibraryFile);
        Timber.d("Id " + getId() + " was saved to file " + savedStoryPathLibraryFile);

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
            Timber.e(ioe.getMessage());
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
            Timber.e(ioe.getMessage());
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
            Timber.e(ioe.getMessage());
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
