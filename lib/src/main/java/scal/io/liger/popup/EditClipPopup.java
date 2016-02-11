package scal.io.liger.popup;

import timber.log.Timber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edmodo.rangebar.RangeBar;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import scal.io.liger.R;
import scal.io.liger.av.ClipCardsPlayer;
import scal.io.liger.model.ClipMetadata;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.StoryPath;
import scal.io.liger.view.Util;

/**
 * Created by josh@vitirolix on 6/7/15.
 */
public class EditClipPopup {
    Context mContext;
    ClipMetadata mSelectedClip;
    MediaFile mMediaFile;
    StoryPath mStoryPath;

    private static final String TAG = "EditClipPopup";

    public EditClipPopup(Context context, StoryPath storyPath, ClipMetadata selectedClip, MediaFile mediaFile) {
        mContext = context;
        mSelectedClip = selectedClip;
        mStoryPath = storyPath;
        mMediaFile = mediaFile;
    }

    public void show() {
        View v = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_clip_playback_trim, null);

        /** Trim dialog views */
        final TextureView videoView = (TextureView) v.findViewById(R.id.textureView);
        final ImageView thumbnailView = (ImageView) v.findViewById(R.id.thumbnail);
        final TextView clipLength = (TextView) v.findViewById(R.id.clipLength);
        final TextView clipStart = (TextView) v.findViewById(R.id.clipStart);
        final TextView clipEnd = (TextView) v.findViewById(R.id.clipEnd);
        final RangeBar rangeBar = (RangeBar) v.findViewById(R.id.rangeSeekbar);
        final SeekBar playbackBar = (SeekBar) v.findViewById(R.id.playbackProgress);
        final SeekBar volumeSeek = (SeekBar) v.findViewById(R.id.volumeSeekbar);
        final TextView volumeDisplay = (TextView) v.findViewById(R.id.volumeDisplay);

        final int tickCount = mContext.getResources().getInteger(R.integer.trim_bar_tick_count);

        /** initialize Right-to-left states **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
                            mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            volumeSeek.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        /** Media player and media */
        final MediaPlayer player = new MediaPlayer();
        //final ClipMetadata selectedClip = mCardModel.getSelectedClip();
        final AtomicInteger clipDurationMs = new AtomicInteger();
        final AtomicInteger clipMediaDurationMs = new AtomicInteger();
        final AtomicInteger lastPlaybackPosition = new AtomicInteger(); // Set when trim set begins. This let's us reset the player to the playback position it was at before trimming started

        /** Values modified by RangeBar listener. Used by Dialog trim listener to
         *  set final trim selections on ClipMetadata */
        final AtomicInteger clipStartMs = new AtomicInteger(mSelectedClip.getStartTime());
        final AtomicInteger clipStopMs = new AtomicInteger(mSelectedClip.getStopTime());

        /** Setup initial values that don't require media loaded */
        clipStart.setText(Util.makeTimeString(mSelectedClip.getStartTime()));
        clipEnd.setText(Util.makeTimeString(mSelectedClip.getStopTime()));
        volumeSeek.setProgress((int) (mSelectedClip.getVolume() * 100f));
        volumeDisplay.setText(volumeSeek.getProgress() + "%");

        Log.i(TAG, String.format("Showing clip trim dialog with intial start: %d stop: %d", mSelectedClip.getStartTime(), mSelectedClip.getStopTime()));

        volumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeDisplay.setText(progress + "%");
                float newVolume = ((float)progress) / 100f;
                mSelectedClip.setVolume(newVolume);
                float playerVolume = Math.max(1f,newVolume);
                player.setVolume(playerVolume,playerVolume);
                Timber.d("SAVING UPDATED VOLUME");
                mStoryPath.getStoryPathLibrary().save(true);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { /* ignored */}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { /* ignored */}
        });

        // Seek MediaPlayer when playbackBar dragged
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            playbackBar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        playbackBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    boolean rightToLeft = false;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
//                            mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
//                        rightToLeft = true;
//                    }

                    int seekPointMs = getMsFromRangeBarIndex(progress,
                            tickCount,
                            clipMediaDurationMs.get(),
                            rightToLeft);

                    Timber.d("Seeking to " + seekPointMs);
                    player.seekTo(getMsFromRangeBarIndex(progress,
                            tickCount,
                            clipMediaDurationMs.get(),
                            rightToLeft));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // unused
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // unused
            }
        });

        // Save / Restore MediaPlayer position before / after trim adjustment
        // This is necessary because we must seek the MediaPlayer position during trim adjustment
        // in order to preview the seek position.
        rangeBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        lastPlaybackPosition.set(player.getCurrentPosition());
                        break;

                    case MotionEvent.ACTION_UP:

                        player.seekTo(lastPlaybackPosition.get());
                        break;
                }
                return false;
            }
        });

        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            int lastStartIdx;
            int lastEndIdx;

            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftIdx, int rightIdx) {
//                Log.i(TAG, String.format("Seek to leftIdx %d rightIdx %d. left: %d. right: %d",
//                                         leftIdx, rightIdx, rangeBar.getLeft(), rangeBar.getRight()));

                // NOTE : RangeBar indices go from 0 (left) to 100 (right)
                // regardless of layout direction

                int startIdx, endIdx;
                boolean rightToLeft = false;
                // Adjust for RTL layouts if necessary
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
//                        mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
//
//                    rightToLeft = true;
//                    startIdx = rightIdx;
//                    endIdx = leftIdx;
//
//                } else {
                    startIdx = leftIdx;
                    endIdx = rightIdx;
//                }

                if (lastStartIdx != startIdx) {
                    // Start seek was adjusted, seek to it
                    clipStartMs.set(getMsFromRangeBarIndex(startIdx,
                            tickCount,
                            clipMediaDurationMs.get(),
                            rightToLeft));
                    //Timber.d(String.format("Seeking start to %d / %d ms from %d / %d", clipStartMs.get(), clipMediaDurationMs.get(), startIdx, tickCount));
                    player.seekTo(clipStartMs.get());
                    clipStart.setText(Util.makeTimeString(clipStartMs.get()));
                    //Log.i(TAG, String.format("Start seek to %d ms", clipStartMs.get()));
                    if (playbackBar.getProgress() < startIdx) playbackBar.setProgress(startIdx);


                } else if (lastEndIdx != endIdx) {
                    // End seek was adjusted, seek to it
                    clipStopMs.set(getMsFromRangeBarIndex(endIdx,
                            tickCount,
                            clipMediaDurationMs.get(),
                            rightToLeft));

                    //Timber.d(String.format("Seeking end to %d / %d ms from %d / %d", clipStopMs.get(), clipMediaDurationMs.get(), endIdx, tickCount));
                    player.seekTo(clipStopMs.get());
                    clipEnd.setText(Util.makeTimeString(clipStopMs.get()));

                    if (playbackBar.getProgress() > endIdx) playbackBar.setProgress(endIdx);
                    //Log.i(TAG, String.format("Stop seek to %d ms", clipStopMs.get()));
                }
                lastStartIdx = startIdx;
                lastEndIdx = endIdx;
                clipDurationMs.set(clipStopMs.get() - clipStartMs.get());
                clipLength.setText(mContext.getString(R.string.total) + " : " + Util.makeTimeString(clipDurationMs.get()));
            }
        });

        View.OnClickListener playbackToggleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thumbnailView.setVisibility(View.GONE);

                if (player.isPlaying()) {
                    player.pause();
                } else {
                    // Begin playback from beginning of clip only if the player
                    // is very near the end of the current clip
                    if (clipStopMs.get() - player.getCurrentPosition() < 50)
                        player.seekTo(clipStartMs.get());
                    player.start();
                }
            }
        };

        videoView.setOnClickListener(playbackToggleClickListener);
        thumbnailView.setOnClickListener(playbackToggleClickListener);

        // DEBUGGING : Remove after resolve clipping issue on small screens
        thumbnailView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();

                float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
                float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

                Toast.makeText(mContext,
                        String.format("Display metrics: %.0f x %.0f dp", dpWidth, dpHeight),
                        Toast.LENGTH_LONG)
                        .show();


                return false;
            }
        });

        videoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mMediaFile.loadThumbnail(thumbnailView, null);
                thumbnailView.setVisibility(View.VISIBLE);

                Uri video = Uri.parse(mMediaFile.getPath());
                Surface s = new Surface(surface);
                try {
                    player.setDataSource(mContext, video);
                    player.setSurface(s);
                    player.prepare();
                    player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            player.seekTo(clipStartMs.get());
                        }
                    });

                    ClipCardsPlayer.adjustAspectRatio(videoView, player.getVideoWidth(), player.getVideoHeight());

                    clipMediaDurationMs.set(player.getDuration());
                    if (clipStopMs.get() == 0) clipStopMs.set(clipMediaDurationMs.get()); // If no stop point set, play whole clip
                    clipDurationMs.set(clipStopMs.get() - clipStartMs.get());

                    // Setup initial views requiring knowledge of clip media

//                    boolean rightToLeft = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 &&
//                            mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                    boolean rightToLeft = false;
                    if (mSelectedClip.getStopTime() == 0) mSelectedClip.setStopTime(clipDurationMs.get());
                    player.seekTo(mSelectedClip.getStartTime());
                    rangeBar.setThumbIndices(getRangeBarIndexForMs(mSelectedClip.getStartTime(), tickCount, clipMediaDurationMs.get(), rightToLeft),
                            getRangeBarIndexForMs(mSelectedClip.getStopTime(), tickCount, clipMediaDurationMs.get(), rightToLeft));
                    clipLength.setText(mContext.getString(R.string.total) + " : " + Util.makeTimeString(clipDurationMs.get()));
                    clipEnd.setText(Util.makeTimeString(mSelectedClip.getStopTime()));
                } catch (IllegalArgumentException | IllegalStateException | SecurityException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }

        });

        // Poll MediaPlayer for position, ensuring it never exceeds clipStopMs
        final Timer timer = new Timer("mplayer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (player.isPlaying()) {
                        if (player.getCurrentPosition() > clipStopMs.get()) {
                            player.pause();
                            Log.i(TAG, "stopping playback at clip end selection");
                        }
                        playbackBar.setProgress((int) (tickCount * ((float) player.getCurrentPosition()) / player.getDuration()));
                    }
                } catch (IllegalStateException e) { /* MediaPlayer in invalid state. Ignore */}
            }
        }, 100, 100);


        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(v)
                .setPositiveButton(mContext.getString(R.string.trim_clip).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSelectedClip.setStartTime(clipStartMs.get());
                        mSelectedClip.setStopTime(clipStopMs.get());

                        // need to save here
                        Timber.d("SAVING START/STOP TIME");
                        mStoryPath.getStoryPathLibrary().save(true);
                    }
                })
                .setNegativeButton(mContext.getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        Dialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Timber.d("dialog dismissed");
                if (player.isPlaying()) player.stop();
                player.release();
                timer.cancel();
            }
        });
        dialog.show();
    }

    private static int getMsFromRangeBarIndex(int tick, int max, int clipDurationMs, boolean rightToLeft) {
        int seekMs;

        if (rightToLeft)
            seekMs = (int) (clipDurationMs * Math.min(1, ((float) (max - tick) / max)));
        else
            seekMs = (int) (clipDurationMs * Math.min(1, ((float) tick / max)));
        //Log.i(TAG, String.format("Seek to index %d equals %d ms. Duration: %d ms", idx, seekMs, clipDurationMs.get()));
        return seekMs;
    }

    private static int getRangeBarIndexForMs(int positionMs, int max, int clipDurationMs, boolean rightToLeft) {
        // Range bar goes from 0 to (max - 1)

        int idx;

        if (rightToLeft)
            idx = (int) Math.min((((clipDurationMs - positionMs) * max) / (float) clipDurationMs), max - 1);
        else
            idx = (int) Math.min(((positionMs * max) / (float) clipDurationMs), max - 1);

        Log.i(TAG, String.format("Converted %d ms to rangebar position %d", positionMs, idx));
        return idx;
    }
}
