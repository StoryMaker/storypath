package scal.io.liger.model;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.annotations.Expose;
import com.google.gson.stream.MalformedJsonException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import scal.io.liger.Constants;
import scal.io.liger.JsonHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.ReferenceHelper;

/**
 * An ordered collection of {@link scal.io.liger.model.Card}s de/serializable from/to JSON
 *
 * @author Matthew Bogner
 * @author Josh Steiner
 */
public class StoryPath {
    public static final String TAG = "StoryPath";

    /** An identifier unique to this Story Path */
    @Expose protected String id;
    @Expose protected String title;
    @Expose protected String classPackage;
    @Expose ArrayList<Card> cards;
    @Expose protected ArrayList<Dependency> dependencies;
    @Expose protected String fileLocation;
    protected StoryPathLibrary storyPathLibrary; // not serialized
    @Expose protected String storyPathLibraryFile;
    @Expose protected String savedFileName = null;

    // versioning
    @Expose protected String language;
    @Expose protected int version;
    @Expose protected String templatePath;

//    @Expose protected ArrayList<String> requiredUploadTargets; // if defined, this story can only upload to this target and we skip the selection ui
//    @Expose protected ArrayList<String> requiredPublishTargets; // if defined, this story can only pulish to this target and we skip the selection ui
//    @Expose protected ArrayList<String> requiredTags; // if defined, these tags will be prepended to the story's tags

    // this is used by the JsonHelper class to load json assets
    // if there is an alternate way to load them, this should be removed
    // also must be cleared before serializing story path
    protected Context context;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClassPackage() {
        return classPackage;
    }

    public void setClassPackage(String classPackage) {
        this.classPackage = classPackage;
    }

    public String getSavedFileName() {
        return savedFileName;
    }

    public void setSavedFileName(String savedFileName) {
        this.savedFileName = savedFileName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public void addCard(Card card) {
        if (this.cards == null) this.cards = new ArrayList<>();

        this.cards.add(card);
        // add it to the adapter
    }

    public void addCardAtPosition(Card card, int position) {
        if (this.cards == null) this.cards = new ArrayList<>();

        this.cards.add(position, card);
    }

    public void activateCard(Card card) {
//        if (this.visibleCards == null)
//            this.visibleCards = new ArrayList<Card>();

//        this.visibleCards.add(card);
        // add it to the adapter
        ((MainActivity) context).activateCard(card); // FIXME unsafe cast
    }

    public void inactivateCard(Card card) {
//        this.visibleCards.remove(card);
        // remove it from the adapter
        ((MainActivity) context).inactivateCard(card); // FIXME unsafe cast
    }

    // TODO : This should be named getCardByFullPath
    public Card getCardById(String fullPath) {
        // assumes the format story::card::field::value
        String[] pathParts = fullPath.split("::");

        // sanity check
        if (!this.id.equals(pathParts[0])) {
            Log.e(TAG, "STORY PATH ID " + pathParts[0] + " DOES NOT MATCH (vs. " + this.id + ")");
            return null;
        }

        for (Card card : cards) {
            if (card.getId().equals(pathParts[1])) {
                return card;
            }
        }

        Log.e(TAG, "CARD ID " + pathParts[1] + " WAS NOT FOUND");
        return null;
    }

    // TOOD : This should be named getCardById
    public Card getCardByIdOnly(String idOnly) {
        for (Card card : cards) {
            if (card.getId().equals(idOnly)) {
                return card;
            }
        }

        Log.e(TAG, "CARD ID " + idOnly + " WAS NOT FOUND");
        return null;
    }

    // new method to get batches of cards while preserving card order
    public ArrayList<Card> getCardsByIds(ArrayList<String> fullPaths) {
        ArrayList<String> cardIds = new ArrayList<String>();
        for (String fullPath : fullPaths) {
            // assumes the format story::card::field::value
            String[] pathParts = fullPath.split("::");
            cardIds.add(pathParts[1]);
        }

        ArrayList<Card> foundCards = new ArrayList<Card>();
        for (Card card : cards) {
            if (cardIds.contains(card.getId())) {
                foundCards.add(card);
            }
        }

        return foundCards;
    }

    /**
     * Get all the cards that should be displayed to the user for this StoryPath
     *
     * Note this method only needs to be called once. Updates to this list can be received
     * by calling {@link StoryPathLibrary#setStoryPathLibraryListener(scal.io.liger.model.StoryPathLibrary.StoryPathLibraryListener)}
     */
    public ArrayList<Card> getValidCards() {
        /*
        if (visibleCards == null) {
            visibleCards = new ArrayList<Card>();

            for (Card card : cards) {
                if (card.checkVisibilityChanged()) {
                    visibleCards.add(card);
                }
            }
        }

        return visibleCards;
        */


        ArrayList<Card> validCards = new ArrayList<Card>();

        for (Card card : cards) {
            if (card.checkReferencedValues()) {
                if (card instanceof HeadlessCard) {
                    Log.d(TAG, "headless card " + card.getId() + " should not return " + card.checkReferencedValues() + " for checkReferencedValues()");
                } else {
                    validCards.add(card);
                }
            }
        }

        return validCards;

    }

    // required for serialization/deserialization
    /*
    public void setValidCards(ArrayList<Card> validCards) {
        this.visibleCards = visibleCards;
    }
    */

    /**
     * @return a Collection of Dependencies describing other StoryPath files this instance references
     */
    public ArrayList<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(ArrayList<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(Dependency dependency) {
        if (this.dependencies == null)
            this.dependencies = new ArrayList<Dependency>();

        this.dependencies.add(dependency);
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public StoryPathLibrary getStoryPathLibrary() {
        return storyPathLibrary;
    }

    public void setStoryPathLibrary(StoryPathLibrary storyPathLibrary) {
        this.storyPathLibrary = storyPathLibrary;
    }

    public String getStoryPathLibraryFile() {
        return storyPathLibraryFile;
    }

    public void setStoryPathLibraryFile(String storyPathLibraryFile) {
        this.storyPathLibraryFile = storyPathLibraryFile;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Set a reference to this story path in each card.
     * Must be done before cards attempt to reference
     * values from previous story paths or cards
     */
    public void setCardReferences() {
        for (Card card : cards) {
            card.setStoryPath(this);
        }
    }

    /**
     * Clear references to this story path from each card.
     * must be done before serializing this story path to
     * prevent duplication or circular references
     *
     * TODO Can this docstring be revised now that we have explicit de/serialization with @Expose?
     */
    public void clearCardReferences() {
        for (Card card : cards) {
            card.setStoryPath(null);
        }
    }

    /**
     * Observers must be initialized after cards are deserialized.
     * Observers must be cleared before serializing to prevent TODO Is this still necessary w/ @Expose?
     * circular references (cards pointing to cards)
     */
    public void initializeObservers() {
        for (Card card : cards) {
            card.registerObservers();
        }
    }

    public void clearObservers() {
        for (Card card : cards) {
            card.removeObservers();
        }
    }

    public void resetVisibility() {
        for (Card card : cards) {
            card.resetStateVisibility();
        }
    }

    /** Return the value corresponding to the fully qualified Id or null if it could not be found.
     * e.g: "default_library::quiz_card_topic::choice"
    */
    public String getReferencedValue(String fullPath) {
        // assumes the format story::card::field::value
        String[] pathParts = fullPath.split("::");

        if (!this.getId().equals(pathParts[0])) {
            return Constants.EXTERNAL; // FIXME we should probably use an exception instead of overloading the return value
        }

        Card card = this.getCardById(fullPath);

        if (card == null) {
            return null;
        } else {
            String value = card.getValueById(fullPath);

            return value;
        }
    }

    public String getExternalReferencedValue(String fullPath) {
        // assumes the format story::card::field::value
        String[] pathParts = fullPath.split("::");

        StoryPath story = null;

        if (dependencies == null) {
            Log.e(TAG, "STORY PATH " + pathParts[0] + " REFERENCED (GET VALUE), BUT DEPENDENCIES IS NULL");
            return null;
        } else if (dependencies.size() == 0) {
            Log.e(TAG, "STORY PATH " + pathParts[0] + " REFERENCED (GET VALUE), BUT DEPENDENCIES IS EMPTY");
            return null;
        }

        // reference targets a serialized story path
        for (Dependency dependency : getDependencies()) {
            if (dependency.getDependencyId().equals(pathParts[0])) {
                //GsonBuilder gBuild = new GsonBuilder();
                //gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
                //Gson gson = gBuild.create();

                MainActivity mainActivity = (MainActivity) context; // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())

                //String json = JsonHelper.loadJSONFromPath(buildZipPath(dependency.getDependencyFile()));
                //story = gson.fromJson(json, StoryPath.class);

                // check for file
                // paths to actual files should fully qualified
                // paths within zip files should be relative
                // (or at least not resolve to actual files)
                String checkPath = buildZipPath(dependency.getDependencyFile());
                File checkFile = new File(checkPath);

                // should not need to load dependencies to get value from path
                // ArrayList<String> referencedFiles = JsonHelper.getInstancePaths();
                ArrayList<String> referencedFiles = new ArrayList<String>();

                if (checkFile.exists()) {
                    story = JsonHelper.loadStoryPath(dependency.getDependencyFile(), this.storyPathLibrary, referencedFiles, this.context, mainActivity.getLanguage());
                    Log.e("FILES", "LOADED FROM FILE: " + dependency.getDependencyFile());
                } else {
                    story = JsonHelper.loadStoryPathFromZip(dependency.getDependencyFile(), this.storyPathLibrary, referencedFiles, this.context, mainActivity.getLanguage());
                    Log.e("FILES", "LOADED FROM ZIP: " + dependency.getDependencyFile());
                }

                //story.context = this.context;
                //story.setCardReferences();
                //story.setFileLocation(buildZipPath(dependency.getDependencyFile()));

                Log.d("getExternalReferencedValue", "FOUND MATCHING DEPENDENCY FILE " + dependency.getDependencyFile() + ", BREAKING FOR LOOP");
                break;
            }
        }

        if (story == null) {
            Log.e(TAG, "STORY PATH ID " + pathParts[0] + " WAS NOT FOUND");
            return null;
        }

        Card card = story.getCardById(fullPath);

        if (card == null) {
            return null;
        } else {
            String value = card.getValueById(fullPath);

            if (value == null) {
                return null;
            } else {
                return value;
            }
        }
    }

    public String buildZipPath(String originalPath) {
        if (originalPath.startsWith(File.separator)) {
            return originalPath;
        }

        // construct path relative to location of story path
        String relativePath = getFileLocation();


        while (originalPath.contains("..")) {
            Log.d("PATHS", "NEED TO DEAL WITH RELATIVE PATHS");
            originalPath = originalPath.substring(originalPath.indexOf(File.separator) + 1);
            relativePath = relativePath.substring(0, relativePath.lastIndexOf(File.separator));
            Log.d("PATHS", "CHECKING... " + relativePath + File.separator + originalPath);
        }


        if ((relativePath != null) && (relativePath.length() != 0)) {
            relativePath = relativePath.substring(0, relativePath.lastIndexOf(File.separator));

            Log.d("TESTING", "ID: " + this.getId() + " BASE PART: " + relativePath + " OTHER PART: " + originalPath);

            relativePath = relativePath + File.separator + originalPath;
            return relativePath;
        } else {
            Log.e(TAG, "NO ROOT TO CONSTRUCT RELATIVE PATH FOR " + originalPath);
            return originalPath;
        }
    }

    public String buildFilePath(String originalPath) {
        if (originalPath.startsWith(File.separator)) {
            return originalPath;
        }

        // construct path relative to location of story path
        String relativePath = getFileLocation();

        if ((relativePath != null) && (relativePath.length() != 0)) {
            relativePath = relativePath.substring(0, relativePath.lastIndexOf(File.separator));

            Log.d("TESTING", "ID: " + this.getId() + " BASE PART: " + relativePath + " OTHER PART: " + originalPath);

            relativePath = relativePath + File.separator + originalPath;
            return relativePath;
        } else {
            Log.e(TAG, "NO ROOT TO CONSTRUCT RELATIVE PATH FOR " + originalPath);
            return originalPath;
        }
    }

    /**
     * @return an absolute filesystem path for a given path relative to an obb file
     *
     * e.g: method called with path 'org.storymaker.app/burundi/00105/1.png' will return
     * '/storage/emulated/0/Android/data/org.storymaker.app/files/org.storymaker.app_burundi_00105_1.png'
     */
    public String buildTargetPath(String originalPath) {
        if (originalPath.startsWith(File.separator)) {
            return originalPath;
        }

        // determine location of app folder
        String basePath = JsonHelper.getSdLigerFilePath(context);

        // flatten path
        String flatPath = originalPath.replace(File.separatorChar, '_');

        Log.d("TESTING", "ID: " + this.getId() + " BASE PART: " + basePath + " FLAT PART: " + flatPath);

        String relativePath = basePath + flatPath;
        return relativePath;
    }

    /**
     * This method tracks changes to cards within this StoryPath
     *
     * A client wishing to maintain a view controller
     * for this StoryPath need only call {@link #getValidCards()} once. This method is responsible
     * for propagating events affecting the value of {@link #getValidCards()} to the listener set in
     * {@link scal.io.liger.model.StoryPathLibrary#setStoryPathLibraryListener(scal.io.liger.model.StoryPathLibrary.StoryPathLibraryListener)}
     */
    public void notifyCardChanged(@NonNull Card firstCard) {
        Log.i(TAG, "notifyCardChanged of update to card " + firstCard.getId());
        if (storyPathLibrary == null || storyPathLibrary.mListener == null) {
            return;
        }

        String action = ((MainActivity)context).checkCard(firstCard);

        if (action.equals("ADD")) {
            storyPathLibrary.mListener.onCardAdded(firstCard);
        }
        if (action.equals("UPDATE")) {
            storyPathLibrary.mListener.onCardChanged(firstCard);
        }
        if (action.equals("DELETE")) {
            storyPathLibrary.mListener.onCardRemoved(firstCard);
        }

        // SEEMS LIKE A REASONABLE TIME TO SAVE
        getStoryPathLibrary().save(true);
    }

    /**
     * This method tracks swapping events between two cards
     * currently in this StoryPath
     */
    public void notifyCardsSwapped(Card cardOne, Card cardTwo) {
        if (storyPathLibrary == null || storyPathLibrary.mListener == null) {
            return;
        }
        storyPathLibrary.mListener.onCardsSwapped(cardOne, cardTwo);
    }

    /*
    public void notifyCardChanged(Card updatedCard) {
        Log.d("StoryPathModel", "notifyCardChanged");

        if (updatedCard.getStateVisiblity()) {
            // new or updated

            int cardIndex = 0;

            if (visibleCards.contains(updatedCard)) {
                visibleCards.remove(updatedCard);
                inactivateCard(updatedCard);
            } else {
                // foo
            }

            cardIndex = findSpot(updatedCard);

            if (cardIndex >= visibleCards.size()) {
                visibleCards.add(updatedCard);
                activateCard(updatedCard);
            } else {
                visibleCards.add(cardIndex, updatedCard);
            }
        } else {
            // deleted

            if (visibleCards.contains(updatedCard)) {
                visibleCards.remove(updatedCard);
                inactivateCard(updatedCard);
            } else {
                // foo
            }
        }

        // call refresh for now, card list will be returned

//        if (context != null) {
//            MainActivity mainActivity = (MainActivity) context; // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())
//            mainActivity.refreshCardView();
//        } else {
//            System.err.println("APP CONTEXT REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
//        }
    }
    */

    /*
    public int findSpot(Card card) {
        int baseIndex = cards.indexOf(card);
        Log.d(" *** FINDSPOT *** ", "CARD " + card.getId() + " - DEFAULT POSITION " + baseIndex);
        int newIndex = 0;
        for (int i = (baseIndex - 1); i >= 0; i--) {
            Card previousCard = cards.get(i);
            Log.d(" *** FINDSPOT *** ", "LOOKING FOR " + previousCard.getId());
            if (visibleCards.contains(previousCard)) {
                newIndex = visibleCards.indexOf(previousCard) + 1;
                Log.d(" *** FINDSPOT *** ", "CARD " + previousCard.getId() + " FOUND AT POSITION " + visibleCards.indexOf(previousCard));

                break;
            }
        }

        Log.d(" *** FINDSPOT *** ", "INSERTING " + card.getId() + " AT POSITION " + newIndex);

        return newIndex;
    }
    */

    public void linkNotification(String linkPath) {
        if (context != null) {
            try {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.goToCard(this, linkPath);
            } catch (com.google.gson.JsonSyntaxException e) {
                Toast.makeText(context, "JSON syntax error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (MalformedJsonException e) {
                Toast.makeText(context, "JSON parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            System.err.println("APP CONTEXT REFERENCE NOT FOUND, CANNOT SEND LINK NOTIFICATION");
        }
    }

    public int getCardIndex(Card cardModel) {
        return cards.indexOf(cardModel);
    }

    public int getValidCardIndex(Card cardModel) {
        return getValidCards().indexOf(cardModel);
    }

    public Card getCardFromIndex(int index) {
        if(index >= cards.size()) {
            return null;
        }

        return cards.get(index);
    }

    public Card getValidCardFromIndex(int index) {
        ArrayList<Card> validCards = getValidCards();

        if(index >= validCards.size()) {
            return null;
        }

        return validCards.get(index);
    }

    public void rearrangeCards(int currentIndex, int newIndex) {
        Card card = cards.remove(currentIndex);
        cards.add(newIndex, card);

        // Log.d(" *** REARRANGE *** ", "MOVED " + card.getId() + " FROM " + currentIndex + " TO " + newIndex);

        notifyCardChanged(card);
        // We should also notify the other affected card, right?
        // e.g: The one that was at newIndex when the op started?
    }

    /**
     * Swap the cards at the given indexes.
     * This is like {@link #rearrangeCards(int, int)} but also supports
     * operations where cards aren't moving to adjacent positions
     */
    public void swapCards(int firstIndex, int secondIndex) {
        Card tempCard = cards.get(firstIndex);
        cards.set(firstIndex, cards.get(secondIndex));
        cards.set(secondIndex, tempCard);

        notifyCardsSwapped(tempCard, cards.get(firstIndex));
    }

    public void saveMediaFile(String uuid, MediaFile file) {
        storyPathLibrary.saveMediaFile(uuid, file);
    }

    public MediaFile loadMediaFile(String uuid) {
        return storyPathLibrary.loadMediaFile(uuid);
    }

    /**
     * Get a Collection of meta data for all Clips.
     *
     * To retrieve the corresponding MediaFile for each ClipMetaData, see
     * {@link #loadMediaFile(String)} using the uuid found via
     * {@link ClipMetadata#getUuid()}
     */
    // FIXME doesn't this belong in SPL?
    public ArrayList<ClipMetadata> exportMetadata() {
        ArrayList<ClipMetadata> metadata = new ArrayList<>();
        ArrayList<String> classReference = new ArrayList<>();
        classReference.add(this.getId() + "::<<" + ClipCard.class.getName() + ">>");
        ArrayList<Card> clipCards = getCards(classReference);
        for (Card c : clipCards) {
            // should be safe to cast, cards fetched based on class
            ClipCard cc = (ClipCard)c;

            if (cc.getClips() != null) {
                for (ClipMetadata cmd : cc.getClips()) {
                    if (!metadata.contains(cmd)) {
                        metadata.add(cmd);
                    }
                }
            }
        }
        return metadata;
    }

    /**
     * Get a Collection of meta data for all Clips.
     *
     * To retrieve the corresponding MediaFile for each ClipMetaData, see
     * {@link #loadMediaFile(String)} using the uuid found via
     * {@link ClipMetadata#getUuid()}
     */
    // FIXME doesn't this belong in SPL?
    public ArrayList<ClipMetadata> exportSelectedClipMetadata() {
        ArrayList<ClipMetadata> metadata = new ArrayList<>();
        ArrayList<String> classReference = new ArrayList<>();
        classReference.add(this.getId() + "::<<" + ClipCard.class.getName() + ">>");
        ArrayList<Card> clipCards = getCards(classReference);
        for (Card c : clipCards) {
            // should be safe to cast, cards fetched based on class
            ClipCard cc = (ClipCard)c;

            if (cc.getClips() != null) {
                for (ClipMetadata cmd : cc.getClips()) {
                    if (!metadata.contains(cmd)) { // FIXME why do we do this ?  we can't havethe exact same clip twice in a row?
                        metadata.add(cmd);
                        break; // we only export the first clip in each clipcard, as this is the selected one
                    }
                }
            }
        }
        return metadata;
    }

    public void importMetadata(ArrayList<ClipMetadata> metadata) {
        ArrayList<String> classReference = new ArrayList<String>();
        classReference.add(this.getId() + "::<<" + ClipCard.class.getName() + ">>");
        ArrayList<Card> clipCards = getCards(classReference);
        for (Card c : clipCards) {
            // should be safe to cast, cards fetched based on class
            ClipCard cc = (ClipCard) c;

            // NEED TO REPLACE THIS WITH A BETTER ALGORITHM FOR MATCHING CLIPS TO CARDS
            if (metadata.size() > 0) {
                ClipMetadata cmd = metadata.remove(0);
                cc.addClip(cmd, false);
            }
        }
    }

    public ArrayList<FullMetadata> adaptClipMetadataToFullMetadata(ArrayList<ClipMetadata> metadata) {
        ArrayList<FullMetadata> allMetadata = new ArrayList<FullMetadata>();
        for (ClipMetadata cm : metadata) {
            MediaFile mf = loadMediaFile(cm.getUuid());
            String path = mf.getPath();
            if (mf.getPath().startsWith("content://")) {
                path = getPath(context, Uri.parse(path));
            }
            mf.setPath(path);
            if (mf == null) {
                Log.e(TAG, "no media file was found for uuid " + cm.getUuid());
            } else {
                FullMetadata fm = new FullMetadata(cm, mf);
                allMetadata.add(fm);
            }
        }
        return allMetadata;
    }

    // FIXME doesn't this belong in SPL?
    public ArrayList<FullMetadata> exportAllMetadata() {
        ArrayList<ClipMetadata> metadata = exportMetadata();
        return adaptClipMetadataToFullMetadata(metadata);
    }

    // FIXME doesn't this belong in SPL?
    public ArrayList<FullMetadata> exportSelectedFullMetadata() {
        ArrayList<ClipMetadata> metadata = exportSelectedClipMetadata();
        return adaptClipMetadataToFullMetadata(metadata);
    }

    public ArrayList<AudioClipFull> exportAudioClips() {
        ArrayList<AudioClip> acs = getStoryPathLibrary().getAudioClips();
        if (acs != null) {
            ArrayList<AudioClipFull> acfs = new ArrayList<AudioClipFull>();
            for (AudioClip ac : acs) {
                acfs.add(new AudioClipFull(getStoryPathLibrary(), ac));
            }
            return acfs;
        } else {
            return null;
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public ArrayList<MediaFile> getExternalMediaFile(String reference) {

        Log.d("CLIPS", "NEW METHOD getExternalMediaFile() CALLED FOR REFERENCE " + reference);

        ArrayList<MediaFile> results = new ArrayList<MediaFile>();

        String[] parts = reference.split("::");

        StoryPath story = null;
        StoryPathLibrary library = null;

        if (dependencies == null) {
            Log.e("CLIPS", "STORY PATH " + parts[0] + " REFERENCED (GET MEDIA), BUT DEPENDENCIES IS NULL");
            return null;
        } else if (dependencies.size() == 0) {
            Log.e("CLIPS", "STORY PATH " + parts[0] + " REFERENCED (GET MEDIA), BUT DEPENDENCIES IS EMPTY");
            return null;
        }

        // reference targets a serialized story path
        for (Dependency dependency : getDependencies()) {
            if (dependency.getDependencyId().equals(parts[0])) {

                MainActivity mainActivity = (MainActivity) context; // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())

                String checkPath = buildZipPath(dependency.getDependencyFile());
                File checkFile = new File(checkPath);

                // should not need to load dependencies to get media file from path
                // ArrayList<String> referencedFiles = JsonHelper.getInstancePaths();
                ArrayList<String> referencedFiles = new ArrayList<String>();


                if (checkFile.exists()) {
                    if (dependency.getDependencyFile().contains("-library-instance")) {
                        story = JsonHelper.loadStoryPathLibrary(dependency.getDependencyFile(), referencedFiles, this.context, mainActivity.getLanguage());
                    } else {
                        story = JsonHelper.loadStoryPath(dependency.getDependencyFile(), this.storyPathLibrary, referencedFiles, this.context, mainActivity.getLanguage());
                    }
                    Log.d("CLIPS", "LOADED FROM FILE: " + dependency.getDependencyFile());
                } else {
                    if (dependency.getDependencyFile().contains("-library-instance")) {
                        story = JsonHelper.loadStoryPathLibraryFromZip(dependency.getDependencyFile(), referencedFiles, this.context, mainActivity.getLanguage());
                    } else {
                        story = JsonHelper.loadStoryPathFromZip(dependency.getDependencyFile(), this.storyPathLibrary, referencedFiles, this.context, mainActivity.getLanguage());
                    }
                    Log.d("CLIPS", "LOADED FROM ZIP: " + dependency.getDependencyFile());
                }

                if (story instanceof StoryPathLibrary) {
                    Log.d("CLIPS", "STORY PATH " + story.getId() + " IS A LIBRARY");
                    library = (StoryPathLibrary)story;
                } else {
                    Log.d("CLIPS", "NEED TO LOAD STORY PATH LIBRARY CORRESPONDING TO " + story.getId());

                    checkPath = story.buildZipPath(story.getStoryPathLibraryFile());
                    checkFile = new File(checkPath);

                    if (checkFile.exists()) {
                        library = JsonHelper.loadStoryPathLibrary(story.getStoryPathLibraryFile(), referencedFiles, this.context, mainActivity.getLanguage());
                        Log.d("CLIPS", "LOADED FROM FILE: " + story.getStoryPathLibraryFile());
                    } else {
                        library = JsonHelper.loadStoryPathLibraryFromZip(story.getStoryPathLibraryFile(), referencedFiles, this.context, mainActivity.getLanguage());
                        Log.d("CLIPS", "LOADED FROM ZIP: " + story.getStoryPathLibraryFile());
                    }
                }

                Log.d("getExternalMediaFile", "FOUND MATCHING DEPENDENCY FILE " + dependency.getDependencyFile() + ", BREAKING FOR LOOP");
                break;
            }
        }

        if (story == null) {
            Log.e("CLIPS", "STORY PATH ID " + parts[0] + " WAS NOT FOUND");
            return null;
        }

        if (library == null) {
            Log.e("CLIPS", "STORY PATH LIBRARY CORRESPONDING TO ID " + parts[0] + " WAS NOT FOUND");
            return null;
        }

        ArrayList<Card> cards = story.gatherCards(parts[1]);

        if ((cards.size() > 1) || (cards.size() < 1)) {
            Log.e("CLIPS", "UNEXPECTED NUMBER OF CARDS FOUND FOR REFERENCE " + reference);
            return null;
        }

        if (!(cards.get(0) instanceof ClipCard)) {
            Log.e("CLIPS", "CARD FOUND FOR REFERENCE " + reference + " IS NOT A CLIP CARD");
            return null;
        }

        ClipCard clipCard = (ClipCard)(cards.get(0));

        ArrayList<ClipMetadata> clips = clipCard.getClips();

        if (clips.size() < 1) {
            Log.e("CLIPS", "NO CLIPS FOUND FOR REFERENCE " + reference);
            return null;
        }

        for (ClipMetadata clip : clips) {
            MediaFile media = library.loadMediaFile(clip.getUuid());
            if (media == null) {
                Log.d("CLIPS", "NO MEDIA FOUND FOR UUID " + clip.getUuid());
            } else {
                Log.d("CLIPS", "FOUND MEDIA FOR UUID " + clip.getUuid());
                results.add(media);
            }
        }

        return results;
    }



    public ArrayList<Card> getCards(ArrayList<String> references) {
        ArrayList<Card> results = new ArrayList<Card>();

        ArrayList<Card> unfilteredCards = null;
        ArrayList<Card> filteredCards = null;

        for (String reference : references) {
            String[] parts = reference.split("::");

            if (!this.getId().equals(parts[0])) {
                unfilteredCards = gatherExternalCards(parts[0], parts[1]);
            } else {
                unfilteredCards = gatherCards(parts[1]);
            }

            if (unfilteredCards == null) {
                continue;
            }

            // check for key/value parts
            if (parts.length == 4) {
                filteredCards = ReferenceHelper.filterCards(unfilteredCards, parts[2], parts[3]);
            } else {
                filteredCards = unfilteredCards;
            }

            // check for duplicates
            for (Card card : filteredCards) {
                if (!results.contains(card)) {
                    results.add(card);
                }
            }
        }

        return results;
    }

    public ArrayList<String> getValues(ArrayList<String> references) {
        HashMap<String, String> resultMap = new HashMap<String, String>();

        ArrayList<Card> unfilteredCards = null;
        ArrayList<Card> filteredCards = null;

        for (String reference : references) {
            String[] parts = reference.split("::");

            if (!this.getId().equals(parts[0])) {
                unfilteredCards = gatherExternalCards(parts[0], parts[1]);
            } else {
                unfilteredCards = gatherCards(parts[1]);
            }

            if (unfilteredCards == null) {
                continue;
            }

            // check for key/value parts
            if (parts.length == 4) {
                filteredCards = ReferenceHelper.filterCards(unfilteredCards, parts[2], parts[3]);
            } else {
                filteredCards = unfilteredCards;
            }

            // check for key part
            if (parts.length > 2) {
                // check for duplicates
                for (Card card : filteredCards) {
                    resultMap.put(card.getId(), card.getValueByKey(parts[2]));
                }
            }
        }

        ArrayList<String> results = new ArrayList<String>(resultMap.values());
        return results;
    }

    /**
     * Search for cards by type or id.
     *
     * Card Types should be of form returned by {@link scal.io.liger.model.Card#getType()}
     * Card Ids should be of form returned by {@link scal.io.liger.model.Card#getId()}
     *
     * @param cardTarget a query by card type or id.
     *                   Example type query: "<<ClipCard>>"
     *                   Example id query: "clip_card_1"
     * @return
     */
    public ArrayList<Card> gatherCards(String cardTarget) {
        ArrayList<Card> results = new ArrayList<>();

        if (cardTarget.equals("*")) {
            results = getCards();
        } else if (cardTarget.startsWith("<<")) {
            // strip "<<" and ">>"
            cardTarget = cardTarget.substring(2, cardTarget.length()-2);
            for (Card card : getCards()) {
                // need to account for separation of package and class name
                // or should class "wildcards" for cards ignore package?
                if ((card.getStoryPath().getClassPackage() + "." + card.getType()).equals(cardTarget) ||
                        (card.getType().equals(cardTarget))) {
                    results.add(card);
                }
            }
        } else {
            for (Card card : getCards()) {
                if (card.getId() == null) {
                    Log.e("JSON ERROR", "CARD TYPE " + card.getType() + " FOUND WITH NO ID!");
                } else if (card.getId().equals(cardTarget)) {
                    results.add(card);
                }
            }
        }

        return results;
    }


    public <T> ArrayList<T> gatherCardsOfClass(Class<T> clazz) {
        String simpleName = clazz.getSimpleName();
        ArrayList<T> results = new ArrayList<>();

        for (Card card : getCards()) {
            if (card.getType().equals(simpleName)) {
                results.add((T) card);
            }
        }
        return results;
    }

    public ArrayList<Card> gatherExternalCards(String pathTarget, String cardTarget) {

        StoryPath story = null;

        if (dependencies == null) {
            Log.e(TAG, "STORY PATH " + pathTarget + " REFERENCED (GET CARD), BUT DEPENDENCIES IS NULL");
            return null;
        } else if (dependencies.size() == 0) {
            Log.e(TAG, "STORY PATH " + pathTarget + " REFERENCED (GET CARD), BUT DEPENDENCIES IS EMPTY");
            return null;
        }

        // reference targets a serialized story path
        for (Dependency dependency : getDependencies()) {
            if (dependency.getDependencyId().equals(pathTarget)) {

                MainActivity mainActivity = (MainActivity) context; // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())

                String checkPath = buildZipPath(dependency.getDependencyFile());
                File checkFile = new File(checkPath);

                // should not need to load dependencies to get card from path
                // ArrayList<String> referencedFiles = JsonHelper.getInstancePaths();
                ArrayList<String> referencedFiles = new ArrayList<String>();

                if (checkFile.exists()) {
                    story = JsonHelper.loadStoryPath(dependency.getDependencyFile(), this.storyPathLibrary, referencedFiles, this.context, mainActivity.getLanguage());
                    Log.e("FILES", "LOADED FROM FILE: " + dependency.getDependencyFile());
                } else {
                    story = JsonHelper.loadStoryPathFromZip(dependency.getDependencyFile(), this.storyPathLibrary, referencedFiles, this.context, mainActivity.getLanguage());
                    Log.e("FILES", "LOADED FROM ZIP: " + dependency.getDependencyFile());
                }

                Log.d("gatherExternalCards", "FOUND MATCHING DEPENDENCY FILE " + dependency.getDependencyFile() + ", BREAKING FOR LOOP");
                break;
            }
        }

        if (story == null) {
            Log.e(TAG, "STORY PATH ID " + pathTarget + " WAS NOT FOUND");
            return null;
        }

        ArrayList<Card> results = story.gatherCards(cardTarget);

        return results;
    }


    /**
     * Returns the List of ClipCards with attached media within the current StoryPath
     */
    public ArrayList<ClipCard> getClipCardsWithAttachedMedia() {

        ArrayList<ClipCard> mediaCards = gatherCardsOfClass(ClipCard.class);
        Iterator iterator = mediaCards.iterator();
        while (iterator.hasNext()) {
            ClipCard clipCard = (ClipCard) iterator.next();
            if ( clipCard.getClips() == null || clipCard.getClips().size() < 1 ) {
                iterator.remove();
            }
        }
        return mediaCards;
    }

    public void setCoverImageThumbnail(ImageView target) {
        ArrayList<ClipCard> cards = getClipCardsWithAttachedMedia();
        for (ClipCard card: cards) {
            MediaFile mediaFile = card.getSelectedMediaFile();
            if (mediaFile != null) {
                mediaFile.loadThumbnail(target);
                return;
            }
        }
        target.setImageResource(R.drawable.no_thumbnail);

    }

    public String getCoverImageThumbnailPath() {
        ArrayList<ClipCard> cards = getClipCardsWithAttachedMedia();
        for (ClipCard card: cards) {
            MediaFile mediaFile = card.getSelectedMediaFile();
            if ((mediaFile != null) && (mediaFile.getThumbnailFilePath() != null)) {
                return mediaFile.getThumbnailFilePath();
            }
        }
        return null;
    }

    public String getMedium() {
        ArrayList<ClipCard> cards = getClipCardsWithAttachedMedia();
        for (ClipCard card: cards) {
            MediaFile mediaFile = card.getSelectedMediaFile();
            if (mediaFile != null) {
                return mediaFile.getMedium();
            }
        }
        return null;
    }
}
