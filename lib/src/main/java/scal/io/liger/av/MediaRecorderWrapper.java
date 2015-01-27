package scal.io.liger.av;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import scal.io.liger.Constants;
import scal.io.liger.model.MediaFile;


/**
 * Wrapper around {@link android.media.MediaRecorder} with sensible
 * defaults for recording high quality audio.
 * <p>
 * Usage:
 *
 * <pre>
 * {@code
 * MediaRecorderWrapper recorder = new MediaRecorderWrapper(context, "/path/to/output/directory");
 * recorder.startRecording();
 * recorder.stopRecording();
 * recorder.reset("path/to/output/directory"); // optional
 * // may repeat #startRecording(), #stopRecording() after #reset()
 * recorder.release();
 * }
 * </pre>
 *
 * Created by davidbrodsky on 1/26/15.
 */
public class MediaRecorderWrapper {
    private final String TAG = getClass().getSimpleName();

    private static SimpleDateFormat sdf = new SimpleDateFormat("yy.mm.dd-HH.mm.ss");

    private final Context mContext;
    private File mOutDirectory;
    private File mOutFile;
    private MediaRecorder mRecorder;

    private boolean mRecording;

    public MediaRecorderWrapper(@NonNull Context context,
                                @NonNull File outputDirectory) {
        mOutDirectory = outputDirectory;
        mContext = context;
        init();
    }

    public boolean startRecording() {
        if (mRecorder == null || mRecording) {
            Log.w(TAG, "startRecording called in invalid state");
            return false;
        }
        try {
            mOutFile = new File(mOutDirectory, sdf.format(new Date()) + ".mp4");
            if (!mOutFile.createNewFile()) throw new IOException("Failed to create " + mOutFile.getAbsolutePath());
            mRecorder.setOutputFile(mOutFile.getAbsolutePath());
            mRecorder.prepare();
            mRecorder.start();
            mRecording = true;
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public @Nullable MediaFile stopRecording() {
        if (mRecorder == null || !mRecording) {
            Log.w(TAG, "stopRecording called in invalid state");
            return null;
        }
        mRecorder.stop();
        mRecording = false;
        mRecorder.reset();
        return new MediaFile(mOutFile.getAbsolutePath(), Constants.AUDIO);
    }

    public boolean isRecording() {
        return mRecording;
    }

    public void reset() {
        init();
    }

    public void reset(@NonNull File newOutputPath) {
        mOutDirectory = newOutputPath;

        init();
    }

    public void release() {
        if (mRecorder == null) {
            Log.w(TAG, "release called in invalid state");
            return;
        }
        mRecorder.release();
        mRecorder = null;
    }

    public int getMaxAmplitude() {
        if (mRecorder == null || !mRecording) {
            Log.w(TAG, "getMaxAmplitude called in invalid state");
            return 0;
        }
        return mRecorder.getMaxAmplitude();
    }

    private void init() {
        if (mRecorder == null) mRecorder = new MediaRecorder();
        else mRecorder.reset();

        // TODO : Verify parameters. Apparently no great solutions here
        // see http://stackoverflow.com/questions/8043387/android-audiorecord-supported-sampling-rates
        mRecorder.setAudioEncodingBitRate(96 * 1000);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // raw AAC ADTS container records properly but results in Unknown MediaPlayer errors when playback attempted. :/
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        // setOutputDirectory and prepare called on {@link #startRecording}
    }

}
