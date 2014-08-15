package scal.io.liger.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.fima.cardsui.objects.Card;

import java.io.File;
import java.util.ArrayList;

import scal.io.liger.model.CardModel;
import scal.io.liger.model.PreviewCardModel;
import scal.io.liger.R;


public class PreviewCardView extends Card {

    private PreviewCardModel mCardModel;
    private Context mContext;
    public ArrayList<String> paths;
    public int videoIndex = 0;
    public VideoView vvCardMedia;

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
        vvCardMedia = ((VideoView) view.findViewById(R.id.vv_card_media));
        TextView tvText= ((TextView) view.findViewById(R.id.tv_text));
        tvText.setText(mCardModel.getText());

        paths = mCardModel.getMedia_paths();
        if ((paths == null) || (paths.size() == 0)) {
            // i assume this will just return a view with no preview
            return view;
        }

        //TODO find better way of checking file is valid
        File mediaFile = new File(paths.get(0));
        if(mediaFile.exists() && !mediaFile.isDirectory()) {
            MediaController mediaController = new MediaController(mContext);
            mediaController.setAnchorView(vvCardMedia);

            Uri video = Uri.parse(paths.get(0));
            vvCardMedia.setMediaController(mediaController);
            vvCardMedia.setVideoURI(video);
            vvCardMedia.seekTo(5); // seems to be need to be done to show its thumbnail?
        } else {
            System.err.println("INVALID MEDIA FILE: " + mediaFile.getPath());
        }

        vvCardMedia.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer vvCardPlayer) {
                videoIndex++;

                // check and loop
                if (videoIndex >= paths.size())
                    videoIndex = 0;

                File mediaFile = new File(paths.get(videoIndex));
                if(mediaFile.exists() && !mediaFile.isDirectory()) {
                    Uri video = Uri.parse(paths.get(0));
                    vvCardMedia.setVideoURI(video);
                    vvCardMedia.start();
                } else {
                    System.err.println("INVALID MEDIA FILE: " + mediaFile.getPath());
                }
            }
        });

        return view;
    }
}
