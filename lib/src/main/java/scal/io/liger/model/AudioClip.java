package scal.io.liger.model;

import com.google.gson.annotations.Expose;

/**
 * Created by josh on 2/13/15.
 */
public class AudioClip {
    @Expose private String position_clip_id; // can be null.  card id we are linked to either this or the next must have a value, but only one
    @Expose private int position_index; // can null
    @Expose private boolean clip_span;  // how many clips it should try to span
    @Expose private boolean truncate; // should this play out past the clips its spans, or trim its end to match
    @Expose private boolean overlap; // if overlap the next clip or push it out, can we
    @Expose private boolean fill_repeat;  // repeat to fill if this audioclip is shorter than the clips it spans

//    public AudioClip() {}
}
