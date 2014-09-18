package scal.io.liger.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import scal.io.liger.model.CardModel;
import scal.io.liger.R;
import scal.io.liger.model.ProgressCardModel;

/**
 * Created by josh on 8/8/14.
 */
public class ProgressCardView extends Card {
    private ProgressCardModel mCardModel;
    private Context mContext;

    public ProgressCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (ProgressCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
//        StoryPathModel spm = mCardModel.getStoryPathReference();

        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_progress, null);

        ((TextView) view.findViewById(R.id.tv_text)).setText(mCardModel.getFilledCount() + " / " + mCardModel.getMaxCount());

        return view;
    }
}
