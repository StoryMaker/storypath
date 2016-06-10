package scal.io.liger.view;

import timber.log.Timber;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.model.Card;
import scal.io.liger.model.ExampleCard;

public class ExampleCardView implements DisplayableCard {
    public static final String TAG = "ExampleCardView";

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

        if (mCardModel.getExampleMediaFile() == null) {
            // getExampleMediaFile().getExampleURI() is too expensive to call on the main thread
            // || (mCardModel.getExampleMediaFile().getExampleURI(mCardModel) == null) ) {

            // using medium cliptype image as default in case media file is missing
            // ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_medium));
            // ivCardPhoto.setVisibility(View.VISIBLE);

            // need to support urls as paths (this code was originally from IntroCardView)
            // this will need to support all mediums but with no value i'm assuming photos
            switch(medium) {
                case Constants.VIDEO:

                    Timber.d("currently unable to generate thumbnails for video files");

                    ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_medium));

                case Constants.AUDIO:

                    Timber.d("currently unable to generate thumbnails for audio files");

                    ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_medium));

                default:

                    if ((mCardModel.getExampleMediaPath() != null) && (mCardModel.getExampleMediaPath().startsWith("http"))) {

                        Timber.d("generating " + medium + " thumbnail from path " + mCardModel.getExampleMediaPath());

                        Picasso.with(context)
                                .load(mCardModel.getExampleMediaPath())
                                .into(ivCardPhoto);
                    } else {

                        Timber.d("unable to generate " + medium + " thumbnail from path " + mCardModel.getExampleMediaPath());

                        ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_medium));
                    }
            }

            ivCardPhoto.setVisibility(View.VISIBLE);

        } else { //if (mediaFile.exists() && !mediaFile.isDirectory()) {
            ivCardPhoto.setVisibility(View.VISIBLE);
            mCardModel.getExampleMediaFile().loadThumbnail(ivCardPhoto);
            switch(medium) {
                case Constants.VIDEO:

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
                    break;

                case Constants.AUDIO:
                    // TODO : Must remove call to getExampleURI on main thread
                    Uri myUri = Uri.parse(mCardModel.getExampleMediaFile().getExampleURI(mCardModel));
                    final MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

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
                    break;

                case Constants.PHOTO:
                    // do nothing beyond displaying thumbnail
                    break;

                default:
                    Timber.w("Unknown medium " + medium);
                    break;
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
