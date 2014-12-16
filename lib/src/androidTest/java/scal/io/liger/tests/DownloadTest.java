package scal.io.liger.tests;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.ZipHelper;

import static android.test.ViewAsserts.assertOnScreen;

/**
 * Created by mnbogner on 12/12/14.
 */
public class DownloadTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;

    public DownloadTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        mMainActivity = getActivity();
        mRecyclerView = (RecyclerView) mMainActivity.findViewById(R.id.recyclerView);

        // copy installation index from assets for testing
        copyIndex(mMainActivity);
    }

    // including only this single test case
    // specified files will be downloaded whenever app first starts
    public void testDownloads() {
        // precondition test
        assertTrue(mMainActivity != null);
        assertTrue(mRecyclerView != null);
        Log.d("AUTOMATION", "ACTIVITY & VIEW EXIST");

        // view test
        assertOnScreen(mMainActivity.getWindow().getDecorView(), mRecyclerView);
        Log.d("AUTOMATION", "VIEW ON SCREEN");

        // setup
        String testFilePath = ZipHelper.getFileFolderName(mMainActivity);
        File indexFile = new File(testFilePath + "installed_index.json");
        File testFile1 = new File(testFilePath + "test" + File.separator + "expansion_test_1.ZIP");
        File testFile2a = new File(testFilePath + "test" + File.separator + "expansion_test_2a.ZIP");
        File testFile2b = new File(testFilePath + "test" + File.separator + "expansion_test_2b.ZIP");
        File tempFile1 = new File(testFilePath + "test" + File.separator + "expansion_test_1.ZIP.tmp");
        File tempFile2a = new File(testFilePath + "test" + File.separator + "expansion_test_2a.ZIP.tmp");
        File tempFile2b = new File(testFilePath + "test" + File.separator + "expansion_test_2b.ZIP.tmp");

        // delay to allow time for downloads
        stall(30000, "WAITING FOR DOWNLOADS");

        // verify index file existence
        assertTrue(indexFile.exists());

        // verify test file existence
        assertTrue(testFile1.exists());
        assertTrue(testFile2a.exists());
        assertTrue(testFile2b.exists());

        // verify test file size
        assertTrue(testFile1.length() > 0);
        assertTrue(testFile2a.length() > 0);
        assertTrue(testFile2b.length() > 0);

        // verify test file cleanup
        assertTrue(!tempFile1.exists());
        assertTrue(!tempFile2a.exists());
        assertTrue(!tempFile2b.exists());

        // verify test file contents
        String testString1 = "";
        try {
            InputStream testStream = ZipHelper.getFileInputStream("test_file_1.txt", mMainActivity);

            if (testStream != null) {
                int size = testStream.available();
                byte[] buffer = new byte[size];
                testStream.read(buffer);
                testStream.close();
                testString1 = new String(buffer);
            }
        } catch (IOException ioe) {
            Log.e("AUTOMATION", "READING JSON FILE " + "test_file_1.txt" + " FROM ZIP FILE FAILED");
        }
        assertTrue(testString1.contains("THIS IS TEST FILE 1"));

        // contents of file 2b should override contents of file 2a
        String testString2 = "";
        try {
            InputStream testStream = ZipHelper.getFileInputStream("test_file_2.txt", mMainActivity);

            if (testStream != null) {
                int size = testStream.available();
                byte[] buffer = new byte[size];
                testStream.read(buffer);
                testStream.close();
                testString2 = new String(buffer);
            }
        } catch (IOException ioe) {
            Log.e("AUTOMATION", "READING JSON FILE " + "test_file_2.txt" + " FROM ZIP FILE FAILED");
        }
        assertTrue(testString2.contains("THIS IS TEST FILE 2B"));

        // delete test files so test can be re-run
        // index file deletion handled by copy method
        if (testFile1.exists()) {
            testFile1.delete();
        }
        if (testFile2a.exists()) {
            testFile2a.delete();
        }
        if (testFile2b.exists()) {
            testFile2b.delete();
        }
        if (tempFile1.exists()) {
            tempFile1.delete();
        }
        if (tempFile2a.exists()) {
            tempFile2a.delete();
        }
        if (tempFile2b.exists()) {
            tempFile2b.delete();
        }
    }

    private void stall(long milliseconds, String message) {
        try {
            Log.d("AUTOMATION", "SLEEP " + (milliseconds / 1000) + " (" + message + ")");
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void copyIndex(Context context) {

        AssetManager assetManager = context.getAssets();

        String jsonFilePath = ZipHelper.getFileFolderName(context);

        Log.d("AUTOMATION", "COPYING JSON FILE " + "installed_index_test.json" + " FROM ASSETS TO " + jsonFilePath);

        File jsonFile = new File(jsonFilePath + "installed_index_test.json");
        if (jsonFile.exists()) {
            jsonFile.delete();
        }

        InputStream assetIn = null;
        OutputStream assetOut = null;

        try {
            assetIn = assetManager.open("installed_index_test.json");

            assetOut = new FileOutputStream(jsonFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = assetIn.read(buffer)) != -1) {
                assetOut.write(buffer, 0, read);
            }
            assetIn.close();
            assetIn = null;
            assetOut.flush();
            assetOut.close();
            assetOut = null;

            // rename file to replace actual index file
            File actualFile = new File(jsonFilePath + "installed_index.json");
            if (actualFile.exists()) {
                actualFile.delete();
            }

            Process p = Runtime.getRuntime().exec("mv " + jsonFile.getPath() + " " + actualFile.getPath());

        } catch (IOException ioe) {
            Log.e("AUTOMATION", "COPYING JSON FILE " + "installed_index_test.json" + " FROM ASSETS TO " + jsonFilePath + " FAILED");
            return;
        }

        return;
    }
}
