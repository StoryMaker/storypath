package scal.io.liger.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import scal.io.liger.model.Card;
import scal.io.liger.model.IntroCard;
import scal.io.liger.R;


public class IntroCardView implements DisplayableCard {

    private IntroCard mCardModel;
    private Context mContext;

    public IntroCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (IntroCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_intro, null);
        ImageView ivCardImage = ((ImageView) view.findViewById(R.id.iv_card_image));
        TextView tvHeadline = ((TextView) view.findViewById(R.id.tv_headline));
        TextView tvLevel = ((TextView) view.findViewById(R.id.tv_level));
        TextView tvTime = ((TextView) view.findViewById(R.id.tv_time));

        //TODO set ivCardImage from model.getMediaPath()

        //Uri uri = Uri.parse(mediaFile.getPath());
        Uri uri = null;

        try {
            String uriString = mCardModel.getExampleMediaFile().getExampleURI(mCardModel);
            if (uriString != null) {
                uri = Uri.parse(uriString);
                ivCardImage.setImageURI(uri);
            } else {
                Drawable drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_character); // FIXME replace this with a more sensible placeholder default image
                ivCardImage.setImageDrawable(drawable);
            }
        } catch (NullPointerException npe) {
            // sample media file probably missing, handle the same as the default case
            Drawable drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_character); // FIXME replace this with a more sensible placeholder default image
            ivCardImage.setImageDrawable(drawable);
        }
        ivCardImage.setVisibility(View.VISIBLE);

        tvHeadline.setText(mCardModel.getHeadline());
        tvLevel.setText(mCardModel.getLevel());
        tvTime.setText(mCardModel.getTime());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Intro Card click", Toast.LENGTH_SHORT).show();
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
