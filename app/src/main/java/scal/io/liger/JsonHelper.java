package scal.io.liger;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mnbogner on 7/14/14.
 */
public class JsonHelper {
    public static String loadJSON(Context context, String jsonPath) {
        String jsonString = "";

        // check sd card first
        jsonString = loadJSONSD(context, jsonPath);

        if ((jsonString!= null) && (jsonString.length() > 0)) {
            return jsonString;
        } else {
            System.out.println("FILE " + jsonPath + " NOT FOUND ON SD CARD, TRYING INTERNAL STORAGE...");
        }

        try {
            InputStream jsonStream = context.getAssets().open(jsonPath);

            int size = jsonStream.available();
            byte[] buffer = new byte[size];
            jsonStream.read(buffer);
            jsonStream.close();
            jsonString = new String(buffer);
        } catch (IOException e) {
            System.err.println("READING JSON FILE FROM INTERNAL STORAGE FAILED: " + e.getMessage());
        }

        return jsonString;
    }

    public static String loadJSONSD(Context context, String jsonPath) {
        String jsonString = "";

        String sdCardState = Environment.getExternalStorageState();

        if (sdCardState.equals(Environment.MEDIA_MOUNTED)) {
            String sdCardFolder = Environment.getExternalStorageDirectory().getPath(); // need subdirectory for storymaker?
            String sdCardFile = sdCardFolder + File.separator + jsonPath;

            try {
                File jsonFile = new File(sdCardFile);
                InputStream jsonStream = new FileInputStream(jsonFile);

                int size = jsonStream.available();
                byte[] buffer = new byte[size];
                jsonStream.read(buffer);
                jsonStream.close();
                jsonString = new String(buffer);
            } catch (IOException e) {
                System.err.println("READING JSON FILE FRON SD CARD FAILED: " + e.getMessage());
            }
        } else {
            System.err.println("SD CARD NOT FOUND");
        }

        return jsonString;
    }
}
