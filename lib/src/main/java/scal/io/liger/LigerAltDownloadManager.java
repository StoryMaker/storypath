package scal.io.liger;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.APKExpansionPolicy;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpRequestRetryHandler;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.conn.ConnectTimeoutException;
import ch.boye.httpclientandroidlib.entity.mime.Header;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpRequestRetryHandler;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.HttpConnectionParams;
import ch.boye.httpclientandroidlib.params.HttpParams;
import ch.boye.httpclientandroidlib.util.EntityUtils;
import info.guardianproject.onionkit.trust.StrongHttpsClient;
import info.guardianproject.onionkit.ui.OrbotHelper;
import scal.io.liger.model.ExpansionIndexItem;
import scal.io.liger.model.QueueItem;

/**
 * Created by mnbogner on 11/7/14.
 */
public class LigerAltDownloadManager implements Runnable {
    private final static String TAG = "LigerAltDownloadManager";

    // TODO use HTTPS
    // TODO pickup Tor settings

    // store in manager to skip index lookups
    private ExpansionIndexItem indexItem = null;

    private String fileName;
    private Context context;

    private DownloadManager manager;
    private long lastDownload = -1L;

    StrongHttpsClient mClient = null;

    boolean useManager = true;
    boolean useTor = true; // CURRENTLY SET TO TRUE, WILL USE TOR IF ORBOT IS RUNNING

    private String mAppTitle;

    public LigerAltDownloadManager(String fileName, Context context, boolean useManager, ExpansionIndexItem indexItem) {
        this.fileName = fileName;
        this.context = context;
        this.useManager = useManager;
        this.indexItem = indexItem;

        this.mAppTitle = context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).getString(Constants.PREFS_APP_TITLE, "StoryPath");
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

        boolean downloadRequired = false;

        // NOTE: if whatever process was waiting for the download has died, but the download is still underway
        //       it may require a second click  or restart to get to this point.  if we end up here, with a
        //       finished download and no visible file progress, we'll manage the file and return without
        //       starting another download.

        if (checkQueue()) {
            Log.d("DOWNLOAD", "ANOTHER PROCESS IS ALREADY DOWNLOADING " + fileName + ", WILL NOT START DOWNLOAD");
        } else {
            Log.d("DOWNLOAD", "NO OTHER PROCESS IS DOWNLOADING " + fileName + ", CHECKING FOR FILES");

            File tempFile = new File(IndexManager.buildFilePath(indexItem), fileName + ".tmp");

            if (tempFile.exists()) {

                File partFile = managePartialFile(tempFile);

                if (partFile == null) {
                    Log.d("DOWNLOAD", tempFile.getPath().replace(".tmp", ".part") + " DOES NOT EXIST");
                    downloadRequired = true;
                } else {
                    Log.d("DOWNLOAD", partFile.getPath() + " FOUND, CHECKING");

                    // file exists, check size/hash (TODO: hash check)

                    if (partFile.length() == 0) {
                        Log.d("DOWNLOAD", partFile.getPath() + " IS A ZERO BYTE FILE ");
                        downloadRequired = true;
                    } else {

                        if (partFile.getPath().contains(Constants.MAIN)) {
                            if ((indexItem.getExpansionFileSize() > 0) && (indexItem.getExpansionFileSize() > partFile.length())) {
                                Log.d("DOWNLOAD", partFile.getPath() + " IS TOO SMALL (" + partFile.length() + "/" + indexItem.getExpansionFileSize() + ")");
                                downloadRequired = true;
                            } else {

                                // hash check?

                                // partial file is correct size, rename

                                File actualFile = new File(partFile.getPath().replace(".part", ""));

                                try {
                                    FileUtils.moveFile(partFile, actualFile);
                                    FileUtils.deleteQuietly(partFile);
                                    Log.d("DOWNLOAD", "MOVED COMPLETED FILE " + partFile.getPath() + " TO " + actualFile.getPath());
                                } catch (IOException ioe) {
                                    Log.e("DOWNLOAD", "FAILED TO MOVE COMPLETED FILE " + partFile.getPath() + " TO " + actualFile.getPath());
                                    ioe.printStackTrace();
                                    downloadRequired = true;
                                }
                            }
                        } else if (partFile.getPath().contains(Constants.PATCH)) {
                            if ((indexItem.getPatchFileSize() > 0) && (indexItem.getPatchFileSize() > partFile.length())) {
                                Log.d("DOWNLOAD", partFile.getPath() + " IS TOO SMALL (" + partFile.length() + "/" + indexItem.getPatchFileSize() + ")");
                                downloadRequired = true;
                            } else {

                                // hash check?

                                // partial file is correct size, rename

                                File actualFile = new File(partFile.getPath().replace(".part", ""));

                                try {
                                    FileUtils.moveFile(partFile, actualFile);
                                    FileUtils.deleteQuietly(partFile);
                                    Log.d("DOWNLOAD", "MOVED COMPLETED FILE " + partFile.getPath() + " TO " + actualFile.getPath());
                                } catch (IOException ioe) {
                                    Log.e("DOWNLOAD", "FAILED TO MOVE COMPLETED FILE " + partFile.getPath() + " TO " + actualFile.getPath());
                                    ioe.printStackTrace();
                                    downloadRequired = true;
                                }
                            }
                        } else {
                            Log.d("DOWNLOAD", "CAN'T DETERMINE FILE SIZE FOR " + partFile.getPath());
                            downloadRequired = true;
                        }
                    }
                }
            } else {
                Log.d("DOWNLOAD", tempFile.getPath() + " DOES NOT EXIST");
                downloadRequired = true;
            }
        }

        if (downloadRequired) {
            Log.d("DOWNLOAD", fileName + " MUST BE DOWNLOADED");
            downloadFromLigerServer();
        } else {
            Log.d("DOWNLOAD", fileName + " WILL NOT BE DOWNLOADED");
        }

        return;
    }

    public boolean checkQueue() {

        File checkFile = new File(IndexManager.buildFilePath(indexItem), fileName + ".tmp");
        boolean foundInQueue = false;

        // need to check if a download has already been queued for this file
        HashMap<Long, QueueItem> queueMap = QueueManager.loadQueue(context);

        for (Long queueId : queueMap.keySet()) {

            Log.d("QUEUE", "QUEUE ITEM IS " + queueMap.get(queueId).getQueueFile() + " LOOKING FOR " + checkFile.getName());

            if (checkFile.getName().equals(queueMap.get(queueId).getQueueFile())) {

                if (queueId < 0) {
                    // use negative numbers to flag non-manager downloads

                    if (checkFileProgress()) {

                        Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " AND DOWNLOAD PROGRESS OBSERVED, LEAVING " + queueId.toString() + " IN QUEUE ");
                        foundInQueue = true;

                    } else {

                        Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT NO DOWNLOAD PROGRESS OBSERVED, REMOVING " + queueId.toString() + " FROM QUEUE ");
                        QueueManager.removeFromQueue(context, Long.valueOf(queueId));

                    }

                } else {
                    // use download manager ids to flag manager downloads

                    // need to init download manager to check queue
                    initDownloadManager();

                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(queueId.longValue());
                    Cursor c = manager.query(query);
                    if (c.moveToFirst()) {

                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {

                            Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT DOWNLOAD STATUS IS FAILED, REMOVING " + queueId.toString() + " FROM QUEUE ");
                            QueueManager.removeFromQueue(context, Long.valueOf(queueId));

                        } else if (DownloadManager.STATUS_PAUSED == c.getInt(columnIndex)) {

                            Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " AND DOWNLOAD STATUS IS PAUSED, LEAVING " + queueId.toString() + " IN QUEUE ");
                            foundInQueue = true;

                        } else if (DownloadManager.STATUS_PENDING == c.getInt(columnIndex)) {

                            Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " AND DOWNLOAD STATUS IS PENDING, LEAVING " + queueId.toString() + " IN QUEUE ");
                            foundInQueue = true;

                        } else if (DownloadManager.STATUS_RUNNING == c.getInt(columnIndex)) {

                            Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " AND DOWNLOAD STATUS IS RUNNING, LEAVING " + queueId.toString() + " IN QUEUE ");
                            foundInQueue = true;

                        } else if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                            Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT DOWNLOAD STATUS IS SUCCESSFUL, REMOVING " + queueId.toString() + " FROM QUEUE ");
                            QueueManager.removeFromQueue(context, Long.valueOf(queueId));

                        } else {

                            Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT DOWNLOAD STATUS IS UNKNOWN, REMOVING " + queueId.toString() + " FROM QUEUE ");
                            QueueManager.removeFromQueue(context, Long.valueOf(queueId));

                        }
                    } else {

                        Log.d("QUEUE", "QUEUE ITEM FOUND FOR " + checkFile.getName() + " BUT NOTHING FOUND IN DOWNLOAD MANAGER, REMOVING " + queueId.toString() + " FROM QUEUE ");
                        QueueManager.removeFromQueue(context, Long.valueOf(queueId));

                    }

                    // cleanup
                    c.close();
                }
            }

            // skipping timeout check for now, timeout duration undecided

            /*
            if (foundInQueue) {
                Date currentTime = new Date();
                long queuedTime = queueMap.get(queueId).getQueueTime();
                if ((currentTime.getTime() - queueMap.get(queueId).getQueueTime()) > QueueManager.queueTimeout) {

                    Log.d("QUEUE", "TIMEOUT EXCEEDED, REMOVING " + queueId.toString() + " FROM DOWNLOAD MANAGER.");
                    int numberRemoved = manager.remove(queueId);

                    if (numberRemoved == 1) {
                        Log.d("QUEUE", "REMOVED FROM DOWNLOAD MANAGER, RE-QUEUEING: " + queueId.toString() + " -> " + uriFile.toString());
                        QueueManager.removeFromQueue(context, Long.valueOf(queueId));
                        foundInQueue = false;
                    } else {
                        Log.d("QUEUE", "FAILED TO REMOVE FROM DOWNLOAD MANAGER, NOT QUEUEING: " + queueId.toString() + " -> " + uriFile.toString());
                    }
                }
            }
            */
        }

        return foundInQueue;
    }

    public boolean checkFileProgress() {

        // not a great solution, but should indicate if file is being actively downloaded
        // only .tmp files should be download targets

        File checkFile = new File(IndexManager.buildFilePath(indexItem), fileName + ".tmp");
        if (checkFile.exists()) {
            long firstSize = checkFile.length();

            // wait for download progress
            try {
                synchronized (this) {
                    wait(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long secondSize = checkFile.length();

            if (secondSize > firstSize) {
                Log.d("DOWNLOAD", "DOWNLOAD IN PROGRESS FOR " + checkFile.getPath() + "(" + firstSize + " -> " + secondSize + ")");

                return true;
            } else {
                Log.d("DOWNLOAD", "NO DOWNLOAD PROGRESS FOR " + checkFile.getPath() + "(" + firstSize + " -> " + secondSize + ")");

                return false;
            }
        } else {
            Log.d("DOWNLOAD", "NO FILE FOUND FOR " + checkFile.getPath());

            return false;
        }
    }

    private File managePartialFile (File tempFile) {

        // return null if an error occurs

        // otherwise return .part file name

        File partFile = new File(tempFile.getPath().replace(".tmp", ".part"));

        // if there is no current partial file, rename .tmp file
        if (!partFile.exists()) {
            try {
                FileUtils.moveFile(tempFile, partFile);
                FileUtils.deleteQuietly(tempFile);
                Log.d("DOWNLOAD", "MOVED INCOMPLETE FILE " + tempFile.getPath() + " TO " + partFile.getPath());
                return partFile;
            } catch (IOException ioe) {
                Log.e("DOWNLOAD", "FAILED TO MOVE INCOMPLETE FILE " + tempFile.getPath() + " TO " + partFile.getPath());
                ioe.printStackTrace();

                return null;
            }
        } else {
            // if there is a current partial file, append .tmp file contents and remove .tmp file
            try {

                Log.d("APPEND", "MAKE FILE INPUT STREAM FOR " + tempFile.getPath());
                BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(tempFile));

                Log.d("APPEND", "MAKE FILE OUTPUT STREAM FOR " + partFile.getPath());
                FileOutputStream fileOutput = new FileOutputStream(partFile, true);

                byte[] buf = new byte[1024];
                int i;
                while ((i = fileInput.read(buf)) > 0) {
                    fileOutput.write(buf, 0, i);
                }

                fileOutput.flush();
                fileOutput.close();
                fileOutput = null;

                fileInput.close();
                fileInput = null;


                FileUtils.deleteQuietly(tempFile);
                Log.d("DOWNLOAD", "APPENDED " + tempFile.getPath() + " TO " + partFile.getPath());

                return partFile;

            } catch (IOException ioe) {
                Log.e("DOWNLOAD", "FAILED TO APPENDED " + tempFile.getPath() + " TO " + partFile.getPath());
                ioe.printStackTrace();

                return null;
            }
        }
    }

    private void downloadFromLigerServer() {

        String ligerUrl = indexItem.getExpansionFileUrl();
        String ligerPath = IndexManager.buildFilePath(indexItem);
        String ligerObb = fileName;

        Log.d("DOWNLOAD", "DOWNLOADING " + ligerObb + " FROM " + ligerUrl + " TO " + ligerPath);

        try {

            URI expansionFileUri = null;
            HttpGet request = null;
            HttpResponse response = null;

            File targetFolder = new File(ligerPath);

            String nameFilter = "";

            if (ligerObb.contains(indexItem.getExpansionFileVersion())) {
                nameFilter = fileName.replace(indexItem.getExpansionFileVersion(), "*") + "*.tmp";
            } else {
                nameFilter = fileName + "*.tmp";
            }

            Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + targetFolder.getPath());

            WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
            for (File oldFile : FileUtils.listFiles(targetFolder, oldFileFilter, null)) {
                Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(oldFile);
            }

            // additional cleanup of pre-name-change files
            if (fileName.contains(Constants.MAIN)) {
                nameFilter = fileName.replace(Constants.MAIN + ".", "").replace(indexItem.getExpansionFileVersion(), "*");

                Log.d("DOWNLOAD", "CLEANUP: DELETING OLD FILES " + nameFilter + " FROM " + targetFolder.getPath());

                oldFileFilter = new WildcardFileFilter(nameFilter);
                for (File oldFile : FileUtils.listFiles(targetFolder, oldFileFilter, null)) {
                    Log.d("DOWNLOAD", "CLEANUP: FOUND OLD FILE " + oldFile.getPath() + ", DELETING");
                    FileUtils.deleteQuietly(oldFile);
                }
            }

            File targetFile = new File(targetFolder, ligerObb + ".tmp");

            // if there is no connectivity, do not queue item (no longer seems to pause if connection is unavailable)
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();

            if ((ni != null) && (ni.isConnectedOrConnecting())) {

                // assuming (Activity) cast is safe since HomeActivity is being passed in as context
                Utility.toastOnUiThread((Activity)context, "Starting download of " + indexItem.getExpansionId() + " content pack.", true); // FIXME move to strings

                if (checkTor(useTor, context)) {
                    downloadWithTor(Uri.parse(ligerUrl + ligerObb), mAppTitle + " content download", ligerObb, targetFile);
                } else {
                    downloadWithManager(Uri.parse(ligerUrl + ligerObb), mAppTitle + " content download", ligerObb, Uri.fromFile(targetFile));
                }

            } else {
                Log.d("DOWNLOAD", "NO CONNECTION, NOT QUEUEING DOWNLOAD: " + ligerUrl + ligerObb + " -> " + targetFile.getPath());
            }

        } catch (Exception e) {
            Log.e("DOWNLOAD", "DOWNLOAD ERROR: " + ligerUrl + ligerObb + " -> " + e.getMessage());
            e.printStackTrace();
        }
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

    private void downloadWithTor(Uri uri, String title, String desc, File targetFile) {
        Log.d("DOWNLOAD/TOR", "DOWNLOAD WITH TOR PROXY: " + Constants.TOR_PROXY_HOST + "/" + Constants.TOR_PROXY_PORT);

        StrongHttpsClient httpClient = getHttpClientInstance();
        httpClient.useProxy(true, "http", Constants.TOR_PROXY_HOST, Constants.TOR_PROXY_PORT); // CLASS DOES NOT APPEAR TO REGISTER A SCHEME FOR SOCKS, ORBOT DOES NOT APPEAR TO HAVE AN HTTPS PORT

        // disable attempts to retry (more retries ties up connection and prevents failure handling)
        HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(0, false);
        httpClient.setHttpRequestRetryHandler(retryHandler);

        // set modest timeout (longer timeout ties up connection and prevents failure handling)
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 1000);
        HttpConnectionParams.setSoTimeout(params, 1000);

        httpClient.setParams(params);

        String actualFileName = targetFile.getName().substring(0, targetFile.getName().lastIndexOf("."));

        Log.d("DOWNLOAD/TOR", "CHECKING URI: " + uri.toString());

        try {

            HttpGet request = new HttpGet(uri.toString());

            // check for partially downloaded file
            File partFile = new File(targetFile.getPath().replace(".tmp", ".part"));

            if (partFile.exists()) {
                long partBytes = partFile.length();
                Log.d("DOWNLOAD", "PARTIAL FILE " + partFile.getPath() + " FOUND, SETTING RANGE HEADER: " + "Range" + " / " + "bytes=" + Long.toString(partBytes) + "-");
                request.setHeader("Range", "bytes=" + Long.toString(partBytes) + "-");
            } else {
                Log.d("DOWNLOAD", "PARTIAL FILE " + partFile.getPath() + " NOT FOUND, STARTING AT BYTE 0");
            }

            // HERE...

            HttpResponse response = httpClient.execute(request);

            HttpEntity entity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();

            if ((statusCode == 200) || (statusCode == 206)) {

                // ...TO HERE, 30 SECOND DELAY?

                Log.d("DOWNLOAD/TOR", "DOWNLOAD SUCCEEDED, STATUS CODE: " + statusCode);

                // queue item here, "download" doesn't start until after we get a status code

                // queue item, use date to get a unique long, subtract to get a negative number (to distinguish from download manager items)
                Date startTime = new Date();
                long queueId = 0 - startTime.getTime();
                QueueManager.addToQueue(context, queueId, targetFile.getName());

                targetFile.getParentFile().mkdirs();

                Log.d("DOWNLOAD/TOR", "DOWNLOAD SUCCEEDED, GETTING ENTITY...");

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
                } catch (ConnectTimeoutException cte) {
                    Log.e("DOWNLOAD/TOR", "FAILED TO SAVE DOWNLOAD TO " + actualFileName + " (CONNECTION EXCEPTION)");
                    //cte.printStackTrace();
                } catch (SocketTimeoutException ste) {
                    Log.e("DOWNLOAD/TOR", "FAILED TO SAVE DOWNLOAD TO " + actualFileName + " (SOCKET EXCEPTION)");
                    //ste.printStackTrace();
                } catch (IOException ioe) {
                    Log.e("DOWNLOAD/TOR", "FAILED TO SAVE DOWNLOAD TO " + actualFileName + " (IO EXCEPTION)");
                    //ioe.printStackTrace();
                }

                // remove from queue here, regardless of success
                QueueManager.removeFromQueue(context, queueId);

                // handle file here, regardless of success
                // (assumes .tmp file will exist if download is interrupted)
                if (!handleFile(targetFile)) {
                    Log.e("DOWNLOAD/TOR", "ERROR DURING FILE PROCESSING FOR " + actualFileName);
                }

            } else {
                Log.e("DOWNLOAD/TOR", "DOWNLOAD FAILED FOR " + actualFileName + ", STATUS CODE: " + statusCode);
            }

            // clean up connection
            EntityUtils.consume(entity);
            request.abort();
            request.releaseConnection();

        } catch (IOException ioe) {
            Log.e("DOWNLOAD/TOR", "DOWNLOAD FAILED FOR " + actualFileName + ", EXCEPTION THROWN");
            ioe.printStackTrace();
        }
    }

    private synchronized StrongHttpsClient getHttpClientInstance() {
        if (mClient == null) {
            mClient = new StrongHttpsClient(context);
        }

        return mClient;
    }

    private void downloadWithManager(Uri uri, String title, String desc, Uri uriFile) {
        initDownloadManager();

        // if there is no connectivity, do not queue item (no longer seems to pause if connection is unavailable)
        //ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //NetworkInfo ni = cm.getActiveNetworkInfo();

        //if ((ni != null) && (ni.isConnectedOrConnecting())) {

            Log.d("DOWNLOAD", "QUEUEING DOWNLOAD: " + uri.toString() + " -> " + uriFile.toString());

            initReceivers();

            DownloadManager.Request request = new DownloadManager.Request(uri)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(title)
                    .setDescription(desc)
                    .setVisibleInDownloadsUi(false)
                    .setDestinationUri(uriFile);

            File partFile = new File(uriFile.toString().replace(".tmp", ".part"));

            if (partFile.exists()) {
                long partBytes = partFile.length();
                Log.d("DOWNLOAD", "PARTIAL FILE " + partFile.getPath() + " FOUND, SETTING RANGE HEADER: " + "Range" + " / " + "bytes=" + Long.toString(partBytes) + "-");
                request.addRequestHeader("Range", "bytes=" + Long.toString(partBytes) + "-");
            } else {
                Log.d("DOWNLOAD", "PARTIAL FILE " + partFile.getPath() + " NOT FOUND, STARTING AT BYTE 0");
            }

            lastDownload = manager.enqueue(request);

            // have to enqueue first to get manager id
            String uriString = uriFile.toString();
            QueueManager.addToQueue(context, Long.valueOf(lastDownload), uriString.substring(uriString.lastIndexOf("/") + 1));

        //} else {
        //    Log.d("DOWNLOAD", "NO CONNECTION, NOT QUEUEING DOWNLOAD: " + uri.toString() + " -> " + uriFile.toString());
        //}
    }

    private synchronized void initDownloadManager() {
        if (manager == null) {
            manager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        }
    }

    private synchronized void initReceivers() {
        FilteredBroadcastReceiver onComplete = new FilteredBroadcastReceiver(fileName);
        BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                // ???
            }
        };

        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        context.registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
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

                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                        File savedFile = new File(Uri.parse(uriString).getPath());
                        Log.d("DOWNLOAD", "PROCESSING DOWNLOADED FILE " + savedFile.getPath());

                        File fileCheck = new File(savedFile.getPath().substring(0, savedFile.getPath().lastIndexOf(".")));

                        if (fileReceived) {
                            Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THIS RECEIVER HAS ALREADY PROCESSED A FILE");
                            return;
                        } else if (!fileCheck.getName().equals(fileFilter)) {
                            Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THIS RECEIVER IS FOR " + fileFilter);
                            return;
                        } else {
                            Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " AND THIS RECEIVER IS FOR " + fileFilter + ", PROCESSING...");
                            fileReceived = true;
                        }

                        QueueManager.removeFromQueue(context, Long.valueOf(downloadId));

                        Log.d("QUEUE", "DOWNLOAD COMPLETE, REMOVING FROM QUEUE: " + downloadId);

                        if (!handleFile(savedFile)) {
                            Log.e("DOWNLOAD", "ERROR DURING FILE PROCESSING FOR " + fileCheck.getName());

                        } else {
                            Log.e("DOWNLOAD", "FILE PROCESSING COMPLETE FOR " + fileCheck.getName());
                        }
                    } else {

                        // COLUMN_LOCAL_URI seems to be null if download fails
                        // COLUMN_URI is the download url, not the .tmp file path
                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                        String uriName = uriString.substring(uriString.lastIndexOf("/"));

                        File savedFile = new File(IndexManager.buildFilePath(indexItem), uriName + ".tmp");
                        Log.d("DOWNLOAD", "PROCESSING DOWNLOADED FILE " + savedFile.getPath());

                        File fileCheck = new File(savedFile.getPath().substring(0, savedFile.getPath().lastIndexOf(".")));

                        if (fileReceived) {
                            Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THIS RECEIVER HAS ALREADY PROCESSED A FILE");
                            return;
                        } else if (!fileCheck.getName().equals(fileFilter)) {
                            Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " BUT THIS RECEIVER IS FOR " + fileFilter);
                            return;
                        } else {
                            Log.d("DOWNLOAD", "GOT FILE " + fileCheck.getName() + " AND THIS RECEIVER IS FOR " + fileFilter + ", PROCESSING...");
                            fileReceived = true;
                        }

                        String status;
                        boolean willResume = true;

                        // improve feedback
                        if (DownloadManager.STATUS_RUNNING == c.getInt(columnIndex)) {
                            status = "RUNNING";
                        } else if (DownloadManager.STATUS_PENDING == c.getInt(columnIndex)) {
                            status = "PENDING";
                        } else if (DownloadManager.STATUS_PAUSED == c.getInt(columnIndex)) {
                            status = "PAUSED";
                        } else if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {
                            status = "FAILED";
                            willResume = false;
                        } else {
                            status = "UNKNOWN";
                            willResume = false;
                        }

                        Log.e("DOWNLOAD", "MANAGER FAILED AT STATUS CHECK, STATUS IS " + status);

                        if (willResume) {
                            Log.e("DOWNLOAD", "STATUS IS " + status + ", LEAVING QUEUE/FILES AS-IS FOR MANAGER TO HANDLE");
                        } else {
                            Log.e("DOWNLOAD", "STATUS IS " + status + ", CLEANING UP QUEUE/FILES, MANAGER WILL NOT RESUME");

                            Log.d("QUEUE", "DOWNLOAD STOPPED, REMOVING FROM QUEUE: " + downloadId);

                            QueueManager.removeFromQueue(context, Long.valueOf(downloadId));

                            if (!handleFile(savedFile)) {
                                Log.e("DOWNLOAD", "ERROR DURING FILE PROCESSING FOR " + fileCheck.getName());
                            } else {
                                Log.e("DOWNLOAD", "FILE PROCESSING COMPLETE FOR " + fileCheck.getName());
                            }
                        }
                    }
                } else {
                    Log.e("DOWNLOAD", "MANAGER FAILED AT QUERY");
                }
            } else {
                Log.e("DOWNLOAD", "MANAGER FAILED AT COMPLETION CHECK");
            }
        }
    }

    private boolean handleFile (File tempFile) {

        File appendedFile = null;

        // need index item first to check file size
        File actualFile = new File(tempFile.getPath().substring(0, tempFile.getPath().lastIndexOf(".")));
        Log.d("DOWNLOAD", "ACTUAL FILE: " + actualFile.getAbsolutePath());

        // additional error checking
        if (tempFile.exists()) {
            if (tempFile.length() == 0) {
                Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + tempFile.getPath() + " BUT IT IS A ZERO BYTE FILE");
                return false;
            } else if (tempFile.length() < indexItem.getExpansionFileSize()) {

                Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + tempFile.getPath() + " BUT IT IS TOO SMALL: " + Long.toString(tempFile.length()) + "/" + Long.toString(indexItem.getExpansionFileSize()));

                // if file is too small, managePartialFile
                appendedFile = managePartialFile(tempFile);

                // if appended file is still too small, fail (leave .part file for next download
                if (appendedFile == null) {
                    Log.e("DOWNLOAD", "ERROR WHILE APPENDING TO PARTIAL FILE FOR " + tempFile.getPath());
                    return false;
                } else if (appendedFile.length() < indexItem.getExpansionFileSize()) {
                    Log.e("DOWNLOAD", "APPENDED FILE " + appendedFile.getPath() + " IS STILL TOO SMALL: " + Long.toString(appendedFile.length()) + "/" + Long.toString(indexItem.getExpansionFileSize()));
                    return false;
                } else {
                    Log.e("DOWNLOAD", "APPENDED FILE " + appendedFile.getPath() + " IS COMPLETE!");
                }
            } else {
                Log.d("DOWNLOAD", "FINISHED DOWNLOAD OF " + tempFile.getPath() + " AND FILE LOOKS OK");
            }
        } else {
            Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + tempFile.getPath() + " BUT IT DOES NOT EXIST");
            return false;
        }

        try {
            // clean up old obbs before renaming new file
            File directory = new File(actualFile.getParent());

            String nameFilter = "";
            if (actualFile.getName().contains(indexItem.getExpansionFileVersion())) {
                nameFilter = actualFile.getName().replace(indexItem.getExpansionFileVersion(), "*");
            } else {
                nameFilter = actualFile.getName();
            }

            Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + directory.getPath());

            WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
            for (File oldFile : FileUtils.listFiles(directory, oldFileFilter, null)) {
                Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                FileUtils.deleteQuietly(oldFile);
            }

            if ((appendedFile != null) && appendedFile.exists()) {
                FileUtils.moveFile(appendedFile, actualFile); // moved to commons-io from using exec and mv because we were getting 0kb obb files on some devices
                FileUtils.deleteQuietly(appendedFile); // for some reason I was getting an 0kb .tmp file lingereing
                FileUtils.deleteQuietly(tempFile); // for some reason I was getting an 0kb .tmp file lingereing
                Log.d("DOWNLOAD", "MOVED PART FILE " + appendedFile.getPath() + " TO " + actualFile.getPath());
            } else if (tempFile.exists()) {
                FileUtils.moveFile(tempFile, actualFile); // moved to commons-io from using exec and mv because we were getting 0kb obb files on some devices
                FileUtils.deleteQuietly(tempFile); // for some reason I was getting an 0kb .tmp file lingereing
                Log.d("DOWNLOAD", "MOVED TEMP FILE " + tempFile.getPath() + " TO " + actualFile.getPath());
            } else {
                // not sure how we get here but this is a failure state
                Log.e("DOWNLOAD", ".TMP AND .PART FILES DO NOT EXIST FOR " + tempFile.getPath());
                return false;
            }
        } catch (IOException ioe) {
            Log.e("DOWNLOAD", "ERROR DURING CLEANUP/MOVING TEMP FILE: " + ioe.getMessage());
            return false;
        }

        return true;
    }
}
