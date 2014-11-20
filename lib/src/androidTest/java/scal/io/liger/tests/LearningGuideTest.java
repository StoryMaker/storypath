package scal.io.liger.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import scal.io.liger.Constants;
import scal.io.liger.DownloadHelper;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.ZipHelper;

import static android.test.ViewAsserts.assertOnScreen;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagValue;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

/**
 * Created by mnbogner on 11/18/14.
 */
public class LearningGuideTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;
    private Instrumentation.ActivityMonitor mActivityMonitor;

    public LearningGuideTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mMainActivity = getActivity();
        mRecyclerView = (RecyclerView) mMainActivity.findViewById(R.id.recyclerView);

        // create references to dummy files for responses
        String packageName = mMainActivity.getApplicationContext().getPackageName();
        File root = Environment.getExternalStorageDirectory();
        String directory = root.toString() + "/Android/data/" + packageName + "/files/";
        String sampleVideo = directory + "SAMPLE.mp4";

        // create an activity monitor to intercept media capture requests
        IntentFilter filter = new IntentFilter(MediaStore.ACTION_VIDEO_CAPTURE);
        Intent intent = new Intent();
        Uri uri = Uri.parse(sampleVideo);
        //Uri uri = Uri.parse("content://media/external/video/media/1258");
        intent.setData(uri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);
        mActivityMonitor = new Instrumentation.ActivityMonitor(filter, result, true);
        getInstrumentation().addMonitor(mActivityMonitor);
    }

    public void testPreConditions() {
        assertTrue(mMainActivity != null);
        assertTrue(mRecyclerView != null);
        Log.d("AUTOMATION", "testPreConditions() COMPLETE");
    }

    public void testRecyclerViewExist() {
        assertOnScreen(mMainActivity.getWindow().getDecorView(), mRecyclerView);
        Log.d("AUTOMATION", "testRecyclerViewExist() COMPLETE");
    }


    public void testClickClickClick() {

        // obb file assumed to be present (copied by test setup script)

        // select file
        stall(1000, "SELECT FILE");
        onData(hasToString(equalToIgnoringCase("learning_guide_1_library.json"))).perform(click());

        // select medium
        stall(1000, "SELECT MEDIUM");
        swipe(6);
        onView(withText("Video")).perform(click());

        // continue
        stall(1000, "CONTINUE");
        swipe(1);
        onView(withText("Start creating")).perform(click());

        // media capture
        stall(1000, "MEDIA CAPTURE 1");
        swipe(2);
        stall(3000, "MEDIA CAPTURE 1b");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_0")))))).perform(click());

        // media capture
        stall(1000, "MEDIA CAPTURE 2");
        swipe(2);
        stall(3000, "MEDIA CAPTURE 2b");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_1")))))).perform(click());

        // media capture
        stall(1000, "MEDIA CAPTURE 3");
        swipe(1);
        stall(3000, "MEDIA CAPTURE 3b");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_2")))))).perform(click());

        // continue
        stall(1000, "CONTINUE");
        swipe(1);
        onView(withText("Order your clips")).perform(click());

        // finish
        stall(1000, "FINISH");
        swipe(1);
        onView(withText("Finish")).perform(click());

        // check box
        stall(1000, "CHECK BOX");
        swipe(1);
        onView(withText("Thumbs Up")).perform(click());

        // continue
        stall(1000, "CONTINUE");
        swipe(1);
        onView(withText("Continue")).perform(click());

        // next
        stall(1000, "NEXT");
        swipe(1);
        onView(withText("Next: Add More Detail to Your Story")).perform(click());

        // pause before closing
        stall(10000, "INTERMISSION (LEARNING GUIDE 1 COMPLETE)");

        /*
        // continue
        stall(1000, "CONTINUE");
        swipe(2);
        onView(withText("Add to my story")).perform(click());

        // media capture
        stall(1000, "MEDIA CAPTURE 4");
        swipe(2);
        stall(3000, "MEDIA CAPTURE 4b");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_3")))))).perform(click());

        // media capture
        stall(1000, "MEDIA CAPTURE 5");
        swipe(2);
        stall(3000, "MEDIA CAPTURE 5b");
        onView(allOf(withText("Capture"), withParent(withParent(withTagValue(is((Object) "clip_video_4")))))).perform(click());

        // continue
        stall(1000, "CONTINUE");
        swipe(1);
        onView(withText("Order your clips")).perform(click());
        */

        Log.d("AUTOMATION", "testClickClickClick() COMPLETE");
    }

    private void stall(long milliseconds, String message) {
        try {
            Log.d("AUTOMATION", "SLEEP " + (milliseconds / 1000) + "(" + message + ")");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void swipe(int swipes) {
        for (int i = 0; i < swipes; i++) {
            onView(withId(R.id.recyclerView)).perform(Util.swipeUpLess());
        }
    }
}