package scal.io.liger.view;

import android.content.Context;
import android.view.View;

/**
 * Interface replacing the role of cardsui Card
 * Created by davidbrodsky on 10/17/14.
 */
public interface DisplayableCard {

    public View getCardView(Context context);
}
