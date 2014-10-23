package scal.io.liger.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.adapter.OrderMediaAdapter;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.OrderMediaCard;
import scal.io.liger.touch.DraggableGridView;
import scal.io.liger.touch.OnRearrangeListener;

public class OrderMediaCardView implements DisplayableCard, ActionMode.Callback, OrderMediaAdapter.OnReorderListener {
    private OrderMediaCard mCardModel;
    private Context mContext;
    private List<Card> mListCards = new ArrayList<Card>();
    private PopupWindow mPopup;

    public OrderMediaCardView(Context context, Card cardModel) {
        mContext = context;
        mCardModel = (OrderMediaCard) cardModel;
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_order_media, null);
        DraggableGridView dgvOrderClips = ((DraggableGridView) view.findViewById(R.id.dgv_media_clips));

        loadClips(mCardModel.getClipPaths(), dgvOrderClips);

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    public void fillList(ArrayList<String> clipPaths) {

        mListCards = mCardModel.getStoryPathReference().getCardsByIds(clipPaths);

    }

    public void loadClips(ArrayList<String> clipPaths, DraggableGridView dgvOrderClips) {
        dgvOrderClips.removeAllViews();

        String medium = mCardModel.getMedium();

        ImageView ivTemp;
        File fileTemp;
        Bitmap bmTemp;

        fillList(clipPaths);

        // removing size check and 1->3 loop, should be covered by fillList + for loop
        for (Card cm : mListCards) {

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
            }

            //File mediaFile = null;
            Uri mediaURI = null;

            if(mediaPath != null) {
                /*
                mediaFile = MediaHelper.loadFileFromPath(ccm.getStoryPathReference().buildPath(mediaPath));
                if(mediaFile.exists() && !mediaFile.isDirectory()) {
                    mediaURI = Uri.parse(mediaFile.getPath());
                }
                */
                mediaURI = Uri.parse(mediaPath);
            }

            if (medium != null && mediaURI != null) {
                if (medium.equals(Constants.VIDEO)) {
                    ivTemp = new ImageView(mContext);
                    //Bitmap videoFrame = Utility.getFrameFromVideo(mediaURI.getPath());
                    Bitmap videoFrame = mf.getThumbnail();
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
                Card currentCard = mListCards.get(currentIndex);
                int currentCardIndex = mCardModel.getStoryPathReference().getCardIndex(currentCard);
                int newCardIndex = currentCardIndex - (currentIndex - newIndex);

                mCardModel.getStoryPathReference().rearrangeCards(currentCardIndex, newCardIndex);

            }
        });

        dgvOrderClips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Activity) view.getContext()).startActionMode(OrderMediaCardView.this);
                // Unsafe
                View root = ((MainActivity) mContext).findViewById(R.id.rootContainer);
                showOrderMediaPopup(root);
            }
        });
    }

    /**
     * Show a PopupWindow allowing you to re-order the clips
     */
    private void showOrderMediaPopup(final View root) {
        root.findViewById(R.id.rootContainer).post(new Runnable() {
            @Override
            public void run() {
                // Create a PopupWindow that occupies the entire screen except the status and action bar
                final View popUp = LayoutInflater.from(mContext).inflate(R.layout.popup_order_media, (ViewGroup) root, false);
                ReorderableRecyclerView recyclerView = (ReorderableRecyclerView) popUp.findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                OrderMediaAdapter adapter = new OrderMediaAdapter(recyclerView, mListCards, mCardModel.getMedium());
                adapter.setOnReorderListener(OrderMediaCardView.this);
                recyclerView.setReordableAdapter(adapter);

                Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                int actionBarHeight = ((Activity) mContext).getActionBar().getHeight();

                Rect rectangle = new Rect();
                Window window = ((Activity) mContext).getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                int statusBarHeight = rectangle.top;


                mPopup = new PopupWindow(popUp, ViewGroup.LayoutParams.MATCH_PARENT, height - actionBarHeight - statusBarHeight, true);
                mPopup.setFocusable(false);
                mPopup.showAtLocation(root, Gravity.BOTTOM, 0, 0);
            }
        });
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.order_media, menu);
        mode.setTitle(mContext.getString(R.string.clip_order));
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.menu_done) {
            if (mPopup != null) {
                mPopup.dismiss();
                mPopup = null;
            }
            mode.finish();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (mPopup != null) {
            mPopup.dismiss();
            mPopup = null;
        }
    }

    @Override
    public void onReorder(int firstIndex, int secondIndex) {
        Card currentCard = mListCards.get(firstIndex);
        int currentCardIndex = mCardModel.getStoryPathReference().getCardIndex(currentCard);
        int newCardIndex = mCardModel.getStoryPathReference().getCardIndex(mListCards.get(secondIndex));
        mCardModel.getStoryPathReference().swapCards(currentCardIndex, newCardIndex);
    }
}