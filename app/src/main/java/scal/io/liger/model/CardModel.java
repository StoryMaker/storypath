package scal.io.liger.model;

import android.content.Context;

import com.fima.cardsui.objects.Card;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by mnbogner on 7/10/14.
 */
public abstract class CardModel {
    public String type;
    public String id;
    public String title;
    public StoryPathModel storyPathReference;
    public ArrayList<String> references;
    public ArrayList<String> values;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() { return title;}

    public void setTitle(String title) {
        this.title = title;
    }

    public StoryPathModel getStoryPathReference() {
        return storyPathReference;
    }


    public abstract Card getCardView(Context context);

    public void setStoryPathReference(StoryPathModel storyPathReference) {
        this.storyPathReference = storyPathReference;
    }

    public ArrayList<String> getReferences() {
        return references;
    }

    public void setReferences(ArrayList<String> references) {
        this.references = references;
    }

    public void addReference(String reference) {
        if (this.references == null)
            this.references = new ArrayList<String>();

        this.references.add(reference);
    }

    public boolean checkReferencedValues() {
        boolean result = true;

        if (references != null) {
            for (String reference : references) {
                // assumes the format story::card::field::value
                String[] pathParts = reference.split("::");
                String referencedValue = storyPathReference.getReferencedValue(reference);
                if (pathParts.length == 3) {
                    // just check that the value is not null
                    if (referencedValue == null) { // FIXME this cold be simplified
                        result = false;
                    }
                } else {
                    if ((referencedValue == null) || (!referencedValue.equals(pathParts[3]))) {
                        result = false;
                    }
                }
            }
        }

        return result;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void clearValues() {
        values = null;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public void addValue(String value) {
        addValue(value, true);
    }

    public void addValue(String value, boolean notify) {
        if (this.values == null)
            this.values = new ArrayList<String>();

        this.values.add(value);

        if (notify) {
            // send notification that a value has been saved so that cards can re-check references
            if (storyPathReference != null) {
                storyPathReference.notifyActivity();
            } else {
                System.err.println("STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
            }
        }
    }

    public String getValueByKey(String key) {
        if (values != null) {
            for (String value : values) {
                String[] valueParts = value.split("::");
                if (valueParts[0].equals(key)) {
                    return valueParts[1];
                }
            }
        }

        // check class properties if no saved value was found
        Class c = null;
        Field f = null;
        try {
            c = this.getClass();
            f = c.getField(key);
            return f.get(this).toString(); // not the best solution, but somehow int fields come back with Integer values
        } catch (Exception e) {
            System.err.println("EXCEPTION THROWN WHILE SEARCHING CLASS PROPERTIES FOR VALUE: " + e.getMessage());
        }

        System.err.println("VALUE " + key + " WAS NOT FOUND");
        return null;
    }

    /**
     * get's a value.
     * @param fullPath accepts is a FQID
     * @return
     */
    public String getValueById (String fullPath) {
        // assumes the format story::card::field
        String[] pathParts = fullPath.split("::");
        //FIXME this should also work for length 3!

         if (pathParts.length == 4 || pathParts.length == 3) {
            // sanity check
            if (!this.id.equals(pathParts[1])) {
                System.err.println("CARD ID " + pathParts[1] + " DOES NOT MATCH");
                return null;
            }

            return getValueByKey(pathParts[2]);
        }

        return null;
    }
}
