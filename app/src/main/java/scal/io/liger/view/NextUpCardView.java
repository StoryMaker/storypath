package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import scal.io.liger.R;
import scal.io.liger.model.CardModel;
import scal.io.liger.model.LinkModel;
import scal.io.liger.model.NextUpCardModel;

public class NextUpCardView extends Card {
    private NextUpCardModel mCardModel;
    private Context mContext;

    public NextUpCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (NextUpCardModel)cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        LinearLayout ll = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.card_next_up, null);
        TextView tv = (TextView) ll.findViewById(R.id.tv_text);
        tv.setText(mCardModel.getText());
        
        for (LinkModel link : mCardModel.getLinks()) {
            Button linkButton = new Button(mContext);

            final String text = link.getLink_text();
            final String path = link.getLink_path();

            linkButton.setText(text);
            linkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCardModel.linkNotification(path);
                }
            });

            ll.addView(linkButton);
        }

        return ll;
    }
}