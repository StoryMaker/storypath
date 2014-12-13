package scal.io.liger.av;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.R;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.MediaFile;
import scal.io.liger.view.BaseRecordCardView;

/**
 * Plays a collection of ClipCards, as well as a secondary audio track.
 *
 * Created by davidbrodsky on 12/12/14.
 */
public class ClipCardsNarrator extends ClipCardsPlayer {
    public final String TAG = getClass().getSimpleName();

    private MediaRecorder mMediaRecorder;
    private File mNarrationOutput;
    private RecordNarrationState mRecordNarrationState;
    private NarrationListener mListener;

    public static enum RecordNarrationState {

        /** Done / Record Buttons present */
        READY,

        /** Recording countdown then Pause / Stop Buttons present */
        RECORDING,

        /** Recording paused. Resume / Stop Buttons present */
        PAUSED,

        /** Recording stopped. Done / Redo Buttons present */
        STOPPED

    }

    public interface NarrationListener {
        public void onNarrationFinished(MediaFile narration);
    }

    // <editor-fold desc="Public API">

    public ClipCardsNarrator(@NonNull FrameLayout container, @NonNull List<ClipCard> clipCards) {
        super(container, clipCards);
        changeRecordNarrationState(RecordNarrationState.READY);
    }

    public void changeRecordNarrationState(RecordNarrationState newState) {
        Log.i(TAG, "Changing state to " + newState.toString());
        mRecordNarrationState = newState;
    }

    public void setNarrationListener(NarrationListener listener) {
        mListener = listener;
    }

    public void startRecordingNarration() {
        changeRecordNarrationState(RecordNarrationState.RECORDING);
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
        startPlayback();
    }

    public void stopRecordingNarration() {
        changeRecordNarrationState(RecordNarrationState.STOPPED);
        MediaFile mf = finishRecordingNarration();
        if (mListener != null) mListener.onNarrationFinished(mf);
        stopPlayback();
    }

    public int getMaxRecordingAmplitude() {
        return mMediaRecorder.getMaxAmplitude();
    }

    public RecordNarrationState getState() {
        return mRecordNarrationState;
    }

    // </editor-fold desc="Public API">

    @Override
    protected void advanceToNextClip(MediaPlayer player) {
        super.advanceToNextClip(player);

        if (!mIsPlaying && mRecordNarrationState == RecordNarrationState.RECORDING) {
            stopRecordingNarration();
//            mContainerLayout.post(new Runnable() {
//                @Override
//                public void run() {
//                    changeRecordNarrationState(RecordNarrationState.STOPPED);
//                }
//            });
        }
    }

    @Override
    protected void startPlayback() {
        if (mRecordNarrationState.equals(RecordNarrationState.RECORDING)) {
            mIsPlaying = true;

            mThumbnailView.setVisibility(View.VISIBLE);

            switch(mCurrentlyPlayingCard.getMedium()) {
                case Constants.VIDEO:
                    mThumbnailView.setVisibility(View.GONE);
                case Constants.AUDIO:
                    // Mute the main media volume if we're recording narration
                    mMainPlayer.setVolume(0, 0);
                    mMainPlayer.start();
                    break;
                case Constants.PHOTO:
                    setThumbnailForClip(mThumbnailView, mCurrentlyPlayingCard);
                    break;
            }
        } else {
            mMainPlayer.setVolume(1, 1);
            super.startPlayback();
        }
    }

    /**
     * Stop recording narration and return the resulting {@link scal.io.liger.model.MediaFile}
     */
    private MediaFile finishRecordingNarration() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();

        return new MediaFile(mNarrationOutput.getAbsolutePath(), Constants.AUDIO);
    }

}
