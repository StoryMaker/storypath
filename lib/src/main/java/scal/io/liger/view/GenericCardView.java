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
import java.util.ArrayList;

import scal.io.liger.model.Card;
import scal.io.liger.model.GenericCard;
import scal.io.liger.R;


public class GenericCardView extends com.fima.cardsui.objects.Card {

    private GenericCard mCardModel;
    private Context mContext;

    public GenericCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (GenericCard) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_generic, null);

        ImageView ivCardImage = ((ImageView) view.findViewById(R.id.iv_card_image));
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        TextView tvText = ((TextView) view.findViewById(R.id.tv_text));
        LinearLayout llPathsWrapper = (LinearLayout) view.findViewById(R.id.ll_story_paths_wrapper);
        LinearLayout llTextWrapper = (LinearLayout) view.findViewById(R.id.ll_text_wrapper);

        //TODO find better way of checking file is valid
        File mediaFile = new File(mCardModel.getMediaPath());
        if(mediaFile.exists() && !mediaFile.isDirectory()) {
            Bitmap bitmap = BitmapFactory.decodeFile(mCardModel.getMediaPath());
            ivCardImage.setImageBitmap(bitmap);
            ivCardImage.setVisibility(View.VISIBLE);
        }

        String txtHeader = mCardModel.getHeader();
        if(!txtHeader.isEmpty()) {
            tvHeader.setText(txtHeader);
        }

        String txtText = mCardModel.getText();
        if(!txtHeader.isEmpty()) {
            tvText.setText(txtText);
            llTextWrapper.setVisibility(View.VISIBLE);
        }

        //add paths
        ArrayList<String> paths = mCardModel.getStoryPaths();
        if (paths != null) {
            for (final String txtPath : paths) {
                final TextView tvPath = new TextView(mContext);
                tvPath.setText(txtPath);

                llPathsWrapper.addView(tvPath);
                tvPath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, txtPath + "click", Toast.LENGTH_SHORT).show();
                        mCardModel.addValue("value", txtPath); // what was intended key?
                    }
                });
            }
        }

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
