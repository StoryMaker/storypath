package scal.io.liger;

import android.app.Activity;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mnbogner on 7/14/14.
 */
public class JsonHelper {
    public static String loadJSON(Context c, String jsonFile) {
        String jsonString = "";

        System.out.println("LOADING " + jsonFile);

        try {
            /*
            String[] assets = c.getAssets().list("");
            for (int i = 0; i < assets.length; i++) {
                System.err.println("ASSEST: " + assets[i]);
            }
            */
            InputStream jsonStream = c.getAssets().open(jsonFile);

            int size = jsonStream.available();
            byte[] buffer = new byte[size];
            jsonStream.read(buffer);
            jsonStream.close();
            jsonString = new String(buffer);
        } catch (IOException e) {
            System.err.println("READING JSON FILE FAILED: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        System.out.println("DONE");

        return jsonString;
    }
}
