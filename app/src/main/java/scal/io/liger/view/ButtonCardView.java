package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import scal.io.liger.model.ButtonCardModel;
import scal.io.liger.model.CardModel;
import scal.io.liger.R;
import scal.io.liger.model.StoryPathModel;

public class ButtonCardView extends Card {
    private ButtonCardModel mCardModel;
    private Context mContext;

    public ButtonCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (ButtonCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_progress_button, null);
        TextView btnCardButton = ((TextView) view.findViewById(R.id.btn_card_button));

        String btnText = mCardModel.getText();

        if(btnText.isEmpty()) {
            btnText = "Next";
        }

        btnCardButton.setText(btnText);

        btnCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardModel.addValue("value::clicked");
                moveToNextCard();
            }
        });

        return view;
    }

    private void moveToNextCard() {
        StoryPathModel spm = mCardModel.getStoryPathReference();
        CardModel cm = spm.getValidCardFromIndex(spm.getValidCardIndex(mCardModel) + 1);
        String linkPath = spm.getId() + "::" + cm.getId();
        spm.linkNotification(linkPath);
    }
}