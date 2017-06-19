package scal.io.liger;

import timber.log.Timber;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;

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

    @NonNull
    public static String getClipTypeLocalized(@NonNull Context context, @Nullable String clipType) {
        if (clipType == null) {
            // replace with empty string to prevent null pointer exceptions
            clipType = "";
        } else if (clipType.equalsIgnoreCase(Constants.CHARACTER)) {
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
    public static final int PATCH_VERSION = 1044; // FIXME move these obb constants into a .json file so I can autogenerate it easily from the content scripts
    public static final int MAIN_SIZE = 356561;
    public static final int PATCH_SIZE = 263464;
    public static final String MAIN_CHECKSUM = "4248a1dd806071d9fa837c6c7cd25be07d1d76aacef9d4801ec42e37c9c9c6c3";
    public static final String PATCH_CHECKSUM = "1114d9db9c1dded087bd8ced87ed86c34528fb62d069f26638d5ac2f23320623";

    public static final int AVAILABLE_INDEX_VERSION = 29; //25 previously?

    public final static String TOR_PROXY_HOST = "localhost"; // FIXME these Orbot host/ports should be pulled from Netcipher instead of hard coded
    public final static int TOR_PROXY_PORT = 8118;

    public static final String EXTRA_STORY_TITLE = "story_title";
    public static final String EXTRA_STORY_INSTANCE_PATH = "story_instance_path";
    public static final String EXTRA_EXPORT_CLIPS = "export_clips";
    public static final String EXTRA_EXPORT_AUDIOCLIPS = "export_audioclips";
    public static final String EXTRA_REQUIRED_UPLOAD_TARGETS = "extra_required_upload_targets";
    public static final String EXTRA_REQUIRED_PUBLISH_TARGETS = "extra_required_publish_targets";
    public static final String EXTRA_REQUIRED_TAGS = "extra_required_tags";

    public static final String AVAILABLE = "available";
    public static final String INSTALLED = "installed";

    // mime type ("photo" is not a valid mime type)
    public static final String IMAGE = "image"; // FIXME rename MIME_IMAGE and add its compadres here too
}
