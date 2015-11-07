package scal.io.liger.view;

import timber.log.Timber;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.IconTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import scal.io.liger.Constants;
import scal.io.liger.MainActivity;
import scal.io.liger.R;
import scal.io.liger.Utility;
import scal.io.liger.av.AudioRecorder;
import scal.io.liger.av.ClipCardsPlayer;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.ClipMetadata;
import scal.io.liger.model.MediaFile;
import scal.io.liger.popup.EditClipPopup;


public class ClipCardView extends ExampleCardView {
    public static final String TAG = "ClipCardView";

    public ClipCard mCardModel;

    private AudioRecorder mRecorder;
    private List<View> mDisplayedClipViews = new ArrayList<>(); // Views representing clips currently displayed
    private int mCardFooterHeight; // The mClipsExpanded height of the card footer (e.g: Capture import buttons)
    private boolean mClipsExpanded = false; // Is the clip stack expanded
    private boolean mHasClips;

    private final float PRIMARY_CLIP_ALPHA = 1.0f;
    private final float SECONDARY_CLIP_ALPHA = .7f;

    private ViewGroup mCollapsableContainer;
    private ViewGroup mClipCandidatesContainer;
    private TextView tvHeader;
    private TextView tvBody;
    private TextView tvImport;
    private TextView tvCapture;
    private TextView tvStop;
    private ImageView ivOverflow;

    private IconTextView itvClipTypeIcon;

    /** Capture Media Button Click Listener */
    View.OnClickListener mCaptureClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
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
                    Timber.e("Unable to make image file");
                    v.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(v.getContext(), "Unable to make image file", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
            mContext.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).edit().putString(Constants.EXTRA_FILE_LOCATION, photoFile.getAbsolutePath()).apply();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                requestId = Constants.REQUEST_IMAGE_CAPTURE;
            }

            if (medium.equals(Constants.AUDIO)) {
                startRecordingAudio();
            } else if (null != intent && intent.resolveActivity(mContext.getPackageManager()) != null) {
                mContext.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).edit().putString(Constants.PREFS_CALLING_CARD_ID, cardMediaId).apply(); // Apply is async and fine for UI thread. commit() is synchronous
                ((Activity) mContext).startActivityForResult(intent, requestId);
            }
        }
    };

    /**
     *  Clip Stack Card Click Listener
     *  Handles click on primary clip (show playback / edit dialog) as well as
     *  secondary clips and footer (expand clip stack or collapse after new primary clip selection)
     */
    View.OnClickListener mClipCardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mHasClips) {
                // This ClipCard is displaying a single item stack with either example or
                // fallback drawables. A click should expand the card footer to reveal the Capture / Import feature
                toggleFooterVisibility(mCollapsableContainer, tvBody);
                return;
            }

            ViewGroup clipContainer = (ViewGroup) v.getParent();

            if (clipContainer.getTag(R.id.view_tag_clip_primary) != null &&
                (boolean) clipContainer.getTag(R.id.view_tag_clip_primary)) {
                // Clicked clip is primary
                Log.i(TAG + "-select", "primary");
                if (mClipsExpanded) {
                    // Collapse clip view, without change
                    toggleClipExpansion(mCardModel.getClips(), mClipCandidatesContainer);
                    toggleFooterVisibility(mCollapsableContainer, tvBody);
                } else if (mCardModel.getMedium().equals(Constants.VIDEO) ||
                        mCardModel.getMedium().equals(Constants.AUDIO)) { //TODO : Support audio trimming
                    //show trim dialog
                    EditClipPopup ecp = new EditClipPopup(mContext,mCardModel.getStoryPath(), mCardModel.getSelectedClip(), mCardModel.getSelectedMediaFile());
                    ecp.show();
                }
            } else {
                // Clicked clip is not primary clip
                if (mClipsExpanded && clipContainer.getTag(R.id.view_tag_clip_primary) != null) {
                    // Clicked view is secondary clip and clips are expanded
                    // This indicates a new secondary clip was selected
                    Log.i(TAG + "-select", "new primary clip selected");
                    // If clips expanded, this event means we've been selected as the
                    // new primary clip!
                    setNewSelectedClip(clipContainer);
                }
                toggleClipExpansion(mCardModel.getClips(), mClipCandidatesContainer);
                toggleFooterVisibility(mCollapsableContainer, tvBody);
            }
        }
    };

    /**
     * Clip Stack Clip delete listener
     * Handles user removing a clip from the Clip stack.
     */
    View.OnClickListener mDeleteClipClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            ViewGroup clipContainerView = (ViewGroup) v.getParent();
            ClipMetadata clipMetadata = (ClipMetadata) clipContainerView.getTag(R.id.view_tag_clip_metadata);

            mCardModel.removeClip(clipMetadata);

            if (mCardModel.getClips().size() > 0) {
                mDisplayedClipViews = inflateClipStack(mCardModel,
                                                       mClipCandidatesContainer,
                                                       mCardModel.getClips(),
                                                       mClipCardClickListener);
            } else {
                mHasClips = false;
                mDisplayedClipViews = inflateEmptyClipStack(mClipCandidatesContainer,
                                                            mClipCardClickListener);
            }

            mCardModel.getStoryPath().getStoryPathLibrary().save(true);
        }
    };

    public ClipCardView(@NonNull Context context, @NonNull Card cardModel) {
        super();
        mContext = context;
        mCardModel = (ClipCard) cardModel;

        Resources r = context.getResources();
        mCardFooterHeight  = r.getDimensionPixelSize(R.dimen.clip_body_height) +
                             r.getDimensionPixelSize(R.dimen.clip_btn_height) * 2;
    }

    @SuppressLint("NewApi")
    @Override
    @Nullable
    public View getCardView(@NonNull final Context context) {
        if (mCardModel == null) {
            return null;
        }
        Log.i(TAG, "getCardView");

        View view = LayoutInflater.from(context).inflate(R.layout.card_clip, null);

        // Views modified by animation callbacks, and must be final
        mCollapsableContainer    = (ViewGroup) view.findViewById(R.id.collapsable);
        mClipCandidatesContainer = (ViewGroup) view.findViewById(R.id.clipCandidates);

        // Views only modified during initial binding
        itvClipTypeIcon = (IconTextView) view.findViewById(R.id.itvClipTypeIcon);
        tvHeader        = (TextView) view.findViewById(R.id.tvHeader);
        tvBody          = (TextView) view.findViewById(R.id.tvBody);
        tvImport        = (TextView) view.findViewById(R.id.tvImport);
        tvCapture       = (TextView) view.findViewById(R.id.tvCapture);
        tvStop          = (TextView) view.findViewById(R.id.tvStop);
        ivOverflow      = (ImageView) view.findViewById(R.id.ivOverflowButton);

//        mVUMeterLayout  = (LinearLayout) view.findViewById(R.id.vumeter_layout);

        /** Stop Button Click Listener */
        View.OnClickListener stopClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                stopRecordingAudio();
            }
        };

        // Set click listeners for actions
        tvCapture.setOnClickListener(mCaptureClickListener);

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

                intent.setType(Utility.getIntentMediaType(mCardModel.getMedium()));

                String cardMediaId = mCardModel.getStoryPath().getId() + "::" + mCardModel.getId() + "::" + MEDIA_PATH_KEY;
                mContext.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE).edit().putString(Constants.PREFS_CALLING_CARD_ID, cardMediaId).apply(); // Apply is async and fine for UI thread. commit() is synchronous
                ((Activity) mContext).startActivityForResult(intent, requestId);
            }
        });

        final int drawableSizeDp = 30;
        Resources r = mContext.getResources();
        int drawableSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, drawableSizeDp, r.getDisplayMetrics());

        //set drawables for actions
        Drawable drwImport = new IconDrawable(mContext, Iconify.IconValue.fa_ic_card_import).colorRes(R.color.storymaker_highlight);
        drwImport.setBounds(0, 0, drawableSizePx, drawableSizePx);

        Drawable drwCapture = new IconDrawable(mContext, Iconify.IconValue.fa_ic_card_capture_photo).colorRes(R.color.storymaker_highlight);
        drwCapture.setBounds(0, 0, drawableSizePx, drawableSizePx);

        IconDrawable iconDrawable = new IconDrawable(mContext, Iconify.IconValue.fa_ic_more_vert_48px).colorRes(R.color.storymaker_highlight);
        iconDrawable.setBounds(0, 0, drawableSizePx, drawableSizePx);
        ivOverflow.setImageDrawable(iconDrawable);

        if (Build.VERSION.SDK_INT >= 17 &&
            mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            tvCapture.setCompoundDrawables(null, null, drwCapture, null);
            tvImport.setCompoundDrawables(null, null, drwImport, null);
        } else {
            tvCapture.setCompoundDrawables(drwCapture, null, null, null);
            tvImport.setCompoundDrawables(drwImport, null, null, null);
        }

        //a-ic_card_capture_photo

        setupOverflowMenu(ivOverflow);

        // TODO: If the recycled view previously belonged to a different
        // card type, tear down and rebuild the view as in onCreateViewHolder.

        mHasClips = (mCardModel.getClips() != null && mCardModel.getClips().size() > 0);

        /** Populate clip stack */
        if (mHasClips) {
            // Begin in the collapsed state
            ViewGroup.LayoutParams params = mCollapsableContainer.getLayoutParams();
            params.height = 0;
            mCollapsableContainer.setLayoutParams(params);

            mDisplayedClipViews = inflateClipStack(mCardModel,
                                                   mClipCandidatesContainer,
                                                   mCardModel.getClips(),
                                                   mClipCardClickListener);
        } else {
            // Begin in the expanded state
            mDisplayedClipViews = inflateEmptyClipStack(mClipCandidatesContainer,
                                                        mClipCardClickListener);
        }

        tvHeader.setText(mCardModel.getClipTypeLocalized().toUpperCase());
        tvHeader.setTextColor(getClipTypeColor(mCardModel.getClipType()));

        // Expand / Collapse footer on click
        tvHeader.setOnClickListener(mClipCardClickListener);

        if (mCardModel.getFirstGoal() != null) {
            tvBody.setText(mCardModel.getFirstGoal());
        }

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    private List<View> inflateEmptyClipStack(@NonNull ViewGroup clipCandidatesContainer,
                                             @NonNull View.OnClickListener clipThumbClickListener) {

        clipCandidatesContainer.removeAllViews();

        View clipThumbContainer = inflateAndAddThumbnailForClip(clipCandidatesContainer, null, 0, 0);
        ImageView clipThumb = (ImageView) clipThumbContainer.findViewById(R.id.thumb);
        clipThumb.setOnClickListener(clipThumbClickListener);
        clipThumbContainer.setTag(R.id.view_tag_clip_primary, true);
        clipThumbContainer.setTag(R.id.view_tag_clip_metadata, null);

        ArrayList<View> displayedClipViews = new ArrayList<>(1);
        displayedClipViews.add(clipThumbContainer);
        return displayedClipViews;
    }

    private List<View> inflateClipStack(@NonNull ClipCard cardModel,
                                        @NonNull ViewGroup clipCandidatesContainer,
                                        @NonNull List<ClipMetadata> clipsToDisplay,
                                        @NonNull View.OnClickListener clipThumbClickListener) {

        clipCandidatesContainer.removeAllViews();

        ArrayList<View> displayedClipViews = new ArrayList<>(clipsToDisplay.size());
        Log.i(TAG + "-clip", String.format("adding %d clips for cardclip ", clipsToDisplay.size()));
        // Thumbnails are added to the clip stack from back to front. This greatly simplifies producing the desired z-order
        Collections.reverse(clipsToDisplay);
        for (int x = 0; x < clipsToDisplay.size(); x++) {
            // Create view for new clip
            MediaFile mediaFile = cardModel.loadMediaFile(clipsToDisplay.get(x));
            ViewGroup clipThumbContainer = inflateAndAddThumbnailForClip(clipCandidatesContainer, mediaFile, x, clipsToDisplay.size() - 1);
            ImageView clipThumb = (ImageView) clipThumbContainer.findViewById(R.id.thumb);
            clipThumb.setOnClickListener(clipThumbClickListener);
            if (x != clipsToDisplay.size() - 1) {
                // Clicking on any but the top clip triggers expansion
                clipThumbContainer.setTag(R.id.view_tag_clip_primary, false);
            } else {
                clipThumbContainer.setTag(R.id.view_tag_clip_primary, true);
            }
            clipThumbContainer.setTag(R.id.view_tag_clip_metadata, clipsToDisplay.get(x));
            displayedClipViews.add(clipThumbContainer);
        }
        // Restore order of clipsToDisplay, since it's a reference to the StoryPath model
        Collections.reverse(clipsToDisplay);
        return displayedClipViews;
    }

    /**
     * Inflate and add to clipsContainer an ImageView populated with a thumbnail appropriate for the given mediaFile.
     * Note: zOrder 0 is the bottom of the clip list.
     * @return the view inflated and added to clipsContainer
     */
    private ViewGroup inflateAndAddThumbnailForClip(@NonNull ViewGroup clipsContainer,
                                                    @Nullable MediaFile mediaFile,
                                                    int zOrder,
                                                    int zTop) {

        LayoutInflater inflater   = (LayoutInflater) clipsContainer.getContext()
                                                                   .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        FrameLayout clipContainer = (FrameLayout) inflater.inflate(R.layout.clip_thumbnail,
                                                                   clipsContainer,
                                                                   false);

        ImageButton deleteBtn     = (ImageButton) clipContainer.findViewById(R.id.btn_delete);
        ImageView thumbnail       = (ImageView) clipContainer.findViewById(R.id.thumb);

        int topMarginPerZ         = clipsContainer.getContext()
                                                  .getResources()
                                                  .getDimensionPixelSize(R.dimen.clip_stack_margin_top);

        FrameLayout.MarginLayoutParams params = (FrameLayout.MarginLayoutParams) clipContainer.getLayoutParams();
        params.topMargin = topMarginPerZ * (zTop - zOrder);
        clipContainer.setLayoutParams(params);

        setThumbnailForClip(thumbnail, mediaFile);
        thumbnail.setAlpha(zOrder == zTop ? PRIMARY_CLIP_ALPHA : SECONDARY_CLIP_ALPHA);

        if (mediaFile != null) {
            deleteBtn.setOnClickListener(mDeleteClipClickListener);
            deleteBtn.setVisibility(View.VISIBLE);
        }

        clipsContainer.addView(clipContainer);
        return clipContainer;
    }

    private int getClipTypeColor(String clipType) {
        if (clipType == null) {
            // handle nulls (same as default case)
            Timber.d("No value found for clipType. (getClipTypeColor)");
            return mContext.getResources().getColor(R.color.storymaker_highlight);
        } else if (clipType.equalsIgnoreCase(Constants.CHARACTER)) {
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
        if (clipType == null) {
            // handle nulls (same as failure case)
            Timber.d("No value found for clipType. (setClipExampleDrawables)");
            drawable = mContext.getResources().getDrawable(R.drawable.ic_launcher); // FIXME replace with a sensible placeholder image
            itvClipTypeIcon.setText("{fa-card_capture_photo}");
        } else if (clipType.equalsIgnoreCase(Constants.CHARACTER)) {
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
            Timber.d("No clipType matching '" + clipType + "' found.");
            drawable = mContext.getResources().getDrawable(R.drawable.ic_launcher); // FIXME replace with a sensible placeholder image
            itvClipTypeIcon.setText("{fa-card_capture_photo}");
        }
        imageView.setImageDrawable(drawable);
        itvClipTypeIcon.setTextColor(getClipTypeColor(clipType));
    }

    private void setThumbnailForClip(@NonNull ImageView ivThumbnail, MediaFile media) {
        final MediaFile mediaFile;
        if (media != null) {
            mediaFile = media;
        } else {
            mediaFile = mCardModel.getExampleMediaFile();
        }

        if (mediaFile == null) {
            Timber.e("no media file was found");
            // Clip has no attached media. Show generic drawable based on clip type
            String clipType = mCardModel.getClipType(); // FIXME why not media.get

            setClipExampleDrawables(clipType, ivThumbnail);
            ivThumbnail.setVisibility(View.VISIBLE);
        } else {

            mediaFile.loadThumbnail(ivThumbnail, new MediaFile.MediaFileThumbnailCallback() {

                        @Override
                        public void newThumbnailAssigned(File newThumbnail) {
                            Timber.d("Notifying StoryPath of newly assigned thumbnail");
                            // need to update media file in model now that thumbnail is set
                            // being overly cautious to avoid null pointers
                            if ((mCardModel.getStoryPath() != null) &&
                                    (mCardModel.getStoryPath().getStoryPathLibrary() != null)) {

                                HashMap<String, MediaFile> mediaFiles = mCardModel.getStoryPath()
                                        .getStoryPathLibrary()
                                        .getMediaFiles();
                                MediaFile targetMediaFile = null;
                                for (String key : mediaFiles.keySet()) {
                                    MediaFile candidateMediaFile = mediaFiles.get(key);
                                    if (candidateMediaFile.getPath().equals(mediaFile.getPath())) {
                                        targetMediaFile = candidateMediaFile;
                                    }
                                }
                                if (targetMediaFile != null) {
                                    mCardModel.getStoryPath().getStoryPathLibrary().saveMediaFile(mediaFile.getPath(), mediaFile);

                                    // set metadata too
                                    if (mCardModel.getStoryPath().getStoryPathLibrary().getMetaThumbnail() == null) {
                                        mCardModel.getStoryPath().getStoryPathLibrary().setMetaThumbnail(newThumbnail.getPath());
                                    }

                                    // force save to store mediafile/metadata updates
                                    // shouldn't need to save story path at this point
                                    mCardModel.getStoryPath().getStoryPathLibrary().save(false);
                                } else {
                                    Timber.w("Could not find MediaFile corresponding to newly assigned thumbnail");
                                }
                            }
                        }
                    }
            );

            ivThumbnail.setVisibility(View.VISIBLE);
        }
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

            final View child = mDisplayedClipViews.get(viewIdx);
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

    private void toggleFooterVisibility(final ViewGroup collapsable, TextView body) {
        final ViewGroup.LayoutParams params = collapsable.getLayoutParams();

        String targetText = body.getText().toString();
        Rect textRect = new Rect();
        body.getPaint().getTextBounds(targetText, 0, targetText.length(), textRect);

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
                        EditClipPopup ecp = new EditClipPopup(mContext,mCardModel.getStoryPath(), mCardModel.getSelectedClip(), mCardModel.getSelectedMediaFile());
                        ecp.show();
                    } else {
                        Toast.makeText(mContext, mContext.getString(R.string.add_clips_generic), Toast.LENGTH_SHORT).show();
                    }
                } else if (itemId == R.id.menu_change_goal) {

                } else if (itemId == R.id.menu_duplicate_card) {
                    try {
                        int thisCardIndex = mCardModel.getStoryPath().getCardIndex(mCardModel);
                        if (thisCardIndex == -1) {
                            Timber.w("Could not find index of this card in StoryPath or StoryPathLibrary. Cannot duplicate");
                            return true;
                        }
                        Card newCard = (Card) mCardModel.clone();
                        mCardModel.getStoryPath().addCardAtPosition(newCard, thisCardIndex);
                        mCardModel.getStoryPath().notifyCardChanged(newCard);
                        ((MainActivity) mContext).refreshCardList(); // FIXME this refreshes the list and jumps to the top, which isn't super friendly
                        // TODO Make Card#stateVisiblity true
                    } catch (CloneNotSupportedException e) {
                        Timber.e("Failed to clone this ClipCard");
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

    private void setNewSelectedClip(View newSelectedClipContainer) {
        // Put the passed clipThumbnail at the top of the stack
        // The top of the stack is the end of mDisplayedClipViews
        Log.i(TAG + "-swap", String.format("Swapping card %d for %d", mDisplayedClipViews.indexOf(newSelectedClipContainer), mDisplayedClipViews.size()-1));
        View oldSelectedClipContainer = mDisplayedClipViews.get(mDisplayedClipViews.size()-1);
        mDisplayedClipViews.remove(mDisplayedClipViews.indexOf(newSelectedClipContainer));
        mDisplayedClipViews.add(newSelectedClipContainer);
        newSelectedClipContainer.bringToFront();

        // Swap alphas
        oldSelectedClipContainer.findViewById(R.id.thumb).setAlpha(SECONDARY_CLIP_ALPHA);
        newSelectedClipContainer.findViewById(R.id.thumb).setAlpha(PRIMARY_CLIP_ALPHA);

        // Change view tags indicating primary / secondary status
        oldSelectedClipContainer.setTag(R.id.view_tag_clip_primary, false);
        newSelectedClipContainer.setTag(R.id.view_tag_clip_primary, true);

        // Set new clip as selected
        mCardModel.selectMediaFile((ClipMetadata) newSelectedClipContainer.getTag(R.id.view_tag_clip_metadata));

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

    private void startRecordingAudio() {
        // Inflate FrameLayout into clipCandidates
        // Add AudioRecorder into it
        FrameLayout.LayoutParams mediaViewParams =
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);

        FrameLayout frame = new FrameLayout(mContext);
        frame.setLayoutParams(mediaViewParams);

        frame.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                // Do nothing
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (mRecorder != null && mRecorder.isRecording()) {
                    Timber.w("ClipCardView detached while recording in progress. Recording will be lost.");
                    mRecorder.stopRecording();
                    mRecorder.release();
                    mCardModel.getStoryPath().getStoryPathLibrary().notifyScrollLockRequested(false, mCardModel);
                    // TODO : Can we attach this recording to the card model without :
                    //  java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling
                    // attaching media to ClipCardView will trigger observers in odd state. Could create separate ClipCard#saveMediaFile
                    // that doesn't trigger observers, but that could have its own issues...
                }
            }
        });

        try {
            mRecorder = new AudioRecorder(frame);

            for(int viewIdx = 0; viewIdx < mClipCandidatesContainer.getChildCount(); viewIdx++) {
                mClipCandidatesContainer.getChildAt(viewIdx).setVisibility(View.GONE);
            }

            mClipCandidatesContainer.addView(frame);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mRecorder.startRecording();
        mCardModel.getStoryPath().getStoryPathLibrary().notifyScrollLockRequested(true, mCardModel);

        tvCapture.setVisibility(View.GONE);
        tvStop.setVisibility(View.VISIBLE);
    }

    private void stopRecordingAudio() {
        MediaFile mf = mRecorder.stopRecording();
        mCardModel.getStoryPath().getStoryPathLibrary().notifyScrollLockRequested(false, mCardModel);
        mRecorder.release();
        mRecorder = null;
        if (mf != null) {
            mCardModel.saveMediaFile(mf);
            // SEEMS LIKE A REASONABLE TIME TO SAVE
            mCardModel.getStoryPath().getStoryPathLibrary().save(true);
        } else {
            // TODO Do something better
            Toast.makeText(mContext, "Failed to save recording", Toast.LENGTH_LONG).show();
        }
        // Instead of manually resetting UI, just call changeCard whether or not recording succeeded
        ((MainActivity) mContext).mCardAdapter.changeCard(mCardModel); // FIXME this isn't pretty
    }
}
