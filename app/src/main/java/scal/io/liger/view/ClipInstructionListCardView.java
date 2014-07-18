package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import scal.io.liger.CardModel;
import scal.io.liger.ClipInstructionListCardModel;
import scal.io.liger.R;

public class ClipInstructionListCardView extends Card {

    private ClipInstructionListCardModel mCardModel;
    private Context mContext;

    public ClipInstructionListCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (ClipInstructionListCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_instuction_list, null);
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        TextView tvBulletList = ((TextView) view.findViewById(R.id.tv_bullet_list));
        ImageView ivCardImage = ((ImageView) view.findViewById(R.id.cardImage));

        //build out bullet list spaces
        String bulletList = "";
        for (String bulletItem : mCardModel.getBullet_list()) {
            bulletList += ("-" + bulletItem + "\n");
        }

        tvHeader.setText(mCardModel.getHeader());
        tvBulletList.setText(bulletList);

        //TODO set ivCardImage from model.getMediaPath()

        super.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "ClipInstructionList Card click", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
