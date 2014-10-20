package scal.io.liger;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import scal.io.liger.model.Card;
import scal.io.liger.model.Dependency;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;
import scal.io.liger.model.VideoCaptureTypeCard;

/**
 * Created by mnbogner on 7/10/14.
 *
 * USAGE:
 *
 * GsonBuilder gBuild = new GsonBuilder();
 * gBuild.registerTypeAdapter(StoryPathModel.class, new StoryPathDeserializer());
 * Gson gson = gBuild.create();
 *
 * String json = loadJSON("story_path_1.json");
 * StoryPathModel spm = gson.fromJson(json, StoryPathModel.class);
 *
 * System.out.println("TEST: " + gson.toJson(spm));
 */
public class StoryPathLibraryDeserializer implements JsonDeserializer<StoryPathLibrary>{

    @Override
    public StoryPathLibrary deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        StoryPathLibrary spl = new StoryPathLibrary();

        JsonObject jObj = jsonElement.getAsJsonObject();

        String id = jObj.get("id").getAsString();
        String title = jObj.get("title").getAsString();
        String class_package = jObj.get("classPackage").getAsString();

        spl.setId(id);
        spl.setTitle(title);
        spl.setClassPackage(class_package);

        JsonElement tempElement = null;

        tempElement = jObj.get("currentStoryPathFile");
        if (tempElement != null) {
            String currentStoryPathFile = tempElement.getAsString();
            spl.setCurrentStoryPathFile(currentStoryPathFile);
        }

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(MilestoneCardDeserializer.class, new MilestoneCardDeserializer());
        gBuild.registerTypeAdapter(VideoCaptureTypeCard.class, new VideoCaptureTypeCardDeserializer());
        Gson gson = gBuild.create();

        JsonObject tempObj = null;

        HashMap<String, String> storyPathTemplateFiles = new HashMap<String, String>();
        tempElement = jObj.get("storyPathTemplateFiles");
        if (tempElement != null) {
            tempObj = tempElement.getAsJsonObject();
            storyPathTemplateFiles = gson.fromJson(tempObj, storyPathTemplateFiles.getClass());
            spl.setStoryPathTemplateFiles(storyPathTemplateFiles);
        }

        ArrayList<String> storyPathInstanceFiles = new ArrayList<String>();
        tempElement = jObj.get("storyPathInstanceFiles");
        if (tempElement != null) {
            tempObj = tempElement.getAsJsonObject();
            storyPathInstanceFiles = gson.fromJson(tempObj, storyPathInstanceFiles.getClass());
            spl.setStoryPathInstanceFiles(storyPathInstanceFiles);
        }

        HashMap<String, MediaFile> mediaFiles = new HashMap<String, MediaFile>();
        tempElement = jObj.get("mediaFiles");
        if (tempElement != null) {
            tempObj = tempElement.getAsJsonObject();
            mediaFiles = gson.fromJson(tempObj, new TypeToken<HashMap<String, MediaFile>>(){}.getType());
            spl.setMediaFiles(mediaFiles);
        }

        JsonElement jEle = jObj.get("dependencies");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                Dependency dependency = (gson.fromJson(arrObj, Dependency.class));
                spl.addDependency(dependency);
            }
        }

        jEle = jObj.get("cards");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                String cardType = class_package + "." + arrObj.get("type").getAsString();
                try {
                    Class cardClass = Class.forName(cardType);
                    Card card = (Card)(gson.fromJson(arrObj, cardClass));
                    spl.addCard(card);
                } catch (ClassNotFoundException e) {
                    System.err.println("MODEL CLASS NOT FOUND FOR CARD TYPE: " + cardType);
                }
            }
        }

        return spl;
    }
}
