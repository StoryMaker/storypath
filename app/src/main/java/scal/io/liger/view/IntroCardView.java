package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import scal.io.liger.model.CardModel;
import scal.io.liger.model.IntroCardModel;
import scal.io.liger.R;


public class IntroCardView extends Card {

    private IntroCardModel mCardModel;
    private Context mContext;

    public IntroCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (IntroCardModel) cardModel;
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
