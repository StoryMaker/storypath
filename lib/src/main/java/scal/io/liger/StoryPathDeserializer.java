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
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import scal.io.liger.model.Card;
import scal.io.liger.model.Dependency;
import scal.io.liger.model.PublishProfile;
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
public class StoryPathDeserializer implements JsonDeserializer<StoryPath> {
    private static final String TAG = "StoryPathDeserializer";

    @Override
    public StoryPath deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        StoryPath spm = new StoryPath();
        boolean errorFlag = false;

        JsonObject jObj = jsonElement.getAsJsonObject();

        String id = jObj.get("id").getAsString();
        String title = jObj.get("title").getAsString();
        String class_package = jObj.get("classPackage").getAsString();

        spm.setId(id);
        spm.setTitle(title);
        spm.setClassPackage(class_package);

        JsonElement tempElement = null;

        tempElement = jObj.get("fileLocation");
        if (tempElement != null) {
            String fileLocation = jObj.get("fileLocation").getAsString();
            spm.setFileLocation(fileLocation);
        }

        tempElement = jObj.get("savedFileName");
        if (tempElement != null) {
            String savedFileName = jObj.get("savedFileName").getAsString();
            spm.setSavedFileName(savedFileName);
        }

        tempElement = jObj.get("storyPathLibraryFile");
        if (tempElement != null) {
            String storyPathLibraryFile = jObj.get("storyPathLibraryFile").getAsString();
            spm.setStoryPathLibraryFile(storyPathLibraryFile);
        }

        tempElement = jObj.get("language");
        if (tempElement != null) {
            String language = jObj.get("language").getAsString();
            spm.setLanguage(language);
        }

        tempElement = jObj.get("version");
        if (tempElement != null) {
            int version = jObj.get("version").getAsInt();
            spm.setVersion(version);
        }

        tempElement = jObj.get("templatePath");
        if (tempElement != null) {
            String templatePath = jObj.get("templatePath").getAsString();
            spm.setTemplatePath(templatePath);
        }

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(MilestoneCardDeserializer.class, new MilestoneCardDeserializer());
        gBuild.registerTypeAdapter(VideoCaptureTypeCard.class, new VideoCaptureTypeCardDeserializer());
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        JsonElement jEle = jObj.get("dependencies");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                try {
                    JsonObject arrObj = jArr.get(i).getAsJsonObject();
                    Dependency dependency = (gson.fromJson(arrObj, Dependency.class));
                    spm.addDependency(dependency);
                } catch (IllegalStateException ise) {
                    System.err.println("ILLEGAL STATE WHILE PROCESSING \"dependencies\" (#" + i + ") -> " + ise.getMessage());
                    errorFlag = true;
                } catch (JsonSyntaxException jse) {
                    System.err.println("JSON SYNTAX ERROR WHILE PROCESSING \"dependencies\" (#" + i + ") -> " + jse.getMessage());
                    errorFlag = true;
                }
            }
        }

        jEle = jObj.get("cards");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                String cardType = arrObj.get("type").getAsString();
                String fqCardType;
                if (cardType.contains(".")) {
                    Log.d(TAG, "StoryPath JSON contains fully qualitfied card type: " + cardType);
                    fqCardType = cardType;
                } else {
                    fqCardType = class_package + "." + cardType;
                }
                try {
                    Class cardClass = Class.forName(fqCardType);
                    Card card = (Card)(gson.fromJson(arrObj, cardClass));
                    spm.addCard(card);
                } catch (ClassNotFoundException e) {
                    System.err.println("MODEL CLASS NOT FOUND FOR CARD TYPE: " + fqCardType);
                    errorFlag = true;
                } catch (IllegalStateException ise) {
                    System.err.println("ILLEGAL STATE WHILE PROCESSING CARD TYPE: " + fqCardType + " -> " + ise.getMessage());
                    errorFlag = true;
                } catch (JsonSyntaxException jse) {
                    System.err.println("JSON SYNTAX ERROR WHILE PROCESSING CARD TYPE: " + fqCardType + " -> " + jse.getMessage());
                    errorFlag = true;
                }
            }
        }

        // don't want to return incomplete models
        if (errorFlag) {
            throw new JsonParseException("encountered cards with no corresponding model");
        }

        return spm;
    }
}