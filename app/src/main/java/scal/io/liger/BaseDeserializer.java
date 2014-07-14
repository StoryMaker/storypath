package scal.io.liger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by mnbogner on 7/14/14.
 */
public class BaseDeserializer {

    public ArrayList<Object> processArray(Gson g, JsonObject jObj, String arrayName) {
        ArrayList<Object> objects = new ArrayList<Object>();
        JsonArray jArr = jObj.get(arrayName).getAsJsonArray();

        for (int i = 0; i < jArr.size(); i++) {
            JsonObject arrObj = jArr.get(i).getAsJsonObject();
            String objectType = arrObj.get("type").getAsString(); // should also be variable?
            System.out.println("FOUND OBJECT: " + objectType);

            try {
                Class objectClass = Class.forName(objectType);
                Object object = g.fromJson(arrObj, objectClass);
                objects.add(object);
                System.out.println("OBJECT COUNT: " + objects.size());
            } catch (ClassNotFoundException e) {
                System.err.println("CLASS NOT FOUND: " + objectType);
            }

        }

        return objects;
    }
}
