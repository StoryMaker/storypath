package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import scal.io.liger.Constants;
import scal.io.liger.model.Card;
import scal.io.liger.model.ChooseMediumCard;
import scal.io.liger.R;
import scal.io.liger.model.StoryPath;


public class ChooseMediumCardView implements DisplayableCard {
    private ChooseMediumCard mCardModel;
    private Context mContext;

    private Button mBtnMediumVideo;
    private Button mBtnMediumAudio;
    private Button mBtnMediumPhoto;

    public ChooseMediumCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (ChooseMediumCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_choose_medium, null);
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        mBtnMediumVideo = ((Button) view.findViewById(R.id.btn_medium_video));
        mBtnMediumAudio = ((Button) view.findViewById(R.id.btn_medium_audio));
        mBtnMediumPhoto = ((Button) view.findViewById(R.id.btn_medium_photo));

        tvHeader.setText(mCardModel.getHeader());

        mBtnMediumVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardModel.clearValues();
                mCardModel.addValue("value", Constants.VIDEO);
                highlightButton(v);

                moveToNextCard();
            }
        });

        mBtnMediumAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardModel.clearValues();
                mCardModel.addValue("value", Constants.AUDIO);
                highlightButton(v);

                moveToNextCard();
            }
        });

        mBtnMediumPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardModel.clearValues();
                mCardModel.addValue("value", Constants.PHOTO);
                highlightButton(v);

                moveToNextCard();
            }
        });

        String value = mCardModel.getValueByKey("value");
        if (value != null) {
            if (value.equals(Constants.VIDEO)) {
                mBtnMediumVideo.setBackgroundColor(mContext.getResources().getColor(R.color.dark_grey));
                mBtnMediumVideo.setTextColor(mContext.getResources().getColor(R.color.white));
            } else if (value.equals(Constants.AUDIO)) {
                mBtnMediumAudio.setBackgroundColor(mContext.getResources().getColor(R.color.dark_grey));
                mBtnMediumAudio.setTextColor(mContext.getResources().getColor(R.color.white));
            } else if (value.equals(Constants.PHOTO)) {
                mBtnMediumPhoto.setBackgroundColor(mContext.getResources().getColor(R.color.dark_grey));
                mBtnMediumPhoto.setTextColor(mContext.getResources().getColor(R.color.white));
            }
        }

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    private void highlightButton(View button) {
        mBtnMediumVideo.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        mBtnMediumAudio.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        mBtnMediumPhoto.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        button.setBackgroundColor(mContext.getResources().getColor(R.color.dark_grey));
        ((Button) button).setTextColor(mContext.getResources().getColor(R.color.white));
    }

    private void moveToNextCard() {
        StoryPath spm = mCardModel.getStoryPathReference();
        Card cm = spm.getValidCardFromIndex(spm.getValidCardIndex(mCardModel) + 1);
        String linkPath = spm.getId() + "::" + cm.getId();
        spm.linkNotification(linkPath);
    }
}
