package scal.io.liger.touch;

import timber.log.Timber;

public interface OnRearrangeListener {
	
	public abstract void onRearrange(int oldIndex, int newIndex);
}
