package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import scal.io.liger.CardModel;
import scal.io.liger.ClipCardModel;
import scal.io.liger.R;
import scal.io.liger.widget.BasicTextCardModel;


public class ClipCardView extends Card {

    private ClipCardModel mCardModel;
    private Context mContext;

    public ClipCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (ClipCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_clip, null);
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));

        return view;
    }
}
