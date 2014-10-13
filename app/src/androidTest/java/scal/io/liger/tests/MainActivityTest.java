package scal.io.liger.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.fima.cardsui.views.CardUI;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import scal.io.liger.MainActivity;
import scal.io.liger.R;

import static android.test.ViewAsserts.assertOnScreen;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isClickable;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagKey;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagValue;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Created by micahlucas on 10/6/14.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private CardUI mCardUI;

    public MainActivityTest() {
        super("scal.io.liger", MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mMainActivity = getActivity();
        mCardUI = (CardUI) mMainActivity.findViewById(R.id.cardsview);
    }

    public void testPreConditions() {
        assertTrue(mMainActivity != null);
        assertTrue(mCardUI != null);
    }

    public void testCardsUIExist() {
        assertOnScreen(mMainActivity.getWindow().getDecorView(), mCardUI);
    }


    public void testLoadJSONFile() {
        getJSONFile();
    }

    public void testClickMediumVideo() {
        getJSONFile();
        onView(withId(R.id.btn_medium_video)).perform(click());
    }

    public void testClickMediumAudio() {
        getJSONFile();
        onView(withId(R.id.btn_medium_audio)).perform(click());
    }

    public void testClickMediumPhoto() {
        getJSONFile();
        onView(withId(R.id.btn_medium_photo)).perform(click());
    }

    public void testClickContinue() {
        getJSONFile();
        onView(withId(R.id.btn_medium_video)).perform(click());
        onView(withId(R.id.cardsview)).perform(Util.swipeUp());
        onView(withText("Got it!")).perform(click());
    }

    public void testRecordVideo() {
        getJSONFile();
        onView(withId(R.id.btn_medium_video)).perform(click());
        onView(withId(R.id.cardsview)).perform(Util.swipeUp());
        onView(withText("Got it!")).perform(click());
        onView(withId(R.id.cardsview)).perform(Util.swipeUp());

        //TODO finish method

        //onView(withId(R.id.btn_record_media)).check(matches(isDisplayed()));

        //ViewInteraction vi = onView(withTagValue(is((Object) "clip_video_character")));//.check(matches(isDisplayed()));

        //View v = (View) withTagValue(is((Object) "clip_video_character"));

        //onData(withTagValue(is((Object) "clip_video_character"))).perform(click());

        //onData(hasToString(equalToIgnoringCase("learning_guide_library.json"))).perform(click());

        //vi.check(matches(isDisplayed()));
        //vi.perform(click());


        //onView(withTagValue(is((Object) "clip_video_character"))).perform(click());
    }


    //internal methods
    private void getJSONFile() {
        onData(hasToString(equalToIgnoringCase("learning_guide_library.json"))).perform(click());
    }
}