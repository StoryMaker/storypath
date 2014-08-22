package scal.io.liger;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;


public class Utility {

    public static Bitmap getFrameFromVideo(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Bitmap videoFrame = null;

        try {
            retriever.setDataSource(videoPath);
            videoFrame = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }

        return videoFrame;
    }
}
