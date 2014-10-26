package scal.io.liger.tests;

import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;

import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.model.Card;

import static android.test.ViewAsserts.assertOnScreen;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagKey;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

/**
 * @author Micah Lucas
 * @author Josh Steiner
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;

    public MainActivityTest() {
        super("scal.io.liger", MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mMainActivity = getActivity();
        mRecyclerView = (RecyclerView) mMainActivity.findViewById(R.id.recyclerView);
    }

    public void testPreConditions() {
        assertTrue(mMainActivity != null);
        assertTrue(mRecyclerView != null);
    }

    public void testRecyclerViewExist() {
        assertOnScreen(mMainActivity.getWindow().getDecorView(), mRecyclerView);
    }


    public void testLoadJSONFile() {
        getJSONFile("learning_guide_library.json");
    }

//    public void testHook() {
//        getJSONFile("default_library.json");
//        onView(withText("An Event")).perform(click());
//    }

    public void testClickMediumVideo() {
        getJSONFile("learning_guide_library.json");
        onView(withText("LOAD IT!")).perform(click());
        onView(withId(R.id.btn_medium_video)).perform(click());
    }

    public void testClickMediumAudio() {
        getJSONFile("learning_guide_library.json");
        onView(withText("LOAD IT!")).perform(click());
        onView(withId(R.id.btn_medium_audio)).perform(click());
    }

    public void testClickMediumPhoto() {
        getJSONFile("learning_guide_library.json");
        onView(withText("LOAD IT!")).perform(click());
        onView(withId(R.id.btn_medium_photo)).perform(click());
    }

    public void testClickContinue() {
        getJSONFile("learning_guide_library.json");
        onView(withText("LOAD IT!")).perform(click());
        onView(withId(R.id.btn_medium_video)).perform(click());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withText("Got it!")).perform(click());
    }
//
//    public void testRecordVideo() {
//        getJSONFile("learning_guide_library.json");
//        onView(withId(R.id.btn_medium_video)).perform(click());
//        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
//        onView(withText("LOAD IT!")).perform(click());
//        onView(withText("Got it!")).perform(click());
//        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
//
//        //TODO finish method
//
//        //onView(withId(R.id.btn_record_media)).check(matches(isDisplayed()));
//
//        //ViewInteraction vi = onView(withTagValue(is((Object) "clip_video_character")));//.check(matches(isDisplayed()));
//
//        //View v = (View) withTagValue(is((Object) "clip_video_character"));
//
//        //onData(withTagValue(is((Object) "clip_video_character"))).perform(click());
//
//        //onData(hasToString(equalToIgnoringCase("learning_guide_library.json"))).perform(click());
//
//        //vi.check(matches(isDisplayed()));
//        //vi.perform(click());
//
//
//        //onView(withTagValue(is((Object) "clip_video_character"))).perform(click());
//    }

    //internal methods
    private void getJSONFile(String fileToClick) {
        onData(hasToString(equalToIgnoringCase(fileToClick))).perform(click());
    }

//    public void testAfterRecording() {
//        getJSONFile("TEST_STORY.json");
//        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
//        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
//        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
//        onView(withText("I like the order")).perform(click());
//        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
//        onView(withText("Yup, I'm all finished!")).perform(click());
//    }
}