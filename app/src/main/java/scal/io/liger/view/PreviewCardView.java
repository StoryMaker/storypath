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

        String path = mCardModel.getMedia_path();
        if ((path == null) && (mCardModel.getMedia_id() != null)) {
            path = mCardModel.getStoryPathReference().getReferencedValue(mCardModel.getMedia_id());
        }
        //TODO find better way of checking file is valid
        File mediaFile = new File(path);
        if(mediaFile.exists() && !mediaFile.isDirectory()) {
            MediaController mediaController = new MediaController(mContext);
            mediaController.setAnchorView(vvCardMedia);

            Uri video = Uri.parse(path);
            vvCardMedia.setMediaController(mediaController);
            vvCardMedia.setVideoURI(video);
            vvCardMedia.seekTo(5); // seems to be need to be done to show its thumbnail?
        }

        tvText.setText(mCardModel.getText());

        return view;
    }
}
