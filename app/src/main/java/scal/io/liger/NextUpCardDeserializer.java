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

import scal.io.liger.model.LinkModel;
import scal.io.liger.model.NextUpCardModel;

/**
 * Created by mnbogner on 7/11/14.
 *
 * USAGE:
 *
 * GsonBuilder gBuild = new GsonBuilder();
 * gBuild.registerTypeAdapter(VideoCaptureTypeCardModel.class, new VideoCaptureTypeCardDeserializer());
 * Gson gson = gBuild.create();
 */
public class NextUpCardDeserializer implements JsonDeserializer<NextUpCardModel> {
    @Override
    public NextUpCardModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        NextUpCardModel nucm = new NextUpCardModel();

        JsonObject jObj = jsonElement.getAsJsonObject();

        String id = jObj.get("id").getAsString();
        String title = jObj.get("title").getAsString();

        nucm.setId(id);
        nucm.setTitle(title);

        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.create();

        JsonElement jEle = jObj.get("links");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                LinkModel link = (gson.fromJson(arrObj, LinkModel.class));
                nucm.addLink(link);
            }
        }

        return nucm;
    }
}
