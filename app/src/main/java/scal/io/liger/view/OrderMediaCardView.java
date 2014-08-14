package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.VideoView;

import com.fima.cardsui.objects.Card;

import java.util.ArrayList;

import scal.io.liger.model.CardModel;
import scal.io.liger.model.OrderMediaCardModel;
import scal.io.liger.R;
import scal.io.liger.touch.DraggableGridView;
import scal.io.liger.touch.OnRearrangeListener;

public class OrderMediaCardView extends Card {
    private OrderMediaCardModel mCardModel;
    private Context mContext;

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
        ArrayList<String> clipPaths = mCardModel.getClips();
        loadClips(clipPaths, dgvOrderClips);

        return view;
    }

    public void loadClips(ArrayList<String> clipPaths, DraggableGridView dgvOrderClips) {
        dgvOrderClips.removeAllViews();

        ImageView ivTemp;
        for (int i=0; i<3; i++) {
            ivTemp = new ImageView(mContext);
            ivTemp.setImageDrawable(mContext.getResources().getDrawable(R.drawable.sample0));

            dgvOrderClips.addView(ivTemp);
        }

        dgvOrderClips.setOnRearrangeListener(new OnRearrangeListener() {
            @Override
            public void onRearrange(int oldIndex, int newIndex) {
                int i = 1;
            }
        });

        dgvOrderClips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i = 1;
            }
        });
    }
}