package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import scal.io.liger.CardModel;
import scal.io.liger.R;
import scal.io.liger.BasicTextCardModel;


public class BasicTextCardView extends Card {

    private BasicTextCardModel mCardModel;
    private Context mContext;

    public BasicTextCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (BasicTextCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_basic_text, null);
        TextView tvText = ((TextView) view.findViewById(R.id.tv_text));

        tvText.setText(mCardModel.getText());

        return view;
    }
}
