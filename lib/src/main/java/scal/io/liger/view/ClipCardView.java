package scal.io.liger.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.IconTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.edmodo.rangebar.RangeBar;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import scal.io.liger.Constants;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.Utility;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.ClipMetadata;
import scal.io.liger.model.ExampleMediaFile;
import scal.io.liger.model.MediaFile;


public class ClipCardView extends BaseRecordCardView {
    public static final String TAG = "ClipCardView";

    public ClipCard mCardModel;

    private ArrayList<View> mDisplayedClips = new ArrayList<>(); // Views representing clips currently displayed
    private int mCardFooterHeight; // The mClipsExpanded height of the card footer (e.g: Capture import buttons)
    private boolean mClipsExpanded = false; // Is the clip stack expanded

    private final float PRIMARY_CLIP_ALPHA = 1.0f;
    private final float SECONDARY_CLIP_ALPHA = .7f;


    TextView tvHeader;
    TextView tvBody;
    TextView tvImport;
    TextView tvCapture;
    TextView tvStop;
    ImageView ivOverflow;

    private IconTextView itvClipTypeIcon;

    public ClipCardView(Context context, Card cardModel) {
        super();
        mContext = context;
        mCardModel = (ClipCard) cardModel;

        Resources r = context.getResources();
        mCardFooterHeight  = r.getDimensionPixelSize(R.dimen.clip_card_footer_height);
    }

    @Override
    public View getCardView(final Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_clip, null);

        // Views modified by animation callbacks, and must be final
        final ViewGroup collapsableContainer    = (ViewGroup) view.findViewById(R.id.collapsable);
        final ViewGroup clipCandidatesContainer = (ViewGroup) view.findViewById(R.id.clipCandidates);

        // Views only modified during initial binding
        itvClipTypeIcon = (IconTextView) view.findViewById(R.id.itvClipTypeIcon);
        tvHeader        = (TextView) view.findViewById(R.id.tvHeader);
        tvBody          = (TextView) view.findViewById(R.id.tvBody);
        tvImport        = (TextView) view.findViewById(R.id.tvImport);
        tvCapture       = (TextView) view.findViewById(R.id.tvCapture);
        tvStop          = (TextView) view.findViewById(R.id.tvStop);
        ivOverflow      = (ImageView) view.findViewById(R.id.ivOverflowButton);

        mVUMeterLayout  = (LinearLayout) view.findViewById(R.id.vumeter_layout);

        /** Capture Media Button Click Listener */
        View.OnClickListener captureClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                int requestId = -1;

                String medium = mCardModel.getMedium();
                String cardMediaId = mCardModel.getStoryPath().getId() + "::" + mCardModel.getId() + "::" + MEDIA_PATH_KEY;
                if (medium.equals(Constants.VIDEO)) {
                    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    requestId = Constants.REQUEST_VIDEO_CAPTURE;

                } else if (medium.equals(Constants.PHOTO)) {
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Log.e(TAG, "Unable to make image file");
                        Utility.toastOnUiThread((Activity) context, "Unable to make image file");
                        return;
                    }
                    mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString(Constants.EXTRA_FILE_LOCATION, photoFile.getAbsolutePath()).apply();
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    requestId = Constants.REQUEST_IMAGE_CAPTURE;
                }

                if (medium.equals(Constants.AUDIO)) {
                    changeRecordNarrationStateChanged(RecordNarrationState.RECORDING);
                    startRecordingNarration();

                    // FIXME hide butons, replace with stop button (and eventually a pause button

//                    intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
//                    requestId = Constants.REQUEST_AUDIO_CAPTURE;
                } else if (null != intent && intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString(Constants.PREFS_CALLING_CARD_ID, cardMediaId).apply(); // Apply is async and fine for UI thread. commit() is synchronous
                    ((Activity) mContext).startActivityForResult(intent, requestId);
                }
            }
        };

        /** Stop Button Click Listener */
        View.OnClickListener stopClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                MediaFile mf = stopRecordingNarration();

                mCardModel.saveMediaFile(mf);

                // SEEMS LIKE A REASONABLE TIME TO SAVE
                mCardModel.getStoryPath().getStoryPathLibrary().save(true);
                ((MainActivity) mContext).mCardAdapter.changeCard(mCardModel); // FIXME this isn't pretty
//                ((MainActivity) mContext).scrollRecyclerViewToCard(mCardModel);

//                mCardModel.setNarration(mf);
                // FIXME save the audio file
                changeRecordNarrationStateChanged(RecordNarrationState.STOPPED);
            }
        };

        // Set click listeners for actions
        tvCapture.setOnClickListener(captureClickListener);

        tvStop.setOnClickListener(stopClickListener);

        tvImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ACTION_OPEN_DOCUMENT is the new API 19 action for the Android file manager
                Intent intent;
                int requestId = Constants.REQUEST_FILE_IMPORT;
                if (Build.VERSION.SDK_INT >= 19) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                } else {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                }

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // TODO : Adjust based on medium?
                intent.setType("*/*");

                String cardMediaId = mCardModel.getStoryPath().getId() + "::" + mCardModel.getId() + "::" + MEDIA_PATH_KEY;
                mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString(Constants.PREFS_CALLING_CARD_ID, cardMediaId).apply(); // Apply is async and fine for UI thread. commit() is synchronous
                ((Activity) mContext).startActivityForResult(intent, requestId);
            }
        });

        final int drawableSizeDp = 30;
        Resources r = mContext.getResources();
        int drawableSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, drawableSizeDp, r.getDisplayMetrics());

        //set drawables for actions
        Drawable drwImport = new IconDrawable(mContext, Iconify.IconValue.fa_ic_card_import).colorRes(R.color.storymaker_highlight);
        drwImport.setBounds(0, 0, drawableSizePx, drawableSizePx);
        tvImport.setCompoundDrawables(drwImport, null, null, null);

        Drawable drwCapture = new IconDrawable(mContext, Iconify.IconValue.fa_ic_card_capture_photo).colorRes(R.color.storymaker_highlight);
        drwCapture.setBounds(0, 0, drawableSizePx, drawableSizePx);
        tvCapture.setCompoundDrawables( drwCapture, null, null, null);

        IconDrawable iconDrawable = new IconDrawable(mContext, Iconify.IconValue.fa_ic_more_vert_48px).colorRes(R.color.storymaker_highlight);
        iconDrawable.setBounds(0, 0, drawableSizePx, drawableSizePx);
        ivOverflow.setImageDrawable(iconDrawable);

        //a-ic_card_capture_photo

        setupOverflowMenu(ivOverflow);

        // TODO: If the recycled view previously belonged to a different
        // card type, tear down and rebuild the view as in onCreateViewHolder.

        final ArrayList<ClipMetadata> clipsToDisplay = mCardModel.getClips();
        final boolean hasClips = (clipsToDisplay != null && clipsToDisplay.size() > 0);
        if (hasClips) clipCandidatesContainer.removeAllViews(); // Remove any prior clip views

        /** Clip Stack Card Click Listener
         *  Handles click on primary clip (show playback / edit dialog) as well as
         *  secondary clips and footer (expand clip stack or collapse after new primary clip selection)
        */
        View.OnClickListener clipCardOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasClips) {
                    // This ClipCard is displaying a single item stack with either example or
                    // fallback drawables. A click should expand the card footer to reveal the Capture / Import feature
                    toggleFooterVisibility(collapsableContainer);
                    return;
                }

                if (v.getTag(R.id.view_tag_clip_primary) != null && (boolean) v.getTag(R.id.view_tag_clip_primary)) {
                    // Clicked clip is primary
                    Log.i(TAG + "-select", "primary");
                    if (mClipsExpanded) {
                        // Collapse clip view, without change
                        toggleClipExpansion(clipsToDisplay, clipCandidatesContainer);
                        toggleFooterVisibility(collapsableContainer);
                    } else if (mCardModel.getMedium().equals(Constants.VIDEO) ||
                               mCardModel.getMedium().equals(Constants.AUDIO)) { //TODO : Support audio trimming
                        //show trim dialog
                        showClipPlaybackAndTrimming();
                    }
                } else {
                    // Clicked clip is not primary clip
                    if (mClipsExpanded &&  v.getTag(R.id.view_tag_clip_primary) != null) {
                        // Clicked view is secondary clip and clips are expanded
                        // This indicates a new secondary clip was selected
                        Log.i(TAG + "-select", "new primary clip selected");
                        // If clips expanded, this event means we've been selected as the
                        // new primary clip!
                        setNewSelectedClip(v);
                    }
                    toggleClipExpansion(clipsToDisplay, clipCandidatesContainer);
                    toggleFooterVisibility(collapsableContainer);
                }
            }
        };

        /** Populate clip stack */
        if (hasClips) {
            // Begin in the collapsed state
            ViewGroup.LayoutParams params = collapsableContainer.getLayoutParams();
            params.height = 0;
            collapsableContainer.setLayoutParams(params);

            Log.i(TAG + "-clip", String.format("adding %d clips for cardclip ", clipsToDisplay.size()));
            // Thumbnails are added to the clip stack from back to front. This greatly simplifies producing the desired z-order
            Collections.reverse(clipsToDisplay);
            for (int x = 0; x < clipsToDisplay.size(); x++) {
                // Create view for new clip
                MediaFile mediaFile = mCardModel.loadMediaFile(clipsToDisplay.get(x));
                View clipThumb = inflateAndAddThumbnailForClip(clipCandidatesContainer, mediaFile, x, clipsToDisplay.size() - 1);
                clipThumb.setOnClickListener(clipCardOnClickListener);
                if (x != clipsToDisplay.size() - 1) {
                    // Clicking on any but the top clip triggers expansion
                    clipThumb.setTag(R.id.view_tag_clip_primary, false);
                } else {
                    clipThumb.setTag(R.id.view_tag_clip_primary, true);
                }
                clipThumb.setTag(R.id.view_tag_clip_metadata, clipsToDisplay.get(x));
                mDisplayedClips.add(clipThumb);
            }
            // Restore order of clipsToDisplay, since it's a reference to the StoryPath model
            Collections.reverse(clipsToDisplay);
        } else {
            // Begin in the expanded state
            View clipThumb = inflateAndAddThumbnailForClip(clipCandidatesContainer, null, 0, 0);
            clipThumb.setOnClickListener(clipCardOnClickListener);
            clipThumb.setTag(R.id.view_tag_clip_primary, true);
            clipThumb.setTag(R.id.view_tag_clip_metadata, null);
            mDisplayedClips.add(clipThumb);
        }

        tvHeader.setText(mCardModel.getClipType().toUpperCase());
        tvHeader.setTextColor(getClipTypeColor(mCardModel.getClipType()));

        // Expand / Collapse footer on click
        tvHeader.setOnClickListener(clipCardOnClickListener);

        if (mCardModel.getFirstGoal() != null) {
            tvBody.setText(mCardModel.getFirstGoal());
        }

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    /**
     * Inflate and add to clipCandidatesContainer an ImageView populated with a thumbnail appropriate for the given mediaFile.
     * Note: zOrder 0 is the bottom of the clip list.
     * @return the view inflated and added to clipCandidatesContainer
     */
    private View inflateAndAddThumbnailForClip(@NonNull ViewGroup clipCandidatesContainer, @Nullable MediaFile mediaFile, int zOrder, int zTop) {
        Resources r = clipCandidatesContainer.getContext().getResources();
        int topMarginPerZ = r.getDimensionPixelSize(R.dimen.clip_stack_margin_top);

        LayoutInflater inflater = (LayoutInflater) clipCandidatesContainer.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView thumbnail = (ImageView) inflater.inflate(R.layout.clip_thumbnail, clipCandidatesContainer, false);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) thumbnail.getLayoutParams();
        params.topMargin = topMarginPerZ * (zTop - zOrder);
        //Log.i("inflate", String.format("Inflating thumbnail for z %d with top margin %d", zOrder, params.topMargin));
        thumbnail.setLayoutParams(params);

        //set clip thumbnail image
        setThumbnailForClip(thumbnail, mediaFile);

        if (zOrder != zTop)
            thumbnail.setAlpha(SECONDARY_CLIP_ALPHA);
        else
            thumbnail.setAlpha(PRIMARY_CLIP_ALPHA);

        clipCandidatesContainer.addView(thumbnail);
        return thumbnail;
    }

    private int getClipTypeColor(String clipType) {
        if (clipType.equalsIgnoreCase(Constants.CHARACTER)) {
            return mContext.getResources().getColor(R.color.storymaker_blue);
        } else if (clipType.equalsIgnoreCase(Constants.ACTION)) {
            return mContext.getResources().getColor(R.color.storymaker_orange);
        } else if (clipType.equalsIgnoreCase(Constants.RESULT)) {
            return mContext.getResources().getColor(R.color.storymaker_lime);
        } else if (clipType.equalsIgnoreCase(Constants.SIGNATURE)) {
            return mContext.getResources().getColor(R.color.storymaker_magenta);
        } else if (clipType.equalsIgnoreCase(Constants.PLACE)) {
            return mContext.getResources().getColor(R.color.storymaker_slate);
        }
        return mContext.getResources().getColor(R.color.storymaker_highlight);
    }

    private void setClipExampleDrawables(String clipType, ImageView imageView) {
        Drawable drawable;
        if (clipType.equalsIgnoreCase(Constants.CHARACTER)) {
            drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_character);
            itvClipTypeIcon.setText("{fa-ic_clip_character}");
        } else if (clipType.equalsIgnoreCase(Constants.ACTION)) {
            drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_action);
            itvClipTypeIcon.setText("{fa-ic_clip_action}");
        } else if (clipType.equalsIgnoreCase(Constants.RESULT)){
            drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_result);
            itvClipTypeIcon.setText("{fa-ic_clip_result}");
        } else if (clipType.equalsIgnoreCase(Constants.SIGNATURE)){
            drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_signature);
            itvClipTypeIcon.setText("{fa-ic_clip_signature}");
        } else if (clipType.equalsIgnoreCase(Constants.PLACE)){
            drawable = new IconDrawable(mContext, Iconify.IconValue.fa_clip_ex_place);
            itvClipTypeIcon.setText("{fa-ic_clip_place}");
        } else {
            //TODO handle invalid clip type
            Log.d(TAG, "No clipType matching '" + clipType + "' found.");
            drawable = mContext.getResources().getDrawable(R.drawable.ic_launcher); // FIXME replace with a sensible placeholder image
            itvClipTypeIcon.setText("{fa-card_capture_photo}");
        }
        imageView.setImageDrawable(drawable);
        itvClipTypeIcon.setTextColor(getClipTypeColor(clipType));
    }

    private void setThumbnailForClip(@NonNull ImageView ivThumbnail, MediaFile media) {
        MediaFile mediaFile;
        if (media != null) {
            mediaFile = media;
        } else {
            mediaFile = mCardModel.getExampleMediaFile();
        }

        if ((mediaFile == null) ||
           ((mediaFile instanceof ExampleMediaFile) &&
           ((ExampleMediaFile)mediaFile).getExampleThumbnail(mCardModel) == null)) {
            Log.e(this.getClass().getName(), "no media file was found");
            // Clip has no attached media. Show generic drawable based on clip type
            String clipType = mCardModel.getClipType();

            setClipExampleDrawables(clipType, ivThumbnail);
            ivThumbnail.setVisibility(View.VISIBLE);
        } else {
            Bitmap thumbnail = mediaFile.getThumbnail(mContext);

            if (thumbnail != null) {
                ivThumbnail.setImageBitmap(thumbnail);
                ivThumbnail.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showClipPlaybackAndTrimming() {
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
        final int tickCount = mContext.getResources().getInteger(R.integer.trim_bar_tick_count);

        /** Media player and media */
        final MediaPlayer player = new MediaPlayer();
        final ClipMetadata selectedClip = mCardModel.getSelectedClip();
        final AtomicInteger clipDurationMs = new AtomicInteger();

        /** Values modified by RangeBar listener. Used by Dialog trim listener to
         *  set final trim selections on ClipMetadata */
        final AtomicInteger clipStartMs = new AtomicInteger(selectedClip.getStartTime());
        final AtomicInteger clipStopMs = new AtomicInteger(selectedClip.getStopTime());

        /** Setup initial values that don't require media loaded */
        clipStart.setText(Util.makeTimeString(selectedClip.getStartTime()));
        clipEnd.setText(Util.makeTimeString(selectedClip.getStopTime()));

        Log.i(TAG, String.format("Showing clip trim dialog with intial start: %d stop: %d", selectedClip.getStartTime(), selectedClip.getStopTime()));

        rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            int lastLeftIdx;
            int lastRightIdx;

            @Override
            public void onIndexChangeListener(RangeBar rangeBar, int leftIdx, int rightIdx) {
                //Log.i(TAG, String.format("Seek to leftIdx %d rightIdx %d. left: %d. right: %d", leftIdx, rightIdx, rangeBar.getLeft(), rangeBar.getRight()));

                if (lastLeftIdx != leftIdx) {
                    // Left seek was adjusted, seek to it
                    clipStartMs.set(getMsFromRangeBarIndex(leftIdx, tickCount, clipDurationMs.get()));
                    player.seekTo(clipStartMs.get());
                    clipStart.setText(Util.makeTimeString(clipStartMs.get()));
                    //Log.i(TAG, String.format("Left seek to %d ms", clipStartMs.get()));
                    if (playbackBar.getProgress() < leftIdx) playbackBar.setProgress(leftIdx);


                } else if (lastRightIdx != rightIdx) {
                    // Right seek was adjusted, seek to it
                    clipStopMs.set(getMsFromRangeBarIndex(rightIdx, tickCount, clipDurationMs.get()));
                    player.seekTo(clipStopMs.get());
                    clipEnd.setText(Util.makeTimeString(clipStopMs.get()));

                    if (playbackBar.getProgress() > rightIdx) playbackBar.setProgress(rightIdx);
                    //Log.i(TAG, String.format("Right seek to %d ms", clipStopMs.get()));
                }
                lastLeftIdx = leftIdx;
                lastRightIdx = rightIdx;
            }
        });

        View.OnClickListener playbackToggleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thumbnailView.setVisibility(View.GONE);

                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.seekTo(clipStartMs.get());
                    player.start();
                }
            }
        };

        videoView.setOnClickListener(playbackToggleClickListener);
        thumbnailView.setOnClickListener(playbackToggleClickListener);

        videoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setThumbnailForClip(thumbnailView, mCardModel.getSelectedMediaFile());

                Uri video = Uri.parse(mCardModel.getSelectedMediaFile().getPath());
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

                    clipDurationMs.set(player.getDuration());
                    if (clipStopMs.get() == 0) clipStopMs.set(clipDurationMs.get()); // If no stop point set, play whole clip

                    // Setup initial views requiring knowledge of clip media
                    if (selectedClip.getStopTime() == 0) selectedClip.setStopTime(clipDurationMs.get());
                    player.seekTo(selectedClip.getStartTime());
                    rangeBar.setThumbIndices(getRangeBarIndexForMs(selectedClip.getStartTime(), tickCount, clipDurationMs.get()),
                                              getRangeBarIndexForMs(selectedClip.getStopTime(), tickCount, clipDurationMs.get()));
                    clipLength.setText("Total : " + Util.makeTimeString(clipDurationMs.get()));
                    clipEnd.setText(Util.makeTimeString(selectedClip.getStopTime()));
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
                .setPositiveButton("TRIM CLIP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCardModel.getSelectedClip().setStartTime(clipStartMs.get());
                        mCardModel.getSelectedClip().setStopTime(clipStopMs.get());

                        // need to save here
                        Log.d(TAG, "SAVING START/STOP TIME");
                        mCardModel.getStoryPath().getStoryPathLibrary().save(true);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        player.release();
                        timer.cancel();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private int getMsFromRangeBarIndex(int tick, int max, int clipDurationMs) {
        int seekMs =  (int) (clipDurationMs * Math.min(1, ((float) tick / max)));
        //Log.i(TAG, String.format("Seek to index %d equals %d ms. Duration: %d ms", idx, seekMs, clipDurationMs.get()));
        return seekMs;
    }

    private int getRangeBarIndexForMs(int positionMs, int max, int clipDurationMs) {
        int idx = (int) Math.min(((positionMs * max) / (float) clipDurationMs), max - 1); // Range bar goes from 0 to (max - 1)
        Log.i(TAG, String.format("Converted %d ms to rangebar position %d", positionMs, idx));
        return idx;
    }

    private final int STAGGERED_ANIMATION_GAP_MS = 70;

    private void toggleClipExpansion(List<ClipMetadata> clipsToDisplay, ViewGroup clipCandidatesContainer) {
        // When this method is called all clips in holder.displayedClips should be
        // added to holder.clipCandidatesContainer in order (e.g: First item is highest z order)
        final int clipCandidateCount = clipsToDisplay.size();

        if (clipsToDisplay.size() < 2) {
            // If less than 2 clips, there's no reason to expand / collapse
            return;
        }
        // Dp to px
        Resources r = clipCandidatesContainer.getContext().getResources();
        int topMargin  = r.getDimensionPixelSize(R.dimen.clip_stack_margin_top);    // Margin between clip thumbs
        int clipHeight = r.getDimensionPixelSize(R.dimen.clip_thumb_height);        // Height of each clip thumb
        int howtoHeight = r.getDimensionPixelSize(R.dimen.card_tap_height);       // Height of howto card that appears at stack top

        final View howtoCard = ((View) clipCandidatesContainer.getParent()).findViewById(R.id.tvTapToContinue);
        float finalHowToOpacity = mClipsExpanded ?  0f : 1f;
        ObjectAnimator howtoAnim = ObjectAnimator.ofFloat(howtoCard, "alpha", 1 - finalHowToOpacity, finalHowToOpacity);
        howtoAnim.setStartDelay((long) (STAGGERED_ANIMATION_GAP_MS * (clipCandidateCount + 1) * finalHowToOpacity));
        howtoAnim.start();

        // Loop over all clip views except the last
        for (int i = 0; i < clipCandidateCount; i ++) {
            int viewIdx = i;
            if (mClipsExpanded) {
                // when mClipsExpanded : 0, 1
                // when compressing : 1, 0
                viewIdx = (clipCandidateCount - 1) - i;
            }

            final View child = mDisplayedClips.get(viewIdx);
            final ViewGroup.LayoutParams params = child.getLayoutParams();
            int marginPerChild = topMargin + clipHeight;

            ValueAnimator animator;
            //int startAnimationMargin;
            int stopAnimationMargin;
            int marginMultiplier = (clipCandidateCount - 1) - viewIdx;
            if (mClipsExpanded) {
                // compress
                stopAnimationMargin = (topMargin * marginMultiplier);   // howtoCard is leaving

                // Remove pressable background drawables
                child.setBackgroundResource(R.drawable.clip_card_unselected);
            } else {
                // expand
                stopAnimationMargin = (marginMultiplier * marginPerChild) + (howtoHeight + topMargin);
                // Add pressable background drawable
                child.setBackgroundResource(R.drawable.clip_card_selectable_bg);
            }
            Log.i(TAG + "-anim", String.format("Animating margin from %d to %d", ((ViewGroup.MarginLayoutParams) params).topMargin, stopAnimationMargin));

            animator = ValueAnimator.ofInt(((ViewGroup.MarginLayoutParams) params).topMargin, stopAnimationMargin);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ((ViewGroup.MarginLayoutParams) params).topMargin = (int) valueAnimator.getAnimatedValue();
                    child.setLayoutParams(params);
                }
            });
            animator.setStartDelay((1+ i) * STAGGERED_ANIMATION_GAP_MS);
            animator.start();
            //Log.i("anim", "View idx " + viewIdx + " animating with delay " + (1+ i) * 400 + " to margin " + stopAnimationMargin);
        }
        mClipsExpanded = !mClipsExpanded;
    }

    private void toggleFooterVisibility(final ViewGroup collapsable) {
        final ViewGroup.LayoutParams params = collapsable.getLayoutParams();

        ValueAnimator animator = null;
        if (collapsable.getHeight() < mCardFooterHeight) {
            // Expand
            animator = ValueAnimator.ofInt(0, mCardFooterHeight);
        } else {
            // Collapse
            animator = ValueAnimator.ofInt(mCardFooterHeight, 0);
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (Integer) valueAnimator.getAnimatedValue();
                collapsable.setLayoutParams(params);
            }
        });
        animator.start();
    }

    //returns stored mediaPath (if exists) or exampleMediaPath (if exists)
    /*
    @Override
    public File getValidFile(String mediaPath, String exampleMediaPath) {
        File mediaFile = null;

        if (mediaPath != null) {
            mediaFile = MediaHelper.loadFileFromPath(mCardModel.getStoryPath().buildZipPath(mediaPath));
        } else if (exampleMediaPath != null) {
            mediaFile = MediaHelper.loadFileFromPath(mCardModel.getStoryPath().buildZipPath(exampleMediaPath));
        }

        return mediaFile;
    }
    */

    private void setupOverflowMenu(ImageView imageView) {
        final PopupMenu popupMenu = new PopupMenu(mContext, imageView);
        popupMenu.inflate(R.menu.popup_clip_card);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId(); // Can't treat Ids as constants in library modules

                if (itemId == R.id.menu_edit_card) {
                    if (mCardModel.getSelectedClip() != null) {
                        showClipPlaybackAndTrimming();
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.add_clips_generic), Toast.LENGTH_SHORT).show();
                    }
                } else if (itemId == R.id.menu_change_goal) {

                } else if (itemId == R.id.menu_duplicate_card) {
                    try {
                        int thisCardIndex = mCardModel.getStoryPath().getCardIndex(mCardModel);
                        if (thisCardIndex == -1) {
                            Log.w(TAG, "Could not find index of this card in StoryPath or StoryPathLibrary. Cannot duplicate");
                            return true;
                        }
                        Card newCard = (Card) mCardModel.clone();
                        mCardModel.getStoryPath().addCardAtPosition(newCard, thisCardIndex);
                        mCardModel.getStoryPath().notifyCardChanged(newCard);
                        // TODO Make Card#stateVisiblity true
                    } catch (CloneNotSupportedException e) {
                        Log.e(TAG, "Failed to clone this ClipCard");
                        e.printStackTrace();
                    }
                } else if (itemId == R.id.menu_duplicate_clip) {
                    if (mCardModel.getSelectedClip() != null) {
                        // TODO duplicate clip
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.add_clips_generic), Toast.LENGTH_SHORT).show();
                    }
                } else if (itemId == R.id.menu_remove_card) {

                }
                Toast.makeText(mContext, "Selected " + (String) item.getTitleCondensed(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });
    }

    private void setNewSelectedClip(View newSelectedClip) {
        // Put the passed clipThumbnail at the top of the stack
        // The top of the stack is the end of mDisplayedClips
        Log.i(TAG + "-swap", String.format("Swapping card %d for %d", mDisplayedClips.indexOf(newSelectedClip), mDisplayedClips.size()-1));
        View oldSelectedClip = mDisplayedClips.get(mDisplayedClips.size()-1);
        mDisplayedClips.remove(mDisplayedClips.indexOf(newSelectedClip));
        mDisplayedClips.add(newSelectedClip);
        newSelectedClip.bringToFront();

        // Swap alphas
        oldSelectedClip.setAlpha(SECONDARY_CLIP_ALPHA);
        newSelectedClip.setAlpha(PRIMARY_CLIP_ALPHA);

        // Change view tags indicating primary / secondary status
        oldSelectedClip.setTag(R.id.view_tag_clip_primary, false);
        newSelectedClip.setTag(R.id.view_tag_clip_primary, true);

        // Set new clip as selected
        mCardModel.selectMediaFile((ClipMetadata) newSelectedClip.getTag(R.id.view_tag_clip_metadata));

        mCardModel.getStoryPath().getStoryPathLibrary().save(true);
    }

    private View.OnTouchListener mClipSelectionListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i(TAG + "-select", "setting clip selected");
                    v.setBackgroundResource(R.drawable.clip_card_selected);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.i(TAG + "-select", "setting clip unselected");
                    v.setBackgroundResource(0);
                    break;
            }

            return false;
        }// onTouch()
    };

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }
    /**
     * Update the UI in response to a new value assignment to {@link #mRecordNarrationState}
     */
    @Override
    void changeRecordNarrationStateChanged(RecordNarrationState newState) {
        super.changeRecordNarrationStateChanged(newState);
        switch(mRecordNarrationState) {
            case READY:
                tvImport.setVisibility(View.VISIBLE);
                tvCapture.setVisibility(View.VISIBLE);
                tvStop.setVisibility(View.GONE);
                mVUMeterLayout.setVisibility(View.GONE);
                break;
            case RECORDING:
                tvImport.setVisibility(View.GONE);
                tvCapture.setVisibility(View.GONE);
                tvStop.setVisibility(View.VISIBLE);
                mVUMeterLayout.setVisibility(View.VISIBLE);
                break;
            case PAUSED:
                tvImport.setVisibility(View.GONE);
                tvCapture.setVisibility(View.GONE);
                tvStop.setVisibility(View.VISIBLE);
                mVUMeterLayout.setVisibility(View.VISIBLE);
                break;
            case STOPPED:
                tvImport.setVisibility(View.VISIBLE);
                tvCapture.setVisibility(View.VISIBLE);
                tvStop.setVisibility(View.GONE);
                mVUMeterLayout.setVisibility(View.GONE);
                break;
        }
    }
}
