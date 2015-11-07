package scal.io.liger;

import timber.log.Timber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import scal.io.liger.model.Link;
import scal.io.liger.model.MilestoneCard;

/**
 * Created by mnbogner on 7/11/14.
 *
 * USAGE:
 *
 * GsonBuilder gBuild = new GsonBuilder();
 * gBuild.registerTypeAdapter(VideoCaptureTypeCardModel.class, new VideoCaptureTypeCardDeserializer());
 * Gson gson = gBuild.create();
 */
public class MilestoneCardDeserializer implements JsonDeserializer<MilestoneCard> {
    @Override
    public MilestoneCard deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        MilestoneCard nucm = new MilestoneCard();

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
                Link link = (gson.fromJson(arrObj, Link.class));
                nucm.addLink(link);
            }
        }

        return nucm;
    }
}
