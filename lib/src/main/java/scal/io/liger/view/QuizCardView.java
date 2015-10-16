package scal.io.liger.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.Choice;
import scal.io.liger.model.QuizCard;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;

// TODO:
public class QuizCardView extends ExampleCardView {
    public static final String TAG = "QuizCardView";

    private static final String VALUES_CHOICE_TAG = "choice"; // Key for mCardModel#addValue(String, String)

    public QuizCard mCardModel;

    private List<Choice> mDisplayedChoices = new ArrayList<>(); // Quiz choices currently displayed
    private List<String> mSelectedChoices = new ArrayList<>();  // Quiz choice ids currently selected. For comparison against QuizCard#getCorrectAnswers()
    private String mLastSelectedChoiceTitle;                    // The last selected choice title to display when collapsed
    private boolean mExpanded = true; // Are the quiz card's possible choices expanded?


    private boolean quizIsPassed() {
        if (mSelectedChoices.size() >= mCardModel.getCorrectRequired()) {
            List<String> correctAnswers = mCardModel.getCorrectAnswers();
            if (correctAnswers == null) return true; // null from getCorrectAnswers means proceed no matter what
            for (String choiceId : mSelectedChoices) {
                if (!correctAnswers.contains(choiceId))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static class QuizCardChoice {
        public int id;
        public String text;
    }

    public QuizCardView(Context context, Card cardModel) {
        super();
        mContext = context;
        mCardModel = (QuizCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }
        View view = LayoutInflater.from(context).inflate(R.layout.card_quiz, null);

        // Views modified by animation callbacks must be final
        final TextView breadCrumb = (TextView) view.findViewById(R.id.breadCrumb);
        final ViewGroup choiceContainer = (ViewGroup) view.findViewById(R.id.choiceContainer);

        breadCrumb.setText(mCardModel.getQuestion());

        /** Quiz Header (breadCrumb) Click Listener */
        breadCrumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : When we allow the Quiz stack to be re-expanded, we'll have to come up with
                // a way to measure all the children to properly animate the expansion. Prior
                // we used an assumption based on each Quiz choice occupying a single line and thus
                // a fixed height.

//                if (!mExpanded) {
//                    toggleQuizResponseExpansion(breadCrumb, choiceContainer, true);
//                }

                // TODO : If quiz completed, expand cards, collapse all following Quiz cards
            }
        });

        mDisplayedChoices = mCardModel.getFilteredChoices();
        final boolean hasQuizResponses = mDisplayedChoices != null && mDisplayedChoices.size() > 0;

        /** Quiz response selection listener
         *  If required responses are gathered, collapse QuizCard and expand next.
         *  Else, continue marking responses selected until required responses gathered.
         */
        View.OnClickListener quizCardChoiceClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Choice choice = (Choice) v.getTag(R.id.view_tag_quiz_choice);
                markQuizChoiceSelected(choiceContainer, v, !v.isSelected(), true);

                // cards no longer saved on any change so need to initiate save if a selection is made
                // not sure if these cards can appear in either libraries or paths, so check first
                StoryPath sp = mCardModel.getStoryPath();
                if (sp instanceof StoryPathLibrary) {
                    StoryPathLibrary spl = (StoryPathLibrary)sp;
                    spl.save(false);
                } else {
                    sp.getStoryPathLibrary().save(true);
                }

                if (quizIsPassed()) {
                    // We're done!
                    toggleQuizResponseExpansion(breadCrumb, choiceContainer, true);
                }
            }
        };

        /** Populate Quiz choices stack */
        if (hasQuizResponses) {
            String choiceId = mCardModel.getValueByKey(VALUES_CHOICE_TAG);
            Log.i("quiz", String.format("adding %d choices for quiz card ", mDisplayedChoices.size()));
            for (Choice displayedChoice : mDisplayedChoices) {
                // Create Quiz choices
                View quizChoiceView = inflateAndAddChoiceForQuiz(choiceContainer, displayedChoice);
                quizChoiceView.setOnClickListener(quizCardChoiceClickListener);
                quizChoiceView.setTag(R.id.view_tag_quiz_choice, displayedChoice);
                if (choiceId != null && displayedChoice.id.equals(choiceId)) {
                    markQuizChoiceSelected(choiceContainer, quizChoiceView, true, false); // Don't notify since we're responding to initial state
                }
            }
            boolean quizIsPassed = quizIsPassed();
            Log.i(TAG, "Quiz is initially passed " + quizIsPassed);
            if (quizIsPassed) {
                // We're done! Don't fire QuizCardComplete() as the quiz's initial state was complete
                toggleQuizResponseExpansion(breadCrumb, choiceContainer, false);
            }

        } else {
            throw new IllegalStateException("Quiz has no responses!"); // FIXME instead of dying, we should just mark this card as not active?
        }

        // supports automated testing
        view.setTag(mCardModel.getId());
        return view;
    }

    /**
     * Mark a quiz choice view as selected.
     *
     * @param quizChoiceView the view that was selected. Must have a tag at R.id.view_tag_quiz_choice corresponding to the {@link scal.io.liger.model.Choice}
     * @param isSelected whether the view should be marked selected or unselected
     * @param doNotify whether to notify observers of the underlying model of this change
     */
    private void markQuizChoiceSelected(ViewGroup choiceContainer, View quizChoiceView, boolean isSelected, boolean doNotify) {
        boolean isSingleChoice = mCardModel.getCorrectRequired() < 2;

        quizChoiceView.setSelected(isSelected);
        Choice choice = (Choice) quizChoiceView.getTag(R.id.view_tag_quiz_choice);
//        StringBuilder logString = new StringBuilder();
//        logString.append(choice.text);
        if (isSelected) {
//            logString.append(" Selected ");
            if (!mSelectedChoices.contains(choice.getId())) {
                mCardModel.addValue(VALUES_CHOICE_TAG, choice.getId(), doNotify);
                mSelectedChoices.add(choice.getId());
                mLastSelectedChoiceTitle = choice.getText();
            }
        } else {
//            logString.append(" Unselected ");
            mSelectedChoices.remove(choice.getId());
            mCardModel.addValue(VALUES_CHOICE_TAG, "", doNotify);
        }

        if (isSingleChoice) {
            // If we're in single-choice mode, unselect all other choices
            int numQuizChoiceViews = choiceContainer.getChildCount();
            for (int x = 0; x < numQuizChoiceViews; x++) {
                View child = choiceContainer.getChildAt(x);
                if (child != quizChoiceView && child.isSelected()) {
                    Choice childChoice = (Choice) child.getTag(R.id.view_tag_quiz_choice);
                    child.setSelected(false);
                    mSelectedChoices.remove(childChoice.getId());
//                    logString.append(" (Unselected " + childChoice.getText() + ") ");
                    // TODO When the QuizCard supports multiple selections, remember to remove
                    // TODO the just-unselected value from its collection
                    //mCardModel.addValue(VALUES_CHOICE_TAG, "", doNotify);
                }
            }
        }
//        logString.append(" . Total selected " + mSelectedChoices.size() + " correctRequired " + mCardModel.getCorrectRequired());
//        logString.append(" passed: " + quizIsPassed());
//        Log.i(TAG, logString.toString());
    }

    private void toggleQuizResponseExpansion(final TextView breadCrumb, final ViewGroup choiceContainer, boolean doAnimate) {
        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) choiceContainer.getLayoutParams();

        final int finalChoiceContainerHeight;
        // Breadcrumb background and text color swap
        final int animFirstColor;
        final int animSecondColor;
        if (mExpanded) {
            // Breadcrumb shows first user response
            breadCrumb.setText(mLastSelectedChoiceTitle);
            finalChoiceContainerHeight = 0;
            animFirstColor = mContext.getResources().getColor(R.color.white);
            animSecondColor = mContext.getResources().getColor(R.color.storymaker_highlight);
        } else {
            // NOTE : This animation is currently disabled. See breadCrumb.setOnClickListener
            // Breadcrumb shows quiz question
            breadCrumb.setText(mCardModel.getQuestion());
            finalChoiceContainerHeight = calculateExpandedChoiceContainerHeightPx();
            animFirstColor = mContext.getResources().getColor(R.color.storymaker_highlight);
            animSecondColor = mContext.getResources().getColor(R.color.white);
        }
        if (doAnimate) {
            // Animate choiceContainer height
            //Log.i(TAG, String.format("animating quiz container from %d to %d", choiceContainer.getHeight(), finalChoiceContainerHeight));
            ValueAnimator animator = ValueAnimator.ofInt(choiceContainer.getHeight(), finalChoiceContainerHeight);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    params.height = (int) valueAnimator.getAnimatedValue();
                    choiceContainer.setLayoutParams(params);
                }
            });
            animator.start();

            // Animate breadCrumb from colored text on white background to white text on colored background
            ObjectAnimator backgroundColorFade = ObjectAnimator.ofObject(breadCrumb, "backgroundColor", new ArgbEvaluator(), animFirstColor, animSecondColor);
            backgroundColorFade.start();
            ObjectAnimator textColorFade = ObjectAnimator.ofObject(breadCrumb, "textColor", new ArgbEvaluator(), animSecondColor, animFirstColor);
            textColorFade.start();
        } else {
            // do not animate
            params.height = finalChoiceContainerHeight;
            choiceContainer.setLayoutParams(params);
            breadCrumb.setBackgroundColor(animSecondColor);
            breadCrumb.setTextColor(animFirstColor);
        }
        mExpanded = !mExpanded;
    }

    private View inflateAndAddChoiceForQuiz(@NonNull ViewGroup quizChoiceContainer, Choice choice) {
        // NOTE: quiz_card_choice layout previously had fixed height, which allowed calculation
        // of expanded quiz card height without performing layout. Come up with a new way to
        // animate the quiz card expansion now that we have varying height quiz choices.
        LayoutInflater inflater = (LayoutInflater) quizChoiceContainer.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView choiceView = (TextView) (inflater.inflate(R.layout.quiz_card_choice, quizChoiceContainer, false)).findViewById(R.id.choiceText);
        choiceView.setText(choice.text);

        quizChoiceContainer.addView(choiceView);
        return choiceView;
    }

    private int calculateExpandedChoiceContainerHeightPx() {
        Resources r = mContext.getResources();
        int choiceHeight = r.getDimensionPixelSize(R.dimen.card_quiz_response_height);
        int choiceMargin = r.getDimensionPixelSize(R.dimen.padding_tiny);
        int numChoices = mDisplayedChoices.size();

        return numChoices * (choiceHeight + (2 * choiceMargin));
    }
}
