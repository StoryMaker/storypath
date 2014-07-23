package scal.io.liger.view;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import org.w3c.dom.Text;

import scal.io.liger.CardModel;
import scal.io.liger.CongratsCardModel;
import scal.io.liger.R;


public class CongratsCardView extends Card {

    private CongratsCardModel mCardModel;
    private Context mContext;

    public CongratsCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (CongratsCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_congrats, null);
        LinearLayout llOptionsWrapper = (LinearLayout) view.findViewById(R.id.ll_next_up_options);
        TextView tvHeadline = ((TextView) view.findViewById(R.id.tv_headline));
        TextView tvText = ((TextView) view.findViewById(R.id.tv_text));

        tvHeadline.setText(mCardModel.getHeadline());
        tvText.setText(mCardModel.getText());

        //add options
        for(final String txtOption : mCardModel.getStory_paths()) {
            final TextView tvOption = new TextView(mContext);
            tvOption.setText(txtOption);

            llOptionsWrapper.addView(tvOption);
            tvOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, txtOption + "click", Toast.LENGTH_SHORT).show();
                    mCardModel.addValue(txtOption);
                }
            });
        }

        return view;
    }
}
