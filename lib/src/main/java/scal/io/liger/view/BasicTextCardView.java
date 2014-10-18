package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import scal.io.liger.R;
import scal.io.liger.model.BasicTextCard;
import scal.io.liger.model.Card;

public class BasicTextCardView implements DisplayableCard {

    private BasicTextCard mCardModel;
    private Context mContext;

    public BasicTextCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (BasicTextCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_basic_text, null);
        TextView tvText = ((TextView) view.findViewById(R.id.tv_text));

        tvText.setText(mCardModel.getText());

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
