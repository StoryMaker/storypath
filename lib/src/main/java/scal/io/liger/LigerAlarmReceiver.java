package scal.io.liger;

import timber.log.Timber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;

/**
 * Created by mnbogner on 11/7/14.
 */
public class LigerAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent, LigerDownloaderService.class);
        } catch (NameNotFoundException nnfe) {
            Timber.d("FAILED TO START DOWNLOAD SERVICE: " + nnfe.getMessage());
        }
    }
}
