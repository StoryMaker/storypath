package scal.io.liger;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class Utility {
    private static final String TAG = "Utility";

    public static Bitmap getFrameFromVideo(String videoPath) {
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        Bitmap videoFrame = null;
//
//        try {
//            retriever.setDataSource(videoPath);
//            videoFrame = retriever.getFrameAtTime(2000000, MediaMetadataRetriever.OPTION_CLOSEST);
//        } catch (IllegalArgumentException ex) {
//            ex.printStackTrace();
//        } catch (RuntimeException ex) {
//            ex.printStackTrace();
//        } finally {
//            try {
//                retriever.release();
//            } catch (RuntimeException ex) {
//            }
//        }
//        return videoFrame;
        return ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
    }

    public static boolean isNullOrEmpty(String s) {
        return (s == null) || (s.length() == 0);
    }

    public static void toastOnUiThread(Activity activity, String message) {
        toastOnUiThread(activity, message, false);
    }

    public static void toastOnUiThread(Activity activity, String message, final boolean isLongToast) {
        final Activity _activity = activity;
        final String _msg = message;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(_activity.getApplicationContext(), _msg, isLongToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void toastOnUiThread(FragmentActivity fragmentActivity, String message) {
        toastOnUiThread(fragmentActivity, message, false);
    }

    public static void toastOnUiThread(FragmentActivity fragmentActivity, String message, final boolean isLongToast) {
        final FragmentActivity _activity = fragmentActivity;
        final String _msg = message;
        fragmentActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(_activity.getApplicationContext(), _msg, isLongToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Trims trailing whitespace. Removes any of these characters:
     * 0009, HORIZONTAL TABULATION
     * 000A, LINE FEED
     * 000B, VERTICAL TABULATION
     * 000C, FORM FEED
     * 000D, CARRIAGE RETURN
     * 001C, FILE SEPARATOR
     * 001D, GROUP SEPARATOR
     * 001E, RECORD SEPARATOR
     * 001F, UNIT SEPARATOR
     * @return "" if source is null, otherwise string with all trailing whitespace removed
     */
    public static CharSequence trimTrailingWhitespace(CharSequence source) {

        if(source == null)
            return "";

        int i = source.length();

        // loop back to the first non-whitespace character
        while(--i >= 0 && Character.isWhitespace(source.charAt(i))) {
        }

        return source.subSequence(0, i+1);
    }

    public static String getIntentMediaType(String mediaType) {
        String intentType = null;

        if (TextUtils.equals(mediaType, Constants.PHOTO)) {
            intentType = "image/*";
        } else if (TextUtils.equals(mediaType, Constants.VIDEO)){
            intentType = "video/*";
        } else if (TextUtils.equals(mediaType, Constants.AUDIO)){
            intentType = "audio/*";
        }

        return intentType;
    }

    // https://gist.github.com/vitriolix/5c50439d49ac188c2d31
    public static @Nullable Matrix getExifTranspositionMatrix(@NonNull String src) {
        Matrix matrix = null;
        try {
            ExifInterface ei = new ExifInterface(src);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            if (orientation == 1) {
                return null;
            }

            matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matrix;
    }

    public static @NonNull Bitmap rotateBitmapForExifData(@NonNull String src, @NonNull Bitmap bitmap) {
        Matrix matrix = getExifTranspositionMatrix(src);
        if (matrix == null) {
            return bitmap;
        }
        try {
            Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return oriented;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bitmap;
        }
    }
}
