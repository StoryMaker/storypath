package scal.io.liger;


import android.content.Context;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Constants {

    /** RequestCodes used for external Media Capture via
     *  {@link android.app.Activity#startActivityForResult(android.content.Intent, int)} */
    public static final int REQUEST_VIDEO_CAPTURE = 100;
    public static final int REQUEST_IMAGE_CAPTURE = 101;
    public static final int REQUEST_AUDIO_CAPTURE = 102;
    public static final int REQUEST_FILE_IMPORT   = 103;

    /** Intent extras used for external Media Capture via
     *  {@link android.app.Activity#startActivityForResult(android.content.Intent, int)} */
    public static final String EXTRA_PATH_ID         = "EXTRA_PATH_ID";
    public static final String EXTRA_FILE_LOCATION   = "FILE_LOCATION";

    public static final String PREFS_FILE = "storypath_prefs";
    public static final String PREFS_CALLING_CARD_ID = "PREFS_CALLING_CARD_ID";
    public static final String PREFS_APP_TITLE = "PREFS_APP_TITLE";

    /** Intent extras used when launching MainActivity */
    public static final String EXTRA_LANG                 = "lang";
    public static final String EXTRA_PHOTO_SLIDE_DURATION = "photo_essay_slide_duration";

    /** Values for {@link scal.io.liger.model.ExampleCard#medium} and
     * {@link scal.io.liger.model.MediaFile#medium} */
    public static final String AUDIO = "audio";
    public static final String PHOTO = "photo";
    public static final String VIDEO = "video";

    /** Values for {@link scal.io.liger.model.ClipMetadata#type */
    public static final String CHARACTER = "character";
    public static final String ACTION    = "action";
    public static final String RESULT    = "result";
    public static final String PLACE     = "place";
    public static final String SIGNATURE = "signature";
    public static final String NARRATION = "narration";

    public static String getClipTypeLocalized(Context context, String clipType) {
        if (clipType.equalsIgnoreCase(Constants.CHARACTER)) {
            return context.getString(R.string.cliptype_character);
        } else if (clipType.equalsIgnoreCase(Constants.ACTION)) {
            return context.getString(R.string.cliptype_action);
        } else if (clipType.equalsIgnoreCase(Constants.RESULT)) {
            return context.getString(R.string.cliptype_result);
        } else if (clipType.equalsIgnoreCase(Constants.SIGNATURE)) {
            return context.getString(R.string.cliptype_signature);
        } else if (clipType.equalsIgnoreCase(Constants.PLACE)) {
            return context.getString(R.string.cliptype_place);
        }

        // else, return the untranslated
        return clipType;
    }

    /** Value returned by {@link scal.io.liger.model.StoryPath#getReferencedValue(String)}
     * when the requested reference value is external to the StoryPath */
    public static final String EXTERNAL = "value_from_external_story_path";

    /** StoryMaker Intent Actions */
    public static final String ACTION_PUBLISH = "io.scal.liger.PUBLISH";

    /** expansion file management */
    // TODO switch to .org & https
    public static final String LIGER_URL = "https://s3-us-west-1.amazonaws.com/storymakerorg/appdata/obb/org.storymaker.app/";
    public static final String MAIN = "main";
    public static final String PATCH = "patch";

    /** Use @ObbType annotation on method parameter for compile time validation */
    @StringDef({Constants.MAIN, Constants.PATCH})
    @Retention(RetentionPolicy.SOURCE)
    public static @interface ObbType{}

    public static final int MAIN_VERSION = 1031;
    public static final int PATCH_VERSION = 1020;
    public static final int MAIN_SIZE = 356561;
    public static final int PATCH_SIZE = -1;
    public static final String MAIN_CHECKSUM = "foo";
    public static final String PATCH_CHECKSUM = "foo";

    public static final int AVAILABLE_INDEX_VERSION = 2;

    public final static String TOR_PROXY_HOST = "localhost";
    public final static int TOR_PROXY_PORT = 8118;

    public static final String EXTRA_STORY_TITLE = "story_title";
    public static final String EXTRA_EXPORT_CLIPS = "export_clips";
    public static final String EXTRA_EXPORT_AUDIOCLIPS = "export_audioclips";

    public final static String DEFAULT_AUDIO_SAMPLE_RATE = "22050";  // FIXME probe if a better audio rate is supported on this device
}
