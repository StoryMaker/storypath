package scal.io.liger;

import timber.log.Timber;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import info.guardianproject.iocipher.VirtualFileSystem;

/**
 * Created by mnbogner on 8/6/15.
 */
public class StorageHelper {

    public static final String KEY_USE_INTERNAL = "p_use_internal_storage";

    @Nullable
    public static File getActualStorageDirectory(Context context) {
        // FIXME this shouldn't be nullable, it should either return internal or external!
        // locate actual external storage path if available

        File returnValue = null;

        // values for debugging
        int storageState = 0;
        int storageCount = 0;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // can't use this method for older versions, just use existing method

            // Timber.d("VERSION " + Build.VERSION.SDK_INT + ", USING OLD METHOD: " + context.getExternalFilesDir(null).getPath());

            storageState = 1;

            // FIXME we should be detecting sd state and reacting appropriately.  if the user has selected SD and its currently diconnectes, perhaps the best action is to popup a message saying "hey, your sd is busy.  either free it up or switch to internal storage (and and stories stored on external should vanish)
            //      getExternalStorageState
            returnValue = context.getExternalFilesDir(null); // FIXME this is nullable, we should check it and use getFilesDir instead?

        } else {
            // use new method to get all directories, only the first directory should be internal storage
            File[] externalFilesDirs = context.getExternalFilesDirs(null);
            storageCount = externalFilesDirs.length;
            /// FIXME what if internal is null?  try to use external even though they said useIinternal
            returnValue = externalFilesDirs[0]; // FIXME default our returnValue to the internal so if we fail finding a more appropriate place this is our fallback
            storageState = 2; // FIXME just for debugging now

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            boolean useInternal = settings.getBoolean(KEY_USE_INTERNAL, false);
            if (useInternal) {
                storageState = 4;
            } else {
                if (externalFilesDirs.length > 1) {
                    // iterate over storage options in case one or more is unavailable
                    int i = 1;
                    while (i < externalFilesDirs.length) {
                        if (externalFilesDirs[i] != null) {
                            storageState = 3;
                            returnValue = externalFilesDirs[i];
                            break; // FIXME using the first available card, if we detect more than one we should probably let the user pick which one?
                        }
                        i++;
                    }
                }
            }
        }

        if (returnValue == null) {
            Timber.e("EXTERNAL FILES DIRECTORY IS NULL (STORAGE IS UNAVAILABLE)");

            switch (storageState) {
                case 1:
                    Timber.e("PRE-JELLYBEAN BUILD " + Build.VERSION.SDK_INT + " FOUND SO INTERNAL STORAGE MUST BE USED");
                    break;
                case 2:
                    Timber.e(storageCount + " EXTERNAL STORAGE OPTIONS FOUND BUT USER SELECTED INTERNAL STORAGE");
                    break;
                case 3:
                    Timber.e(storageCount + " EXTERNAL STORAGE OPTIONS FOUND AND USER SELECTED EXTERNAL STORAGE");
                    break;
                case 4:
                    Timber.e(storageCount + " EXTERNAL STORAGE OPTIONS FOUND SO INTERNAL STORAGE MUST BE USED");
                    break;
                default:
                    Timber.e("UNEXPECTED STATE");
                    break;
            }
        }

        return returnValue;
    }

    public static String fixPath (String currentPath, Context context) {

        // instances and indexes may store full paths, need to compare and update

        String actualPath = getActualStorageDirectory(context).getPath();

        if (!currentPath.contains(actualPath)) {

            Timber.d(currentPath + " MUST BE UPDATED -> " + actualPath);

            return actualPath + currentPath.substring(currentPath.lastIndexOf(File.separator));

        }

        return currentPath;
    }

    public static boolean migrateToExternal(Context context) {

        // migrate files from internal storage to external storage if available

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // can't use this method for older versions, so no point in migrating

            Timber.e("VERSION " + Build.VERSION.SDK_INT + ", CAN'T DETERMINE EXTERNAL PATH FOR MIGRATION");

            return false;

        } else {

            // use new method to get all directories, only the first directory should be internal storage

            File[] externalFilesDirs = context.getExternalFilesDirs(null);

            if (externalFilesDirs.length > 1) {

                Timber.d("VERSION " + Build.VERSION.SDK_INT + ", MIGRATING FILES TO EXTERNAL PATH " + externalFilesDirs[1].getPath());

                return moveFromHereToThere(externalFilesDirs[0], externalFilesDirs[1]);

            } else {

                // no external directories available, so no point in migrating

                Timber.e("VERSION " + Build.VERSION.SDK_INT + ", NO EXTERNAL PATH FOR MIGRATION");

                return false;

            }
        }
    }

    // adding this method to allow the user to switch back to internal storage

    public static boolean migrateFromExternal(Context context) {

        // migrate files from external storage (if available) to internal storage

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            // can't use this method for older versions, so no point in migrating

            Timber.e("VERSION " + Build.VERSION.SDK_INT + ", CAN'T DETERMINE EXTERNAL PATH FOR MIGRATION");

            return false;

        } else {

            // use new method to get all directories, only the first directory should be internal storage

            File[] externalFilesDirs = context.getExternalFilesDirs(null);

            if (externalFilesDirs.length > 1) {

                // TODO: actually migrate files

                Timber.d("VERSION " + Build.VERSION.SDK_INT + ", MIGRATING FILES TO INTERNAL PATH " + externalFilesDirs[0].getPath());

                return moveFromHereToThere(externalFilesDirs[1], externalFilesDirs[0]);

            } else {

                // no external directories available, so no point in migrating

                Timber.e("VERSION " + Build.VERSION.SDK_INT + ", NO EXTERNAL PATH FOR MIGRATION");

                return false;

            }
        }
    }

    private static boolean moveFromHereToThere(File source, File destination) {

        if (!source.exists()) {
            Timber.e("SOURCE DIRECTORY " + source.getPath() + " DOES NOT EXIST");
            return false;
        }

        try {
            FileUtils.copyDirectory(source, destination, true);
        } catch (IOException ioe) {
            Timber.e("FAILED TO COPY " + source.getPath() + " - " + ioe.getMessage());
            return false;
        }

        try {
            FileUtils.deleteDirectory(source);
        } catch (IOException ioe) {
            Timber.e("FAILED TO DELETE " + source.getPath() + " - " + ioe.getMessage());
            return false;
        }

        return true;
    }

    // new iocipher mount/unmount methods

    private final static String DEFAULT_PATH = "storymaker_vfs.db";

    public static boolean isStorageMounted() {
        return VirtualFileSystem.get().isMounted();
    }

    public static boolean mountStorage(Context context, String storagePath, byte[] passphrase) {
        File vfsFile = null;

        if (storagePath == null) {
            vfsFile = new File(context.getDir("vfs", Context.MODE_PRIVATE), DEFAULT_PATH);
            // PROBABLY SHOULD BE -> dbFile = new File(getActualStorageDirectory(context), DEFAULT_PATH);
        } else {
            vfsFile = new File(storagePath);
        }

        vfsFile.getParentFile().mkdirs();

        Timber.d("VFS FILE IS " + vfsFile.getAbsolutePath());

        if (!vfsFile.exists()) {
            VirtualFileSystem.get().createNewContainer(vfsFile.getAbsolutePath(), passphrase);
            Timber.d("CREATED NEW VFS FILE");
        } else {
            Timber.d("USING EXISTING VFS FILE");
        }

        if (!VirtualFileSystem.get().isMounted()) {
            VirtualFileSystem.get().mount(vfsFile.getAbsolutePath(), passphrase);
            Timber.d("MOUNTED VIRTUAL FILE SYSTEM");
        } else {
            Timber.d("VIRTUAL FILE SYSTEM ALREADY MOUNTED");
        }

        return true;
    }

    public static boolean unmountStorage() {
        try {
            VirtualFileSystem.get().unmount();
            Timber.d("UNMOUNTED VIRTUAL FILE SYSTEM");
            return true;
        } catch (IllegalStateException ise) {
            Timber.e("EXCEPTION WHILE UNMOUNTING VIRTUAL SYSTEM: " + ise.getMessage());
            return false;
        }
    }

    public static boolean saveVirtualFile() {
return false;
    }

    public static void migrateToIOCipher(Context context) {
        ArrayList<File> filesToMigrate = getActualFiles(context);

        for (File sourceFile : filesToMigrate) {
            try {
                info.guardianproject.iocipher.File targetFile = new info.guardianproject.iocipher.File(sourceFile.getPath());
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
                FileInputStream fis = new FileInputStream(sourceFile);
                info.guardianproject.iocipher.FileOutputStream fos = new info.guardianproject.iocipher.FileOutputStream(targetFile);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fis.close();
                fos.close();
                Timber.d("MIGRATED ACTUAL FILE " + sourceFile.getPath());
                sourceFile.delete();
                Timber.d("DELETED ACTUAL FILE " + sourceFile.getPath());
            } catch (FileNotFoundException fnfe) {
                Timber.e("EXCEPTION WHILE MIGRATING ACTUAL FILE " + sourceFile.getPath() + ": " + fnfe.getMessage());
            } catch (IOException ioe) {
                Timber.e("EXCEPTION WHILE MIGRATING ACTUAL FILE " + sourceFile.getPath() + ": " + ioe.getMessage());
            }
        }
    }

    public static void migrateFromIOCipher(Context context) {
        ArrayList<info.guardianproject.iocipher.File> filesToMigrate = getVirtualFiles(context);

        for (info.guardianproject.iocipher.File sourceFile : filesToMigrate) {
            try {
                File targetFile = new File(sourceFile.getPath());
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
                info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(sourceFile);
                FileOutputStream fos = new FileOutputStream(targetFile);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fis.close();
                fos.close();
                Timber.d("MIGRATED VIRTUAL FILE " + sourceFile.getPath());
                sourceFile.delete();
                Timber.d("DELETED VIRTUAL FILE " + sourceFile.getPath());
            } catch (FileNotFoundException fnfe) {
                Timber.e("EXCEPTION WHILE MIGRATING VIRTUAL FILE " + sourceFile.getPath() + ": " + fnfe.getMessage());
            } catch (IOException ioe) {
                Timber.e("EXCEPTION WHILE MIGRATING VIRTUAL FILE " + sourceFile.getPath() + ": " + ioe.getMessage());
            }
        }
    }

    public static ArrayList<File> getActualFiles(Context context) {
        ArrayList<File> results = new ArrayList<File>();

        File instanceFolder = StorageHelper.getActualStorageDirectory(context);
        if (instanceFolder == null) {
            Timber.d("getActualStorageDirectory() RETURNED NULL, CANNOT GATHER ACTUAL INSTANCE FILES");
            return results;
        } else if (instanceFolder.listFiles() == null) {
            Timber.d("listFiles() RETURNED NULL, CANNOT GATHER ACTUAL INSTANCE FILES");
            return results;
        } else {
            for (File instanceFile : instanceFolder.listFiles()) {
                if (instanceFile.getName().contains("-instance") &&
                        instanceFile.getName().endsWith(".json") &&
                        !instanceFile.isDirectory()) {
                    Timber.d("FOUND ACTUAL INSTANCE FILE: " + instanceFile.getName());
                    File foundFile = new File(instanceFile.getPath());
                    results.add(foundFile);
                }
            }
        }

        return results;
    }

    public static ArrayList<info.guardianproject.iocipher.File> getVirtualFiles(Context context) {
        ArrayList<info.guardianproject.iocipher.File> results = new ArrayList<info.guardianproject.iocipher.File>();

        File actualFolder = StorageHelper.getActualStorageDirectory(context);
        if (actualFolder == null) {
            Timber.d("getActualStorageDirectory() RETURNED NULL, CANNOT GATHER VIRTUAL INSTANCE FILES");
            return results;
        }

        info.guardianproject.iocipher.File instanceFolder = new info.guardianproject.iocipher.File(actualFolder.getPath());

        if (instanceFolder.listFiles() == null) {
            Timber.d("listFiles() RETURNED NULL, CANNOT GATHER VIRTUAL INSTANCE FILES");
            return results;
        } else {
            for (info.guardianproject.iocipher.File instanceFile : instanceFolder.listFiles()) {
                if (instanceFile.getName().contains("-instance") &&
                        instanceFile.getName().endsWith(".json") &&
                        !instanceFile.isDirectory()) {
                    Timber.d("FOUND VIRTUAL INSTANCE FILE: " + instanceFile.getName());
                    info.guardianproject.iocipher.File foundFile = new info.guardianproject.iocipher.File(instanceFile.getPath());
                    results.add(foundFile);
                }
            }
        }

        return results;
    }
}
