package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.fima.cardsui.objects.Card;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.model.CardModel;
import scal.io.liger.model.OrderMediaCardModel;
import scal.io.liger.R;
import scal.io.liger.touch.DraggableGridView;
import scal.io.liger.touch.OnRearrangeListener;

public class OrderMediaCardView extends Card {
    private OrderMediaCardModel mCardModel;
    private Context mContext;

    private static List<Integer> listDrawables = new ArrayList<Integer>();
    private static boolean firstRun = true;
    private List<CardModel> listCards = new ArrayList<CardModel>();

    public OrderMediaCardView(Context context, CardModel cardModel) {
        mContext = context;
        mCardModel = (OrderMediaCardModel) cardModel;
    }

    @Override
    public View getCardContent(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_order_media, null);
        DraggableGridView dgvOrderClips = ((DraggableGridView) view.findViewById(R.id.dgv_media_clips));

        loadClips(mCardModel.getClipPaths(), dgvOrderClips);

        return view;
    }

    public void loadClips(ArrayList<String> clipPaths, DraggableGridView dgvOrderClips) {

        if(firstRun) {
            listDrawables.add(0, R.drawable.cliptype_close);
            listDrawables.add(1, R.drawable.cliptype_medium);
            listDrawables.add(2, R.drawable.cliptype_long);

            firstRun = false;
        }

        dgvOrderClips.removeAllViews();

        String medium = mCardModel.getMedium();

        ImageView ivTemp;
        VideoView vvTemp;
        File fileTemp;
        Bitmap bmTemp;

if (clipPaths.size() > 0) {
        for (int i=0; i<3; i++) {
            CardModel cm = mCardModel.storyPathReference.getCardById(clipPaths.get(i));
            listCards.add(i, cm);

            Uri mediaURI = null;
            String mediaPath = listCards.get(i).getValueByKey("value");

            if(mediaPath != null) {
                File mediaFile = new File(mediaPath);
                if(mediaFile.exists() && !mediaFile.isDirectory()) {
                    mediaURI = Uri.parse(mediaFile.getPath());
                }
            }

            if (medium != null && mediaURI != null) {
                if (medium.equals(Constants.VIDEO)) {
                    vvTemp = new VideoView(mContext);
                    vvTemp.setVideoPath(mediaURI.getPath());
                    vvTemp.seekTo(10);
                    dgvOrderClips.addView(vvTemp);

                } else if (medium.equals(Constants.AUDIO)) {
                    ivTemp = new ImageView(mContext);
                    ivTemp.setImageURI(mediaURI);
                    dgvOrderClips.addView(ivTemp);

                } else if (medium.equals(Constants.PHOTO)) {
                    ivTemp = new ImageView(mContext);
                    ivTemp.setImageURI(mediaURI);
                    dgvOrderClips.addView(ivTemp);
                }
            } else {
                ivTemp = new ImageView(mContext);
                ivTemp.setImageDrawable(mContext.getResources().getDrawable(listDrawables.get(i)));
                dgvOrderClips.addView(ivTemp);
            }
        }
}

        dgvOrderClips.setOnRearrangeListener(new OnRearrangeListener() {
            @Override
            public void onRearrange(int currentIndex, int newIndex) {

                //update internal list
                CardModel currentCard = listCards.remove(currentIndex);
                listCards.add(newIndex, currentCard);

                //update actual card list
                int currentCardIndex = mCardModel.getStoryPathReference().getCardIndex(currentCard);
                int newCardIndex = currentCardIndex - (currentIndex - newIndex);

                mCardModel.getStoryPathReference().rearrangeCards(currentCardIndex, newCardIndex);

                //update internal drawables
                int currentValue = listDrawables.remove(currentIndex);
                listDrawables.add(newIndex, currentValue);
            }
        });

        dgvOrderClips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }
}