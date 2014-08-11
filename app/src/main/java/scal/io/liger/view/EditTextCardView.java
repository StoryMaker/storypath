package scal.io.liger.view;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.fima.cardsui.objects.Card;

import scal.io.liger.R;
import scal.io.liger.model.EditTextCardModel;
import scal.io.liger.model.CardModel;


public class EditTextCardView extends Card {

    private EditTextCardModel mCardModel;
    private Context mContext;

    public EditTextCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (EditTextCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_edit_text, null);

        String txtHeader = mCardModel.getHeader();
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        if(!txtHeader.isEmpty()) {
            tvHeader.setText(txtHeader);
        }

        EditText etText = ((EditText) view.findViewById(R.id.et_text));
        etText.setHint(mCardModel.getHint_text());
        etText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    mCardModel.clearValues();
                    mCardModel.addValue(v.getText().toString());
                    Log.d("EditTextCardView", "editing done: " + v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        return view;
    }
}
