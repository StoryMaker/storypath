package scal.io.liger.tests;

import timber.log.Timber;

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

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.MainActivity;
import scal.io.liger.R;

import static android.test.ViewAsserts.assertOnScreen;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withTagValue;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.doesNotExist;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

/**
 * @author Josh Steiner
 */
public class DefaultLibraryTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;

    private ArrayList<String> brokenPaths = new ArrayList<String>();

    String[] firstOption = {
        "An Event",
        "A Person",
        "An Issue"
    };

    String[] secondOption = {
        "Talk to people about it.",
        "Ask the same question to many people.",
        "Create an accurate summary.",
        "Document the steps taken.",
        "Collect a set of pictures of people.",
        "Show the best moments."
    };

    String[] thirdOption = {
        "Audio",
        "Video",
        "Photo"
    };

    String[] fourthOption = {
        "Characters",
        "Actions",
        "Results",
        "Places",
        "Signatures"
    };

    String[] fifthOption = {
        "Why is the event important?",
        "What is your opinion of the event?",
        "What do you think is the most exciting part of the event?",
        "Why is the issue important?",
        "What is your opinion of the issue?",
        "What do you think should be done about the issue?",
        "Why is the character important?",
        "What is your opinion of the character?",
        "What do you think the character should do next?",
    };

    public DefaultLibraryTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mMainActivity = getActivity();
        mRecyclerView = (RecyclerView) mMainActivity.findViewById(R.id.recyclerView);
    }

    public void testPreConditions() {
        assertTrue(mMainActivity != null);
        assertTrue(mRecyclerView != null);
        Timber.d("testPreConditions() COMPLETE");
    }

    public void testRecyclerViewExist() {
        assertOnScreen(mMainActivity.getWindow().getDecorView(), mRecyclerView);
        Timber.d("testRecyclerViewExist() COMPLETE");
    }

    public void testHookPaths() {

        for (int i = 0; i < firstOption.length; i++) {

            String firstSelection = firstOption[i];

            for (int j = 0; j < secondOption.length; j++) {

                String secondSelection = secondOption[j];

                for (int k = 0; k < thirdOption.length; k++) {

                    String thirdSelection = thirdOption[k];

                    // some selections spawn a fourth set of options
                    if (secondSelection.equals("Collect a set of pictures of people.")) {

                        for (int l = 0; l < fourthOption.length; l++) {

                            String fourthSelection = fourthOption[l];

                            // obb file assumed to be present (copied by test setup script)

                            // select file
                            stall(500, "SELECT FILE");
                            onData(hasToString(equalToIgnoringCase("default_library.json"))).perform(click());

                            // first selection
                            stall(500, "FIRST SELECTION (" + firstSelection + ")");
                            onView(withText(firstSelection)).perform(click());

                            // second selection
                            try {
                                stall(500, "SECOND SELECTION (" + secondSelection + ")");
                                onView(withText(secondSelection)).perform(click());

                                // third selection
                                try {
                                    stall(500, "THIRD SELECTION (" + thirdSelection + ")");
                                    onView(withText(thirdSelection)).perform(click());

                                    // fourth selection
                                    try {
                                        stall(500, "FOURTH SELECTION (" + fourthSelection + ")");
                                        onView(withText(fourthSelection)).perform(click());

                                        // check to see if a story path was loaded?
                                        // scroll to bottom, check for publish button
                                        stall(500, "SWIPING");
                                        swipe(15);

                                        try {
                                            stall(500, "PUBLISH BUTTON");
                                            onView(allOf(withText("Publish"), withParent(withTagValue(is((Object) "publish_card_1"))))).check(matches(isDisplayed()));
                                            // onView(withText("Publish")).check(matches(isDisplayed()));

                                            // pause before next loop
                                            stall(2000, "INTERMISSION (" + firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection + " COMPLETE)");
                                        } catch (NoMatchingViewException nmve) {
                                            // implies no button was found (failure)
                                            Timber.d("NO PUBLISH BUTTON FOUND AT THE END OF " + firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection);
                                            // return;
                                            brokenPaths.add(firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection);
                                        }
                                    } catch (NoMatchingViewException nmve) {
                                        // some options do not support all questions (not a failure state)
                                        Timber.d("SELECTION " + fourthSelection + " NOT AVAILABLE");
                                    }
                                } catch (NoMatchingViewException nmve) {
                                    // some options do not support all media types (not a failure state)
                                    Timber.d("SELECTION " + thirdSelection + " NOT AVAILABLE");
                                }
                            } catch (NoMatchingViewException nmve) {
                                // some options do not support all formats (not a failure state???)
                                Timber.d("SELECTION " + secondSelection + " NOT AVAILABLE");
                            }

                            // restart app
                            mMainActivity.finish();
                            mMainActivity.startActivity(mMainActivity.getIntent());
                            mRecyclerView = (RecyclerView) mMainActivity.findViewById(R.id.recyclerView);

                            // allow time for restart
                            stall(1000, "RESTARTING ACTIVITY FOR NEXT PASS");
                        }

                        continue;
                    }

                    if (secondSelection.equals("Ask the same question to many people.")) {

                        for (int l = 0; l < fifthOption.length; l++) {

                            String fourthSelection = fifthOption[l];

                            // obb file assumed to be present (copied by test setup script)

                            // select file
                            stall(500, "SELECT FILE");
                            onData(hasToString(equalToIgnoringCase("default_library.json"))).perform(click());

                            // first selection
                            stall(500, "FIRST SELECTION (" + firstSelection + ")");
                            onView(withText(firstSelection)).perform(click());

                            // second selection
                            try {
                                stall(500, "SECOND SELECTION (" + secondSelection + ")");
                                onView(withText(secondSelection)).perform(click());

                                // third selection
                                try {
                                    stall(500, "THIRD SELECTION (" + thirdSelection + ")");
                                    onView(withText(thirdSelection)).perform(click());

                                    // fourth selection
                                    try {
                                        stall(500, "FOURTH SELECTION (" + fourthSelection + ")");
                                        onView(withText(fourthSelection)).perform(click());

                                        // check to see if a story path was loaded?
                                        // scroll to bottom, check for publish button
                                        stall(500, "SWIPING");
                                        swipe(15);

                                        try {
                                            stall(500, "PUBLISH BUTTON");
                                            onView(allOf(withText("Publish"), withParent(withTagValue(is((Object) "publish_card_1"))))).check(matches(isDisplayed()));
                                            // onView(withText("Publish")).check(matches(isDisplayed()));

                                            // pause before next loop
                                            stall(2000, "INTERMISSION (" + firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection + " COMPLETE)");
                                        } catch (NoMatchingViewException nmve) {
                                            // implies no button was found (failure)
                                            Timber.d("NO PUBLISH BUTTON FOUND AT THE END OF " + firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection);
                                            // return;
                                            brokenPaths.add(firstSelection + " > " + secondSelection + " > " + thirdSelection + " > " + fourthSelection);
                                        }
                                    } catch (NoMatchingViewException nmve) {
                                        // some options do not support all questions (not a failure state)
                                        Timber.d("SELECTION " + fourthSelection + " NOT AVAILABLE");
                                    }
                                } catch (NoMatchingViewException nmve) {
                                    // some options do not support all media types (not a failure state)
                                    Timber.d("SELECTION " + thirdSelection + " NOT AVAILABLE");
                                }
                            } catch (NoMatchingViewException nmve) {
                                // some options do not support all formats (not a failure state???)
                                Timber.d("SELECTION " + secondSelection + " NOT AVAILABLE");
                            }


                            // restart app
                            mMainActivity.finish();
                            mMainActivity.startActivity(mMainActivity.getIntent());
                            mRecyclerView = (RecyclerView) mMainActivity.findViewById(R.id.recyclerView);

                            // allow time for restart
                            stall(1000, "RESTARTING ACTIVITY FOR NEXT PASS");
                        }

                        continue;
                    }

                    // obb file assumed to be present (copied by test setup script)

                    // select file
                    stall(500, "SELECT FILE");
                    onData(hasToString(equalToIgnoringCase("default_library.json"))).perform(click());

                    // first selection
                    stall(500, "FIRST SELECTION (" + firstSelection + ")");
                    onView(withText(firstSelection)).perform(click());

                    // second selection
                    try {
                        stall(500, "SECOND SELECTION (" + secondSelection + ")");
                        onView(withText(secondSelection)).perform(click());

                        // third selection
                        try {
                            stall(500, "THIRD SELECTION (" + thirdSelection + ")");
                            onView(withText(thirdSelection)).perform(click());

                            // check to see if a story path was loaded?
                            // scroll to bottom, check for publish button
                            stall(500, "SWIPING");
                            swipe(15);

                            try {
                                stall(500, "PUBLISH BUTTON");
                                onView(allOf(withText("Publish"), withParent(withTagValue(is((Object) "publish_card_1"))))).check(matches(isDisplayed()));
                                // onView(withText("Publish")).check(matches(isDisplayed()));

                                // pause before next loop
                                stall(2000, "INTERMISSION (" + firstSelection + " > " + secondSelection + " > " + thirdSelection + " COMPLETE)");
                            } catch (NoMatchingViewException nmve) {
                                // implies no button was found (failure)
                                Timber.d("NO PUBLISH BUTTON FOUND AT THE END OF " + firstSelection + " > " + secondSelection + " > " + thirdSelection);
                                // return;
                                brokenPaths.add(firstSelection + " > " + secondSelection + " > " + thirdSelection);
                            }
                        } catch (NoMatchingViewException nmve) {
                            // some options do not support all media types (not a failure state)
                            Timber.d("SELECTION " + thirdSelection + " NOT AVAILABLE");
                        }
                    } catch (NoMatchingViewException nmve) {
                        // some options do not support all formats (not a failure state???)
                        Timber.d("SELECTION " + secondSelection + " NOT AVAILABLE");
                    }

                    // restart app
                    mMainActivity.finish();
                    mMainActivity.startActivity(mMainActivity.getIntent());
                    mRecyclerView = (RecyclerView) mMainActivity.findViewById(R.id.recyclerView);

                    // allow time for restart
                    stall(1000, "RESTARTING ACTIVITY FOR NEXT PASS");
                }
            }
        }

        for (String brokenPath : brokenPaths) {
            Timber.d("BROKEN PATH: " + brokenPath);
        }

        assertEquals(brokenPaths.size(), 0);
    }

    private void stall(long milliseconds, String message) {
        try {
            Timber.d("SLEEP " + (milliseconds / 1000) + " (" + message + ")");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void swipe(int swipes) {
        for (int i = 0; i < swipes; i++) {
            onView(withId(R.id.recyclerView)).perform(Util.swipeUp());
        }
    }
}