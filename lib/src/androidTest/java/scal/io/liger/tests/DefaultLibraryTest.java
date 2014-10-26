package scal.io.liger.tests;

import android.content.Intent;
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
 * Created by micahlucas on 10/6/14.
 */
public class DefaultLibraryTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;

    String[] files = {
            "event_discussion_audio_question_1_library.json",
            "event_discussion_audio_question_2_library.json",
            "event_discussion_audio_question_3_library.json",
            "event_discussion_video_question_4_library.json",
            "event_discussion_video_question_5_library.json",
            "event_discussion_video_question_6_library.json",
            "event_highlights_audio_library.json",
            "event_highlights_photo_library.json",
            "event_highlights_video_library.json",
            "event_interview_audio_library.json",
            "event_interview_video_library.json",
            "event_process_audio_library.json",
            "event_process_photo_library.json",
            "event_process_video_library.json",
            "event_report_audio_library.json",
            "event_report_video_library.json",
            "event_series_photo_action_library.json",
            "event_series_photo_character_library.json",
            "event_series_photo_place_library.json",
            "event_series_photo_result_library.json",
            "issue_discussion_audio_question_7_library.json",
            "issue_discussion_audio_question_8_library.json",
            "issue_discussion_audio_question_9_library.json",
            "issue_discussion_video_question_10_library.json",
            "issue_discussion_video_question_11_library.json",
            "issue_discussion_video_question_12_library.json",
            "issue_highlights_audio_library.json",
            "issue_highlights_photo_library.json",
            "issue_highlights_video_library.json",
            "issue_interview_audio_library.json",
            "issue_interview_video_library.json",
            "issue_report_audio_library.json",
            "issue_report_video_library.json",
            "issue_series_photo_character_library.json",
            "issue_series_photo_place_library.json",
            "issue_series_photo_signature_library.json",
            "profile_discussion_audio_question_13_library.json",
            "profile_discussion_audio_question_14_library.json",
            "profile_discussion_audio_question_15_library.json",
            "profile_discussion_video_question_16_library.json",
            "profile_discussion_video_question_17_library.json",
            "profile_discussion_video_question_18_library.json",
            "profile_highlights_audio_library.json",
            "profile_highlights_photo_library.json",
            "profile_highlights_video_library.json",
            "profile_interview_audio_library.json",
            "profile_interview_video_library.json",
            "profile_process_audio_library.json",
            "profile_process_photo_library.json",
            "profile_process_video_library.json",
            "profile_report_audio_library.json",
            "profile_report_video_library.json",
            "profile_series_photo_action_library.json",
            "profile_series_photo_character_library.json"
    };

    public DefaultLibraryTest() {
        super("scal.io.liger", MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        Intent i = new Intent();
        i.putExtra("storypathlibrary_json", "default/default_library/test/" + files[0]);
        setActivityIntent(i);
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
}