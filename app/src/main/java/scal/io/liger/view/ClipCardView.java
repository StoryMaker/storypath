package scal.io.liger.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.fima.cardsui.objects.Card;

import java.io.File;
import java.io.IOException;

import scal.io.liger.JsonHelper;
import scal.io.liger.model.CardModel;
import scal.io.liger.model.ClipCardModel;
import scal.io.liger.Constants;
import scal.io.liger.R;


public class ClipCardView extends Card {

    private ClipCardModel mCardModel;
    private Context mContext;
    private static final String MEDIA_PATH_KEY = "value";

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
        VideoView vvCardVideo = ((VideoView) view.findViewById(R.id.vv_card_video));
        ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        TextView tvType = ((TextView) view.findViewById(R.id.tv_type));
        Button btnRecord = ((Button) view.findViewById(R.id.btn_record_media));
        ToggleButton btnAudioPlay = ((ToggleButton) view.findViewById(R.id.tb_card_audio));

        tvHeader.setText(mCardModel.getHeader());
        tvType.setText(mCardModel.getClipType());

        final String clipMedium = mCardModel.getClipMedium();
        final String cardMediaId = mCardModel.getStoryPathReference().getId() + "::" + mCardModel.getId() + "::" + MEDIA_PATH_KEY;

        //set up media display
        File mediaFile = getValidFile(mCardModel.getValueByKey(MEDIA_PATH_KEY), mCardModel.getExampleMediaPath());

        if (mediaFile == null) {
            String clipType = mCardModel.getClipType();

            if (clipType.equals(Constants.CHARACTER)) {
                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_close));
            } else if (clipType.equals(Constants.ACTION)) {
                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_medium));
            } else if (clipType.equals(Constants.RESULT)){
                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_long));
            } else {
                //TODO handle invalid clip type
                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher));
            }

            ivCardPhoto.setVisibility(View.VISIBLE);
        } else if (mediaFile.exists() && !mediaFile.isDirectory()) {
            if (clipMedium.equals(Constants.VIDEO)) {
                MediaController mediaController = new MediaController(mContext);
                mediaController.setAnchorView(vvCardVideo);

                Uri video = Uri.parse(mediaFile.getPath());
                vvCardVideo.setMediaController(mediaController);
                vvCardVideo.setVideoURI(video);
                vvCardVideo.seekTo(5);

                vvCardVideo.setVisibility(View.VISIBLE);
            } else if (clipMedium.equals(Constants.PHOTO)) {
                Uri uri = Uri.parse(mediaFile.getPath());
                ivCardPhoto.setImageURI(uri);

                ivCardPhoto.setVisibility(View.VISIBLE);
            } else if (clipMedium.equals(Constants.AUDIO)) {
                Uri myUri = Uri.parse(mediaFile.getPath());
                final MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(mContext, myUri);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                btnAudioPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mediaPlayer.seekTo(5);
                            mediaPlayer.start();
                        } else {
                            mediaPlayer.pause();
                        }
                    }
                });

                mediaPlayer.seekTo(5);
                btnAudioPlay.setVisibility(View.VISIBLE);
            } else {
                //TODO handle invalid-medium error
            }
        }

        //set correct listener for record button
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                int requestId = -1;

                if(clipMedium.equals(Constants.VIDEO)) {
                    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    requestId = Constants.REQUEST_VIDEO_CAPTURE;

                } else if(clipMedium.equals(Constants.PHOTO)) {
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    requestId = Constants.REQUEST_IMAGE_CAPTURE;

                }  else if(clipMedium.equals(Constants.AUDIO)) {
                    intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    requestId = Constants.REQUEST_AUDIO_CAPTURE;
                }

                if (null != intent && intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString(Constants.PREFS_CALLING_CARD_ID, cardMediaId).apply(); // FIXME should be done off the ui thread
                    ((Activity) mContext).startActivityForResult(intent, requestId);
                }
            }
        });

        return view;
    }

    //returns stored mediaPath (if exists) or exampleMediaPath (if exists)
    private File getValidFile(String mediaPath, String exampleMediaPath) {
        File mediaFile = null;

        if (mediaPath != null) {
            mediaFile = new File(mediaPath);
        } else if (exampleMediaPath != null) {
            mediaFile = new File(exampleMediaPath);
        }

        return mediaFile;
    }
}
