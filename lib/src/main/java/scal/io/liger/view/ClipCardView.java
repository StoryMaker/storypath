package scal.io.liger.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import scal.io.liger.Constants;
import scal.io.liger.MediaHelper;
import scal.io.liger.R;
import scal.io.liger.Utility;
import scal.io.liger.model.Card;
import scal.io.liger.model.ClipCard;
import scal.io.liger.model.ClipMetadata;
import scal.io.liger.model.MediaFile;


public class ClipCardView extends ExampleCardView implements AdapterView.OnItemSelectedListener {
    public static final String TAG = "ClipCardView";

    public ClipCard mCardModel;

    private ArrayList<View> mDisplayedClips = new ArrayList<>(); // Views representing clips currently displayed
    private int mCardFooterHeight; // The mClipsExpanded height of the card footer (e.g: Capture import buttons)
    private boolean mClipsExpanded = false; // Is the clip stack expanded

    private final float PRIMARY_CLIP_ALPHA = 1.0f;
    private final float SECONDARY_CLIP_ALPHA = .7f;

    public ClipCardView(Context context, Card cardModel) {
        super();
        mContext = context;
        mCardModel = (ClipCard) cardModel;

        Resources r = context.getResources();
        mCardFooterHeight  = r.getDimensionPixelSize(R.dimen.clip_card_footer_height);
    }

    @Override
    public View getCardView(Context context) {
        if (mCardModel == null) {
            return null;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.card_clip, null);

        // Views modified by animation callbacks, and must be final
        final ViewGroup collapsableContainer    = (ViewGroup) view.findViewById(R.id.collapsable);
        final ViewGroup clipCandidatesContainer = (ViewGroup) view.findViewById(R.id.clipCandidates);

        // Views only modified during initial binding
        TextView headerText  = (TextView) view.findViewById(R.id.headerText);
        TextView bodyText    = (TextView) view.findViewById(R.id.bodyText);
        Spinner spinner      = (Spinner) view.findViewById(R.id.overflowSpinner);
        Button captureButton = (Button) view.findViewById(R.id.captureBtn);

        /** Capture Media Button Click Listener */
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                int requestId = -1;

                String clipMedium = mCardModel.getClipMedium();
                String cardMediaId = mCardModel.getStoryPathReference().getId() + "::" + mCardModel.getId() + "::" + MEDIA_PATH_KEY;
                if (clipMedium.equals(Constants.VIDEO)) {
                    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    requestId = Constants.REQUEST_VIDEO_CAPTURE;

                } else if (clipMedium.equals(Constants.PHOTO)) {
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    requestId = Constants.REQUEST_IMAGE_CAPTURE;

                } else if (clipMedium.equals(Constants.AUDIO)) {
                    intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    requestId = Constants.REQUEST_AUDIO_CAPTURE;
                }

                if (null != intent && intent.resolveActivity(mContext.getPackageManager()) != null) {
                    mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString(Constants.PREFS_CALLING_CARD_ID, cardMediaId).apply(); // Apply is async and fine for UI thread. commit() is synchronous
                    ((Activity) mContext).startActivityForResult(intent, requestId);
                }
            }
        });

        setupSpinner(spinner);
        ViewGroup.LayoutParams params = collapsableContainer.getLayoutParams();
        params.height = 0;
        collapsableContainer.setLayoutParams(params);

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
                    Log.i("select", "primary");
                    if (mClipsExpanded) {
                        // Collapse clip view, without change
                        toggleClipExpansion(clipsToDisplay, clipCandidatesContainer);
                        toggleFooterVisibility(collapsableContainer);
                    } else {
                        //show trim dialog
                        showClipPlaybackAndTrimming();
                    }
                } else {
                    // Clicked clip is not primary clip
                    if (mClipsExpanded &&  v.getTag(R.id.view_tag_clip_primary) != null) {
                        // Clicked view is secondary clip and clips are expanded
                        // This indicates a new secondary clip was selected
                        Log.i("select", "new primary clip selected");
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
            Log.i("clip", String.format("adding %d clips for cardclip ", clipsToDisplay.size()));
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
                mDisplayedClips.add(clipThumb);
            }
        } else {
            View clipThumb = inflateAndAddThumbnailForClip(clipCandidatesContainer, null, 0, 0);
            clipThumb.setOnClickListener(clipCardOnClickListener);
            clipThumb.setTag(R.id.view_tag_clip_primary, true);
            mDisplayedClips.add(clipThumb);
        }

        headerText.setText(mCardModel.getClipType().toUpperCase());

        // Expand / Collapse footer on click
        headerText.setOnClickListener(clipCardOnClickListener);


        /** Original ClipCard view binding below: */

        // Expand / Collapse clip stack on thumbnail click
//        clipCandidatesContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggleClipExpansion(clipsToDisplay, clipCandidatesContainer);
//                toggleFooterVisibility(footerHeight, collapsableContainer);
//            }
//        });
        bodyText.setText(mCardModel.getHeader());
//        final VideoView vvCardVideo = ((VideoView) view.findViewById(R.id.vv_card_video));
//        final ImageView ivCardPhoto = ((ImageView) view.findViewById(R.id.iv_card_photo));
//        TextView tvHeader = ((TextView) view.findViewById(R.id.tv_header));
//        TextView tvType = ((TextView) view.findViewById(R.id.tv_type));
//        Button btnRecord = ((Button) view.findViewById(R.id.btn_record_media));
//        final ToggleButton btnMediaPlay = ((ToggleButton) view.findViewById(R.id.tb_card_audio));
//
//        tvHeader.setText(mCardModel.getHeader());
//        tvType.setText(mCardModel.getClipType());
//
//        final String clipMedium = mCardModel.getClipMedium();
//        final String cardMediaId = mCardModel.getStoryPathReference().getId() + "::" + mCardModel.getId() + "::" + MEDIA_PATH_KEY;
//
//        //set up media display
//        String p = null;
//
//        if ((mCardModel.getClips() != null) && (mCardModel.getClips().size() > 0)) {
//
//            MediaFile mf = mCardModel.getSelectedMediaFile();
//
//            if (mf == null) {
//                Log.e(this.getClass().getName(), "no media file was found");
//            } else {
//                p = mf.getPath();
//            }
//        }
//
//        final File mediaFile = getValidFile(p, mCardModel.getExampleMediaPath());
//
//        if (mediaFile == null) {
//            String clipType = mCardModel.getClipType();
//
//            if (clipType.equals(Constants.CHARACTER)) {
//                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_close));
//            } else if (clipType.equals(Constants.ACTION)) {
//                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_medium));
//            } else if (clipType.equals(Constants.RESULT)){
//                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_long));
//            } else {
//                //TODO handle invalid clip type
//                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher));
//            }
//
//            ivCardPhoto.setVisibility(View.VISIBLE);
//        } else if (mediaFile.exists() && !mediaFile.isDirectory()) {
//            if (clipMedium.equals(Constants.VIDEO)) {
//
//                //set up image as preview
//                Bitmap videoFrame = Utility.getFrameFromVideo(mediaFile.getPath());
//                if(null != videoFrame) {
//                    ivCardPhoto.setImageBitmap(videoFrame);
//                }
//
//                ivCardPhoto.setVisibility(View.VISIBLE);
//                btnMediaPlay.setVisibility(View.VISIBLE);
//                btnMediaPlay.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Uri video = Uri.parse(mediaFile.getPath());
//                        vvCardVideo.setVideoURI(video);
//                        vvCardVideo.seekTo(5);
//                        vvCardVideo.setMediaController(null);
//                        vvCardVideo.setVisibility(View.VISIBLE);
//                        ivCardPhoto.setVisibility(View.GONE);
//                        btnMediaPlay.setVisibility(View.GONE);
//                        vvCardVideo.start();
//                    }
//                });
//
//                //revert back to image on video completion
//                vvCardVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    public void onCompletion(MediaPlayer mp) {
//                        vvCardVideo.setVisibility(View.GONE);
//                        ivCardPhoto.setVisibility(View.VISIBLE);
//                        btnMediaPlay.setVisibility(View.VISIBLE);
//                        btnMediaPlay.setChecked(false);
//                    }
//                });
//            } else if (clipMedium.equals(Constants.PHOTO)) {
//                Uri uri = Uri.parse(mediaFile.getPath());
//                ivCardPhoto.setImageURI(uri);
//                ivCardPhoto.setVisibility(View.VISIBLE);
//            } else if (clipMedium.equals(Constants.AUDIO)) {
//                Uri myUri = Uri.parse(mediaFile.getPath());
//                final MediaPlayer mediaPlayer = new MediaPlayer();
//                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//
//                //set background image
//                String clipType = mCardModel.getClipType();
//                int drawable = R.drawable.ic_launcher;
//
//                if (clipType.equals(Constants.CHARACTER)) {
//                    drawable = R.drawable.cliptype_close;
//                } else if (clipType.equals(Constants.ACTION)) {
//                    drawable = R.drawable.cliptype_medium;
//                } else if (clipType.equals(Constants.RESULT)){
//                    drawable = R.drawable.cliptype_long;
//                }
//                ivCardPhoto.setImageDrawable(mContext.getResources().getDrawable(drawable));
//                ivCardPhoto.setVisibility(View.VISIBLE);
//
//                //set up media player
//                try {
//                    mediaPlayer.setDataSource(mContext, myUri);
//                    mediaPlayer.prepare();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                btnMediaPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        if (isChecked) {
//                            mediaPlayer.seekTo(5);
//                            mediaPlayer.start();
//                        } else {
//                            mediaPlayer.pause();
//                        }
//                    }
//                });
//
//                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer arg0) {
//                        btnMediaPlay.setChecked(false);
//                    }
//                });
//
//                mediaPlayer.seekTo(5);
//                btnMediaPlay.setVisibility(View.VISIBLE);
//            } else {
//                //TODO handle invalid-medium error
//            }
//        }
//
//        //set correct listener for record button
//        btnRecord.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = null;
//                int requestId = -1;
//
//                if(clipMedium.equals(Constants.VIDEO)) {
//                    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                    requestId = Constants.REQUEST_VIDEO_CAPTURE;
//
//                } else if(clipMedium.equals(Constants.PHOTO)) {
//                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    requestId = Constants.REQUEST_IMAGE_CAPTURE;
//
//                }  else if(clipMedium.equals(Constants.AUDIO)) {
//                    intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
//                    requestId = Constants.REQUEST_AUDIO_CAPTURE;
//                }
//
//                if (null != intent && intent.resolveActivity(mContext.getPackageManager()) != null) {
//                    mContext.getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putString(Constants.PREFS_CALLING_CARD_ID, cardMediaId).apply(); // FIXME should be done off the ui thread
//                    ((Activity) mContext).startActivityForResult(intent, requestId);
//                }
//            }
//        });

        // supports automated testing
        view.setTag(mCardModel.getId());

        return view;
    }

    /**
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

    private void setThumbnailForClip(@NonNull ImageView thumbnail, MediaFile media) {
        String mediaPath = null;
        if ((mCardModel.getClips() != null) && (mCardModel.getClips().size() > 0)) {
            if (media == null) {
                Log.e(TAG, "no media file was found");
            } else {
                mediaPath = media.getPath();
            }
        }

        final File mediaFile = getValidFile(mediaPath, mCardModel.getExampleMediaPath());

        if (mediaFile == null) {
            // Clip has no attached media. Show generic drawable based on clip type
            String clipType = mCardModel.getClipType();

            if (clipType.equals(Constants.CHARACTER)) {
                thumbnail.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_close));
            } else if (clipType.equals(Constants.ACTION)) {
                thumbnail.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_medium));
            } else if (clipType.equals(Constants.RESULT)){
                thumbnail.setImageDrawable(mContext.getResources().getDrawable(R.drawable.cliptype_long));
            } else {
                //TODO handle invalid clip type
                thumbnail.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher));
            }

            thumbnail.setVisibility(View.VISIBLE);
        } else if (mediaFile.exists() && !mediaFile.isDirectory()) {
            // Clip has attached media. Show an appropriate preview
            // e.g: A thumbnail for video
            String clipMedium = mCardModel.getClipMedium();
            if (clipMedium.equals(Constants.VIDEO)) {

                //set up image as preview
                Bitmap videoFrame = Utility.getFrameFromVideo(mediaFile.getPath());
                if(null != videoFrame) {
                    thumbnail.setImageBitmap(videoFrame);
                }

                thumbnail.setVisibility(View.VISIBLE);
//                btnMediaPlay.setVisibility(View.VISIBLE);
//                btnMediaPlay.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Uri video = Uri.parse(mediaFile.getPath());
//                        vvCardVideo.setVideoURI(video);
//                        vvCardVideo.seekTo(5);
//                        vvCardVideo.setMediaController(null);
//                        vvCardVideo.setVisibility(View.VISIBLE);
//                        thumbnail.setVisibility(View.GONE);
//                        btnMediaPlay.setVisibility(View.GONE);
//                        vvCardVideo.start();
//                    }
//                });
//
//                //revert back to image on video completion
//                vvCardVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    public void onCompletion(MediaPlayer mp) {
//                        vvCardVideo.setVisibility(View.GONE);
//                        ivCardPhoto.setVisibility(View.VISIBLE);
//                        btnMediaPlay.setVisibility(View.VISIBLE);
//                        btnMediaPlay.setChecked(false);
//                    }
//                });
            } else if (clipMedium.equals(Constants.PHOTO)) {
                Uri uri = Uri.parse(mediaFile.getPath());
                thumbnail.setImageURI(uri);
                thumbnail.setVisibility(View.VISIBLE);
            } else if (clipMedium.equals(Constants.AUDIO)) {
                Uri myUri = Uri.parse(mediaFile.getPath());
                final MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                //set background image
                String clipType = mCardModel.getClipType();
                int drawable = R.drawable.ic_launcher;

                if (clipType.equals(Constants.CHARACTER)) {
                    drawable = R.drawable.cliptype_close;
                } else if (clipType.equals(Constants.ACTION)) {
                    drawable = R.drawable.cliptype_medium;
                } else if (clipType.equals(Constants.RESULT)) {
                    drawable = R.drawable.cliptype_long;
                }
                thumbnail.setImageDrawable(mContext.getResources().getDrawable(drawable));
                thumbnail.setVisibility(View.VISIBLE);

//                //set up media player
//                try {
//                    mediaPlayer.setDataSource(mContext, myUri);
//                    mediaPlayer.prepare();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                btnMediaPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        if (isChecked) {
//                            mediaPlayer.seekTo(5);
//                            mediaPlayer.start();
//                        } else {
//                            mediaPlayer.pause();
//                        }
//                    }
//                });
//
//                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer arg0) {
//                        btnMediaPlay.setChecked(false);
//                    }
//                });
//
//                mediaPlayer.seekTo(5);
//                btnMediaPlay.setVisibility(View.VISIBLE);
            } else {
                //TODO handle invalid-medium error
            }
        }
    }

    private void showClipPlaybackAndTrimming() {
        View v = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_clip_playback_trim, null);

        final VideoView videoView = (VideoView) v.findViewById(R.id.videoView);
        final ImageView thumbnailView = (ImageView) v.findViewById(R.id.thumbnail);
        final TextView clipLength = (TextView) v.findViewById(R.id.clipLength);
        final TextView clipEnd = (TextView) v.findViewById(R.id.clipEnd);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                thumbnailView.setVisibility(View.GONE);
                // TODO : Properly display clip duration
                clipLength.setText(String.format("Total 0:%d",videoView.getDuration() / 1000));
                clipEnd.setText(String.format("0:%d",videoView.getDuration() / 1000));

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(v)
                .setPositiveButton("TRIM CLIP", null)
                .setNegativeButton("CANCEL", null);
        Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                setThumbnailForClip(thumbnailView, mCardModel.getSelectedMediaFile());

                Uri video = Uri.parse(mCardModel.getSelectedMediaFile().getPath());
                videoView.setVideoURI(video);
                videoView.setMediaController(null);
                videoView.start();
            }
        });
        dialog.show();
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
        int howtoHeight = r.getDimensionPixelSize(R.dimen.card_howto_height);       // Height of howto card that appears at stack top

        final View howtoCard = ((View) clipCandidatesContainer.getParent()).findViewById(R.id.howtoCard);
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
            Log.i("anim", String.format("Animating margin from %d to %d", ((ViewGroup.MarginLayoutParams) params).topMargin, stopAnimationMargin));

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
    @Override
    public File getValidFile(String mediaPath, String exampleMediaPath) {
        File mediaFile = null;

        if (mediaPath != null) {
            mediaFile = MediaHelper.loadFileFromPath(mCardModel.getStoryPathReference().buildPath(mediaPath));
        } else if (exampleMediaPath != null) {
            mediaFile = MediaHelper.loadFileFromPath(mCardModel.getStoryPathReference().buildPath(exampleMediaPath));
        }

        return mediaFile;
    }

    private void setupSpinner(Spinner spinner) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.clipcard_overflow, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    /** Spinner OnItemSelectedListener */

    private int mItemSelectedCount = 0;
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Android bug: http://stackoverflow.com/questions/5624825/spinner-onitemselected-executes-when-it-is-not-suppose-to
        // This fixes the issue but intro's a new problem:
        // Whenever the 0 position element is the first selected I get
        // scal.io.liger W/InputEventReceiver﹕ Attempted to finish an input event but the input event receiver has already been disposed.
        // instead of a callback here. wtf.
        //Log.i("spinner", "count " + mItemSelectedCount + " position " + position);
        mItemSelectedCount++;
        if (mItemSelectedCount > 1)
            Toast.makeText(mContext, "Selected " + (String) parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /** End Spinner OnItemSelectedListener */

    private void setNewSelectedClip(View newSelectedClip) {
        // Put the passed clipThumbnail at the top of the stack
        // The top of the stack is the end of mDisplayedClips
        Log.i("swap", String.format("Swapping card %d for %d", mDisplayedClips.indexOf(newSelectedClip), mDisplayedClips.size()-1));
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
    }

    private View.OnTouchListener mClipSelectionListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.i("select", "setting clip selected");
                    v.setBackgroundResource(R.drawable.clip_card_selected);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Log.i("select", "setting clip unselected");
                    v.setBackgroundResource(0);
                    break;
            }

            return false;
        }// onTouch()
    };
}
