package scal.io.liger.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.GenericCard;


public class CoverCardView implements DisplayableCard {

    private GenericCard mCardModel;
    private Context mContext;

    public CoverCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (GenericCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_cover, null);

        LinearLayout llTextWrapper = (LinearLayout) view.findViewById(R.id.ll_content_wrapper);
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        TextView tvText = ((TextView) view.findViewById(R.id.tv_text));

        String txtHeader = mCardModel.getHeader();
        if(!txtHeader.isEmpty()) {
            tvHeader.setText(txtHeader);
        }

        String txtText = mCardModel.getText();
        if(!txtHeader.isEmpty()) {
            tvText.setText(txtText);
        }

        //TODO find better way of checking file is valid
        File mediaFile = new File(mCardModel.getMediaPath());
        if(mediaFile.exists() && !mediaFile.isDirectory()) {
            llTextWrapper.setBackground(BitmapDrawable.createFromPath(mCardModel.getMediaPath()));
        }

        //supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}