package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;

import scal.io.liger.MediaHelper;
import scal.io.liger.Utility;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.PreviewCard;
import scal.io.liger.R;


public class PreviewCardView implements DisplayableCard {

    private PreviewCard mCardModel;
    private Context mContext;
    public ArrayList<String> paths = new ArrayList<String>();
    public int videoIndex = 0;

    public PreviewCardView(Context context, Card cardModel) {
        Log.d("PreviewCardView", "constructor");
        mContext = context;
        mCardModel = (PreviewCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        Log.d("PreviewCardView", "getCardView");
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_preview, null);
        final ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));
        final VideoView vvCardVideo = ((VideoView) view.findViewById(R.id.vv_card_video));
        TextView tvText= ((TextView) view.findViewById(R.id.tv_text));
        tvText.setText(mCardModel.getText());

        loadClips(mCardModel.getClipPaths());

        //TODO find better way of checking file is valid
        File mediaFile = MediaHelper.loadFileFromPath(paths.get(0)); // preview will play clips in sequence, but thumbnail is taken from first clip
        if(mediaFile.exists() && !mediaFile.isDirectory()) {

            Uri video = Uri.parse(mediaFile.getPath());
            vvCardVideo.setMediaController(null);
            vvCardVideo.setVideoURI(video);
            vvCardVideo.seekTo(5); // seems to be need to be done to show its thumbnail?

            //set up image as preview
            Bitmap videoFrame = Utility.getFrameFromVideo(video.getPath());
            if(null != videoFrame) {
                ivCardPhoto.setImageBitmap(videoFrame);
                ivCardPhoto.setVisibility(View.VISIBLE);
            }
        } else {
            System.err.println("INVALID MEDIA FILE: " + mediaFile.getPath());
        }

        ivCardPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vvCardVideo.setVisibility(View.VISIBLE);
                ivCardPhoto.setVisibility(View.GONE);
                Uri video = Uri.parse(paths.get(videoIndex));
                vvCardVideo.setVideoURI(video);
                vvCardVideo.start();
            }
        });

        vvCardVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer vvCardPlayer) {
                videoIndex++;

                if (videoIndex >= paths.size()) {
                    vvCardVideo.setVisibility(View.GONE);
                    ivCardPhoto.setVisibility(View.VISIBLE);
                    videoIndex = 0;
                    return; // don't loop
                }

                File mediaFile = MediaHelper.loadFileFromPath(paths.get(videoIndex));
                if (mediaFile.exists() && !mediaFile.isDirectory()) {
                    Uri video = Uri.parse(mediaFile.getPath());
                    vvCardVideo.setVideoURI(video);
                    vvCardVideo.start();
                } else {
                    System.err.println("INVALID MEDIA FILE: " + mediaFile.getPath());
                }
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    public void loadClips(ArrayList<String> clipPaths) {
        // get batch of ordered cards
        ArrayList<Card> clipCards = mCardModel.getStoryPath().getCardsByIds(clipPaths);
        for (Card cm : clipCards) {
            ClipCard ccm = null;

            if (cm instanceof ClipCard) {
                ccm = (ClipCard) cm;
            } else {
                continue;
            }

            String mediaPath = null;
            MediaFile mf = ccm.getSelectedMediaFile();

            if (mf == null) {
                Log.e(this.getClass().getName(), "no media file was found");
            } else {
                mediaPath = mf.getPath();
                paths.add(mCardModel.getStoryPath().buildPath(mediaPath));
            }
        }
    }
}
