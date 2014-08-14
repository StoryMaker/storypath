package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import scal.io.liger.R;
import scal.io.liger.model.CardModel;
import scal.io.liger.model.LinkCardModel;

public class LinkCardView extends Card {
    private LinkCardModel mCardModel;
    private Context mContext;

    public LinkCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (LinkCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
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

        return view;
    }
}