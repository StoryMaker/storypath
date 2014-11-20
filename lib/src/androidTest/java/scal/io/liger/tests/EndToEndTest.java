package scal.io.liger.tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;

import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import scal.io.liger.MainActivity;
import scal.io.liger.R;

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
 * Created by mnbogner on 11/17/14.
 */
public class EndToEndTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;
    private Instrumentation.ActivityMonitor mActivityMonitor;

    public EndToEndTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mMainActivity = getActivity();
        mRecyclerView = (RecyclerView) mMainActivity.findViewById(R.id.recyclerView);

        // create an activity monitor to intercept media capture requests
        IntentFilter filter = new IntentFilter(MediaStore.ACTION_VIDEO_CAPTURE);
        Intent intent = new Intent();
        Uri uri = Uri.parse("content://media/external/video/media/1258");
        intent.setData(uri);
        Instrumentation.ActivityResult result = new  Instrumentation.ActivityResult(Activity.RESULT_OK, intent);
        mActivityMonitor = new Instrumentation.ActivityMonitor(filter, result, true);
        getInstrumentation().addMonitor(mActivityMonitor);
    }

    public void testPreConditions() {
        assertTrue(mMainActivity != null);
        assertTrue(mRecyclerView != null);
    }

    public void testRecyclerViewExist() {
        assertOnScreen(mMainActivity.getWindow().getDecorView(), mRecyclerView);
    }

    /*
    public void testLoadJSONFile() {
        getJSONFile("default_library.json");
    }
    */

    public void testClickClickClick() {
        // stall for loading?
        try {
            Log.d("AUTOMATION", "SLEEP 10");
            Thread.sleep(10000);
            Log.d("AUTOMATION", "SLEEP 10, done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // select file
        onData(hasToString(equalToIgnoringCase("default_library.json"))).perform(click());

        //getJSONFile("default_library.json");
        // select a minimal test case

        // stall?
        try {
            Log.d("AUTOMATION", "SLEEP 1a");
            Thread.sleep(1000);
            Log.d("AUTOMATION", "SLEEP 1a, done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("An Event")).perform(click());
        // stall?
        try {
            Log.d("AUTOMATION", "SLEEP 1b");
            Thread.sleep(1000);
            Log.d("AUTOMATION", "SLEEP 1b, done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText("Show the best moments.")).perform(click());
        // stall?
        try {
            Log.d("AUTOMATION", "SLEEP 1c");
            Thread.sleep(1000);
            Log.d("AUTOMATION", "SLEEP 1c, done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText("Video")).perform(click());
        // stall?
        try {
            Log.d("AUTOMATION", "SLEEP 1d");
            Thread.sleep(1000);
            Log.d("AUTOMATION", "SLEEP 1d, done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // move to clip card 1
        onView(withId(R.id.recyclerView)).perform(Util.swipeUpLess());

        // stall for loading?
        try {
            Log.d("AUTOMATION", "SLEEP 10");
            Thread.sleep(10000);
            Log.d("AUTOMATION", "SLEEP 10, done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(allOf(withText("Capture"), withParent(  withParent(withTagValue(is((Object)"clip_card_0")))  ))).perform(click());


        //onView(withId(R.id.btn_record_media)).check(matches(isDisplayed()));
        //ViewInteraction vi = onView(withTagValue(is((Object)"foo")));//.check(matches(isDisplayed()));
        //View v = (View) withTagValue(is((Object) "clip_video_character"));
        //onData(withTagValue(is((Object) "clip_video_character"))).perform(click());


    }

    //internal methods
    /*
    private void getJSONFile(String fileToClick) {
        onData(hasToString(equalToIgnoringCase(fileToClick))).perform(click());
    }
    */

}
