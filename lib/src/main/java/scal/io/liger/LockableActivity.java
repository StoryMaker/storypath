package scal.io.liger;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
    public String CACHEWORD_UNSET;
    public String CACHEWORD_FIRST_LOCK;
    public String CACHEWORD_SET;

    protected VirtualFileSystem vfs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CACHEWORD_UNSET = getText(R.string.cacheword_state_unset).toString();
        CACHEWORD_FIRST_LOCK = getText(R.string.cacheword_state_first_lock).toString();
        CACHEWORD_SET = getText(R.string.cacheword_state_set).toString();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int timeout = Integer.parseInt(settings.getString("pcachewordtimeout", "600"));
        mCacheWordHandler = new CacheWordHandler(this, timeout); // TODO: timeout of -1 represents no timeout (revisit)


        // TEST

        String path = StorageHelper.getActualStorageDirectory(this).getPath() + "/" + "FOO" + ".db";
        java.io.File db = new java.io.File(path);
        if (db.exists()) {
            Log.d("IOCIPHER", "delete existing file " + db.getAbsolutePath());
            db.delete();
        } else {
            Log.d("IOCIPHER", "no existing file " + db.getAbsolutePath());
        }
        vfs = VirtualFileSystem.get();
        vfs.setContainerPath(path);

        vfs.mount("PASSWORD");
        if (vfs.isMounted()) {
            Log.d("IOCIPHER", "vfs is mounted");
        } else {
            Log.d("IOCIPHER", "vfs is NOT mounted");
        }
        vfs.unmount();

        //TEST


        /*
        vfs = VirtualFileSystem.get();
        Log.d("IOCIPHER", "onCreate - got vfs singleton");
        // vfs.setContainerPath(getDir("vfs", MODE_PRIVATE).getAbsolutePath() + File.separator + "liger.db");
        // Log.d("IOCIPHER", "onCreate - set path to " + getDir("vfs", MODE_PRIVATE).getAbsolutePath() + File.separator + "liger.db");
        /*
        File checkFile = new File(getDir("vfs", MODE_PRIVATE).getAbsolutePath() + File.separator + "liger.db");
        if (checkFile.exists()) {
            Log.d("IOCIPHER", "found iocipher file");
        } else {
            try {
                checkFile.createNewFile();
                Log.d("IOCIPHER", "created iocipher file");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                Log.d("IOCIPHER", "could not create iocipher file");
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
            Log.d("CACHEWORD", "pin set, so display notification (lockable)");
            mCacheWordHandler.setNotification(buildNotification(this));
        } else {
            Log.d("CACHEWORD", "no pin set, so no notification (lockable)");
        }

        mCacheWordHandler.connectToService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
        if (vfs.isMounted()) {
            vfs.unmount();
            Log.d("IOCIPHER", "onDestroy - vfs un-mounted");
        } else {
            Log.d("IOCIPHER", "onDestroy - vfs not mounted");
        }
        */
    }

    protected Notification buildNotification(Context c) {

        Log.d("CACHEWORD", "buildNotification (lockable)");

        NotificationCompat.Builder b = new NotificationCompat.Builder(c);
        b.setSmallIcon(R.drawable.ic_menu_key);
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
        Log.d("CACHEWORD", "cacheword uninitialized, activity will not continue");
        finish();

    }

    @Override
    public void onCacheWordLocked() {

        // unmount vfs file
        /*
        if (vfs.isMounted()) {
            vfs.unmount();
            Log.d("IOCIPHER", "onCacheWordLocked - vfs un-mounted");
        } else {
            Log.d("IOCIPHER", "onCacheWordLocked - vfs not mounted");
        }
        */

        // if we're locked, default behavior should be to stop
        Log.d("CACHEWORD", "cacheword locked, activity will not continue");
        finish();

    }

    @Override
    public void onCacheWordOpened() {

        // mount vfs file (if a pin has been set)
        /*
        SharedPreferences sp = getSharedPreferences("appPrefs", MODE_PRIVATE);
        String cachewordStatus = sp.getString("cacheword_status", "default");
        if (cachewordStatus.equals(CACHEWORD_SET)) {
            if (mCacheWordHandler.isLocked()) {
                Log.d("IOCIPHER", "onResume - pin set but cacheword locked, cannot mount vfs");
            } else {
                Log.d("IOCIPHER", "onResume - pin set and cacheword unlocked, mounting vfs");

                if (vfs.isMounted()) {
                    Log.d("IOCIPHER", "onResume - vfs already mounted?");
                } else {
                    Log.d("IOCIPHER", "onResume - vfs not mounted");

                    // create file?
                    String vfsPath = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + File.separator + "liger.db";
                    File checkFile = new File(vfsPath);
                    if (checkFile.exists()) {
                        Log.d("IOCIPHER", "onResume - iocipher file found, setting path to " + vfsPath);
                        vfs.setContainerPath(vfsPath);
                        Log.d("IOCIPHER", "onResume - done");

                        // this was the same "password" used for getWritableDatabase() in StoryMakerDBWrapper
                        vfs.mount(encodeRawKey(mCacheWordHandler.getEncryptionKey()));
                        Log.d("IOCIPHER", "onResume - vfs mounted");
                    } else {
                        //checkFile.createNewFile();
                        Log.d("IOCIPHER", "onResume - iocipher file not found, creating new file at " + vfsPath);

                        vfs.unmount();

                        // this was the same "password" used for getWritableDatabase() in StoryMakerDBWrapper
                        vfs.createNewContainer(vfsPath, mCacheWordHandler.getEncryptionKey());
                        Log.d("IOCIPHER", "onResume - done");

                        // createNewContainer also seems to mount the file?
                        Log.d("IOCIPHER", "onResume - vfs mounted (?)");
                    }

                    // this was the same "password" used for getWritableDatabase() in StoryMakerDBWrapper
                    // vfs.mount(encodeRawKey(mCacheWordHandler.getEncryptionKey()));
                    // Log.d("IOCIPHER", "onResume - vfs mounted");
                }
            }
        } else {
            Log.d("IOCIPHER", "onResume - no pin set, cannot mount vfs");
        }
        */

        // if we're opened, check db and update menu status
        Log.d("CACHEWORD", "cacheword opened (liger), activity will continue");

    }

    // copied from StoryMakerDBWrapper, may not be necessary but trying to be consistent
    public static String encodeRawKey(byte[] raw_key) {
        if (raw_key.length != 32)
            throw new IllegalArgumentException("provided key not 32 bytes (256 bits) wide");

        final String kPrefix;
        final String kSuffix;

        if (check_sqlcipher_uses_native_key()) {
            Log.d("IOCIPHER", "sqlcipher uses native method to set key");
            kPrefix = "x'";
            kSuffix = "'";
        } else {
            Log.d("IOCIPHER", "sqlcipher uses PRAGMA to set key - SPECIAL HACK IN PROGRESS");
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
