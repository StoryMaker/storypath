package scal.io.liger;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Created by mnbogner on 11/7/14.
 */
public class LigerDownloadManager implements Runnable {


    // TODO use HTTPS
    // TODO pickup Tor settings

    private String mainOrPatch;
    private int version;
    private Context context;

    boolean useManager = true;
    private DownloadManager manager;
    private long lastDownload = -1L;

    public LigerDownloadManager (String mainOrPatch, int version, Context context, boolean useManager) {
        this.mainOrPatch = mainOrPatch;
        this.version = version;
        this.context = context;
        this.useManager = useManager;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getMainOrPatch() {
        return mainOrPatch;
    }

    public void setMainOrPatch(String mainOrPatch) {
        this.mainOrPatch = mainOrPatch;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isUseManager() {
        return useManager;
    }

    public void setUseManager(boolean useManager) {
        this.useManager = useManager;
    }

    @Override
    public void run() {
        Log.e("DOWNLOAD", "DOWNLOADING EXTENSION");

        String ligerUrl = Constants.LIGER_URL + "obb" + "/";
        String ligerObb = ZipHelper.getExtensionZipFilename(context, mainOrPatch, version);

        try {
            // if we're managing the download, download only to the files folder
            // if we're using the google play api, download only to the obb folder
            File targetFolder = new File(ZipHelper.getFileFolderName(context));

            Log.e("DOWNLOAD", "TARGET FOLDER: " + targetFolder.getPath());

            URI expansionFileUri = null;
            HttpGet request = null;
            HttpResponse response = null;

            try {
                Log.e("DOWNLOAD", "TARGET URL: " + ligerUrl + ligerObb);

                if (useManager) {
                    File targetFile = new File(targetFolder, ligerObb);
                    downloadWithManager(Uri.parse(ligerUrl + ligerObb), "Liger " + mainOrPatch + " file download", ligerObb, Uri.fromFile(targetFile));
                } else {
                    expansionFileUri = new URI(ligerUrl + ligerObb);

                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    request = new HttpGet(expansionFileUri);
                    response = httpClient.execute(request);

                    File targetFile = new File(targetFolder, ligerObb);
                    targetFile.getParentFile().mkdirs();

                    BufferedInputStream responseInput = new BufferedInputStream(response.getEntity().getContent());

                    try {
                        FileOutputStream targetOutput = new FileOutputStream(targetFile);
                        byte[] buf = new byte[1024];
                        int i;
                        while ((i = responseInput.read(buf)) > 0) {
                            targetOutput.write(buf, 0, i);
                        }
                        targetOutput.close();
                        responseInput.close();
                        Log.d("DOWNLOAD", "SAVED DOWNLOAD TO " + targetFile);
                    } catch (IOException ioe) {
                        Log.e("DOWNLOAD", "FAILED TO SAVE DOWNLOAD TO " + targetFile + " -> " + ioe.getMessage());
                        ioe.printStackTrace();
                    }
                }
            } catch (Exception ioe) {
                Log.e("DOWNLOAD", "ERROR DOWNLOADING FROM " + expansionFileUri + " -> " + ioe.getMessage());
                ioe.printStackTrace();

                if (response != null) {
                    response.getEntity().consumeContent();
                }
            }
        } catch (Exception e) {
            Log.e("DOWNLOAD", "DOWNLOAD ERROR: " + ligerUrl + ligerObb + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void downloadWithManager(Uri uri, String title, String desc, Uri uriFile) {
        initDownloadManager();

        Log.d("DOWNLOAD", "QUEUEING DOWNLOAD: " + uri.toString() + " -> " + uriFile.toString());

        lastDownload = manager.enqueue(new DownloadManager.Request(uri)
                              .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                              .setAllowedOverRoaming(false)
                              .setTitle(title)
                              .setDescription(desc)
                              .setVisibleInDownloadsUi(false)
                              .setDestinationUri(uriFile));
    }

    private synchronized void initDownloadManager() {
        if (manager == null) {
            manager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            context.registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
        }
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor c = manager.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        File savedFile = new File(Uri.parse(uriString).getPath());
                        Log.d("DOWNLOAD", "MANAGER SAVED DOWNLOAD TO " + savedFile.getPath());

                    } else {
                        Log.e("DOWNLOAD", "MANAGER FAILED AT STATUS CHECK");
                    }
                } else {
                    Log.e("DOWNLOAD", "MANAGER FAILED AT CURSOR MOVE");
                }
            } else {
                Log.e("DOWNLOAD", "MANAGER FAILED AT COMPLETION CHECK");
            }
        }
    };

    BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            // ???
        }
    };
}
