package scal.io.liger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import scal.io.liger.model.Card;
import scal.io.liger.model.Dependency;
import scal.io.liger.model.StoryPath;
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
public class StoryPathDeserializer implements JsonDeserializer<StoryPath>{

    @Override
    public StoryPath deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        StoryPath spm = new StoryPath();

        JsonObject jObj = jsonElement.getAsJsonObject();

        String id = jObj.get("id").getAsString();
        String title = jObj.get("title").getAsString();
        String class_package = jObj.get("classPackage").getAsString();

        spm.setId(id);
        spm.setTitle(title);
        spm.setClassPackage(class_package);

        JsonElement tempObj = null;

        tempObj = jObj.get("fileLocation");
        if (tempObj != null) {
            String fileLocation = jObj.get("fileLocation").getAsString();
            spm.setFileLocation(fileLocation);
        }

        tempObj = jObj.get("storyPathLibraryFile");
        if (tempObj != null) {
            String storyPathLibraryFile = jObj.get("storyPathLibraryFile").getAsString();
            spm.setStoryPathLibraryFile(storyPathLibraryFile);
        }

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(MilestoneCardDeserializer.class, new MilestoneCardDeserializer());
        gBuild.registerTypeAdapter(VideoCaptureTypeCard.class, new VideoCaptureTypeCardDeserializer());
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        JsonElement jEle = jObj.get("dependencies");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                Dependency dependency = (gson.fromJson(arrObj, Dependency.class));
                spm.addDependency(dependency);
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
                    spm.addCard(card);
                } catch (ClassNotFoundException e) {
                    System.err.println("MODEL CLASS NOT FOUND FOR CARD TYPE: " + cardType);
                }
            }
        }

        return spm;
    }
}
