package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import scal.io.liger.model.Card;
import scal.io.liger.model.ClipInstructionListCard;
import scal.io.liger.R;


public class ClipInstructionListCardView extends com.fima.cardsui.objects.Card {

    private ClipInstructionListCard mCardModel;
    private Context mContext;

    public ClipInstructionListCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (ClipInstructionListCard) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_instuction_list, null);
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        TextView tvBulletList = ((TextView) view.findViewById(R.id.tv_bullet_list));
        ImageView ivCardImage = ((ImageView) view.findViewById(R.id.iv_card_image));

        //build out bullet list spaces
        String bulletList = "";
        for (String bulletItem : mCardModel.getBullet_list()) {
            bulletList += ("-" + bulletItem + "\n");
        }

        tvHeader.setText(mCardModel.getHeader());
        tvBulletList.setText(bulletList);

        //TODO find better way of checking file is valid
        File mediaFile = new File(mCardModel.getMedia_path());
        if(mediaFile.exists() && !mediaFile.isDirectory()) {
            Bitmap bitmap = BitmapFactory.decodeFile(mCardModel.getMedia_path());
            ivCardImage.setImageBitmap(bitmap);
        }


        super.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "ClipInstructionList Card click", Toast.LENGTH_SHORT).show();
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
