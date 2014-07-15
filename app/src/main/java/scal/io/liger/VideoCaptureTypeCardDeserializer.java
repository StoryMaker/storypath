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
 */
public class VideoCaptureTypeCardDeserializer extends BaseDeserializer implements JsonDeserializer<VideoCaptureTypeCardModel> {
    @Override
    public VideoCaptureTypeCardModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        VideoCaptureTypeCardModel vctcm = new VideoCaptureTypeCardModel();

        JsonObject jObj = jsonElement.getAsJsonObject();

        String id = jObj.get("id").getAsString();
        String title = jObj.get("title").getAsString();
        System.out.println("GOT VALUES: "  + id + ", " + title);

        vctcm.setId(id);
        vctcm.setTitle(title);

        GsonBuilder gBuild = new GsonBuilder();
        Gson g = gBuild.create();


        JsonArray jArr = jObj.get("body").getAsJsonArray();
        for (int i = 0; i < jArr.size(); i++){
            JsonObject arrObj = jArr.get(i).getAsJsonObject();
            String widgetType = arrObj.get("type").getAsString();
            System.out.println("FOUND WIDGET: " + widgetType);

            try {
                Class widgetClass = Class.forName(widgetType);
                Object widget = g.fromJson(arrObj, widgetClass);
                vctcm.addBody(widget);
                System.out.println("WIDGET COUNT: " + vctcm.getBody().size());
            } catch (ClassNotFoundException e) {
                System.err.println("CLASS NOT FOUND: " + widgetType);
            }
        }

        System.out.println("DONE!");
        return vctcm;

    }
}
