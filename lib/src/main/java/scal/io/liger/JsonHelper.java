package scal.io.liger;

import android.content.Context;
import android.content.res.AssetManager;
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
import java.util.HashMap;

import scal.io.liger.model.Dependency;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;

/**
 * @author Matt Bogner
 * @author Josh Steiner
 */
public class JsonHelper {
    private static final String TAG = "JsonHelper";
    private static final String LIGER_DIR = "Liger";
    private static File selectedJSONFile = null;
    private static String selectedJSONPath = null;
    private static ArrayList<String> jsonFileNamesList = null;
    private static ArrayList<File> jsonFileList = null;
    private static ArrayList<String> jsonPathList = null;
    private static HashMap<String, String> jsonKeyToPath = null;
    private static String sdLigerFilePath = null;

    //private static String language = null; // TEMP

    // TEMP - for gathering insance files to test references
    public static ArrayList<String> getInstanceFiles() {
        ArrayList<String> instanceList = new ArrayList<String>();
        for (String s : jsonPathList) {
            if (s.contains("-instance")) {
                instanceList.add(s);
                Log.d("FILES", "FOUND INSTANCE FILE: " + s);
            }
        }
        return instanceList;
    }

    public static String loadJSONFromPath(String jsonPath, String language) {

        String jsonString = "";
        String sdCardState = Environment.getExternalStorageState();

        String localizedFilePath = jsonPath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonPath.lastIndexOf("-" + language + jsonPath.substring(jsonPath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonPath.substring(0, jsonPath.lastIndexOf(".")) + "-" + language + jsonPath.substring(jsonPath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadJSONFromPath() - LOCALIZED PATH: " + localizedFilePath);
        }

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File jsonFile = new File(jsonPath);
                InputStream jsonStream = new FileInputStream(jsonFile);

                File localizedFile = new File(localizedFilePath);
                // if there is a file at the localized path, use that instead
                if ((localizedFile.exists()) && (!jsonPath.equals(localizedFilePath))) {
                    Log.d("LANGUAGE", "loadJSONFromPath() - USING LOCALIZED FILE: " + localizedFilePath);
                    jsonStream = new FileInputStream(localizedFile);
                }

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

    public static String loadJSON(Context context, String language) {
        // check for file
        // paths to actual files should fully qualified
        // paths within zip files should be relative
        // (or at least not resolve to actual files)
        File checkFile = new File(selectedJSONPath);
        //if (selectedJSONFile.exists()) {
        //    return loadJSON(selectedJSONFile, language);
        //} else {
        if (checkFile.exists()) {
            return loadJSON(checkFile, language);
        } else {
            return loadJSONFromZip(selectedJSONPath, context, language);
        }
    }

    public static String loadJSON(File file, String language) {
        if(null == file) {
            return null;
        }

        String jsonString = "";
        String sdCardState = Environment.getExternalStorageState();

        String localizedFilePath = file.getPath();

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (file.getPath().lastIndexOf("-" + language + file.getPath().substring(file.getPath().lastIndexOf("."))) < 0) {
                localizedFilePath = file.getPath().substring(0, file.getPath().lastIndexOf(".")) + "-" + language + file.getPath().substring(file.getPath().lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadJSON() - LOCALIZED PATH: " + localizedFilePath);
        }

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                InputStream jsonStream = new FileInputStream(file);

                File localizedFile = new File(localizedFilePath);
                // if there is a file at the localized path, use that instead
                if ((localizedFile.exists()) && (!file.getPath().equals(localizedFilePath))) {
                    Log.d("LANGUAGE", "loadJSON() - USING LOCALIZED FILE: " + localizedFilePath);
                    jsonStream = new FileInputStream(localizedFile);
                }

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

    // NEW

    // merged with regular loadJSON method
    /*
    public static String loadJSONFromZip(Context context, String language) {
        return loadJSONFromZip(selectedJSONPath, context, language);
    }
    */

    public static String loadJSONFromZip(String jsonFilePath, Context context, String language) {

        Log.d(" *** TESTING *** ", "NEW METHOD loadJSONFromZip CALLED FOR " + jsonFilePath);

        if(null == jsonFilePath) {
            return null;
        }

        String jsonString = "";

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadJSONFromZip() - LOCALIZED PATH: " + localizedFilePath);
        }

        // removed sd card check as expansion file should not be located on sd card
            try {
                InputStream jsonStream = ZipHelper.getFileInputStream(localizedFilePath, context);

                // if there is no result with the localized path, retry with default path
                if ((jsonStream == null) && (!jsonFilePath.equals(localizedFilePath))) {
                    jsonStream = ZipHelper.getFileInputStream(jsonFilePath, context);
                } else {
                    Log.d("LANGUAGE", "loadJSONFromZip() - USING LOCALIZED FILE: " + localizedFilePath);
                }

                if (jsonStream == null)
                    return null;

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonString = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from ZIP file failed: " + ioe.getMessage());
            }

        return jsonString;
    }

    public static String getSdLigerFilePath() {
        return sdLigerFilePath;
    }

    private static void copyFilesToSdCard(Context context, String basePath) {
        copyFileOrDir(context, basePath, ""); // copy all files in assets folder in my project
    }

    private static void copyFileOrDir(Context context, String assetFromPath, String baseToPath) {
        AssetManager assetManager = context.getAssets();
        String assets[] = null;
        try {
            Log.i("tag", "copyFileOrDir() "+assetFromPath);
            assets = assetManager.list(assetFromPath);
            if (assets.length == 0) {
                copyFile(context, assetFromPath, baseToPath);
            } else {
                String fullPath =  baseToPath + assetFromPath;
                Log.i("tag", "path="+fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !assetFromPath.startsWith("images") && !assetFromPath.startsWith("sounds") && !assetFromPath.startsWith("webkit"))
                    if (!dir.mkdirs())
                        Log.i("tag", "could not create dir "+fullPath);
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (assetFromPath.equals(""))
                        p = "";
                    else
                        p = assetFromPath + "/";

                    if (!assetFromPath.startsWith("images") && !assetFromPath.startsWith("sounds") && !assetFromPath.startsWith("webkit"))
                        copyFileOrDir(context, p + assets[i], baseToPath);
                }
            }
        } catch (IOException ex) {
            Log.e("tag", "I/O Exception", ex);
        }
    }

    private static void copyFile(Context context, String fromFilename, String baseToPath) {
        AssetManager assetManager = context.getAssets();

        if (fromFilename.endsWith(".obb"))
            return;  // let's not copy .obb files, they get copied elseware

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Log.i("tag", "copyFile() "+fromFilename);
            in = assetManager.open(fromFilename);
            if (fromFilename.endsWith(".jpg")) // extension was added to avoid compression on APK file
                newFileName = baseToPath + fromFilename.substring(0, fromFilename.length()-4);
            else
                newFileName = baseToPath + fromFilename;
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", "Exception in copyFile() of "+newFileName);
            Log.e("tag", "Exception in copyFile() "+e.toString());
        }

    }

    private static void copyObbFile(Context context, String fromFilename, String toPath) {
        AssetManager assetManager = context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            Log.i("tag", "copyObbFile() "+fromFilename);
            in = assetManager.open(fromFilename);
            out = new FileOutputStream(toPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e("tag", "Exception in copyObbFile() of "+toPath);
            Log.e("tag", "Exception in copyObbFile() "+e.toString());
        }

    }

    public static void setupFileStructure(Context context) {
        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            // FIXME we need to remove this, it seems like the popup stuff requires it even though we acutally read files from .obb not the Liger folder
            String sdCardFolderPath = Environment.getExternalStorageDirectory().getPath();
            sdLigerFilePath = sdCardFolderPath + File.separator + LIGER_DIR + File.separator;
            new File(sdLigerFilePath).mkdirs();
            // based on http://stackoverflow.com/questions/4447477/android-how-to-copy-files-from-assets-folder-to-sdcard/8366081#8366081
//            copyFilesToSdCard(context, sdLigerFilePath); // this used to copy all assets to /sdcard/Liger

            // copy the zipped assets to the right folder
            String inputAssetFilename = "main.1.obb"; // FIXME we should parse this out from the assets so its always right
            String zipPath = ZipHelper.getExtensionFolderPath(context);
            String zipFilename = ZipHelper.getExtensionZipFilename(context, inputAssetFilename);
            String zipFullpath = zipPath + zipFilename;

            Log.d("JsonHelper", "copying obb file '" + inputAssetFilename + "' to '" + zipFullpath + "'");
            new File(zipPath).mkdirs();
            // FIXME check size & datestamp and don't copy if it exists?  maybe we can check hash? key?
            copyObbFile(context, inputAssetFilename, zipFullpath);
        } else {
            Log.e(TAG, "SD CARD NOT FOUND"); // FIXME don't bury errors in logs, we should let this crash
        }
    }

    private static void setupJSONFileList() {

        // HARD CODING LIST
        if (jsonFileNamesList == null) {
            jsonFileNamesList = new ArrayList<String>();
            File ligerFile_1 = new File(sdLigerFilePath + "/default/default_library/default_library.json");
            File ligerFile_2 = new File(sdLigerFilePath + "/default/learning_guide_TEST.json");
            File ligerFile_3 = new File(sdLigerFilePath + "/default/LIB_1/LIB_1_TEST.json");
            File ligerFile_4 = new File(sdLigerFilePath + "/default/LIB_2/LIB_2_TEST.json");
            File ligerFile_5 = new File(sdLigerFilePath + "/default/learning_guide_library.json");
            File ligerFile_6 = new File(sdLigerFilePath + "/default/learning_guide_library_SAVE.json");

            jsonFileNamesList.add(ligerFile_1.getName());
            jsonFileNamesList.add(ligerFile_2.getName());
            jsonFileNamesList.add(ligerFile_3.getName());
            jsonFileNamesList.add(ligerFile_4.getName());
            jsonFileNamesList.add(ligerFile_5.getName());
            jsonFileNamesList.add(ligerFile_6.getName());

            jsonFileList = new ArrayList<File>();
            jsonFileList.add(ligerFile_1);
            jsonFileList.add(ligerFile_2);
            jsonFileList.add(ligerFile_3);
            jsonFileList.add(ligerFile_4);
            jsonFileList.add(ligerFile_5);
            jsonFileList.add(ligerFile_6);

            jsonPathList = new ArrayList<String>();
            jsonPathList.add("default/default_library/default_library.json");
            jsonPathList.add("default/learning_guide_TEST.json");
            jsonPathList.add("default/LIB_1/LIB_1_TEST.json");
            jsonPathList.add("default/LIB_2/LIB_2_TEST.json");
            jsonPathList.add("default/learning_guide_library.json");
            jsonPathList.add("default/learning_guide_library_SAVE.json");

            jsonKeyToPath = new HashMap<String, String>();
            jsonKeyToPath.put("default_library", "default/default_library/default_library.json");
            jsonKeyToPath.put("learning_guide_TEST", "default/learning_guide_TEST.json");
            jsonKeyToPath.put("LIB_1_TEST", "default/LIB_1/LIB_1_TEST.json");
            jsonKeyToPath.put("LIB_2_TEST", "default/LIB_2/LIB_2_TEST.json");
            jsonKeyToPath.put("learning_guide_library", "default/learning_guide_library.json");
            jsonKeyToPath.put("learning_guide_library_SAVE", "default/learning_guide_library_SAVE.json");

            File jsonFolder = new File(getSdLigerFilePath());
            // check for nulls (uncertain as to cause of nulls)
            if ((jsonFolder != null) && (jsonFolder.listFiles() != null)) {
                for (File jsonFile : jsonFolder.listFiles()) {
                    if (jsonFile.getName().contains("-instance") && !jsonFile.isDirectory()) {
                        File localFile = new File(jsonFile.getPath());
                        Log.d("FILES", "FOUND INSTANCE FILE: " + localFile.getName());
                        jsonFileNamesList.add(localFile.getName());
                        jsonFileList.add(localFile);
                        jsonPathList.add(localFile.getPath());
                        jsonKeyToPath.put(localFile.getName(), localFile.getPath());
                    }
                }
            } else {
                Log.d("FILES", getSdLigerFilePath() + " WAS NULL OR listFiles() RETURNED NULL, CANNOT GATHER INSTANCE FILES");
            }
        }
    }

    public static String[] getJSONFileList() {
        //ensure path has been set
        if(null == sdLigerFilePath) {
            return null;
        }

        setupJSONFileList();

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

    public static String getJsonPathByKey(String key) {
        setupJSONFileList();
        if (jsonKeyToPath.containsKey(key)) {
            return jsonKeyToPath.get(key);
        } else {
            return null;
        }
    }

    public static File setSelectedJSONFile(int index) {
        selectedJSONFile = jsonFileList.get(index);
        return selectedJSONFile;
    }

    public static String setSelectedJSONPath(int index) {
        selectedJSONPath = jsonPathList.get(index);
        return selectedJSONPath;
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

    public static StoryPathLibrary loadStoryPathLibrary(String jsonFilePath, ArrayList<String> referencedFiles, Context context, String language) {

        //Log.d(" *** TESTING *** ", "NEW METHOD loadStoryPathLibrary CALLED FOR " + jsonFilePath);

        String storyPathLibraryJson = "";
        String sdCardState = Environment.getExternalStorageState();

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadStoryPathLibrary() - LOCALIZED PATH: " + localizedFilePath);
        }

        File f = new File(localizedFilePath);
        if ((!f.exists()) && (!localizedFilePath.equals(jsonFilePath))) {
            f = new File(jsonFilePath);
        } else {
            Log.d("LANGUAGE", "loadStoryPathLibrary() - USING LOCALIZED FILE: " + localizedFilePath);
        }

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

        return deserializeStoryPathLibrary(storyPathLibraryJson, f.getPath(), referencedFiles, context);

    }

    // NEW

    public static StoryPathLibrary loadStoryPathLibraryFromZip(String jsonFilePath, ArrayList<String> referencedFiles, Context context, String language) {

        Log.d(" *** TESTING *** ", "NEW METHOD loadStoryPathLibraryFromZip CALLED FOR " + jsonFilePath);

        String storyPathLibraryJson = "";

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadStoryPathLibraryFromZip() - LOCALIZED PATH: " + localizedFilePath);
        }

        // removed sd card check as expansion file should not be located on sd card
            try {
                InputStream jsonStream = ZipHelper.getFileInputStream(localizedFilePath, context);

                // if there is no result with the localized path, retry with default path
                if ((jsonStream == null) && (!localizedFilePath.equals(jsonFilePath))) {
                    jsonStream = ZipHelper.getFileInputStream(jsonFilePath, context);
                } else {
                    Log.d("LANGUAGE", "loadStoryPathLibraryFromZip() - USING LOCALIZED FILE: " + localizedFilePath);
                }

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                storyPathLibraryJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from ZIP file failed: " + ioe.getMessage());
                return null;
            }

        return deserializeStoryPathLibrary(storyPathLibraryJson, jsonFilePath, referencedFiles, context);
    }

    public static StoryPathLibrary deserializeStoryPathLibrary(String storyPathLibraryJson, String jsonFilePath, ArrayList<String> referencedFiles, Context context) {

        //Log.d(" *** TESTING *** ", "NEW METHOD deserializeStoryPathLibrary CALLED FOR " + jsonFilePath);

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPathLibrary.class, new StoryPathLibraryDeserializer());
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        StoryPathLibrary storyPathLibrary = gson.fromJson(storyPathLibraryJson, StoryPathLibrary.class);

        // a story path library model must have a file location to manage relative paths
        // if it is loaded from a saved state, the location should already be set
        if ((jsonFilePath == null) || (jsonFilePath.length() == 0)) {
            if ((storyPathLibrary.getFileLocation() == null) || (storyPathLibrary.getFileLocation().length() == 0)) {
                Log.e(TAG, "file location for story path library " + storyPathLibrary.getId() + " could not be determined");
                return null;
            }
        } else {
            File checkFile = new File(jsonFilePath);
            if (!checkFile.exists()) {
                storyPathLibrary.setFileLocation(jsonFilePath); // FOR NOW, DON'T SAVE ACTIAL FILE LOCATIONS
            }
        }

        // construct and insert dependencies
        for (String referencedFile : referencedFiles) {
            Dependency dependency = new Dependency();
            dependency.setDependencyFile(referencedFile);

            // extract id from path/file name
            // assumes format <path/library id>-instance-<timestamp>.json
            // assumes path/library id doesn't not contain "-"
            String derivedId = referencedFile.substring(referencedFile.lastIndexOf(File.separator) + 1);
            derivedId = derivedId.substring(0, derivedId.indexOf("-"));
            dependency.setDependencyId(derivedId);
            storyPathLibrary.addDependency(dependency);
            Log.d("FILES", "DEPENDENCY: " + derivedId + " -> " + referencedFile);
        }

        storyPathLibrary.setCardReferences();
        storyPathLibrary.initializeObservers();
        storyPathLibrary.setContext(context);

        return storyPathLibrary;
    }

    public static String getStoryPathLibrarySaveFileName(StoryPathLibrary storyPathLibrary) {

        Log.d(" *** TESTING *** ", "NEW METHOD getStoryPathLibrarySaveFileName CALLED FOR " + storyPathLibrary.getId());

        Date timeStamp = new Date();
        //String jsonFilePath = storyPathLibrary.buildZipPath(storyPathLibrary.getId() + "_" + timeStamp.getTime() + ".json");
        //TEMP
        String jsonFilePath = storyPathLibrary.buildTargetPath(storyPathLibrary.getId() + "-instance-" + timeStamp.getTime() + ".json");

        return jsonFilePath;
    }

    public static boolean saveStoryPathLibrary(StoryPathLibrary storyPathLibrary, String jsonFilePath) {

        Log.d(" *** TESTING *** ", "NEW METHOD saveStoryPathLibrary CALLED FOR " + storyPathLibrary.getId() + " -> " + jsonFilePath);

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File storyPathLibraryFile = new File(jsonFilePath + ".swap"); // NEED TO WRITE TO SWAP AND COPY
                if (storyPathLibraryFile.exists()) {
                    storyPathLibraryFile.delete();
                }
                storyPathLibraryFile.createNewFile();
                FileOutputStream storyPathLibraryStream = new FileOutputStream(storyPathLibraryFile);

                String storyPathLibraryJson = serializeStoryPathLibrary(storyPathLibrary);

                byte storyPathLibraryData[] = storyPathLibraryJson.getBytes();
                storyPathLibraryStream.write(storyPathLibraryData);
                storyPathLibraryStream.flush();
                storyPathLibraryStream.close();

                Process p = Runtime.getRuntime().exec("mv " + jsonFilePath + ".swap " + jsonFilePath);

            } catch (IOException ioe) {
                Log.e(TAG, "writing json file " + jsonFilePath + " to SD card failed: " + ioe.getMessage());
                return false;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return false;
        }

        // update file location
        // TEMP - this will break references to content in zip file.  unsure what to do...
        // storyPathLibrary.setFileLocation(jsonFilePath);

        return true;
    }

    public static String serializeStoryPathLibrary(StoryPathLibrary storyPathLibrary) {

        Log.d(" *** TESTING *** ", "NEW METHOD serializeStoryPathLibrary CALLED FOR " + storyPathLibrary.getId());

        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        String storyPathLibraryJson = gson.toJson(storyPathLibrary);

        return storyPathLibraryJson;
    }

    public static StoryPath loadStoryPath(String jsonFilePath, StoryPathLibrary storyPathLibrary, ArrayList<String> referencedFiles, Context context, String language) {

        //Log.d(" *** TESTING *** ", "NEW METHOD loadStoryPath CALLED FOR " + jsonFilePath);

        String storyPathJson = "";
        String sdCardState = Environment.getExternalStorageState();

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadStoryPath() - LOCALIZED PATH: " + localizedFilePath);
        }

        File f = new File(localizedFilePath);
        if ((!f.exists()) && (!localizedFilePath.equals(jsonFilePath))) {
            f = new File(jsonFilePath);
        } else {
            Log.d("LANGUAGE", "loadStoryPath() - USING LOCALIZED FILE: " + localizedFilePath);
        }

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

        return deserializeStoryPath(storyPathJson, f.getPath(), storyPathLibrary, referencedFiles, context);
    }

    // NEW

    public static StoryPath loadStoryPathFromZip(String jsonFilePath, StoryPathLibrary storyPathLibrary, ArrayList<String> referencedFiles, Context context, String language) {

        Log.d(" *** TESTING *** ", "NEW METHOD loadStoryPathFromZip CALLED FOR " + jsonFilePath);

        String storyPathJson = "";

        String localizedFilePath = jsonFilePath;

        // check language setting and insert country code if necessary
        if (language != null) {
            // just in case, check whether country code has already been inserted
            if (jsonFilePath.lastIndexOf("-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."))) < 0) {
                localizedFilePath = jsonFilePath.substring(0, jsonFilePath.lastIndexOf(".")) + "-" + language + jsonFilePath.substring(jsonFilePath.lastIndexOf("."));
            }
            Log.d("LANGUAGE", "loadStoryPathFromZip() - LOCALIZED PATH: " + localizedFilePath);
        }

        // removed sd card check as expansion file should not be located on sd card
            try {
                InputStream jsonStream = ZipHelper.getFileInputStream(localizedFilePath, context);

                // if there is no result with the localized path, retry with default path
                if ((jsonStream == null) && (!localizedFilePath.equals(jsonFilePath))) {
                    jsonStream = ZipHelper.getFileInputStream(jsonFilePath, context);
                } else {
                    Log.d("LANGUAGE", "loadStoryPathFromZip() - USING LOCALIZED FILE: " + localizedFilePath);
                }

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                storyPathJson = new String(buffer);
            } catch (IOException ioe) {
                Log.e(TAG, "reading json file " + jsonFilePath + " from ZIP file failed: " + ioe.getMessage());
                return null;
            }

        return deserializeStoryPath(storyPathJson, jsonFilePath, storyPathLibrary, referencedFiles, context);
    }

    public static StoryPath deserializeStoryPath(String storyPathJson, String jsonFilePath, StoryPathLibrary storyPathLibrary, ArrayList<String> referencedFiles, Context context) {

        //Log.d(" *** TESTING *** ", "NEW METHOD deserializeStoryPath CALLED FOR " + jsonFilePath);

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(StoryPath.class, new StoryPathDeserializer());
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        StoryPath storyPath = gson.fromJson(storyPathJson, StoryPath.class);

        // a story path model must have a file location to manage relative paths
        // if it is loaded from a saved state, the location should already be set
        if ((jsonFilePath == null) || (jsonFilePath.length() == 0)) {
            if ((storyPath.getFileLocation() == null) || (storyPath.getFileLocation().length() == 0)) {
                Log.e(TAG, "file location for story path " + storyPath.getId() + " could not be determined");
            }
        } else {
            File checkFile = new File(jsonFilePath);
            if (!checkFile.exists()) {
                storyPath.setFileLocation(jsonFilePath); // FOR NOW, DON'T SAVE ACTIAL FILE LOCATIONS
            }
        }

        storyPath.setCardReferences();
        storyPath.initializeObservers();
        storyPath.setStoryPathLibrary(storyPathLibrary);
        // THIS MAY HAVE UNINTENDED CONSEQUENCES...
        if (storyPath.getStoryPathLibraryFile() == null) {
            storyPath.setStoryPathLibraryFile(storyPathLibrary.getFileLocation());

        }

        // construct and insert dependencies
        for (String referencedFile : referencedFiles) {
            Dependency dependency = new Dependency();
            dependency.setDependencyFile(referencedFile);

            // extract id from path/file name
            // assumes format <path/library id>-instance-<timestamp>.json
            // assumes path/library id doesn't not contain "-"
            String derivedId = referencedFile.substring(referencedFile.lastIndexOf(File.separator) + 1);
            derivedId = derivedId.substring(0, derivedId.indexOf("-"));
            dependency.setDependencyId(derivedId);
            storyPath.addDependency(dependency);
            Log.d("FILES", "DEPENDENCY: " + derivedId + " -> " + referencedFile);
        }

        storyPath.setContext(context);

        return storyPath;
    }

    public static String getStoryPathSaveFileName(StoryPath storyPath) {

        Log.d(" *** TESTING *** ", "NEW METHOD getStoryPathSaveFileName CALLED FOR " + storyPath.getId());

        Date timeStamp = new Date();
        //String jsonFilePath = storyPath.buildZipPath(storyPath.getId() + "_" + timeStamp.getTime() + ".json");
        //TEMP
        String jsonFilePath = storyPath.buildTargetPath(storyPath.getId() + "-instance-" + timeStamp.getTime() + ".json");

        return jsonFilePath;
    }

    public static boolean saveStoryPath(StoryPath storyPath, String jsonFilePath) {

        Log.d(" *** TESTING *** ", "NEW METHOD getStoryPathSaveFileName CALLED FOR " + storyPath.getId() + " -> " + jsonFilePath);

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                File storyPathFile = new File(jsonFilePath + ".swap"); // NEED TO WRITE TO SWAP AND COPY
                if (storyPathFile.exists()) {
                    storyPathFile.delete();
                }
                storyPathFile.createNewFile();
                FileOutputStream storyPathStream = new FileOutputStream(storyPathFile);

                String storyPathJson = serializeStoryPath(storyPath);

                byte storyPathData[] = storyPathJson.getBytes();
                storyPathStream.write(storyPathData);
                storyPathStream.flush();
                storyPathStream.close();

                Process p = Runtime.getRuntime().exec("mv " + jsonFilePath + ".swap " + jsonFilePath);

            } catch (IOException ioe) {
                Log.e(TAG, "writing json file " + jsonFilePath + " to SD card failed: " + ioe.getMessage());
                return false;
            }
        } else {
            Log.e(TAG, "SD card not found");
            return false;
        }

        // update file location
        // TEMP - this will break references to content in zip file.  unsure what to do...
        // storyPath.setFileLocation(jsonFilePath);

        return true;
    }

    public static String serializeStoryPath(StoryPath storyPath) {

        Log.d(" *** TESTING *** ", "NEW METHOD serializeStoryPath CALLED FOR " + storyPath.getId());

        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        // set aside references to prevent circular dependencies when serializing
        /*
        Context tempContext = storyPath.getContext();

        StoryPathLibrary tempStoryPathLibrary = storyPath.getStoryPathLibrary();
        storyPath.setContext(null);
        storyPath.setStoryPathLibrary(null);
        storyPath.clearObservers();
        storyPath.clearCardReferences();
        */

        String storyPathJson = gson.toJson(storyPath);

        // restore references
        /*
        storyPath.setCardReferences();
        storyPath.initializeObservers();
        storyPath.setStoryPathLibrary(tempStoryPathLibrary);
        storyPath.setContext(tempContext);
        */

        return storyPathJson;
    }

}
