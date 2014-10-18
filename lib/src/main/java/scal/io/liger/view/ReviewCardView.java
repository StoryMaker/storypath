package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;

import scal.io.liger.MediaHelper;
import scal.io.liger.R;
import scal.io.liger.Utility;
import scal.io.liger.model.Card;
import scal.io.liger.model.ReviewCard;


public class ReviewCardView implements DisplayableCard {

    private ReviewCard mCardModel;
    private Context mContext;

    public ReviewCardView(Context context, Card cardModel) {
        Log.d("RevieCardView", "constructor");
        mContext = context;
        mCardModel = (ReviewCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        Log.d("RevieCardView", "getCardView");
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_preview, null);
        final ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));
        final VideoView vvCardVideo = ((VideoView) view.findViewById(R.id.vv_card_video));
        final String mediaPath = mCardModel.getMediaPath();

        Button btnJumble =((Button) view.findViewById(R.id.btn_jumble));
        Button btnOrder =((Button) view.findViewById(R.id.btn_order));
        Button btnNarrate =((Button) view.findViewById(R.id.btn_narrate));
        Button btnPublish =((Button) view.findViewById(R.id.btn_publish));

        btnJumble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnNarrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });

        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });


        //TODO find better way of checking file is valid
        final File mediaFile = MediaHelper.loadFileFromPath(mediaPath); // preview will play clips in sequence, but thumbnail is taken from first clip
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
                Uri video = Uri.parse(mediaPath);
                vvCardVideo.setVideoURI(video);
                vvCardVideo.start();
            }
        });

        vvCardVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer vvCardPlayer) {
                vvCardVideo.setVisibility(View.GONE);
                ivCardPhoto.setVisibility(View.VISIBLE);
            }
        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }
}
