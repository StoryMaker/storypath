package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.fima.cardsui.objects.Card;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import scal.io.liger.model.CardModel;
import scal.io.liger.model.PreviewCardModel;
import scal.io.liger.R;
import scal.io.liger.touch.DraggableGridView;


public class PreviewCardView extends Card {

    private PreviewCardModel mCardModel;
    private Context mContext;
    public ArrayList<String> paths = new ArrayList<String>();
    public int videoIndex = 0;
    public VideoView vvCardMedia;

    private static List<CardModel> listCards = new ArrayList<CardModel>();
    private static boolean firstTime = true;

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

        loadClips(mCardModel.getClipPaths());

//        paths = mCardModel.getMedia_paths();
//        if ((paths == null) || (paths.size() == 0)) {
//            // i assume this will just return a view with no preview
//            return view;
//        }

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
                if (mediaFile.exists() && !mediaFile.isDirectory()) {
                    Uri video = Uri.parse(paths.get(videoIndex));
                    vvCardMedia.setVideoURI(video);
                    vvCardMedia.start();
                } else {
                    System.err.println("INVALID MEDIA FILE: " + mediaFile.getPath());
                }
            }
        });


        return view;
    }

    public void loadClips(ArrayList<String> clipPaths) {
        if (firstTime && clipPaths.size() > 0) {
            for (int i = 0; i < 3; i++) {
                CardModel cm = mCardModel.storyPathReference.getCardById(clipPaths.get(i));
                listCards.add(i, cm);
                String value = cm.getValueByKey("value");
                paths.add(i, value);
            }
        }

        firstTime = false;
    }
}
