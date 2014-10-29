package scal.io.liger.view;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import scal.io.liger.Constants;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.model.FullMetadata;
import scal.io.liger.model.PublishButtonCard;
import scal.io.liger.model.Card;
import scal.io.liger.model.StoryPath;

public class PublishButtonCardView implements DisplayableCard{
    private PublishButtonCard mCardModel;
    private Context mContext;

    public PublishButtonCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (PublishButtonCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_progress_button, null);
        TextView btnCardButton = ((TextView) view.findViewById(R.id.btn_card));

        String btnText = mCardModel.getText();

        if(btnText.isEmpty()) {
            btnText = "Next";
        }

        btnCardButton.setText(btnText);

        btnCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TEMP
                final MainActivity mainActivity = (MainActivity) mCardModel.getStoryPath().getContext(); // FIXME this isn't a safe cast as context can sometimes not be an activity (getApplicationContext())
                Handler h = new Handler();

                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        StoryPath spm = mCardModel.getStoryPath();
                        ArrayList<FullMetadata> exportMetadata = spm.exportAllMetadata();
                        Intent i = new Intent();
                        i.setAction(Constants.ACTION_PUBLISH);
                        i.putParcelableArrayListExtra("export_metadata", exportMetadata);
                        mainActivity.startActivity(i);
                        int iasdfasd = 0;
                        mainActivity.finish();
                    }
                }, 0);
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId()); // FIXME move this into the base class

        return view;
    }

    /*
    private void moveToNextCard() {
        StoryPathModel spm = mCardModel.getStoryPath();
        CardModel cm = spm.getValidCardFromIndex(spm.getValidCardIndex(mCardModel) + 1);
        String linkPath = spm.getId() + "::" + cm.getId();
        spm.linkNotification(linkPath);
    }
    */
}