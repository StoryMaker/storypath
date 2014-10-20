package scal.io.liger.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import scal.io.liger.MediaHelper;
import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.QuizCard;


public class QuizCardView extends ExampleCardView {
    public static final String TAG = "QuizCardView";

    public QuizCard mCardModel;

    private List<QuizCard.Choice> mDisplayedChoices = new ArrayList<>(); // Quiz choices currently displayed
    private List<QuizCard.Choice> mSelectedChoices = new ArrayList<>();  // Quiz choices currently selected
    private int mExpandedHeight = UNSET_HEIGHT; // The height of the quiz choice container when expanded
    private boolean mExpanded = false; // Are the quiz card's possible choices expanded?

    private static final int UNSET_HEIGHT = -42; // placeholder value to indicate quiz choice container not measured

    private boolean quizIsPassed() {
        if (mSelectedChoices.size() == mCardModel.getCorrectRequired()) {
            List<String> correctAnswers = mCardModel.getCorrectAnswers();
            for (QuizCard.Choice choice : mSelectedChoices) {
                if (!correctAnswers.contains(choice.id))
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

//        Resources r = context.getResources();
//        mExpandedHeight = r.getDimensionPixelSize(R.dimen.clip_card_footer_height);
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }
        // TODO: How to determine if this card should begin expanded?
        View view = LayoutInflater.from(context).inflate(R.layout.card_quiz, null);

        // Views modified by animation callbacks, and must be final
        final TextView breadCrumb = (TextView) view.findViewById(R.id.breadCrumb);
        final ViewGroup choiceContainer = (ViewGroup) view.findViewById(R.id.choiceContainer);

        /** Quiz Header (breadCrumb) Click Listener */
        breadCrumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mExpanded) {
                    toggleQuizResponseExpansion(breadCrumb, choiceContainer);
                }
                // TODO : If quiz completed, expand cards, collapse all following Quiz cards
            }
        });

        /** Get expanded height of choiceContainer on layout */
        choiceContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mExpandedHeight == UNSET_HEIGHT) {
                    mExpandedHeight = choiceContainer.getHeight();
                    if (mExpandedHeight > 0) mExpanded = true;
                    Log.i("layout", "on quiz choice layout height is " + mExpandedHeight);
                }
            }
        });

        mDisplayedChoices = mCardModel.getOptions();
        final boolean hasQuizResponses = mDisplayedChoices != null && mDisplayedChoices.size() > 0;

        /** Quiz response selection listener
         *  If required responses are gathered, collapse QuizCard and expand next.
         *  Else, continue marking responses selected until required responses gathered.
         */
        View.OnClickListener quizCardChoiceClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuizCard.Choice choice = (QuizCard.Choice) v.getTag(R.id.view_tag_quiz_choice);

                if (v.isSelected()) {
                    // Choice is selected
                    mSelectedChoices.remove(choice);
                } else {
                    // Choice is unselected
                    if (!mSelectedChoices.contains(choice)) {
                        mSelectedChoices.add(choice);
                        mCardModel.addValue("choice", choice.id);
                        toggleQuizResponseExpansion(breadCrumb, choiceContainer);
                        quizCardComplete();
                    }
                }
                v.setSelected(!v.isSelected());

//                if (quizIsPassed()) {
//                    // We're done!
//                    toggleQuizResponseExpansion(breadCrumb, choiceContainer);
//                    quizCardComplete();
//                }
            }
        };

        /** Populate Quiz choices stack */
        if (hasQuizResponses) {
            Log.i("quiz", String.format("adding %d choices for quiz card ", mDisplayedChoices.size()));
            for (QuizCard.Choice displayedChoice : mDisplayedChoices) {
                // Create Quiz choices
                View quizChoice = inflateAndAddChoiceForQuiz(choiceContainer, displayedChoice);
                quizChoice.setOnClickListener(quizCardChoiceClickListener);
                quizChoice.setTag(R.id.view_tag_quiz_choice, displayedChoice);
            }
        } else {
            throw new IllegalStateException("Quiz has no responses!");
        }

        breadCrumb.setText(mCardModel.getQuestion());

        // supports automated testing
        view.setTag(mCardModel.getId());
        return view;
    }

    private void toggleQuizResponseExpansion(final TextView breadCrumb, final ViewGroup choiceContainer) {
        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) choiceContainer.getLayoutParams();

        final int finalChoiceContainerHeight;
        // Breadcrumb background and text color swap
        final int animFirstColor;
        final int animSecondColor;
        if (mExpanded) {
            // Breadcrumb shows first user response
            breadCrumb.setText(mSelectedChoices.get(0).text);
            finalChoiceContainerHeight = 0;
            animFirstColor = mContext.getResources().getColor(R.color.white);
            animSecondColor = mContext.getResources().getColor(R.color.signature);
        } else {
            // Breadcrumb shows quiz question
            breadCrumb.setText(mCardModel.getQuestion());
            finalChoiceContainerHeight = mExpandedHeight;
            animFirstColor = mContext.getResources().getColor(R.color.signature);
            animSecondColor = mContext.getResources().getColor(R.color.white);
        }

        // Animate choiceContainer to height 0
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

        mExpanded = !mExpanded;
    }

    private View inflateAndAddChoiceForQuiz(@NonNull ViewGroup quizChoiceContainer, QuizCard.Choice choice) {
        LayoutInflater inflater = (LayoutInflater) quizChoiceContainer.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView choiceView = (TextView) (inflater.inflate(R.layout.quiz_card_choice, quizChoiceContainer, false)).findViewById(R.id.choiceText);
        choiceView.setText(choice.text);

        quizChoiceContainer.addView(choiceView);
        return choiceView;
    }

    //returns stored mediaPath (if exists) or exampleMediaPath (if exists)
    /*
    @Override
    public File getValidFile(String mediaPath, String exampleMediaPath) {
        File mediaFile = null;

        if (mediaPath != null) {
            mediaFile = MediaHelper.loadFileFromPath(mCardModel.getStoryPathReference().buildPath(mediaPath));
        } else if (exampleMediaPath != null) {
            mediaFile = MediaHelper.loadFileFromPath(mCardModel.getStoryPathReference().buildPath(exampleMediaPath));
        }

        return mediaFile;
    }
    */

    /**
     * This quiz card is completed and collapsed into the breadcrumb state.
     * Expand the next quiz card.
     */
    private void quizCardComplete() {
        // TODO
        mCardModel.addValue("value", mSelectedChoices.get(0).id);
    }
}
