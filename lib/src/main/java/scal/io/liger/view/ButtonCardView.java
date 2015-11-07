package scal.io.liger.view;

import timber.log.Timber;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import scal.io.liger.model.ButtonCard;
import scal.io.liger.model.Card;
import scal.io.liger.R;
import scal.io.liger.model.StoryPath;

public class ButtonCardView implements DisplayableCard{
    private ButtonCard mCardModel;
    private Context mContext;

    public ButtonCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (ButtonCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_progress_button, null);
        TextView btnCardButton = ((TextView) view.findViewById(R.id.btn_card));

        String btnText = mCardModel.getText();

        if(btnText.isEmpty()) {
            btnText = "Next";
        }

        btnCardButton.setText(btnText);

        btnCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StoryPath spm = mCardModel.getStoryPath();
                //Card cm = spm.getValidCardFromIndex(spm.getValidCardIndex(mCardModel));

                mCardModel.clearValues();
                mCardModel.addValue("value", "true");
                // moveToNextCard();
                moveToThisCard();

                // currently broken in several places
                //String linkPath = spm.getId() + "::" + cm.getId();
                //spm.linkNotification(linkPath);
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    /*
    private void moveToNextCard() {
        StoryPathModel spm = mCardModel.getStoryPath();
        CardModel cm = spm.getValidCardFromIndex(spm.getValidCardIndex(mCardModel) + 1);
        String linkPath = spm.getId() + "::" + cm.getId();
        spm.linkNotification(linkPath);
    }
    */

    private void moveToThisCard() {
        StoryPath sp = mCardModel.getStoryPath();
        String linkPath = sp.getId() + "::" + mCardModel.getId();
        sp.linkNotification(linkPath);
    }
}