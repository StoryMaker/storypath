package scal.io.liger;

import java.util.ArrayList;

/**
 * Created by mnbogner on 7/10/14.
 */
public class ClipTypeCardModel extends CardModel {
    public ArrayList<Object> clipTypes;
    public ValueModel value;

    public ClipTypeCardModel() {
        this.type = this.getClass().getName();
    }

    public ArrayList<Object> getClipTypes() {
        return clipTypes;
    }

    public void setClipTypes(ArrayList<Object> clipTypes) {
        this.clipTypes = clipTypes;
    }

    public void addClipType(Object clipType) {
        if (this.clipTypes == null)
            this.clipTypes = new ArrayList<Object>();

        this.clipTypes.add(clipType);
    }

    public ValueModel getValue() {
        return value;
    }

    public void setValue(ValueModel value) {
        this.value = value;
    }
}
