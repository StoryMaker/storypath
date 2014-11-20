package scal.io.liger.tests;

import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.model.Tip;
import scal.io.liger.model.TipCollectionHeadlessCard;

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
public class TipCollectionHeadlessCardTest extends TestCase {

    ArrayList<Tip> tips;
    TipCollectionHeadlessCard card;

    final static String TIP1 = "This is the first tip";
    final static String TAG1 = "tag1";
    final static String TIP2 = "2nd tip";
    final static String TAG2 = "tag2";
    final static String TIP3 = "Third tip!";
    final static String TAG3 = "tag3";
    final static String TIP4 = "should never be seen";
    final static String TAG4 = "tag4";
    final static String TIP_NONE = "Tip that matches none";
    final static String TIP_ALL_3 = "Tip that matches all 3";

    // TODO test case sensitivity

    public TipCollectionHeadlessCardTest() {
        super();
//        super("scal.io.liger.model", TipCollectionHeadlessCard.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

//        tips = new ArrayList<Tip>();
//        tips.add(new Tip("This is the first tip", new ArrayList<String>(Arrays.asList("tag1", "tag2"))));
        tips = new ArrayList<Tip>(Arrays.asList(
                new Tip(TIP1, new ArrayList<String>(Arrays.asList(
                        TAG1
                        ))),
                new Tip(TIP2, new ArrayList<String>(Arrays.asList(
                        TAG2
                        ))),
                new Tip(TIP3, new ArrayList<String>(Arrays.asList(
                        TAG3
                        ))),
                new Tip(TIP_NONE, new ArrayList<String>()),
                new Tip(TIP_ALL_3, new ArrayList<String>(Arrays.asList(
                        TAG1,
                        TAG2,
                        TAG3
                )))

                // note there are no TAG4 tips
        ));
        card = new TipCollectionHeadlessCard(tips);
    }

    public void testPreConditions() {
    }

    public void testTagFiltering() {
        // FIXME should actually test that we get teh right tips back

        assertEquals(tips.size(), 5);

        ArrayList<Tip> tag1Tips = card.getTipsByTags(new ArrayList<String>(Arrays.asList(TAG1)));
        assertEquals(tag1Tips.size(), 2);

        ArrayList<Tip> tag2Tips = card.getTipsByTags(new ArrayList<String>(Arrays.asList(TAG2)));
        assertEquals(tag2Tips.size(), 2);

        ArrayList<Tip> tag3Tips = card.getTipsByTags(new ArrayList<String>(Arrays.asList(TAG3)));
        assertEquals(tag3Tips.size(), 2);

        ArrayList<Tip> tagAllTips = card.getTipsByTags(new ArrayList<String>(Arrays.asList(
                TAG1,
                TAG2,
                TAG3
        )));
        assertEquals(tagAllTips.size(), 4);

        ArrayList<Tip> tag4Tips = card.getTipsByTags(new ArrayList<String>(Arrays.asList(TAG4)));
        assertEquals(tag4Tips.size(), 0);
    }

    // text getTextB...
}