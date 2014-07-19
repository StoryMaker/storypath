package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.CardModel;
import scal.io.liger.ClipInstructionListCardModel;
import scal.io.liger.IntroCardModel;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.SelfEvalCardModel;

public class SelfEvalCardView extends Card {

    private SelfEvalCardModel mCardModel;
    private Context mContext;

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
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        Button btnContinue = (Button) view.findViewById(R.id.btn_continue);
        final CheckBox cbAddIntroCard = ((CheckBox) view.findViewById(R.id.cb_add_intro_card));
        final CheckBox cbAddClipInstructionCard = ((CheckBox) view.findViewById(R.id.cb_add_clip_instuction_card));

        tvHeader.setText(mCardModel.getHeader());

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity mainActivity = (MainActivity) mContext;

                if(cbAddIntroCard.isChecked()) {
                    IntroCardModel introCardModel = new IntroCardModel();
                    introCardModel.setHeadline("Dynamically Added!");
                    introCardModel.setLevel("Mad Skillz");
                    introCardModel.setTime("1 hour");

                    mainActivity.addCardModel(introCardModel);
                }

                if (cbAddClipInstructionCard.isChecked()) {
                    ClipInstructionListCardModel clipInstructionListCardModel = new ClipInstructionListCardModel();
                    clipInstructionListCardModel.setHeader("Dynamic Header");

                    ArrayList<String> bulletPoints = new ArrayList<String>();
                    bulletPoints.add("Dynamic Point 0");
                    bulletPoints.add("Dynamic Point 1");
                    bulletPoints.add("Dynamic Point 2");
                    clipInstructionListCardModel.setBullet_list(bulletPoints);

                    mainActivity.addCardModel(clipInstructionListCardModel);
                }

                Toast.makeText(mContext, "Action Complete", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
