package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.LinkCard;

public class LinkCardView implements DisplayableCard {
    private LinkCard mCardModel;
    private Context mContext;

    public LinkCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (LinkCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_progress_button, null);
        TextView linkCardButton = ((TextView) view.findViewById(R.id.btn_card_button));

        String btnText = mCardModel.getText();

        if(btnText.isEmpty()) {
            btnText = "Next";
        }

        linkCardButton.setText(btnText);

        linkCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardModel.linkNotification(mCardModel.getLink());
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}