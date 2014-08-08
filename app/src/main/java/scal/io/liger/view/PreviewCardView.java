package scal.io.liger.view;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.fima.cardsui.objects.Card;

import java.io.File;

import scal.io.liger.CardModel;
import scal.io.liger.PreviewCardModel;
import scal.io.liger.R;


public class PreviewCardView extends Card {

    private PreviewCardModel mCardModel;
    private Context mContext;

    public PreviewCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (PreviewCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_preview, null);
        VideoView vvCardMedia = ((VideoView) view.findViewById(R.id.vv_card_media));
        TextView tvText= ((TextView) view.findViewById(R.id.tv_text));

        //TODO find better way of checking file is valid
        File mediaFile = new File(mCardModel.getMediaPath());
        if(true) {//mediaFile.exists() && !mediaFile.isDirectory()) {
            MediaController mediaController = new MediaController(mContext);
            mediaController.setAnchorView(vvCardMedia);

            Uri video = Uri.parse(mCardModel.getMediaPath());
            vvCardMedia.setMediaController(mediaController);
            //TODO dont hardcode video file
            vvCardMedia.setVideoPath("file:///android_asset/videosample1.mp4");
            vvCardMedia.start();
        }

        tvText.setText(mCardModel.getText());

        return view;
    }
}
