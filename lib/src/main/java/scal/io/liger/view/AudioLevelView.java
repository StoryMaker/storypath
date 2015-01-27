/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package scal.io.liger.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

import java.util.LinkedList;

import scal.io.liger.R;

/**
 * A view that displays audio data on the screen as a waveform.
 * Modified from SurfaceView / short[] based WaveformView from Google:
 * https://github.com/googleglass/gdk-waveform-sample/blob/master/src/com/google/android/glass/sample/waveform/WaveformView.java
 */
public class AudioLevelView extends TextureView implements TextureView.SurfaceTextureListener{
    private final String TAG = getClass().getSimpleName();

    // The number of buffer frames to keep around.
    private static final int HISTORY_SIZE = 30;
    // Oldest samples to fade out. Must be less than history size
    private static final int FADE_SIZE = 5;

    // Audio amplitudes over this value will be clipped
    private static final float MAX_AMPLITUDE_TO_DRAW = 35000;
    // Percentage of MAX_AMPLITUDE_TO_DRAW over which to color samples as too loud. e.g red
    private static final float DANGER_AMPLITUDE_FACTOR = .85f;

    // The queue that will hold historical audio data.
    private final LinkedList<Integer> mAudioData;

    private final Paint mPaint;
    private int mRegularColor;
    private int mDangerColor;

    public AudioLevelView(Context context) {
        this(context, null, 0);
    }

    public AudioLevelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioLevelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mRegularColor = context.getResources().getColor(R.color.storymaker_highlight);
        mDangerColor = context.getResources().getColor(R.color.stormaker_danger);

        mAudioData = new LinkedList<>();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0);
        mPaint.setAntiAlias(true);

        setSurfaceTextureListener(this);
    }

    /**
     * Updates the waveform view with a new "frame" of samples and renders it. The new frame gets
     * added to the front of the rendering queue, pushing the previous frames back, causing them to
     * be faded out visually.
     *
     * @param maxAmplitude the most recent max amplitude of recent audio samples
     */
    public synchronized void updateAudioData(int maxAmplitude) {
        //Log.d(TAG, "updateAudioData with amplitude " + (maxAmplitude / MAX_AMPLITUDE_TO_DRAW));

        // We want to keep a small amount of history in the view to provide a nice fading effect.
        // We use a linked list that we treat as a queue for this.
        if (mAudioData.size() == HISTORY_SIZE) {
            mAudioData.removeFirst();
        }

        mAudioData.addLast(maxAmplitude);

        // Update the display.

        Canvas canvas = lockCanvas();
        if (canvas != null) {
            drawWaveform(canvas);
            unlockCanvasAndPost(canvas);
        }
    }

    /**
     * Repaints the view's surface.
     *
     * @param canvas the {@link Canvas} object on which to draw
     */
    private void drawWaveform(Canvas canvas) {
        // Clear the screen each time because SurfaceView won't do this for us.
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2;
        float pixelsPerSample = width / HISTORY_SIZE;
        float lastX = 0;
        // We draw the history from oldest to newest so that the older audio data is further back
        // and darker than the most recent data.
        int brightDelta = 255 / (FADE_SIZE);
        int brightness = brightDelta;

        for (int x = 0; x < mAudioData.size(); x++) {
            float normalizedAmplitude = Math.min((mAudioData.get(x) / MAX_AMPLITUDE_TO_DRAW), 1f);

            if (normalizedAmplitude > DANGER_AMPLITUDE_FACTOR) {
                mPaint.setColor(mDangerColor);
            } else {
                mPaint.setColor(mRegularColor);
            }

            if (x < FADE_SIZE)
                mPaint.setAlpha(brightness);
            else
                mPaint.setAlpha(255);

            canvas.drawRect(lastX,
                           height - (Math.min((mAudioData.get(x) / MAX_AMPLITUDE_TO_DRAW), 1f) * height),
                           lastX + pixelsPerSample,
                           height,
                           mPaint);

            brightness += brightDelta;
            lastX += pixelsPerSample;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // TODO Destroy audioRecord resources here
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}