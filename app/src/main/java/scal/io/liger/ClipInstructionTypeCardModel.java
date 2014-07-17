package scal.io.liger;

import java.util.ArrayList;

/**
 * Created by mnbogner on 7/17/14.
 */
public class ClipInstructionTypeCardModel extends CardModel {
    public String media_path;
    public String header;
    public ArrayList<String> clip_types;

    public ClipInstructionTypeCardModel() {
        this.type = this.getClass().getName();
    }

    public String getMedia_path() {
        return media_path;
    }

    public void setMedia_path(String media_path) {
        this.media_path = media_path;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public ArrayList<String> getClip_types() {
        return clip_types;
    }

    public void setClip_types(ArrayList<String> clip_types) {
        this.clip_types = clip_types;
    }

    public void addClipType(String clip_type) {
        if (this.clip_types == null)
            this.clip_types = new ArrayList<String>();

        this.clip_types.add(clip_type);
    }
}
