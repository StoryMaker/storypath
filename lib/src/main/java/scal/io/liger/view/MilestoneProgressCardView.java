package scal.io.liger.view;

import timber.log.Timber;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import scal.io.liger.model.Card;
import scal.io.liger.R;
import scal.io.liger.model.MilestoneProgressCard;

/**
 * Created by josh on 8/8/14.
 */
public class MilestoneProgressCardView implements DisplayableCard {
    private MilestoneProgressCard mCardModel;
    private Context mContext;

    public MilestoneProgressCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (MilestoneProgressCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_milestone_progress, null);
        ((TextView) view.findViewById(R.id.tv_text)).setText(mCardModel.getFilledCount() + " / " + mCardModel.getMaxCount());

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
