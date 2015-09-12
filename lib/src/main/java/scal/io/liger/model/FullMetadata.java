package scal.io.liger.model;

import android.media.MediaMetadataRetriever;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import scal.io.liger.Constants;
import scal.io.liger.view.Util;

/**
 * @author Matt Bogner
 * @author Josh Steiner
 */
public class FullMetadata implements Parcelable {
    private static final String TAG = "FullMetadata";

    // TODO this is a cleaner form of parcelable: http://www.parcelabler.com/

    // class used for export, not serialized

    private int startTime;
    private int stopTime;
    private int duration; // duration in millisecond
    private float volume;
    private String effect;
    private String type;
    private String medium;
    @NonNull private String filePath;

    public FullMetadata (ClipMetadata cm, MediaFile mf) {
        this.startTime = cm.getStartTime();
        this.stopTime = cm.getStopTime();
        this.volume = cm.getVolume();
        this.effect = cm.getEffect();
        this.type = cm.getType();
        this.medium = mf.getMedium();
        this.filePath = mf.getPath();

        this.duration = calculateDuration();
    }

    public FullMetadata(Parcel in) {
        String[] data = new String[7];

        in.readStringArray(data);

        this.startTime = Integer.parseInt(data[0]);
        this.stopTime = Integer.parseInt(data[1]);
        this.volume = Float.parseFloat(data[2]);
        this.effect = data[3];
        this.type = data[4];
        this.medium = data[5];
        this.filePath = data[6];

        this.duration = calculateDuration();
    }

    private int calculateDuration () {
        if (medium.equals(Constants.VIDEO) || medium.equals(Constants.AUDIO)) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            String time = null;
            try {
                retriever.setDataSource(filePath);
                time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            } catch (RuntimeException re) {
                Log.e(TAG, "MediaMetadataRetriever cannot deal with " + filePath + " -> " + re.getMessage());
            }
            long timeMs = 0;
            if (time != null) {
                timeMs = Long.parseLong(time);
            }
            return Util.safeLongToInt(timeMs);
        } else {
            return 0;
        }
    }

    public static final Parcelable.Creator<FullMetadata> CREATOR
            = new Parcelable.Creator<FullMetadata>() {
        public FullMetadata createFromParcel(Parcel in) {
            return new FullMetadata(in);
        }

        public FullMetadata[] newArray(int size) {
            return new FullMetadata[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        String[] data = new String[7];
        out.writeStringArray(new String[]{
                "" + this.startTime,
                "" + this.stopTime,
                "" + this.volume,
                this.effect,
                this.type,
                this.medium,
                this.filePath
        });
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getStopTime() {
        return stopTime;
    }

    public void setStopTime(int stopTime) {
        this.stopTime = stopTime;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int volume) {
        this.duration = duration;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
