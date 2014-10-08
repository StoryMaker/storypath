package scal.io.liger.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.fima.cardsui.views.CardUI;

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

    private void getJSONFile() {
        onData(hasToString(equalToIgnoringCase("learning_guide_library.json"))).perform(click());
    }

    public void testClickContinue() {
        getJSONFile();
        onView(withId(R.id.btn_medium_video)).perform(click());
        onView(withId(R.id.cardsview)).perform(Util.swipeUp());
        onView(withText("Got it!")).perform(click());
    }
}