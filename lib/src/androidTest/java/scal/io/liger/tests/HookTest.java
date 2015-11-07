package scal.io.liger.tests;

import timber.log.Timber;

import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;

import scal.io.liger.MainActivity;
import scal.io.liger.R;

import static android.test.ViewAsserts.assertOnScreen;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasToString;

/**
 * @author Josh Steiner
 */
public class HookTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;

    public HookTest() {
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
        getJSONFile("default_library.json");
    }

    public void testClickMediumVideo() {
        getJSONFile("default_library.json");
        onView(withText("An Event")).perform(click());
        onView(withText("Ask many people...")).perform(click());
        onView(withText("Video")).perform(click());
        onView(withText("Why is the event important?")).perform(click());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
    }

    //internal methods
    private void getJSONFile(String fileToClick) {
        onData(hasToString(equalToIgnoringCase(fileToClick))).perform(click());
    }
}