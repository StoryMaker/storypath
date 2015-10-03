package scal.io.liger.av;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import scal.io.liger.MediaHelper;
import scal.io.liger.R;
import scal.io.liger.model.MediaFile;
import scal.io.liger.view.AudioLevelView;
import scal.io.liger.view.Util;

/**
 * An enclosed audio recorder that provides feedback via a {@link scal.io.liger.view.AudioLevelView}
 * Created by davidbrodsky on 1/26/15.
 */
public class AudioRecorder {

    private final int TIMER_INTERVAL_MS = 50;

    private Context mContext;
    private Timer mTimer;
    private MediaRecorderWrapper mRecorder;
    private FrameLayout mContainer;
    private AudioLevelView mWaveformView;
    private TextView mTimeCode;

    private long mStartTimeMs;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            onTimerTick();
        }
    };

    public AudioRecorder(@NonNull FrameLayout container) throws IOException {
        mContainer = container;
        mContext = container.getContext();
        init();
    }

    public boolean startRecording() {
        boolean result = mRecorder.startRecording();
        if (result) {
            setupTimer();
            mStartTimeMs = System.currentTimeMillis();
        }

        return result;
    }

    public MediaFile stopRecording() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        return mRecorder.stopRecording();
    }

    public boolean isRecording() {
        return mRecorder.isRecording();
    }

    public void release() {
        mRecorder.release();
    }

    private void init() throws IOException {
        mRecorder = new MediaRecorderWrapper(mContext, MediaHelper.getAudioDirectory(mContext));
        inflateViews(mContainer);
    }

    private void inflateViews(@NonNull FrameLayout root) {
        Context context = root.getContext();

        /* Waveform view */
        Resources r = context.getResources();
        int bottomPadPx = r.getDimensionPixelSize(R.dimen.padding_small);
        int mediaHeight = r.getDimensionPixelSize(R.dimen.clip_thumb_height);
        FrameLayout.LayoutParams mediaViewParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                             mediaHeight);
        mediaViewParams.setMargins(0, 0, 0, bottomPadPx);

        mWaveformView = new AudioLevelView(context);
        mWaveformView.setLayoutParams(mediaViewParams);

        /* Timecode label */
        FrameLayout.LayoutParams alignTopParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        alignTopParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        mTimeCode = new TextView(context);
        mTimeCode.setLayoutParams(alignTopParams);
        mTimeCode.setVisibility(View.VISIBLE);
        mTimeCode.setShadowLayer(2, 2, 2, Color.WHITE);

        // TODO Stop button?

        root.addView(mWaveformView);
        root.addView(mTimeCode);
    }

    private void setupTimer() {
        // Poll MediaPlayer for position, ensuring it never exceeds stop point indicated by ClipMetadata
        if (mTimer != null) mTimer.cancel(); // should never happen but JIC
        mTimer = new Timer("audioRecorder");
        mTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.sendMessage(mHandler.obtainMessage());
                    }
                },
                50,                   // Initial delay ms
                TIMER_INTERVAL_MS);   // Repeat interval ms
    }

    private void onTimerTick() {
        mWaveformView.notifyNewAmplitude(mRecorder.getMaxAmplitude());
        mTimeCode.setText(Util.makeTimeString(System.currentTimeMillis() - mStartTimeMs));
    }
}
