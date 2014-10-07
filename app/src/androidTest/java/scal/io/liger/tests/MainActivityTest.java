package scal.io.liger.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.fima.cardsui.views.CardUI;

import scal.io.liger.MainActivity;
import scal.io.liger.R;

import static android.test.ViewAsserts.assertOnScreen;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasToString;

/**
 * Created by micahlucas on 10/6/14.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private CardUI mCardUI;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mMainActivity = getActivity();
        mCardUI = (CardUI) mMainActivity.findViewById(R.id.cardsview);
    }

    public void testCardsUIExist() {
        assertOnScreen(mMainActivity.getWindow().getDecorView(), mCardUI);
    }

    public void testLoadJSONFile() {
        onData(hasToString(equalToIgnoringCase("learning_guide_library.json"))).perform(click());
    }
}