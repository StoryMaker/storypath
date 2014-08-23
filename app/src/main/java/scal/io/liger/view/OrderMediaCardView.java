package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.fima.cardsui.objects.Card;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.Utility;
import scal.io.liger.model.CardModel;
import scal.io.liger.model.ClipCardModel;
import scal.io.liger.model.OrderMediaCardModel;
import scal.io.liger.touch.DraggableGridView;
import scal.io.liger.touch.OnRearrangeListener;

public class OrderMediaCardView extends Card {
    private OrderMediaCardModel mCardModel;
    private Context mContext;

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

    public void fillList(ArrayList<String> clipPaths) {

        listCards = mCardModel.getStoryPathReference().getCardsByIds(clipPaths);

    }

    public void loadClips(ArrayList<String> clipPaths, DraggableGridView dgvOrderClips) {
        dgvOrderClips.removeAllViews();

        String medium = mCardModel.getMedium();

        ImageView ivTemp;
        File fileTemp;
        Bitmap bmTemp;

        fillList(clipPaths);

        // removing size check and 1->3 loop, should be covered by fillList + for loop
        for (CardModel cm : listCards) {

            ClipCardModel ccm = null;

            if (cm instanceof ClipCardModel) {
                ccm = (ClipCardModel) cm;
            }

            Uri mediaURI = null;
            String mediaPath = ccm.getValueByKey("value");
            File mediaFile = null;

            if(mediaPath != null) {
                mediaFile = new File(mediaPath);
                if(mediaFile.exists() && !mediaFile.isDirectory()) {
                    mediaURI = Uri.parse(mediaFile.getPath());
                }
            }

            if (medium != null && mediaURI != null) {
                if (medium.equals(Constants.VIDEO)) {
                    ivTemp = new ImageView(mContext);
                    Bitmap videoFrame = Utility.getFrameFromVideo(mediaURI.getPath());
                    if(null != videoFrame) {
                        ivTemp.setImageBitmap(videoFrame);
                    }
                    dgvOrderClips.addView(ivTemp);
                    continue;
                }else if (medium.equals(Constants.PHOTO)) {
                    ivTemp = new ImageView(mContext);
                    ivTemp.setImageURI(mediaURI);
                    dgvOrderClips.addView(ivTemp);
                    continue;
                }
            }

            //handle fall-through cases: (media==null || medium==AUDIO)
            ivTemp = new ImageView(mContext);

            String clipType = ccm.getClipType();
            int drawable = R.drawable.ic_launcher;

            if (clipType.equals(Constants.CHARACTER)) {
                drawable = R.drawable.cliptype_close;
            } else if (clipType.equals(Constants.ACTION)) {
                drawable = R.drawable.cliptype_medium;
            } else if (clipType.equals(Constants.RESULT)){
                drawable = R.drawable.cliptype_long;
            }

            ivTemp.setImageDrawable(mContext.getResources().getDrawable(drawable));
            dgvOrderClips.addView(ivTemp);
        }

        dgvOrderClips.setOnRearrangeListener(new OnRearrangeListener() {
            @Override
            public void onRearrange(int currentIndex, int newIndex) {
                //update actual card list
                CardModel currentCard = listCards.get(currentIndex);
                int currentCardIndex = mCardModel.getStoryPathReference().getCardIndex(currentCard);
                int newCardIndex = currentCardIndex - (currentIndex - newIndex);

                mCardModel.getStoryPathReference().rearrangeCards(currentCardIndex, newCardIndex);

            }
        });

        dgvOrderClips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }
}