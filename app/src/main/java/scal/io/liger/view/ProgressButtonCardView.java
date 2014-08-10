package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import scal.io.liger.model.ProgressButtonCardModel;
import scal.io.liger.model.CardModel;
import scal.io.liger.R;

public class ProgressButtonCardView extends Card {
    private ProgressButtonCardModel mCardModel;
    private Context mContext;

    public ProgressButtonCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (ProgressButtonCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_progress_button, null);
        TextView btnCardButton = ((TextView) view.findViewById(R.id.btn_card_button));

        String btnText = mCardModel.getText();

        if(btnText.isEmpty()) {
            btnText = "Next";
        }

        btnCardButton.setText(btnText);

        btnCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Button Click", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}