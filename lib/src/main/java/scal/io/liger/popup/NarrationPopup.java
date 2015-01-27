package scal.io.liger.popup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import scal.io.liger.R;
import scal.io.liger.adapter.NarrationMediaAdapter;
import scal.io.liger.av.ClipCardsNarrator;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPath;

/**
 * Created by davidbrodsky on 12/12/14.
 */
public class NarrationPopup {

    private Activity mActivity;
    private ViewGroup mVuLayout;
    private ClipCardsNarrator mNarrator;
    private Handler mHandler;
    private int mPreviousVUMax;

    /**
     * Create a new NarrationPopup.
     */
    public NarrationPopup(Activity host) {
        mActivity = host;
        host.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHandler = new Handler();
            }
        });
        // TODO : Technically we should block here until Handler is set
    }

    /**
     * Show the NarrationPopup. May be called from any thread.
     *
     * @param cards A list of ClipCards to allow recording narration over.
     * @param listener A listener notified whenever a narration is recorded.
     */
    public void show(final List<ClipCard> cards, final ClipCardsNarrator.NarrationListener listener) {
        final View decorView = mActivity.getWindow().getDecorView();
        decorView.post(new Runnable() {
            @Override
            public void run() {
                // Create a PopupWindow that occupies the entire screen except the status and action bar
                final View popUpView = LayoutInflater.from(mActivity).inflate(R.layout.popup_narrate, (ViewGroup) decorView, false);
                final Button recordButton = (Button) popUpView.findViewById(R.id.record_button);
                mVuLayout = (ViewGroup) popUpView.findViewById(R.id.vumeter_layout);
                FrameLayout mediaPlayerContainer = (FrameLayout) popUpView.findViewById(R.id.mixed_media_player);
                try {
                    mNarrator = new ClipCardsNarrator(mediaPlayerContainer, cards);
                } catch (IOException e) {
                    e.printStackTrace();
                    // TODO : report failed, close popup?
                }
                RecyclerView recyclerView = (RecyclerView) popUpView.findViewById(R.id.recycler_view);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

                final NarrationMediaAdapter adapter = new NarrationMediaAdapter(recyclerView, cards);
                recyclerView.setAdapter(adapter);

                ClipCardsNarrator.NarrationListener narrationListener = new ClipCardsNarrator.NarrationListener() {
                    @Override
                    public void onNarrationFinished(MediaFile narration) {
                        mNarrator.addAudioTrack(narration);
                        recordButton.setText(mActivity.getString(R.string.dialog_record));
                        if (listener != null) listener.onNarrationFinished(narration);
                        mVuLayout.setVisibility(View.INVISIBLE);
                    }
                };
                mNarrator.setNarrationListener(narrationListener);

                recordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mNarrator.getState() == ClipCardsNarrator.RecordNarrationState.RECORDING) {
                            Log.i("NarratePopup", "stopping");
                            mNarrator.stopRecordingNarration();
                            recordButton.setText(mActivity.getString(R.string.dialog_record));
                        } else {
                            Log.i("NarratePopup", "starting");
                            List<ClipCard> selectedCards = adapter.getSelectedCards();
                            if (selectedCards.size() == 0) {
                                Toast.makeText(mActivity, "Please select a range of clips to narrate", Toast.LENGTH_LONG).show();
                                return;
                            }
                            mNarrator.startRecordingNarrationForCards(selectedCards);
                            recordButton.setText(mActivity.getString(R.string.dialog_stop));
                            mVuLayout.setVisibility(View.VISIBLE);
                            updateVUMeterView(mVuLayout);
                        }
                    }
                });

                Display display = mActivity.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                int actionBarHeight = mActivity.getActionBar().getHeight();

                Rect rectangle = new Rect();
                Window window = mActivity.getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
                int statusBarHeight = rectangle.top;


                final PopupWindow popUp = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT, height - actionBarHeight - statusBarHeight, true);
                popUp.setFocusable(false);
                popUp.showAtLocation(decorView, Gravity.BOTTOM, 0, 0);

                final StoryPath storyPath = cards.get(0).getStoryPath();

                /** ActionMode Callback */
                ActionMode.Callback actionCallback = new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        MenuInflater inflater = mode.getMenuInflater();
                        inflater.inflate(R.menu.order_media, menu);
                        mode.setTitle(mActivity.getString(R.string.narration));
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        if (item.getItemId() == R.id.menu_done) {
                            popUp.dismiss();
                            mode.finish();
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        popUp.dismiss();
                    }
                };
                mActivity.startActionMode(actionCallback);
            }
        });
    }

    void updateVUMeterView(final ViewGroup vuMeterLayout) {
        final int MAX_VU_SIZE = 11;
        boolean showVUArray[] = new boolean[MAX_VU_SIZE];

        if (vuMeterLayout.getVisibility() == View.VISIBLE
                &&  mNarrator.getState() != ClipCardsNarrator.RecordNarrationState.STOPPED) {
            int amp = mNarrator.getMaxRecordingAmplitude();
            int vuSize = MAX_VU_SIZE * amp / 32768;
            if (vuSize >= MAX_VU_SIZE) {
                vuSize = MAX_VU_SIZE - 1;
            }

            if (vuSize >= mPreviousVUMax) {
                mPreviousVUMax = vuSize;
            } else if (mPreviousVUMax > 0) {
                mPreviousVUMax--;
            }

            for (int i = 0; i < MAX_VU_SIZE; i++) {
                if (i <= vuSize) {
                    showVUArray[i] = true;
                } else if (i == mPreviousVUMax) {
                    showVUArray[i] = true;
                } else {
                    showVUArray[i] = false;
                }
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mNarrator.getState() != ClipCardsNarrator.RecordNarrationState.STOPPED) {
                        updateVUMeterView(vuMeterLayout);
                    }
                }
            }, 100);
        } else if (vuMeterLayout.getVisibility() == View.VISIBLE) {
            mPreviousVUMax = 0;
            for (int i = 0; i < MAX_VU_SIZE; i++) {
                showVUArray[i] = false;
            }
        }

        if (vuMeterLayout.getVisibility() == View.VISIBLE) {
            vuMeterLayout.removeAllViews();
            for (boolean show : showVUArray) {
                ImageView imageView = new ImageView(mActivity);
                imageView.setBackgroundResource(R.drawable.background_vumeter);
                if (show) {
                    imageView.setImageResource(R.drawable.icon_vumeter);
                }
                imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                vuMeterLayout.addView(imageView);
            }
        }
    }
}
