package scal.io.liger;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.APKExpansionPolicy;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.http.HttpResponse;
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
    private final static String TAG = "LigerDownloadManager";

    // TODO use HTTPS
    // TODO pickup Tor settings

    private String mainOrPatch;
    private int version;
    private Context context;

    boolean useManager = true;
    private DownloadManager manager;
    private long lastDownload = -1L;

    private static final String ligerId = "scal.io.liger";
    private static final String ligerDevice = Build.MODEL;

    AESObfuscator ligerObfuscator = null;
    APKExpansionPolicy ligerPolicy = null;
    LicenseChecker ligerChecker = null;

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
        // SHOULD BE ABLE TO ATTEMPT TO GET URL FROM GOOGLE LICENSING AND FALL BACK ON OUR SERVER

        byte[] ligerSALT = context.getResources().getString(R.string.liger_salt).getBytes();

        ligerObfuscator = new AESObfuscator(ligerSALT, ligerId, ligerDevice);
        ligerPolicy = new APKExpansionPolicy(context, ligerObfuscator);
        try {
            ligerChecker = new LicenseChecker(context, ligerPolicy, context.getResources().getString(R.string.base64_public_key));
        } catch (Exception e) {
            // need to catch exception thrown if publisher key is invalid
            // default to downloading from our servers
            Log.d("DOWNLOAD", "LICENSE CHECK EXCEPTION THROWN: " + e.getClass().getName() + ", DOWNLOADING FROM LIGER SERVER");
            downloadFromLigerServer();
            return;
        }

        // callback will download from our servers if licence check fails
        LigerCallback ligerCallback = new LigerCallback();

        String deviceVersion = Build.VERSION.RELEASE;

        // not sure what the best way to compare versions is (too many decimal points to convert to a number)
        if (!deviceVersion.startsWith("5.")) {
            Log.d("DOWNLOAD", "ABOUT TO CHECK ACCESS ON ANDROID VERSION " + deviceVersion);
            ligerChecker.checkAccess(ligerCallback);
            Log.d("DOWNLOAD", "ACCESS CHECK WAS INITIATED");
        } else {
            Log.d("DOWNLOAD", "CANNOT CHECK ACCESS ON ANDROID VERSION " + deviceVersion + ", DOWNLOADING FROM LIGER SERVER");
            downloadFromLigerServer();
            return;
        }
    }

    private void downloadFromLigerServer() {
        Log.e("DOWNLOAD", "DOWNLOADING EXTENSION");

        String ligerUrl = Constants.LIGER_URL + "obb" + "/";
        String ligerObb = ZipHelper.getExpansionZipFilename(context, mainOrPatch, version);

        try {
            // if we're managing the download, download only to the files folder
            // if we're using the google play api, download only to the obb folder
            File targetFolder = new File(ZipHelper.getFileFolderName(context));

            Log.d("DOWNLOAD", "TARGET FOLDER: " + targetFolder.getPath());

            URI expansionFileUri = null;
            HttpGet request = null;
            HttpResponse response = null;

            try {
                Log.d("DOWNLOAD", "TARGET URL: " + ligerUrl + ligerObb);

                if (useManager) {

                    // clean up old tmps before downloading

                    String nameFilter = "";
                    if (ligerObb.startsWith(Constants.MAIN)) {
                        nameFilter = nameFilter + Constants.MAIN + ".*." + context.getPackageName() + ".*.tmp";
                    }
                    if (ligerObb.startsWith(Constants.PATCH)) {
                        nameFilter = nameFilter + Constants.PATCH + ".*." + context.getPackageName() + ".*.tmp";
                    }

                    if (nameFilter.length() == 0) {
                        Log.d("DOWNLOAD", "CLEANUP: DON'T KNOW HOW TO BUILD WILDCARD FILTER BASED ON " + ligerObb);
                    } else {
                        Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + targetFolder.getPath());
                    }

                    WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
                    for (File oldFile : FileUtils.listFiles(targetFolder, oldFileFilter, null)) {
                        Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                        FileUtils.deleteQuietly(oldFile);
                    }

                    File targetFile = new File(targetFolder, ligerObb + ".tmp");
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

            FilteredBroadcastReceiver onComplete = new FilteredBroadcastReceiver(ZipHelper.getExpansionZipFilename(context, mainOrPatch, version));
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

                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        File savedFile = new File(Uri.parse(uriString).getPath());
                        Log.d("DOWNLOAD", "MANAGER SAVED DOWNLOAD TO " + savedFile.getPath());

                        // additional error checking
                        if (savedFile.exists()) {
                            if (savedFile.length() == 0) {
                                Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + savedFile.getPath() + " BUT IT IS A ZERO BYTE FILE");
                                return;
                            } else {
                                Log.d("DOWNLOAD", "FINISHED DOWNLOAD OF " + savedFile.getPath() + " AND FILE LOOKS OK");
                            }
                        } else {
                            Log.e("DOWNLOAD", "FINISHED DOWNLOAD OF " + savedFile.getPath() + " BUT IT DOES NOT EXIST");
                            return;
                        }

                        // move .tmp file to actual file
                        File newFile = new File(savedFile.getPath().substring(0, savedFile.getPath().lastIndexOf(".")));
                        Log.d(TAG, "newFile: " + newFile.getAbsolutePath());

                        if (fileReceived) {
                            Log.d("DOWNLOAD", "GOT FILE " + newFile.getName() + " BUT THAT FILE WAS ALREADY PROCESSED");
                            return;
                        } else if (!newFile.getName().equals(fileFilter)) {
                            Log.d("DOWNLOAD", "GOT FILE " + newFile.getName() + " BUT THIS RECEIVER IS FOR " + fileFilter);
                            return;
                        } else {
                            Log.d("DOWNLOAD", "GOT FILE " + newFile.getName() + " AND THIS RECEIVER IS FOR " + fileFilter + ", PROCESSING...");
                            fileReceived = true;
                        }

                        try {
                            // clean up old obbs before renaming new file
                            File directory = new File(newFile.getParent());

                            String nameFilter = "";
                            if (newFile.getName().startsWith(Constants.MAIN)) {
                                nameFilter = nameFilter + Constants.MAIN + ".*." + context.getPackageName() + ".obb";
                            }
                            if (newFile.getName().startsWith(Constants.PATCH)) {
                                nameFilter = nameFilter + Constants.PATCH + ".*." + context.getPackageName() + ".obb";
                            }

                            if (nameFilter.length() == 0) {
                                Log.d("DOWNLOAD", "CLEANUP: DON'T KNOW HOW TO BUILD WILDCARD FILTER BASED ON " + newFile.getName());
                            } else {
                                Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + directory.getPath());
                            }

                            WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
                            for (File oldFile : FileUtils.listFiles(directory, oldFileFilter, null)) {
                                Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                                FileUtils.deleteQuietly(oldFile);
                            }

                            FileUtils.moveFile(savedFile, newFile); // moved to commons-io from using exec and mv because we were getting 0kb obb files on some devices
                            if (savedFile.exists()) {
                                FileUtils.deleteQuietly(savedFile); // for some reason I was getting an 0kb .tmp file lingereing
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("DOWNLOAD", "MOVED TEMP FILE " + savedFile.getPath() + " TO " + newFile.getPath());

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

    private class LigerCallback implements LicenseCheckerCallback {

        @Override
        public void allow(int reason) {
            Log.d("DOWNLOAD", "forcing DOWNLOADING FROM LIGER SERVER"); // FIXME dont check this in
            Log.d("DOWNLOAD", "LICENSE CHECK ALLOWED, DOWNLOADING FROM GOOGLE PLAY");

            String ligerUrl = null;
            String ligerObb = null;

            int count = ligerPolicy.getExpansionURLCount();
            if (mainOrPatch.equals(Constants.MAIN)) {
                if (count < 1) {
                    Log.e("DOWNLOAD", "LOOKING FOR MAIN FILE BUT URL COUNT IS " + count + ", DOWNLOADING FROM LIGER SERVER");
                    downloadFromLigerServer();
                    return;
                } else {
                    ligerUrl = ligerPolicy.getExpansionURL(APKExpansionPolicy.MAIN_FILE_URL_INDEX);
                    ligerObb = ligerPolicy.getExpansionFileName(APKExpansionPolicy.MAIN_FILE_URL_INDEX);
                }
            }
            if (mainOrPatch.equals(Constants.PATCH)) {
                if (count < 2) {
                    Log.e("DOWNLOAD", "LOOKING FOR PATCH FILE BUT URL COUNT IS " + count + ", DOWNLOADING FROM LIGER SERVER");
                    downloadFromLigerServer();
                    return;
                } else {
                    ligerUrl = ligerPolicy.getExpansionURL(APKExpansionPolicy.PATCH_FILE_URL_INDEX);
                    ligerObb = ligerPolicy.getExpansionFileName(APKExpansionPolicy.PATCH_FILE_URL_INDEX);
                }
            }

            // if we're managing the download, download only to the files folder
            // if we're using the google play api, download only to the obb folder
            File targetFolder = new File(ZipHelper.getObbFolderName(context));

            Log.d("DOWNLOAD", "TARGET FOLDER: " + targetFolder.getPath());

            Log.d("DOWNLOAD", "TARGET URL: " + ligerUrl);

            if (useManager) {

                // clean up old tmps before downloading

                String nameFilter = "";
                if (ligerObb.startsWith(Constants.MAIN)) {
                    nameFilter = nameFilter + Constants.MAIN + ".*." + context.getPackageName() + ".*.tmp";
                }
                if (ligerObb.startsWith(Constants.PATCH)) {
                    nameFilter = nameFilter + Constants.PATCH + ".*." + context.getPackageName() + ".*.tmp";
                }

                if (nameFilter.length() == 0) {
                    Log.d("DOWNLOAD", "CLEANUP: DON'T KNOW HOW TO BUILD WILDCARD FILTER BASED ON " + ligerObb);
                } else {
                    Log.d("DOWNLOAD", "CLEANUP: DELETING " + nameFilter + " FROM " + targetFolder.getPath());
                }

                WildcardFileFilter oldFileFilter = new WildcardFileFilter(nameFilter);
                for (File oldFile : FileUtils.listFiles(targetFolder, oldFileFilter, null)) {
                    Log.d("DOWNLOAD", "CLEANUP: FOUND " + oldFile.getPath() + ", DELETING");
                    FileUtils.deleteQuietly(oldFile);
                }

                File targetFile = new File(targetFolder, ligerObb + ".tmp");
                downloadWithManager(Uri.parse(ligerUrl), "Liger " + mainOrPatch + " file download", ligerObb, Uri.fromFile(targetFile));
            } else {
                Log.e("DOWNLOAD", "GOOGLE PLAY DOWNLOADS MUST USE DOWNLOAD MANAGER");
            }
        }

        @Override
        public void dontAllow(int reason) {
            Log.d("DOWNLOAD", "LICENSE CHECK NOT ALLOWED, DOWNLOADING FROM LIGER SERVER");
            downloadFromLigerServer();
        }

        @Override
        public void applicationError(int errorCode) {
            // if your app or version is not managed by google play the result appears
            // to be an application error (code 3?) rather than "do not allow"
            Log.d("DOWNLOAD", "LICENSE CHECK ERROR CODE " + errorCode + ", DOWNLOADING FROM LIGER SERVER");
            downloadFromLigerServer();
        }
    }
}
