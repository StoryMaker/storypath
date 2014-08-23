package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.fima.cardsui.objects.Card;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import scal.io.liger.MediaHelper;
import scal.io.liger.Utility;
import scal.io.liger.model.CardModel;
import scal.io.liger.model.PreviewCardModel;
import scal.io.liger.R;
import scal.io.liger.touch.DraggableGridView;


public class PreviewCardView extends Card {

    private PreviewCardModel mCardModel;
    private Context mContext;
    public ArrayList<String> paths = new ArrayList<String>();
    public int videoIndex = 0;

    public PreviewCardView(Context context, CardModel cardModel) {
        Log.d("PreviewCardView", "constructor");
        mContext = context;
        mCardModel = (PreviewCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        Log.d("PreviewCardView", "getCardContent");
        if(mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_preview, null);
        final ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));
        final VideoView vvCardVideo = ((VideoView) view.findViewById(R.id.vv_card_media));
        TextView tvText= ((TextView) view.findViewById(R.id.tv_text));
        tvText.setText(mCardModel.getText());

        loadClips(mCardModel.getClipPaths());

        //TODO find better way of checking file is valid
        File mediaFile = MediaHelper.loadFileFromPath(paths.get(0));
        if(mediaFile.exists() && !mediaFile.isDirectory()) {

            Uri video = Uri.parse(mediaFile.getPath());
            vvCardVideo.setMediaController(null);
            vvCardVideo.setVideoURI(video);

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


        return view;
    }

    public void loadClips(ArrayList<String> clipPaths) {
        // get batch of ordered cards
        ArrayList<CardModel> clipCards = mCardModel.getStoryPathReference().getCardsByIds(clipPaths);
        for (CardModel cm : clipCards) {
            String value = cm.getValueByKey("value");
            paths.add(value);
        }
    }
}
