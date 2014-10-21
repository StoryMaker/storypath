package scal.io.liger;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import scal.io.liger.model.Card;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;

/**
 * Created by mnbogner on 7/14/14.
 */
public class JsonHelper {
    private static final String TAG = "JsonHelper";
    private static final String LIGER_DIR = "Liger";
    private static File selectedJSONFile = null;
    private static ArrayList<File> jsonFileList = null;
    private static String sdLigerFilePath = null;

    public static String loadJSONFromPath(String jsonPath) {

        String jsonString = "";
        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File jsonFile = new File(jsonPath);
                InputStream jsonStream = new FileInputStream(jsonFile);

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonString = new String(buffer);
            } catch (IOException e) {
                Log.e(TAG, "READING JSON FILE FROM SD CARD FAILED: " + e.getMessage());
            }
        } else {
            System.err.println("SD CARD NOT FOUND");
        }

        return jsonString;
    }

    public static String loadJSON() {
        if(null == selectedJSONFile) {
            return null;
        }

        String jsonString = "";
        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                InputStream jsonStream = new FileInputStream(selectedJSONFile);

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonString = new String(buffer);
            } catch (IOException e) {
                Log.e(TAG, "READING JSON FILE FRON SD CARD FAILED: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "SD CARD NOT FOUND");
        }

        return jsonString;
    }

    public static void setupFileStructure(Context context) {
        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            String sdCardFolderPath = Environment.getExternalStorageDirectory().getPath();
            sdLigerFilePath = sdCardFolderPath + File.separator + LIGER_DIR + File.separator;
            Log.d(TAG, "sdLigerFilePath: " + sdLigerFilePath);
            //create folder if first app launch
            new File( sdLigerFilePath + "/default/").mkdirs();

            try {
                String[] assets = context.getAssets().list("default");
                for (String asset: assets) {
                    if (!asset.contains("LIB")){
                        String filePath = "/default/" + asset;
                        InputStream jsonStream = context.getAssets().open("default/" + asset);
                        addFileToSDCard(jsonStream, filePath);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // HARD CODED FOR TESTING
            new File( sdLigerFilePath + "/default/LIB_1/").mkdirs();

            try {
                String[] assets = context.getAssets().list("default/LIB_1");
                for (String asset: assets) {
                    String filePath = "/default/LIB_1/" + asset;
                    InputStream jsonStream = context.getAssets().open("default/LIB_1/" + asset);
                    addFileToSDCard(jsonStream, filePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            new File( sdLigerFilePath + "/default/LIB_2/").mkdirs();

            try {
                String[] assets = context.getAssets().list("default/LIB_2");
                for (String asset: assets) {
                    String filePath = "/default/LIB_2/" + asset;
                    InputStream jsonStream = context.getAssets().open("default/LIB_2/" + asset);
                    addFileToSDCard(jsonStream, filePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "SD CARD NOT FOUND");
        }
    }

    public static String[] getJSONFileList() {
        //ensure path has been set
        if(null == sdLigerFilePath) {
            return null;
        }

        ArrayList<String> jsonFileNamesList = new ArrayList<String>();
        jsonFileList = new ArrayList<File>();

        // HARD CODING LIST

        File ligerFile_1 = new File(sdLigerFilePath + "/default/default_library.json");
        File ligerFile_2 = new File(sdLigerFilePath + "/default/learning_guide_TEST.json");
        File ligerFile_3 = new File(sdLigerFilePath + "/default/LIB_1/LIB_1_TEST.json");
        File ligerFile_4 = new File(sdLigerFilePath + "/default/LIB_2/LIB_2_TEST.json");
        File ligerFile_5 = new File(sdLigerFilePath + "/default/learning_guide_library.json");
        File ligerFile_6 = new File(sdLigerFilePath + "/default/learning_guide_library_saved1.json");

        File ligerFile_7 = new File(sdLigerFilePath + "/default/learning_guide_library_SAVE.json");

        jsonFileNamesList.add(ligerFile_1.getName());
        jsonFileNamesList.add(ligerFile_2.getName());
        jsonFileNamesList.add(ligerFile_3.getName());
        jsonFileNamesList.add(ligerFile_4.getName());
        jsonFileNamesList.add(ligerFile_5.getName());
        jsonFileNamesList.add(ligerFile_6.getName());

        jsonFileNamesList.add(ligerFile_7.getName());

        jsonFileList.add(ligerFile_1);
        jsonFileList.add(ligerFile_2);
        jsonFileList.add(ligerFile_3);
        jsonFileList.add(ligerFile_4);
        jsonFileList.add(ligerFile_5);
        jsonFileList.add(ligerFile_6);

        jsonFileList.add(ligerFile_7);

        /*
        File ligerDir = new File(sdLigerFilePath);
        if (ligerDir != null) {
            for (File file : ligerDir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    jsonFileNamesList.add(file.getName());
                    jsonFileList.add(file);
                }
            }
        }

        File defaultLigerDir = new File(sdLigerFilePath + "/default/");
        if (defaultLigerDir != null) {
            for (File file : defaultLigerDir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    jsonFileNamesList.add(file.getName());
                    jsonFileList.add(file);
                }
            }
        }
        */

        return jsonFileNamesList.toArray(new String[jsonFileNamesList.size()]);
    }

    public static File setSelectedJSONFile(int index) {
        selectedJSONFile = jsonFileList.get(index);
        return selectedJSONFile;
    }

    private static void addFileToSDCard(InputStream jsonInputStream, String filePath) {
        OutputStream outputStream = null;

        try {
            // write the inputStream to a FileOutputStream
            outputStream = new FileOutputStream(new File(sdLigerFilePath + filePath));

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = jsonInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (jsonInputStream != null) {
                try {
                    jsonInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    // NEW

    public static StoryPathLibrary loadStoryPathLibrary(String jsonFilePath, Context context) {

        Log.e(" *** TESTING *** ", "NEW METHOD loadStoryPathLibrary CALLED");

        String storyPathLibraryJson = "";
        String sdCardState = Environment.getExternalStorageState();

        File f = new File(jsonFilePath);
        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                InputStream jsonStream = new FileInputStream(f);

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                storyPathLibraryJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from SD card failed: " + ioe.getMessage());
                return null;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return null;
        }

        return deserializeStoryPathLibrary(storyPathLibraryJson, f.getPath(), context);

    }

    public static StoryPathLibrary deserializeStoryPathLibrary(String storyPathLibraryJson, String jsonFilePath, Context context) {

        Log.e(" *** TESTING *** ", "NEW METHOD deserializeStoryPathLibrary CALLED");

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPathLibrary.class, new StoryPathLibraryDeserializer());
        Gson gson = gBuild.create();

        StoryPathLibrary storyPathLibrary = gson.fromJson(storyPathLibraryJson, StoryPathLibrary.class);

        // a story path library model must have a file location to manage relative paths
        // if it is loaded from a saved state, the location should already be set
        if ((jsonFilePath == null) || (jsonFilePath.length() == 0)) {
            if ((storyPathLibrary.getFileLocation() == null) || (storyPathLibrary.getFileLocation().length() == 0)) {
                Log.e(TAG, "file location for story path library " + storyPathLibrary.getId() + " could not be determined");
                return null;
            }
        } else {
            storyPathLibrary.setFileLocation(jsonFilePath);
        }

        storyPathLibrary.setCardReferences();
        storyPathLibrary.initializeObservers();
        storyPathLibrary.setContext(context);

        // need to reset visibility so that visible cards will update their status properly
        storyPathLibrary.resetVisibility();

        return storyPathLibrary;

    }

    public static String saveStoryPathLibrary(StoryPathLibrary storyPathLibrary) {

        Log.e(" *** TESTING *** ", "NEW METHOD saveStoryPathLibrary CALLED");

        Date timeStamp = new Date();
        String jsonFilePath = storyPathLibrary.buildPath(storyPathLibrary.getId() + "_" + timeStamp.getTime() + ".json");

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File storyPathLibraryFile = new File(jsonFilePath);
                FileOutputStream storyPathLibraryStream = new FileOutputStream(storyPathLibraryFile);
                if (!storyPathLibraryFile.exists()) {
                    storyPathLibraryFile.createNewFile();
                }

                String storyPathLibraryJson = serializeStoryPathLibrary(storyPathLibrary);

                byte storyPathLibraryData[] = storyPathLibraryJson.getBytes();
                storyPathLibraryStream.write(storyPathLibraryData);
                storyPathLibraryStream.flush();
                storyPathLibraryStream.close();
            } catch (IOException ioe) {
                Log.e(TAG, "writing json file " + jsonFilePath + " to SD card failed: " + ioe.getMessage());
                return null;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return null;
        }

        // update file location
        storyPathLibrary.setFileLocation(jsonFilePath);

        return jsonFilePath;

    }

    public static String serializeStoryPathLibrary(StoryPathLibrary storyPathLibrary) {

        Log.e(" *** TESTING *** ", "NEW METHOD serializeStoryPathLibrary CALLED");

        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.create();

        // set aside references to prevent circular dependencies when serializing
        Context tempContext = storyPathLibrary.getContext();
        StoryPath tempCurrentStoryPath = storyPathLibrary.getCurrentStoryPath();
        //ArrayList<Card> tempValidCards = storyPathLibrary.getValidCards();

        storyPathLibrary.setContext(null);
        storyPathLibrary.setCurrentStoryPath(null);
        //storyPathLibrary.setValidCards(null);
        storyPathLibrary.clearObservers();
        storyPathLibrary.clearCardReferences();

        String storyPathLibraryJson = gson.toJson(storyPathLibrary);

        // restore references

        storyPathLibrary.setCardReferences();
        storyPathLibrary.initializeObservers();
        //storyPathLibrary.setValidCards(tempValidCards);
        storyPathLibrary.setCurrentStoryPath(tempCurrentStoryPath);
        storyPathLibrary.setContext(tempContext);

        return storyPathLibraryJson;

    }

    public static StoryPath loadStoryPath(String jsonFilePath, StoryPathLibrary storyPathLibrary, Context context) {

        Log.e(" *** TESTING *** ", "NEW METHOD loadStoryPath CALLED");

        String storyPathJson = "";
        String sdCardState = Environment.getExternalStorageState();

        File f = new File(jsonFilePath);
        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                InputStream jsonStream = new FileInputStream(f);

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                storyPathJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from SD card failed: " + ioe.getMessage());
                return null;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return null;
        }

        return deserializeStoryPath(storyPathJson, f.getPath(), storyPathLibrary, context);

    }

    public static StoryPath deserializeStoryPath(String storyPathJson, String jsonFilePath, StoryPathLibrary storyPathLibrary, Context context) {

        Log.e(" *** TESTING *** ", "NEW METHOD deserializeStoryPath CALLED");

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
        Gson gson = gBuild.create();

        StoryPath storyPath = gson.fromJson(storyPathJson, StoryPath.class);

        // a story path model must have a file location to manage relative paths
        // if it is loaded from a saved state, the location should already be set
        if ((jsonFilePath == null) || (jsonFilePath.length() == 0)) {
            if ((storyPath.getFileLocation() == null) || (storyPath.getFileLocation().length() == 0)) {
                Log.e(TAG, "file location for story path " + storyPath.getId() + " could not be determined");
            }
        } else {
            storyPath.setFileLocation(jsonFilePath);
        }

        storyPath.setCardReferences();
        storyPath.initializeObservers();
        storyPath.setStoryPathLibraryReference(storyPathLibrary);
        // THIS MAY HAVE UNINTENDED CONSEQUENCES...
        if (storyPath.getStoryPathLibraryFile() == null) {
            storyPath.setStoryPathLibraryFile(storyPathLibrary.getFileLocation());

        }

        storyPath.setContext(context);

        // need to reset visibility so that visible cards will update their status properly
        storyPath.resetVisibility();

        return storyPath;

    }

    public static String saveStoryPath(StoryPath storyPath) {

        Log.e(" *** TESTING *** ", "NEW METHOD saveStoryPath CALLED");

        Date timeStamp = new Date();
        String jsonFilePath = storyPath.buildPath(storyPath.getId() + "_" + timeStamp.getTime() + ".json");

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File storyPathFile = new File(jsonFilePath);
                FileOutputStream storyPathStream = new FileOutputStream(storyPathFile);
                if (!storyPathFile.exists()) {
                    storyPathFile.createNewFile();
                }

                String storyPathJson = serializeStoryPath(storyPath);

                byte storyPathData[] = storyPathJson.getBytes();
                storyPathStream.write(storyPathData);
                storyPathStream.flush();
                storyPathStream.close();
            } catch (IOException ioe) {
                Log.e(TAG, "writing json file " + jsonFilePath + " to SD card failed: " + ioe.getMessage());
                return null;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return null;
        }

        // update file location
        storyPath.setFileLocation(jsonFilePath);

        return jsonFilePath;

    }

    public static String serializeStoryPath(StoryPath storyPath) {

        Log.e(" *** TESTING *** ", "NEW METHOD serializeStoryPath CALLED");

        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.create();

        // set aside references to prevent circular dependencies when serializing
        Context tempContext = storyPath.getContext();
        StoryPathLibrary tempStoryPathLibrary = storyPath.getStoryPathLibraryReference();
        //ArrayList<Card> tempValidCards = storyPath.getValidCards();
        storyPath.setContext(null);
        storyPath.setStoryPathLibraryReference(null);
        //storyPath.setValidCards(null);
        storyPath.clearObservers();
        storyPath.clearCardReferences();
        //storyPath.clearValidCards();

        String storyPathJson = gson.toJson(storyPath);

        // restore references
        // valid cards will be reset next time getValidCards() is called
        storyPath.setCardReferences();
        storyPath.initializeObservers();
        //storyPath.setValidCards(tempValidCards);
        storyPath.setStoryPathLibraryReference(tempStoryPathLibrary);
        storyPath.setContext(tempContext);

        return storyPathJson;

    }

}