package scal.io.liger;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;

import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;

/**
 * Created by mnbogner on 11/11/14.
 */
public class LigerDownloadActivity extends Activity implements IDownloaderClient {

    IStub mDownloaderClientStub = null;
    IDownloaderService mRemoteService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // checking for files is already being done outside of this class
        // this activity should only be created if files need to be downloaded

        // "Build an Intent to start this activity from the Notification"
        // using this activity rather than MainActivity
        // hoping to handle progress and status within this class
        Intent notifierIntent = new Intent(this, ((Object)this).getClass());
        notifierIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifierIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.d("DOWNLOAD", "CREATED PENDING INTENT");

        try {
            // "Start the download service (if required)"
            int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(this, pendingIntent, LigerDownloaderService.class);

            // "If download has started, initialize this activity to show download progress"
            // notification probably limited to debug output at this time
            if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
                // "Instantiate a member instance of IStub"
                mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this, LigerDownloaderService.class);
                // "Inflate layout that shows download progress"
                // currently no ui for progress/status
                // setContentView(R.layout.downloader_ui);
                Log.d("DOWNLOAD", "CLIENT MARSHALLER CREATED STUB");
                return;
            } else {
                Log.d("DOWNLOAD", "CLIENT MARSHALLER SAYS DOWNLOAD NOT REQUIRED");
            }
        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.e("DOWNLOAD", "CLASS PASSED TO DOWNLOAD SERVICE NOT FOUND?");
            nnfe.printStackTrace();
        }

        // as currently implemented, main activity will already have started
        // uncertain if this needs to perform some sort of notification?
    }

    @Override
    protected void onResume() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.connect(this);
            Log.d("DOWNLOAD", "STUB CONNECTED");
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (null != mDownloaderClientStub) {
            mDownloaderClientStub.disconnect(this);
            Log.d("DOWNLOAD", "STUB DISCONNECTED");
        }
        super.onStop();
    }

    @Override
    public void onServiceConnected(Messenger m) {
        Log.d("DOWNLOAD", "SERVICE CONNECTED");

        mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
        mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());

        Log.d("DOWNLOAD", "PROXY CREATED, GOT MESSENGER");
    }

    @Override
    public void onDownloadStateChanged(int newState) {
        int stateNumber = Helpers.getDownloaderStringResourceIDFromState(newState);
        Log.d("DOWNLOAD", "DOWNLOAD STATE CHANGED: " + stateNumber);
    }

    @Override
    public void onDownloadProgress(DownloadProgressInfo progress) {
        Log.d("DOWNLOAD", "DOWNLOAD PROGRESS RECEIVED, TIME REMAINING: " + progress.mTimeRemaining);
    }
}
