package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.CardModel;
import scal.io.liger.R;
import scal.io.liger.SelfEvalCardModel;


public class SelfEvalCardView extends Card {

    private SelfEvalCardModel mCardModel;
    private Context mContext;
    private ArrayList<CheckBox> cbOptionsList;

    public SelfEvalCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (SelfEvalCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_self_eval, null);
        LinearLayout llOptionsWrapper = (LinearLayout) view.findViewById(R.id.ll_self_eval_options);
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        Button btnContinue = (Button) view.findViewById(R.id.btn_continue);

        CheckBox cbOption;
        cbOptionsList = new ArrayList<CheckBox>();

        tvHeader.setText(mCardModel.getHeader());

        //add checkbox options
        for(String txtOption : mCardModel.getChecklist()) {
            cbOption = new CheckBox(mContext);
            cbOption.setText(txtOption);

            llOptionsWrapper.addView(cbOption);
            cbOptionsList.add(cbOption);
        }

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cbCurrent;
                String value;

                for(int i=0; i < cbOptionsList.size(); i++) {
                    cbCurrent = cbOptionsList.get(i);

                    if(cbCurrent.isChecked()) {
                        value = String.format("value_%d::true", i);
                        mCardModel.addValue(value);
                    }
                }
            }
        });

        return view;
    }
}
