package scal.io.liger;

/**
 * Created by mnbogner on 7/14/14.
 */
public class ReferenceModel {
    public String reference; // in the form story::card::field::value

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
