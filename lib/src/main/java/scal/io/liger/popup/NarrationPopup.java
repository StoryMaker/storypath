package scal.io.liger.popup;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.List;

import scal.io.liger.R;
import scal.io.liger.adapter.MediaAdapter;
import scal.io.liger.av.ClipCardCollectionPlayer;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.StoryPath;

/**
 * Created by davidbrodsky on 12/12/14.
 */
public class NarrationPopup {

    public static void show(final Activity activity, final List<ClipCard> cards) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.post(new Runnable() {
            @Override
            public void run() {
                // Create a PopupWindow that occupies the entire screen except the status and action bar
                final View popUpView = LayoutInflater.from(activity).inflate(R.layout.popup_narrate, (ViewGroup) decorView, false);
                FrameLayout mediaPlayerContainer = (FrameLayout) popUpView.findViewById(R.id.mixed_media_player);
                ClipCardCollectionPlayer player = new ClipCardCollectionPlayer(mediaPlayerContainer, cards);

                Button recordButton = (Button) popUpView.findViewById(R.id.record_button);
                recordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(activity, "RECORDING", Toast.LENGTH_SHORT).show();
                    }
                });

                RecyclerView recyclerView = (RecyclerView) popUpView.findViewById(R.id.recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
                MediaAdapter adapter = new MediaAdapter(recyclerView, cards);
                recyclerView.setAdapter(adapter);

                Display display = activity.getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                int actionBarHeight = activity.getActionBar().getHeight();

                Rect rectangle = new Rect();
                Window window = activity.getWindow();
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
                        mode.setTitle(activity.getString(R.string.narration));
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
                activity.startActionMode(actionCallback);
            }
        });
    }
}
