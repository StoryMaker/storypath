package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

import java.io.IOException;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.ExampleCard;

public class ExampleCardView implements DisplayableCard {

    public ExampleCard mCardModel;
    public Context mContext;
    public static final String MEDIA_PATH_KEY = "value";

    public MediaController mMediaController;

    public ExampleCardView() {
        // empty, required for ClipCardView
    }

    public ExampleCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (ExampleCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_example, null);
        final VideoView vvCardVideo = ((VideoView) view.findViewById(R.id.vv_card_video));
        final ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));
        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
        final ToggleButton btnMediaPlay = ((ToggleButton) view.findViewById(R.id.tb_card_audio));

        tvHeader.setText(mCardModel.getHeader());

        final String medium = mCardModel.getMedium();
        final String cardMediaId = mCardModel.getStoryPath().getId() + "::" + mCardModel.getId() + "::" + MEDIA_PATH_KEY;

        //set up media display
        //final File mediaFile = getValidFile(null, mCardModel.getExampleMediaPath());

        //if (mediaFile == null) {
        if ((mCardModel.getExampleMediaFile() == null) ||
            (mCardModel.getExampleMediaFile().getExampleURI(mCardModel) == null)) {
            // using medium cliptype image as default in case media file is missing
            ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_medium));
            ivCardPhoto.setVisibility(View.VISIBLE);
        } else { //if (mediaFile.exists() && !mediaFile.isDirectory()) {
            if (medium.equals(Constants.VIDEO)) {

                //set up image as preview
                //Bitmap videoFrame = Utility.getFrameFromVideo(mediaFile.getPath());
                Bitmap videoFrame = mCardModel.getExampleMediaFile().getExampleThumbnail(mCardModel);
                if(null != videoFrame) {
                    ivCardPhoto.setImageBitmap(videoFrame);
                }

                ivCardPhoto.setVisibility(View.VISIBLE);
                btnMediaPlay.setVisibility(View.VISIBLE);
                btnMediaPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Uri video = Uri.parse(mediaFile.getPath());

                        // need to extract video to a temp file before playing
                        Uri video = Uri.parse(mCardModel.getExampleMediaFile().getExampleURI(mCardModel));
                        vvCardVideo.setVideoURI(video);
                        vvCardVideo.seekTo(5);
                        vvCardVideo.setMediaController(null);
                        vvCardVideo.setVisibility(View.VISIBLE);
                        ivCardPhoto.setVisibility(View.GONE);
                        btnMediaPlay.setVisibility(View.GONE);
                        vvCardVideo.start();
                    }
                });

                //revert back to image on video completion
                vvCardVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        vvCardVideo.setVisibility(View.GONE);
                        ivCardPhoto.setVisibility(View.VISIBLE);
                        btnMediaPlay.setVisibility(View.VISIBLE);
                        btnMediaPlay.setChecked(false);
                    }
                });
            } else if (medium.equals(Constants.PHOTO)) {
                //Uri uri = Uri.parse(mediaFile.getPath());
                Uri uri = Uri.parse(mCardModel.getExampleMediaFile().getExampleURI(mCardModel));
                ivCardPhoto.setImageURI(uri);
                ivCardPhoto.setVisibility(View.VISIBLE);
            } else if (medium.equals(Constants.AUDIO)) {
                //Uri myUri = Uri.parse(mediaFile.getPath());
                Uri myUri = Uri.parse(mCardModel.getExampleMediaFile().getExampleURI(mCardModel));
                final MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                //set background image (using medium cliptype image as placeholder)
                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_medium));
                ivCardPhoto.setVisibility(View.VISIBLE);

                //set up media player
                try {
                    mediaPlayer.setDataSource(mContext, myUri);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                btnMediaPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            mediaPlayer.seekTo(5);
                            mediaPlayer.start();
                        } else {
                            mediaPlayer.pause();
                        }
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer arg0) {
                        btnMediaPlay.setChecked(false);
                    }
                });

                mediaPlayer.seekTo(5);
                btnMediaPlay.setVisibility(View.VISIBLE);
            } else {
                //TODO handle invalid-medium error
            }
        }

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    //returns stored mediaPath (if exists) or exampleMediaPath (if exists)
    /*
    public File getValidFile(String mediaPath, String exampleMediaPath) {
        File mediaFile = null;

        if (mediaPath != null) {
            mediaFile = MediaHelper.loadFileFromPath(mCardModel.getStoryPath().buildZipPath(mediaPath));
        } else if (exampleMediaPath != null) {
            mediaFile = MediaHelper.loadFileFromPath(mCardModel.getStoryPath().buildZipPath(exampleMediaPath));
        }
        return mediaFile;
    }
    */
}
