package scal.io.liger.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.fima.cardsui.objects.Card;

import java.io.File;

import scal.io.liger.CardModel;
import scal.io.liger.ClipCardModel;
import scal.io.liger.Constants;
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
        TextView tvType = ((TextView) view.findViewById(R.id.tv_type));
        Button btnRecord = ((Button) view.findViewById(R.id.btn_record_media));
        VideoView vvCardVideo = ((VideoView) view.findViewById(R.id.vv_card_video));
        ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));

        tvHeader.setText(mCardModel.getHeader());
        tvType.setText(mCardModel.getClipType());

        final String clipMedium = mCardModel.getClipMedium();
        final String storyPathId = mCardModel.getStoryPathReference().getId() + "::" + mCardModel.getId() + "::" + "media_path";

        String mediaPath = mCardModel.getValueById(storyPathId);
        //if no media
        if (null == mediaPath) {
            btnRecord.setVisibility(View.VISIBLE);

            btnRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    int requestId = -1;

                    if(clipMedium.equals("video")) {
                        intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        requestId = Constants.REQUEST_VIDEO_CAPTURE;

                    } else if(clipMedium.equals("photo")) {
                        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        requestId = Constants.REQUEST_IMAGE_CAPTURE;

                    }  else if(clipMedium.equals("audio")) {
                        intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                        requestId = Constants.REQUEST_AUDIO_CAPTURE;
                    }

                    if (null != intent && intent.resolveActivity(mContext.getPackageManager()) != null) {
                        intent.putExtra(Constants.EXTRA_PATH_ID, storyPathId);
                        ((Activity) mContext).startActivityForResult(intent, requestId);
                    }
                }
            });

            return view;
        }

        //set up media display
        //TODO find better way of checking file is valid
        File mediaFile = new File(mediaPath);

        if(clipMedium.equals("video")) {
            if(mediaFile.exists() && !mediaFile.isDirectory()) {
                MediaController mediaController = new MediaController(mContext);
                mediaController.setAnchorView(vvCardVideo);

                Uri video = Uri.parse(mediaFile.getPath());
                vvCardVideo.setMediaController(mediaController);
                vvCardVideo.setVideoURI(video);
                vvCardVideo.start();
            }

            vvCardVideo.setVisibility(View.VISIBLE);
        } else if(clipMedium.equals("photo")) {
            if(mediaFile.exists() && !mediaFile.isDirectory()) {
                Uri uri = Uri.parse(mediaFile.getPath());
                ivCardPhoto.setImageURI(uri);
            }

            ivCardPhoto.setVisibility(View.VISIBLE);
        } else if (clipMedium.equals("audio")) {
            //TODO handle audio
        } else {
            //TODO handle invalid-medium error
        }

        return view;
    }
}
