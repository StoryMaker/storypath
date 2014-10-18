package scal.io.liger.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import scal.io.liger.R;

/**
 * Created by davidbrodsky on 10/17/14.
 */
public class ClipPlaybackAndTrimDialogFragment extends DialogFragment {

    private View mView;

    public static ClipPlaybackAndTrimDialogFragment newInstance() {
        ClipPlaybackAndTrimDialogFragment f = new ClipPlaybackAndTrimDialogFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_clip_playback_trim, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v)
                .setPositiveButton("TRIM CLIP", null)
                .setNegativeButton("CANCEL", null);
        mView = v;
        return builder.create();
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
