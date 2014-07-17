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

/**
 * Created by mnbogner on 7/11/14.
 *
 * USAGE:
 *
 * GsonBuilder gBuild = new GsonBuilder();
 * gBuild.registerTypeAdapter(VideoCaptureTypeCardModel.class, new VideoCaptureTypeCardDeserializer());
 * Gson gson = gBuild.create();
 */
public class VideoCaptureTypeCardDeserializer implements JsonDeserializer<VideoCaptureTypeCardModel> {
    @Override
    public VideoCaptureTypeCardModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        VideoCaptureTypeCardModel vctcm = new VideoCaptureTypeCardModel();

        JsonObject jObj = jsonElement.getAsJsonObject();

        String id = jObj.get("id").getAsString();
        String title = jObj.get("title").getAsString();

        vctcm.setId(id);
        vctcm.setTitle(title);

        GsonBuilder gBuild = new GsonBuilder();
        Gson gson = gBuild.create();

        JsonElement jEle = jObj.get("body");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                String widgetType = arrObj.get("type").getAsString();
                try {
                    Class widgetClass = Class.forName(widgetType);
                    Object widget = gson.fromJson(arrObj, widgetClass);
                    vctcm.addBody(widget);
                } catch (ClassNotFoundException e) {
                    System.err.println("MODEL CLASS NOT FOUND FOR WIDGET TYPE: " + widgetType);
                }
            }
        }

        return vctcm;
    }
}
