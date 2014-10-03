package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import scal.io.liger.model.Card;
import scal.io.liger.model.IntroCard;
import scal.io.liger.R;


public class IntroCardView extends com.fima.cardsui.objects.Card {

    private IntroCard mCardModel;
    private Context mContext;

    public IntroCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (IntroCard) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_intro, null);
        ImageView ivCardImage = ((ImageView) view.findViewById(R.id.iv_card_image));
        TextView tvHeadline = ((TextView) view.findViewById(R.id.tv_headline));
        TextView tvLevel = ((TextView) view.findViewById(R.id.tv_level));
        TextView tvTime = ((TextView) view.findViewById(R.id.tv_time));

        //TODO set ivCardImage from model.getMediaPath()

        tvHeadline.setText(mCardModel.getHeadline());
        tvLevel.setText(mCardModel.getLevel());
        tvTime.setText(mCardModel.getTime());

        super.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Intro Card click", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
