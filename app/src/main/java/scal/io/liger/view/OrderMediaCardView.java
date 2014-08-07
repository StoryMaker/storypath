package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import scal.io.liger.CardModel;
import scal.io.liger.OrderMediaCardModel;
import scal.io.liger.R;

public class OrderMediaCardView extends Card {
    private OrderMediaCardModel mCardModel;
    private Context mContext;

    public OrderMediaCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (OrderMediaCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_order_media, null);

        return view;
    }
}