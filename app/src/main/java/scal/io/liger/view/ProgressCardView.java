package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import scal.io.liger.CardModel;
import scal.io.liger.R;
import scal.io.liger.ProgressCardModel;
import scal.io.liger.StoryPathModel;

/**
 * Created by josh on 8/8/14.
 */
public class ProgressCardView extends Card {
    private ProgressCardModel mCardModel;
    private Context mContext;

    public ProgressCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (ProgressCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
//        StoryPathModel spm = mCardModel.getStoryPathReference();

        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_progress, null);
//        TextView btnCardButton = ((TextView) view.findViewById(R.id.btn_card_button));
//
//        String btnText = mCardModel.getText();
//
//        if(btnText.isEmpty()) {
//            btnText = "Next";
//        }
//
//        btnCardButton.setText(btnText);
//
//        btnCardButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext, "Button Click", Toast.LENGTH_SHORT).show();
//            }
//        });

        return view;
    }
}
