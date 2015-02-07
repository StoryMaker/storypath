package scal.io.liger;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.APKExpansionPolicy;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import info.guardianproject.onionkit.trust.StrongHttpsClient;
import info.guardianproject.onionkit.ui.OrbotHelper;
import scal.io.liger.model.ExpansionIndexItem;

/**
 * Created by mnbogner on 11/7/14.
 */
public class LigerAltDownloadManager implements Runnable {
    private final static String TAG = "LigerAltDownloadManager";

    // TODO use HTTPS
    // TODO pickup Tor settings

    private String fileName;
    private Context context;

    private DownloadManager manager;
    private long lastDownload = -1L;

    StrongHttpsClient mClient = null;

    boolean useManager = true;
    boolean useTor = true; // CURRENTLY SET TO TRUE, WILL USE TOR IF ORBOT IS RUNNING

    public LigerAltDownloadManager(String fileName, Context context, boolean useManager) {
        this.fileName = fileName;
        this.context = context;
        this.useManager = useManager;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean isUseManager() {
        return useManager;
    }

    public void setUseManager(boolean useManager) {
        this.useManager = useManager;
    }

    @Override
    public void run() {
        downloadFromLigerServer();
        return;
    }

    private void downloadFromLigerServer() {
        Log.e("DOWNLOAD", "DOWNLOADING EXTENSION");

        ExpansionIndexItem expansionIndexItem = IndexManager.loadInstalledFileIndex(context).get(fileName);

        if (expansionIndexItem == null) {
            Log.e("DOWNLOAD", "FAILED TO LOCATE EXPANSION INDEX ITEM FOR " + fileName);
            return;
        }

        String ligerUrl = expansionIndexItem.getExpansionFileUrl();
        String ligerObb = fileName;

        try {
            // we're managing the download, download only to the files folder
            File targetFolder = new File(ZipHelper.getFileFolderName(context, fileName));

            Log.d("DOWNLOAD", "TARGET FOLDER: " + targetFolder.getPath());

            URI expansionFileUri = null;
            HttpGet request = null;
            HttpResponse response = null;

            try {
                Log.d("DOWNLOAD", "TARGET URL: " + ligerUrl + ligerObb);

                if (useManager) {

                    // clean up old tmps before downloading

                    String nameFilter = "";

                    if (ligerObb.contains(expansionIndexItem.getExpansionFileVersion())) {
                        nameFilter = fileName.replace(expansionIndexItem.getExpansionFileVersion(), "*") + "*.tmp";
                    } else {
                        nameFilter = fileName + "*.tmp";
                    }

                    Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + targetFolder.getPath());

                    WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
                    for (File oldFile : FileUtils.listFiles(targetFolder, oldFileFilter, null)) {
                        Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                        FileUtils.deleteQuietly(oldFile);
                    }

                    File targetFile = new File(targetFolder, ligerObb + ".tmp");

                    if (checkTor(useTor, context)) {
                        downloadWithTor(Uri.parse(ligerUrl + ligerObb), "Liger expansion file download", ligerObb, targetFile);
                    } else {
                        downloadWithManager(Uri.parse(ligerUrl + ligerObb), "Liger expansion file download", ligerObb, Uri.fromFile(targetFile));
                    }
                } else {

                    // useManager IS HARDCODED SO THIS CODE SHOULD PROBABLY BE REMOVED

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

    private void downloadWithTor(Uri uri, String title, String desc, File targetFile) {
        Log.d("DOWNLOAD/TOR", "DOWNLOAD WITH TOR PROXY: " + Constants.TOR_PROXY_HOST + "/" + Constants.TOR_PROXY_PORT);

        StrongHttpsClient httpClient = getHttpClientInstance();
        httpClient.useProxy(true, "http", Constants.TOR_PROXY_HOST, Constants.TOR_PROXY_PORT); // CLASS DOES NOT APPEAR TO REGISTER A SCHEME FOR SOCKS, ORBOT DOES NOT APPEAR TO HAVE AN HTTPS PORT

        Log.d("DOWNLOAD/TOR", "CHECKING URI: " + uri.toString());

        try {
            HttpGet request = new HttpGet(uri.toString());
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                Log.d("DOWNLOAD/TOR", "DOWNLOAD SUCCEEDED, STATUS CODE: " + statusCode);

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
                    Log.d("DOWNLOAD/TOR", "SAVED DOWNLOAD TO " + targetFile);

                    if (!handleFile(targetFile)) {
                        Log.d("DOWNLOAD/TOR", "ERROR DURING FILE PROCESSING");
                        return;
                    }
                } catch (IOException ioe) {
                    Log.e("DOWNLOAD/TOR", "FAILED TO SAVE DOWNLOAD TO " + targetFile + " -> " + ioe.getMessage());
                    ioe.printStackTrace();
                }
            } else {
                Log.e("DOWNLOAD/TOR", "DOWNLOAD FAILED, STATUS CODE: " + statusCode);
            }
        } catch (IOException ioe) {
            Log.e("DOWNLOAD/TOR", "DOWNLOAD FAILED, EXCEPTION: " + ioe.getMessage());
        }
    }

    private synchronized StrongHttpsClient getHttpClientInstance() {
        if (mClient == null) {
            mClient = new StrongHttpsClient(context);
        }

        return mClient;
    }

    public static boolean checkTor(boolean useTor, Context mContext) {
        OrbotHelper orbotHelper = new OrbotHelper(mContext);

        if(useTor && orbotHelper.isOrbotRunning()) {
            Log.d("DOWNLOAD/TOR", "ORBOT RUNNING, USE TOR");
            return true;
        } else {
            Log.d("DOWNLOAD/TOR", "ORBOT NOT RUNNING, DON'T USE TOR");
            return false;
        }
    }

    private boolean handleFile (File targetFile) {

        // additional error checking
        if (targetFile.exists()) {
            if (targetFile.length() == 0) {
                Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + targetFile.getPath() + " BUT IT IS A ZERO BYTE FILE");
                return false;
            } else {
                Log.d("DOWNLOAD", "FINISHED DOWNLOAD OF " + targetFile.getPath() + " AND FILE LOOKS OK");
            }
        } else {
            Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + targetFile.getPath() + " BUT IT DOES NOT EXIST");
            return false;
        }

        // move .tmp file to actual file
        File newFile = new File(targetFile.getPath().substring(0, targetFile.getPath().lastIndexOf(".")));
        Log.d("DOWNLOAD", "ACTUAL FILE: " + newFile.getAbsolutePath());

        try {
            // clean up old obbs before renaming new file
            File directory = new File(newFile.getParent());

            ExpansionIndexItem expansionIndexItem = IndexManager.loadInstalledFileIndex(context).get(newFile.getName());

            if (expansionIndexItem == null) {
                Log.e("DOWNLOAD", "FAILED TO LOCATE EXPANSION INDEX ITEM FOR " + newFile.getName());
                return false;
            }

            String nameFilter = "";
            if (newFile.getName().contains(expansionIndexItem.getExpansionFileVersion())) {
                nameFilter = newFile.getName().replace(expansionIndexItem.getExpansionFileVersion(), "*");
            } else {
                nameFilter = newFile.getName();
            }

            Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + directory.getPath());

            WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
            for (File oldFile : FileUtils.listFiles(directory, oldFileFilter, null)) {
                Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(oldFile);
            }

            FileUtils.moveFile(targetFile, newFile); // moved to commons-io from using exec and mv because we were getting 0kb obb files on some devices
            if (targetFile.exists()) {
                FileUtils.deleteQuietly(targetFile); // for some reason I was getting an 0kb .tmp file lingereing
            }
        } catch (IOException ioe) {
            Log.e("DOWNLOAD", "ERROR DURING CLEANUP/MOVING TEMP FILE: " + ioe.getMessage());
            return false;
        }

        Log.d("DOWNLOAD", "MOVED TEMP FILE " + targetFile.getPath() + " TO " + newFile.getPath());
        return true;
    }

    private void downloadWithManager(Uri uri, String title, String desc, Uri uriFile) {
        initDownloadManager();

        // need to check if a download has already been queued for this file
        HashMap<Long, String> queueMap = QueueManager.loadQueue(context);
        boolean foundInQueue = false;
        for (Long queueId : queueMap.keySet()) {
            if (uriFile.toString().equals(queueMap.get(queueId))) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(queueId.longValue());
                Cursor c = manager.query(query);
                if (c.moveToFirst()) {

                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "DOWNLOAD STATUS FAILED, RE-QUEUEING: " + queueId.toString() + " -> " + uriFile.toString());
                        QueueManager.removeFromQueue(context, Long.valueOf(queueId));

                    } else if (DownloadManager.STATUS_PAUSED == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "DOWNLOAD STATUS PAUSED, NOT QUEUEING: " + queueId.toString() + " -> " + uriFile.toString());
                        foundInQueue = true;

                    } else if (DownloadManager.STATUS_PENDING == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "DOWNLOAD STATUS PENDING, NOT QUEUEING: " + queueId.toString() + " -> " + uriFile.toString());
                        foundInQueue = true;

                    } else if (DownloadManager.STATUS_RUNNING == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "DOWNLOAD STATUS RUNNING, NOT QUEUEING: " + queueId.toString() + " -> " + uriFile.toString());
                        foundInQueue = true;

                    } else if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                        Log.d("QUEUE", "DOWNLOAD STATUS SUCCESS, COMPLETE SO RE-QUEUEING: " + queueId.toString() + " -> " + uriFile.toString());
                        QueueManager.removeFromQueue(context, Long.valueOf(queueId));

                    } else {

                        Log.d("QUEUE", "DOWNLOAD STATUS UNKNOWN, RE-QUEUEING: " + queueId.toString() + " -> " + uriFile.toString());
                        QueueManager.removeFromQueue(context, Long.valueOf(queueId));

                    }
                }
            }
        }

        if (!foundInQueue) {

            Log.d("DOWNLOAD", "QUEUEING DOWNLOAD: " + uri.toString() + " -> " + uriFile.toString());

            lastDownload = manager.enqueue(new DownloadManager.Request(uri)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(title)
                    .setDescription(desc)
                    .setVisibleInDownloadsUi(false)
                    .setDestinationUri(uriFile));

            QueueManager.addToQueue(context, Long.valueOf(lastDownload), uriFile.toString());

        }
    }

    private synchronized void initDownloadManager() {
        if (manager == null) {
            manager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);

            FilteredBroadcastReceiver onComplete = new FilteredBroadcastReceiver(fileName);
            BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    // ???
                }
            };

            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            context.registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
        }
    }

    private class FilteredBroadcastReceiver extends BroadcastReceiver {

        public String fileFilter;
        public boolean fileReceived = false;

        public FilteredBroadcastReceiver(String fileFilter) {
            this.fileFilter = fileFilter;
        }

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

                        Log.d("QUEUE", "DOWNLOAD COMPLETE, REMOVING FROM QUEUE: " + downloadId);
                        QueueManager.removeFromQueue(context, Long.valueOf(downloadId));

                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        File savedFile = new File(Uri.parse(uriString).getPath());
                        Log.d("DOWNLOAD", "MANAGER SAVED DOWNLOAD TO " + savedFile.getPath());

                        File fileCheck = new File(savedFile.getPath().substring(0, savedFile.getPath().lastIndexOf(".")));

                        if (fileReceived) {
                            Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THAT FILE WAS ALREADY PROCESSED");
                            return;
                        } else if (!fileCheck.getName().equals(fileFilter)) {
                            Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THIS RECEIVER IS FOR " + fileFilter);
                            return;
                        } else {
                            Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " AND THIS RECEIVER IS FOR " + fileFilter + ", PROCESSING...");
                            fileReceived = true;
                        }

                        if (!handleFile(savedFile)) {
                            Log.d("DOWNLOAD", "ERROR DURING FILE PROCESSING");
                            return;
                        }
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
    }
}
