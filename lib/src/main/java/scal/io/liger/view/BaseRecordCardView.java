package scal.io.liger.view;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.model.MediaFile;

/**
 * Created by josh on 12/4/14.
 */
public class BaseRecordCardView extends ExampleCardView {
    public static final String TAG = "BaseRecordCardView";

    /** Records audio */
    MediaRecorder mMediaRecorder;
    File mNarrationOutput;

    LinearLayout mVUMeterLayout;
    private final Handler mHandler = new Handler();

    private int mPreviousVUMax;

    /** Current Narration State. To change use
     * {@link #changeRecordNarrationStateChanged(scal.io.liger.view.ReviewCardView.RecordNarrationState)}
     */
    RecordNarrationState mRecordNarrationState = RecordNarrationState.READY;

    /** The ReviewCard Narration Dialog State */
    static enum RecordNarrationState {

        /** Done / Record Buttons present */
        READY,

        /** Recording countdown then Pause / Stop Buttons present */
        RECORDING,

        /** Recording paused. Resume / Stop Buttons present */
        PAUSED,

        /** Recording stopped. Done / Redo Buttons present */
        STOPPED

    }

    /**
     * Start recording an audio narration track synced and instruct player to
     * begin playback simultaneously.
     */
    void startRecordingNarration() {
        DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String now = df.format(new Date());
        mNarrationOutput = new File(Environment.getExternalStorageDirectory(), now + /*".aac"*/ ".mp4");

        if (mMediaRecorder == null) mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioEncodingBitRate(96 * 1000); // FIXME we need to probe the hardware for these values
        mMediaRecorder.setAudioSamplingRate(44100);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // raw AAC ADTS container records properly but results in Unknown MediaPlayer errors when playback attempted. :/
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(mNarrationOutput.getAbsolutePath());

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            Toast.makeText(mContext, mContext.getString(R.string.recording_narration), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
            Toast.makeText(mContext, mContext.getString(R.string.could_not_start_narration), Toast.LENGTH_SHORT).show();
        }
//        mClipCollectionPlayer.setVolume(0, 0); // Mute track volume while recording narration
//        mClipCollectionPlayer.start();
        updateVUMeterView();
    }

    Runnable mUpdateVUMeter = new Runnable() {
        @Override
        public void run() {
            if (mRecordNarrationState != RecordNarrationState.STOPPED) {
                updateVUMeterView();
            }
        }
    };

    void updateVUMeterView() {
        final int MAX_VU_SIZE = 11;
        boolean showVUArray[] = new boolean[MAX_VU_SIZE];

        if (mVUMeterLayout.getVisibility() == View.VISIBLE
                &&  mRecordNarrationState != RecordNarrationState.STOPPED) {
            int amp = mMediaRecorder.getMaxAmplitude();
            int vuSize = MAX_VU_SIZE * amp / 32768;
            if (vuSize >= MAX_VU_SIZE) {
                vuSize = MAX_VU_SIZE - 1;
            }

            if (vuSize >= mPreviousVUMax) {
                mPreviousVUMax = vuSize;
            } else if (mPreviousVUMax > 0) {
                mPreviousVUMax--;
            }

            for (int i = 0; i < MAX_VU_SIZE; i++) {
                if (i <= vuSize) {
                    showVUArray[i] = true;
                } else if (i == mPreviousVUMax) {
                    showVUArray[i] = true;
                } else {
                    showVUArray[i] = false;
                }
            }

            mHandler.postDelayed(mUpdateVUMeter, 100);
        } else if (mVUMeterLayout.getVisibility() == View.VISIBLE) {
            mPreviousVUMax = 0;
            for (int i = 0; i < MAX_VU_SIZE; i++) {
                showVUArray[i] = false;
            }
        }

        if (mVUMeterLayout.getVisibility() == View.VISIBLE) {
            mVUMeterLayout.removeAllViews();
            for (boolean show : showVUArray) {
                ImageView imageView = new ImageView(mContext);
                imageView.setBackgroundResource(R.drawable.background_vumeter);
                if (show) {
                    imageView.setImageResource(R.drawable.icon_vumeter);
                }
                imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                mVUMeterLayout.addView(imageView);
            }
        }
    }

    /**
     * Stop and reset {@link #mMediaRecorder} so it may be used again
     */
    MediaFile stopRecordingNarration() {
//        if (mClipCollectionPlayer.isPlaying()) mClipCollectionPlayer.pause();
//        mClipCollectionPlayer.setVolume(1, 1); // Restore track volume when finished recording narration

        mMediaRecorder.stop();
        mMediaRecorder.reset();

        // Attach the just-recorded narration to ReviewCard
        MediaFile narrationMediaFile = new MediaFile(mNarrationOutput.getAbsolutePath(), Constants.AUDIO);
//        mCardModel.setNarration(narrationMediaFile);
        Log.i(TAG, "Saving narration file " + mNarrationOutput.getAbsolutePath());
        return narrationMediaFile;
    }

    void pauseRecordingNarration(MediaPlayer player) {
        throw new UnsupportedOperationException("Pausing and resuming a recording is not yet supported!");
        // TODO This is going to require Android 4.3's MediaCodec APIs or
        // TODO file concatenation of multiple recordings.
    }

    /**
     * Release {@link #mMediaRecorder} when no more recordings will be made
     */
    void releaseMediaRecorder() {
        if (mMediaRecorder != null) mMediaRecorder.release();
    }

    /**
     * Update the UI in response to a new value assignment to {@link #mRecordNarrationState}
     */
    void changeRecordNarrationStateChanged(RecordNarrationState newState) {
        mRecordNarrationState = newState;
    }
}
