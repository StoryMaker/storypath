package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import scal.io.liger.model.Card;
import scal.io.liger.model.ClipInstructionTypeCard;
import scal.io.liger.R;


public class ClipInstructionTypeCardView extends com.fima.cardsui.objects.Card {

    private ClipInstructionTypeCard mCardModel;
    private Context mContext;

    public ClipInstructionTypeCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (ClipInstructionTypeCard) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_instuction_type, null);
        LinearLayout llOptionsWrapper = (LinearLayout) view.findViewById(R.id.ll_clip_options);
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        ImageView ivCardImage = ((ImageView) view.findViewById(R.id.iv_card_image));

        tvHeader.setText(mCardModel.getHeader());

        //TODO find better way of checking file is valid
        File mediaFile = new File(mCardModel.getMedia_path());
        if(mediaFile.exists() && !mediaFile.isDirectory()) {
            Bitmap bitmap = BitmapFactory.decodeFile(mCardModel.getMedia_path());
            ivCardImage.setImageBitmap(bitmap);
        }

        //add options
        for(final String txtOption : mCardModel.getClip_types()) {
            final TextView tvOption = new TextView(mContext);
            tvOption.setText(txtOption);

            llOptionsWrapper.addView(tvOption);
            tvOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext, txtOption + "click", Toast.LENGTH_SHORT).show();
                    mCardModel.addValue("value", txtOption); // what was intended key?
                }
            });
        }

        return view;
    }
}
