package scal.io.liger.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import scal.io.liger.MediaHelper;
import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.QuizCard;


public class QuizCardView extends ExampleCardView {
    public static final String TAG = "HookCardView";

    public QuizCard mCardModel;

    private List<QuizCard.Choice> mDisplayedChoices = new ArrayList<>(); // Quiz choices currently displayed
    private List<QuizCard.Choice> mSelectedChoices = new ArrayList<>();  // Quiz choices currently selected
    private int mExpandedHeight; // The height of the quiz choice container when expanded
    private boolean mExpanded = false; // Are the quiz card's possible choices expanded?

    private boolean quizIsPassed() {
        // TODO: DO IT
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
                // TODO : If quiz completed, expand cards, collapse all following Quiz cards
            }
        });

        /** Get expanded height of choiceContainer on layout */
        choiceContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mExpanded) {
                    mExpandedHeight = choiceContainer.getLayoutParams().height;
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
        View.OnClickListener quizCardResponseClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(true);
                mSelectedChoices.add((QuizCard.Choice) v.getTag(R.id.view_tag_quiz_choice));
                if (quizIsPassed()) {
                    // We're done!
                    toggleQuizResponseExpansion(breadCrumb, choiceContainer);
                    // TODO: Expand next quizCard
                    return;
                }
            }
        };

        /** Populate Quiz choices stack */
        if (hasQuizResponses) {
            Log.i("quiz", String.format("adding %d choices for quiz card ", mDisplayedChoices.size()));
            for (QuizCard.Choice displayedChoice : mDisplayedChoices) {
                // Create Quiz choices
                View quizChoice = inflateAndAddChoiceForQuiz(choiceContainer, displayedChoice);
                quizChoice.setOnClickListener(quizCardResponseClickListener);
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
        // Change breadCrumb title
        breadCrumb.setText(mCardModel.getQuestion());
        // Animate choiceContainer to height 0
        final ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) choiceContainer.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(params.height, 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (int) valueAnimator.getAnimatedValue();
                choiceContainer.setLayoutParams(params);
            }
        });
        animator.start();

        // Animate breadCrumb from colored text on white background to white text on colored background
        ObjectAnimator backgroundColorFade = ObjectAnimator.ofObject(breadCrumb, "backgroundColor", new ArgbEvaluator(), mContext.getResources().getColor(R.color.white), mContext.getResources().getColor(R.color.signature));
        backgroundColorFade.start();
        ObjectAnimator textColorFade = ObjectAnimator.ofObject(breadCrumb, "backgroundColor", new ArgbEvaluator(), mContext.getResources().getColor(R.color.signature), mContext.getResources().getColor(R.color.white));
        textColorFade.start();

        mExpanded = !mExpanded;
    }

    private View inflateAndAddChoiceForQuiz(@NonNull ViewGroup quizChoiceContainer, QuizCard.Choice choice) {

        LayoutInflater inflater = (LayoutInflater) quizChoiceContainer.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView choiceView = (TextView) (inflater.inflate(R.layout.quiz_card_choice, quizChoiceContainer, true)).findViewById(R.id.choiceText);
        choiceView.setText(choice.text);

        return choiceView;
    }

    //returns stored mediaPath (if exists) or exampleMediaPath (if exists)
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
}
