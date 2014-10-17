package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import scal.io.liger.model.Card;
import scal.io.liger.R;
import scal.io.liger.model.ProgressCard;

/**
 * Created by josh on 8/8/14.
 */
public class ProgressCardView implements DisplayableCard {
    private ProgressCard mCardModel;
    private Context mContext;

    public ProgressCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (ProgressCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        // StoryPath spm = mCardModel.getStoryPathReference();

        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_progress, null);

        ((TextView) view.findViewById(R.id.tv_text)).setText(mCardModel.getFilledCount() + " / " + mCardModel.getMaxCount());

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
