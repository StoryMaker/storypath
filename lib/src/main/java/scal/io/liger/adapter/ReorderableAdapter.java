package scal.io.liger.adapter;

import timber.log.Timber;

import android.util.Pair;

/**
 * Created by davidbrodsky on 10/23/14.
 */
public interface ReorderableAdapter {

    public void swapItems(int positionOne, int positionTwo);

}
