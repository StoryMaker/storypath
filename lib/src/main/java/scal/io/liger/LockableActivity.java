package scal.io.liger;

import timber.log.Timber;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.CharBuffer;

import info.guardianproject.cacheword.CacheWordHandler;
import info.guardianproject.cacheword.ICacheWordSubscriber;
import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.VirtualFileSystem;

/**
 * Created by mnbogner on 4/14/15.
 */
public class LockableActivity extends Activity implements ICacheWordSubscriber {

    protected CacheWordHandler mCacheWordHandler;
    public static final String CACHEWORD_UNSET = "unset";
    public static final String CACHEWORD_FIRST_LOCK = "first_lock";
    public static final String CACHEWORD_SET = "set";
    public static final String CACHEWORD_TIMEOUT = "300";

    // protected VirtualFileSystem vfs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int timeout = Integer.parseInt(settings.getString("pcachewordtimeout", CACHEWORD_TIMEOUT));
        mCacheWordHandler = new CacheWordHandler(this, timeout);

        // TEST

        /*
        String path = StorageHelper.getActualStorageDirectory(this).getPath() + "/" + "FOO" + ".db";
        java.io.File db = new java.io.File(path);
        if (db.exists()) {
            Timber.d("delete existing file " + db.getAbsolutePath());
            db.delete();
        } else {
            Timber.d("no existing file " + db.getAbsolutePath());
        }
        vfs = VirtualFileSystem.get();
        vfs.setContainerPath(path);

        vfs.mount("PASSWORD");
        if (vfs.isMounted()) {
            Timber.d("vfs is mounted");
        } else {
            Timber.d("vfs is NOT mounted");
        }
        vfs.unmount();
        */

        //TEST

        /*
        vfs = VirtualFileSystem.get();
        Timber.d("onCreate - got vfs singleton");
        // vfs.setContainerPath(getDir("vfs", MODE_PRIVATE).getAbsolutePath() + File.separator + "liger.db");
        // Timber.d("onCreate - set path to " + getDir("vfs", MODE_PRIVATE).getAbsolutePath() + File.separator + "liger.db");
        /*
        File checkFile = new File(getDir("vfs", MODE_PRIVATE).getAbsolutePath() + File.separator + "liger.db");
        if (checkFile.exists()) {
            Timber.d("found iocipher file");
        } else {
            try {
                checkFile.createNewFile();
                Timber.d("created iocipher file");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Timber.d("could not create iocipher file");
            }
        }
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCacheWordHandler.disconnectFromService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // only display notification if the user has set a pin
        SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        if (cachewordStatus.equals(CACHEWORD_SET)) {
            Timber.d("pin set, so display notification (lockable)");
            mCacheWordHandler.setNotification(buildNotification(this));
        } else {
            Timber.d("no pin set, so no notification (lockable)");
        }

        mCacheWordHandler.connectToService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
        if (vfs.isMounted()) {
            vfs.unmount();
            Timber.d("onDestroy - vfs un-mounted");
        } else {
            Timber.d("onDestroy - vfs not mounted");
        }
        */
    }

    protected Notification buildNotification(Context c) {

        Timber.d("buildNotification (lockable)");

        NotificationCompat.Builder b = new NotificationCompat.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_menu_info_details);
        b.setContentTitle(c.getText(R.string.cacheword_notification_cached_title));
        b.setContentText(c.getText(R.string.cacheword_notification_cached_message));
        b.setTicker(c.getText(R.string.cacheword_notification_cached));
        b.setWhen(System.currentTimeMillis());
        b.setOngoing(true);
        b.setContentIntent(CacheWordHandler.getPasswordLockPendingIntent(c));
        return b.build();
    }

    @Override
    public void onCacheWordUninitialized() {

        // if we're uninitialized, default behavior should be to stop
        Timber.d("cacheword uninitialized, activity will not continue");
        finish();

    }

    @Override
    public void onCacheWordLocked() {

        // unmount vfs file
        /*
        if (vfs.isMounted()) {
            vfs.unmount();
            Timber.d("onCacheWordLocked - vfs un-mounted");
        } else {
            Timber.d("onCacheWordLocked - vfs not mounted");
        }
        */

        // if we're locked, default behavior should be to stop
        Timber.d("cacheword locked, activity will not continue");
        finish();

    }

    @Override
    public void onCacheWordOpened() {

        // mount vfs file (if a pin has been set)

        // NEW ATTEMPT, USES MANAGER

        SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        if (cachewordStatus.equals(CACHEWORD_SET)) {
            if (mCacheWordHandler.isLocked()) {
                Timber.d("onCacheWordOpened - pin set but cacheword locked, cannot mount vfs");
            } else {
                Timber.d("onCacheWordOpened - pin set and cacheword unlocked, mounting vfs");

                StorageHelper.mountStorage(this, null, mCacheWordHandler.getEncryptionKey());
            }
        } else {
            Timber.d("onCacheWordOpened - no pin set, cannot mount vfs");
        }

        /*
        SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        if (cachewordStatus.equals(CACHEWORD_SET)) {
            if (mCacheWordHandler.isLocked()) {
                Timber.d("onResume - pin set but cacheword locked, cannot mount vfs");
            } else {
                Timber.d("onResume - pin set and cacheword unlocked, mounting vfs");

                if (vfs.isMounted()) {
                    Timber.d("onResume - vfs already mounted?");
                } else {
                    Timber.d("onResume - vfs not mounted");

                    // create file?
                    String vfsPath = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + File.separator + "liger.db";
                    File checkFile = new File(vfsPath);
                    if (checkFile.exists()) {
                        Timber.d("onResume - iocipher file found, setting path to " + vfsPath);
                        vfs.setContainerPath(vfsPath);
                        Timber.d("onResume - done");

                        // this was the same "password" used for getWritableDatabase() in StoryMakerDBWrapper
                        vfs.mount(encodeRawKey(mCacheWordHandler.getEncryptionKey()));
                        Timber.d("onResume - vfs mounted");
                    } else {
                        //checkFile.createNewFile();
                        Timber.d("onResume - iocipher file not found, creating new file at " + vfsPath);

                        vfs.unmount();

                        // this was the same "password" used for getWritableDatabase() in StoryMakerDBWrapper
                        vfs.createNewContainer(vfsPath, mCacheWordHandler.getEncryptionKey());
                        Timber.d("onResume - done");

                        // createNewContainer also seems to mount the file?
                        Timber.d("onResume - vfs mounted (?)");
                    }

                    // this was the same "password" used for getWritableDatabase() in StoryMakerDBWrapper
                    // vfs.mount(encodeRawKey(mCacheWordHandler.getEncryptionKey()));
                    // Timber.d("onResume - vfs mounted");
                }
            }
        } else {
            Timber.d("onResume - no pin set, cannot mount vfs");
        }
        */

        // if we're opened, check db and update menu status
        Timber.d("cacheword opened (liger), activity will continue");

    }

    // copied from StoryMakerDBWrapper, may not be necessary but trying to be consistent
    public static String encodeRawKey(byte[] raw_key) {
        if (raw_key.length != 32)
            throw new IllegalArgumentException("provided key not 32 bytes (256 bits) wide");

        final String kPrefix;
        final String kSuffix;

        if (check_sqlcipher_uses_native_key()) {
            Timber.d("sqlcipher uses native method to set key");
            kPrefix = "x'";
            kSuffix = "'";
        } else {
            Timber.d("sqlcipher uses PRAGMA to set key - SPECIAL HACK IN PROGRESS");
            kPrefix = "x''";
            kSuffix = "''";
        }
        final char[] key_chars = encodeHex(raw_key, HEX_DIGITS_LOWER);
        if (key_chars.length != 64)
            throw new IllegalStateException("encoded key is not 64 bytes wide");

        char[] kPrefix_c = kPrefix.toCharArray();
        char[] kSuffix_c = kSuffix.toCharArray();
        CharBuffer cb = CharBuffer.allocate(kPrefix_c.length + kSuffix_c.length + key_chars.length);
        cb.put(kPrefix_c);
        cb.put(key_chars);
        cb.put(kSuffix_c);

        return cb.toString();
    }

    private static final char[] HEX_DIGITS_LOWER = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    private static boolean check_sqlcipher_uses_native_key() {

        for (Method method : SQLiteDatabase.class.getDeclaredMethods()) {
            if (method.getName().equals("native_key"))
                return true;
        }
        return false;
    }

    private static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }
}
