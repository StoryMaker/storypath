package scal.io.liger.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * RecyclerView who's scroll events may be blocked via calls to
 * {@link #setCanScroll(boolean)}
 *
 * Created by davidbrodsky on 3/18/15.
 */
public class ScrollLockRecyclerView extends RecyclerView {
    public static final String TAG = "ScrollLockRecyclerView";

    private boolean canScroll = true;

    public ScrollLockRecyclerView(Context context) {
        super(context);
    }

    public ScrollLockRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollLockRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                if (!canScroll) return true;
        }
        return super.onTouchEvent(event);
    }

}
