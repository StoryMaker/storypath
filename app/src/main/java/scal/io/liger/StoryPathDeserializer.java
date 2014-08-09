package scal.io.liger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONObject;

import java.lang.reflect.Type;

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
public class StoryPathDeserializer implements JsonDeserializer<StoryPathModel>{

    @Override
    public StoryPathModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        StoryPathModel spm = new StoryPathModel();

        JsonObject jObj = jsonElement.getAsJsonObject();

        String id = jObj.get("id").getAsString();
        String title = jObj.get("title").getAsString();

        spm.setId(id);
        spm.setTitle(title);

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(VideoCaptureTypeCardModel.class, new VideoCaptureTypeCardDeserializer());
        Gson gson = gBuild.create();

        JsonElement jEle = jObj.get("dependencies");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                DependencyModel dependency = (gson.fromJson(arrObj, DependencyModel.class));
                spm.addDependency(dependency);
            }
        }

        jEle = jObj.get("cards");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                String cardType = arrObj.get("type").getAsString();
                try {
                    Class cardClass = Class.forName(cardType);
                    CardModel card = (CardModel)(gson.fromJson(arrObj, cardClass));
                    spm.addCard(card);
                } catch (ClassNotFoundException e) {
                    System.err.println("MODEL CLASS NOT FOUND FOR CARD TYPE: " + cardType);
                }
            }
        }

        return spm;
    }
}
