package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import scal.io.liger.IntroCardModel;
import scal.io.liger.R;


public class IntroCardView extends Card {

    private View.OnClickListener mListener;
    private IntroCardModel mCardModel;

    public IntroCardView(IntroCardModel cardModel) {
        mCardModel = cardModel;
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        mListener = listener;
        super.setOnClickListener(mListener);
    }

    @Override
    public View getCardContent(Context context) {
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_intro, null);
        TextView tvHeadline = ((TextView) view.findViewById(R.id.tv_headline));
        TextView tvLevel = ((TextView) view.findViewById(R.id.tv_level));
        TextView tvTime = ((TextView) view.findViewById(R.id.tv_time));

        tvHeadline.setText(mCardModel.getHeadline());
        tvLevel.setText(mCardModel.getLevel());
        tvTime.setText(mCardModel.getTime());

        view.setOnClickListener(mListener);

        return view;
    }
}
