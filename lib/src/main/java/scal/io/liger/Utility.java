package scal.io.liger;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;


public class Utility {

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

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
