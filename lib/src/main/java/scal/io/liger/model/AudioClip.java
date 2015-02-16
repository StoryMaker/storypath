package scal.io.liger.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

/**
 * Created by josh on 2/13/15.
 */
public class AudioClip implements Parcelable {
    @Expose private String position_clip_id; // can be null.  card id we are linked to either this or the next must have a value, but only one
    @Expose private int position_index; // can null
    @Expose private float volume; // 1.0 is full volume
    @Expose private boolean clip_span;  // how many clips it should try to span
    @Expose private boolean truncate; // should this play out past the clips its spans, or trim its end to match
    @Expose private boolean overlap; // if overlap the next clip or push it out, can we
    @Expose private boolean fill_repeat;  // repeat to fill if this audioclip is shorter than the clips it spans

    // TODO this is a cleaner form of parcelable: http://www.parcelabler.com/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        String[] data = new String[7];
        out.writeStringArray(new String[]{
                this.position_clip_id,
                "" + this.position_index,
                "" + this.volume,
                (this.clip_span ? "1" : "0"),
                (this.truncate ? "1" : "0"),
                (this.overlap ? "1" : "0"),
                (this.fill_repeat ? "1" : "0")
        });
    }



    public AudioClip(Parcel in) {
        String[] data = new String[7];

        in.readStringArray(data);

        this.position_clip_id = data[0];
        this.position_index = Integer.parseInt(data[1]);
        this.volume = Float.parseFloat(data[2]);
        this.clip_span = data[3].equals("1");
        this.truncate = data[4].equals("1");
        this.overlap = data[5].equals("1");
        this.fill_repeat = data[6].equals("1");
    }


    public static final Parcelable.Creator<AudioClip> CREATOR = new Parcelable.Creator<AudioClip>() {
        public AudioClip createFromParcel(Parcel in) {
            return new AudioClip(in);
        }

        public AudioClip[] newArray(int size) {
            return new AudioClip[size];
        }
    };

//    public AudioClip() {}
}
