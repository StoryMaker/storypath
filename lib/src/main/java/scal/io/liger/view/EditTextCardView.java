package scal.io.liger.view;

import timber.log.Timber;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.EditTextCard;


public class EditTextCardView implements DisplayableCard {

    private EditTextCard mCardModel;
    private Context mContext;

    public EditTextCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (EditTextCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
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
        etText.setHint(mCardModel.getHintText());
        etText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    mCardModel.clearValues();
                    mCardModel.addValue("value", v.getText().toString()); // what was intended key?
                    Timber.d("editing done: " + v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        final Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCardModel.clearValues();
                mCardModel.addValue("value", button.getText().toString()); // what was intended key?
                Timber.d("editing done: " + button.getText().toString());
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
