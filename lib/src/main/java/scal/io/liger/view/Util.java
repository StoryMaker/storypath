package scal.io.liger.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.Utility;
import scal.io.liger.adapter.OrderMediaAdapter;
import scal.io.liger.model.AudioClip;
import scal.io.liger.model.AudioClipFull;
import scal.io.liger.model.Card;
import scal.io.liger.model.FullMetadata;
import scal.io.liger.model.StoryPath;

/**
 * Functions common to the view package
 * Created by davidbrodsky on 10/28/14.
 */
public class Util {

    /**
     * @return a String of form "MM:SS:MS" from a raw ms value
     */
    public static String makeTimeString(long timeMs) {
        long millisecond = (timeMs % 1000) / 10;
        long second = (timeMs / 1000) % 60;
        long minute = (timeMs / (1000 * 60)) % 60;

        return String.format("%02d:%02d;%02d", minute, second, millisecond);
    }

    public static void startPublishActivity(Activity host, StoryPath storyPath) {

        try {
            ArrayList<FullMetadata> exportMetadata = storyPath.exportSelectedFullMetadata(); // TODO : Place in AsyncTask?

            // check for audio/video clips with no duration
            int num = 1;
            String message = "";
            for (FullMetadata fm : exportMetadata) {
                if ((fm.getMedium().equals(Constants.VIDEO) || fm.getMedium().equals(Constants.AUDIO)) && fm.getDuration() == 0) {
                    message = message + num + " ";
                }
                num++;
            }
            if (message.length() > 3) {
                Toast.makeText(host, "Clips " + message + "have a duration of 0", Toast.LENGTH_LONG).show();
                Log.e("Util", "Clips " + message + "have a duration of 0");
                return;
            } else if (message.length() > 0) {
                Toast.makeText(host, "Clip " + message + "has a duration of 0", Toast.LENGTH_LONG).show();
                Log.e("Util", "Clip " + message + "has a duration of 0");
                return;
            }

            ArrayList<AudioClipFull> exportAudioClipsMetadata = storyPath.exportAudioClips();
            if (exportMetadata.size() > 0) {
                Intent i = new Intent();
                i.setAction(Constants.ACTION_PUBLISH);
                i.putExtra(Constants.EXTRA_STORY_TITLE, storyPath.getTitle());
                i.putExtra(Constants.EXTRA_STORY_INSTANCE_PATH, storyPath.getStoryPathLibraryFile());
//            i.putExtra(Constants.EXTRA_REQUIRED_UPLOAD_TARGETS, storyPath.getRequiredUploadTargets());
//            i.putExtra(Constants.EXTRA_REQUIRED_PUBLISH_TARGETS, storyPath.getRequiredPublishTargets());
//            i.putExtra(Constants.EXTRA_REQUIRED_TAGS, storyPath.getRequiredTags());
                i.putParcelableArrayListExtra(Constants.EXTRA_EXPORT_CLIPS, exportMetadata);
                if (exportAudioClipsMetadata != null) {
                    i.putParcelableArrayListExtra(Constants.EXTRA_EXPORT_AUDIOCLIPS, exportAudioClipsMetadata);
                }
                host.startActivity(i);
                host.finish(); // Do we definitely want to finish the host Activity?
            } else {
                Toast.makeText(host, host.getString(R.string.this_story_has_no_clips_to_publish), Toast.LENGTH_LONG).show();
            }
        } catch (StoryPath.MediaException me) {
            Toast.makeText(host, me.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
}
