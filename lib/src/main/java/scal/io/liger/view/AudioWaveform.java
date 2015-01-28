package scal.io.liger.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import scal.io.liger.R;

/**
 * Allow easy creation of Audio waveform bitmaps given a source path.
 * Currently only tested against AAC audio.
 *
 * @see <a href="http://developer.android.com/guide/appendix/media-formats.html">Android's supported media formats</a>
 * Created by davidbrodsky on 1/27/15.
 */
public class AudioWaveform {
    private static final String TAG = "AudioWaveform";
    private static final boolean VERBOSE = false; // Enable logging

    /** Waveform drawing parameters */
    private static final int BITMAP_WIDTH = 720;
    private static final int BITMAP_HEIGHT = 480;
    private static final float MAX_AMPLITUDE_TO_DRAW = 32767f;
    private static final int MAX_SAMPLES_TO_DRAW = 100;

    /**
     * Generate a waveform image for an audio file at the given path. This should be called
     * on a background thread.
     *
     * @return a Bitmap waveform for the audio file at the given path or null if an error occurred.
     */
    public static @Nullable Bitmap createBitmap(Context context, String path) {
        try {
            long startTime = System.currentTimeMillis();
            short[] audioOutput = decodeToMemory(path, 0);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(context.getResources().getColor(R.color.storymaker_highlight));
            paint.setStrokeWidth(4);
            paint.setAntiAlias(true);

            Bitmap bitmap = Bitmap.createBitmap(BITMAP_WIDTH,
                                                BITMAP_HEIGHT,
                                                Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawWaveform(canvas, paint, audioOutput);
            Log.d(TAG, String.format("Decoded %d audio samples in %d ms", audioOutput.length, System.currentTimeMillis() - startTime));
            return bitmap;
        } catch (IOException | IllegalArgumentException e) {
            Log.w(TAG, String.format("Unable to generate waveform for %s. Is it an Android-friendly audio file with only 1 track?", path));
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decode an audio path into an array of samples with max length.
     * Modified from:
     * https://android.googlesource.com/platform/cts/+/jb-mr2-release/tests/tests/media/src/android/media/cts/DecoderTest.java
     *
     * @param path
     * @param maxLength the maximum number of samples to take. If this is less than the
     *                  total number of samples, the returned samples will be evenly distributed
     * @throws IOException
     */
    private static short[] decodeToMemory(String path, int maxLength) throws IOException, IllegalArgumentException {
        short [] decoded = new short[0];
        int decodedIdx = 0;
        MediaExtractor extractor;
        MediaCodec codec;
        ByteBuffer[] codecInputBuffers;
        ByteBuffer[] codecOutputBuffers;
        extractor = new MediaExtractor();
        extractor.setDataSource(path);
        if (extractor.getTrackCount() != 1) throw new IllegalArgumentException("File must have only one track");
        MediaFormat format = extractor.getTrackFormat(0);
        String mime = format.getString(MediaFormat.KEY_MIME);
        int sampleRate;                   // Samples / sec of target audio file
        long usDuration;                  // Encoded audio duration in us
        long fileSize;                    // Total size of target audio file in bytes
        int numSamplesToSeek;             // Num of samples to skip between reads. Used to calculate usSeekInterval
        long usSeekInterval = -1;         // microsecond Interval to seek between samples to decode only MAX_SAMPLES_TO_DRAW samples
        int samplesQueuedForDecoding = 0; // For help calculating us seek
        int samplesDecoded = 0;
        if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            if (VERBOSE) Log.d(TAG, "prelim output has sample rate " + sampleRate);
        } else {
            throw new IOException("MediaFormat has no sample rate parameter");
        }
        if (format.containsKey(MediaFormat.KEY_DURATION)) {
            usDuration = format.getLong(MediaFormat.KEY_DURATION);
            if (VERBOSE) Log.d(TAG, "prelim output has duration " + usDuration);
        } else {
            throw new IOException("MediaFormat has no duration parameter");
        }
        File f = new File(path);
        fileSize = f.length();
        if (VERBOSE) Log.d(TAG, "Got audio size " + fileSize);
        codec = MediaCodec.createDecoderByType(mime);
        codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);
        codec.start();
        codecInputBuffers = codec.getInputBuffers();
        codecOutputBuffers = codec.getOutputBuffers();
        extractor.selectTrack(0);
        // start decoding
        final long kTimeOutUs = 5000;
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean sawInputEOS = false;
        boolean sawOutputEOS = false;
        int noOutputCounter = 0;
        while (!sawOutputEOS && noOutputCounter < 50) {
            noOutputCounter++;
            if (!sawInputEOS) {
                int inputBufIndex = codec.dequeueInputBuffer(kTimeOutUs);
                if (inputBufIndex >= 0) {
                    ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                    int sampleSize =
                            extractor.readSampleData(dstBuf, 0 /* offset */);
                    long presentationTimeUs = 0;
                    if (sampleSize < 0) {
                        if (VERBOSE) Log.d(TAG, "saw input EOS.");
                        sawInputEOS = true;
                        sampleSize = 0;
                    } else {

                        presentationTimeUs = extractor.getSampleTime();
                        if (VERBOSE) Log.d(TAG, "Sample size is " + sampleSize);
                        if (usSeekInterval == -1) {
                            if (VERBOSE) Log.d(TAG, "Estimated bit rate : " + sampleSize * sampleRate);
                            int numExpectedSamples = (int) (((float) fileSize) / sampleSize);
                            numSamplesToSeek = (int) (((float) numExpectedSamples) / MAX_SAMPLES_TO_DRAW);
                            usSeekInterval = (long) (usDuration * (((float)numSamplesToSeek) / numExpectedSamples));
                            if (VERBOSE) Log.d(TAG, String.format("usSeekInterval %d us", usSeekInterval));
                        }
                        else if (usSeekInterval > 0) {
                            if (VERBOSE) Log.d(TAG, "Seeking to " + usSeekInterval * samplesQueuedForDecoding);
                            extractor.seekTo(usSeekInterval * samplesQueuedForDecoding, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                        }
                    }
                    codec.queueInputBuffer(
                            inputBufIndex,
                            0 /* offset */,
                            sampleSize,
                            presentationTimeUs,
                            sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                    samplesQueuedForDecoding ++;
                    if (!sawInputEOS) {
                        extractor.advance();
                    }
                }
            }
            int res = codec.dequeueOutputBuffer(info, kTimeOutUs);
            if (res >= 0) {
                //Log.d(TAG, "got frame, size " + info.size + "/" + info.presentationTimeUs);
                if (info.size > 0) {
                    noOutputCounter = 0;
                }
                int outputBufIndex = res;
                ByteBuffer buf = codecOutputBuffers[outputBufIndex];
                samplesDecoded++;
                if (decodedIdx + (info.size / 2) >= decoded.length) {
                    decoded = Arrays.copyOf(decoded, decodedIdx + (info.size / 2));
                }
                for (int i = 0; i < info.size; i += 2) {
                    decoded[decodedIdx++] = buf.getShort(i);
                }
                codec.releaseOutputBuffer(outputBufIndex, false /* render */);
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (VERBOSE) Log.d(TAG, "saw output EOS.");
                    sawOutputEOS = true;
                }
            } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                codecOutputBuffers = codec.getOutputBuffers();
                if (VERBOSE) Log.d(TAG, "output buffers have changed.");
            } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat oformat = codec.getOutputFormat();
                if (VERBOSE) Log.d(TAG, "output format has changed to " + oformat);
            } else {
                if (VERBOSE) Log.d(TAG, "dequeueOutputBuffer returned " + res);
            }
        }
        codec.stop();
        codec.release();
        if (VERBOSE) Log.d(TAG, "Decoded " + samplesDecoded + " samples");
        return decoded;
    }

    private static void drawWaveform(Canvas canvas, Paint paint, short[] audioData) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        int numSamplesToDraw = Math.min(MAX_SAMPLES_TO_DRAW, audioData.length);

        float width = canvas.getWidth();
        float height = canvas.getHeight();
        int samplesPerPixel = (int) (audioData.length / (float) numSamplesToDraw);
        int pixelsPerSample = (int) (width / numSamplesToDraw);
        float centerY = height / 2;
        float lastX = -1;
        float lastY = -1;

        for (int idx = 0; idx < numSamplesToDraw; idx++) {
            short sample = audioData[idx * samplesPerPixel];
            float y = (sample / MAX_AMPLITUDE_TO_DRAW) * centerY + centerY;
            float x = lastX + pixelsPerSample;

            if (lastX != -1) {
                canvas.drawLine(lastX, lastY, x, y, paint);
            }

            lastX = x;
            lastY = y;
        }
    }
}
