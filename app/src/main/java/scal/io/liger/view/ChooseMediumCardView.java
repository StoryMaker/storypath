package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.cardsui.objects.Card;

import scal.io.liger.CardModel;
import scal.io.liger.ChooseMediumCardModel;
import scal.io.liger.R;


public class ChooseMediumCardView extends Card {

    private ChooseMediumCardModel mCardModel;
    private Context mContext;

    public ChooseMediumCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (ChooseMediumCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_choose_medium, null);
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        Button btnMediumVideo = ((Button) view.findViewById(R.id.btn_medium_video));
        Button btnMediumAudio = ((Button) view.findViewById(R.id.btn_medium_audio));
        Button btnMediumPhoto = ((Button) view.findViewById(R.id.btn_medium_photo));

        tvHeader.setText(mCardModel.getHeader());

        btnMediumVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Video click", Toast.LENGTH_SHORT).show();
            }
        });

        btnMediumAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Audio click", Toast.LENGTH_SHORT).show();
            }
        });

        btnMediumPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Photo click", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
