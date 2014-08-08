package scal.io.liger.view;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.fima.cardsui.objects.Card;

import java.io.File;

import scal.io.liger.CardModel;
import scal.io.liger.ClipCardModel;
import scal.io.liger.R;


public class ClipCardView extends Card {

    private ClipCardModel mCardModel;
    private Context mContext;

    public ClipCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (ClipCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_clip, null);
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        TextView tvType = ((TextView) view.findViewById(R.id.tv_header));
        VideoView vvCardVideo = ((VideoView) view.findViewById(R.id.vv_card_video));
        ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));

        tvHeader.setText(mCardModel.getHeader());
        tvType.setText(mCardModel.getClipType());

        //Set up media display
        //TODO find better way of checking file is valid
        File mediaFile = new File(mCardModel.getMedia_path());

        String clipMedium = mCardModel.getClipMedium();
        if(clipMedium.equals("video")) {
            if(mediaFile.exists() && !mediaFile.isDirectory()) {
                MediaController mediaController = new MediaController(mContext);
                mediaController.setAnchorView(vvCardVideo);

                Uri video = Uri.parse(mCardModel.getMedia_path());
                vvCardVideo.setMediaController(mediaController);
                vvCardVideo.setVideoURI(video);
                vvCardVideo.start();
            }

            ivCardPhoto.setVisibility(View.GONE);

        } else if(clipMedium.equals("photo")) {
            if(mediaFile.exists() && !mediaFile.isDirectory()) {
                Uri uri = Uri.parse(mediaFile.getPath());
                ivCardPhoto.setImageURI(uri);
            }

            vvCardVideo.setVisibility(View.GONE);
        } else if (clipMedium.equals("audio")) {
            //TODO handle audio
        } else {
            //ERROR
        }

        return view;
    }
}
