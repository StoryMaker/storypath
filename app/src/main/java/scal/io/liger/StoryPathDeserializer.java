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
 * String json1 = loadJSON("story_path_1.json");
 * StoryPathModel spm1 = gson.fromJson(json1, StoryPathModel.class);
 *
 * System.out.println("TEST: " + gson.toJson(spm1));
 */
public class StoryPathDeserializer implements JsonDeserializer<StoryPathModel>{

    @Override
    public StoryPathModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        StoryPathModel spm = new StoryPathModel();

        JsonObject jObj = jsonElement.getAsJsonObject();

        String id = jObj.get("id").getAsString();
        String title = jObj.get("title").getAsString();
        System.out.println("GOT VALUES: "  + id + ", " + title);

        spm.setId(id);
        spm.setTitle(title);

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(VideoCaptureTypeCardModel.class, new VideoCaptureTypeCardDeserializer());
        Gson g = gBuild.create();

        JsonArray jArr = jObj.get("cards").getAsJsonArray();
        for (int i = 0; i < jArr.size(); i++){
            JsonObject arrObj = jArr.get(i).getAsJsonObject();
            String cardType = arrObj.get("type").getAsString();
            System.out.println("FOUND CARD: " + cardType);

            try {
                Class cardClass = Class.forName(cardType);
                Object card = g.fromJson(arrObj, cardClass);
                spm.addCard(card);
                System.out.println("CARD COUNT: " + spm.getCards().size());
            } catch (ClassNotFoundException e) {
                System.err.println("CLASS NOT FOUND: " + cardType);
            }

        }

        System.out.println("DONE!");
        return spm;
    }
}
