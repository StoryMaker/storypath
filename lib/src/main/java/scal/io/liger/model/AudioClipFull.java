package scal.io.liger.model;

import timber.log.Timber;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

/**
 * Created by josh on 2/13/15.
 *
 * this is a copy of AudioClip.java that flattens the MediaFile object down to a simple path so we can pass it out to through the intent to the parent app which knows nothing of MediaFiles... yeah, not so pretty but it works
 */
public class AudioClipFull implements Parcelable {
    @Expose private String path; // path media file
    @Expose private String positionClipId; // can be null if unused.  card id we are linked to either this or the next must have a value, but only one
    @Expose private int positionIndex; // can be -1 if unused.
    @Expose private float volume; // 1.0 is full volume
    @Expose private int clipSpan;  // how many clips it should try to span
    @Expose private boolean truncate; // should this play out past the clips its spans, or trim its end to match
    @Expose private boolean overlap; // if overlap the next clip or push it out, can we
    @Expose private boolean fillRepeat;  // repeat to fill if this audioclip is shorter than the clips it spans

    public AudioClipFull(String path, String positionClipId, int positionIndex, float volume, int clipSpan, boolean truncate, boolean overlap, boolean fillRepeat) {
        this.path = path;
        this.positionClipId = positionClipId;
        this.positionIndex = positionIndex;
        this.volume = volume;
        this.clipSpan = clipSpan;
        this.truncate = truncate;
        this.overlap = overlap;
        this.fillRepeat = fillRepeat;
    }

    public AudioClipFull(StoryPathLibrary spl, AudioClip ac) {
        this(spl.getMediaFile(ac.getUuid()).getPath(), ac.getPositionClipId(), ac.getPositionIndex(), ac.getVolume(), ac.getClipSpan(), ac.doTruncate(), ac.doOverlap(), ac.doFillRepeat());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return whether this audio should repeat to fill clips it spans.
     * as defined by {@link #getClipSpan()} and {@link #getPositionClipId()} or {@link #getPositionIndex()}
     */
    public boolean doFillRepeat() {
        return fillRepeat;
    }

    public void setFillRepeat(boolean fillRepeat) {
        this.fillRepeat = fillRepeat;
    }

    /**
     * @return whether this audio should be allowed to overlap with adjacent clips.
     * Used if {@link #doTruncate()} is false.
     * e.g: If this audio extends beyond the clips it spans, should the audio
     * continue against no video / photo, or be mixed into the next Clip.
     */
    public boolean doOverlap() {
        return overlap;
    }

    public void setOverlap(boolean overlap) {
        this.overlap = overlap;
    }

    /**
     * @return whether this audio should be truncated
     * to have duration no longer than the clips it spans.
     */
    public boolean doTruncate() {
        return truncate;
    }

    public void setTruncate(boolean truncate) {
        this.truncate = truncate;
    }

    /**
     * @return how many ClipCards this audio should span, assuming
     * it's length allows
     */
    public int getClipSpan() {
        return clipSpan;
    }

    public void setClipSpan(int newSpan) {
        clipSpan = newSpan;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * @return the index of the starting ClipCard within the StoryPath's ClipCards.
     * For a convenience method to quickly find the first ClipCard see
     * {@link StoryPathLibrary#getFirstClipCardForAudioClip(scal.io.liger.model.AudioClipFull, java.util.List)}
     */
    public int getPositionIndex() {
        return positionIndex;
    }

    /**
     * Assign a new value for the starting ClipCard position. This will
     * unset any value passed to {@link #setPositionClipId(String)}
     */
    public void setPositionIndex(int newIndex) {
        positionIndex = newIndex;
        positionClipId = null;
    }

    /**
     * @return The id identifying the starting ClipCard in a StoryPathLibrary
     * For a convenience method to quickly find the first ClipCard see
     * {@link StoryPathLibrary#getFirstClipCardForAudioClip(scal.io.liger.model.AudioClipFull, java.util.List)}
     */
    public String getPositionClipId() {
        return positionClipId;
    }

    /**
     * Assign a new value for the starting ClipCard Id. This will
     * unset any value passed to {@link #setPositionIndex(int)}
     */
    public void setPositionClipId(String newClipId) {
        positionClipId = newClipId;
        positionIndex= -1;
    }

    // TODO this is a cleaner form of parcelable: http://www.parcelabler.com/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        String[] data = new String[8];
        out.writeStringArray(new String[]{
                this.path,
                this.positionClipId,
                "" + this.positionIndex,
                "" + this.volume,
                "" + this.clipSpan,
                (this.truncate ? "1" : "0"),
                (this.overlap ? "1" : "0"),
                (this.fillRepeat ? "1" : "0")
        });
    }

    public AudioClipFull(Parcel in) {
        String[] data = new String[8];

        in.readStringArray(data);
        this.path = data[0];
        this.positionClipId = data[1];
        this.positionIndex = Integer.parseInt(data[2]);
        this.volume = Float.parseFloat(data[3]);
        this.clipSpan = Integer.parseInt(data[4]);
        this.truncate = data[5].equals("1");
        this.overlap = data[6].equals("1");
        this.fillRepeat = data[7].equals("1");
    }


    public static final Creator<AudioClipFull> CREATOR = new Creator<AudioClipFull>() {
        public AudioClipFull createFromParcel(Parcel in) {
            return new AudioClipFull(in);
        }

        public AudioClipFull[] newArray(int size) {
            return new AudioClipFull[size];
        }
    };

//    public AudioClip() {}
}
