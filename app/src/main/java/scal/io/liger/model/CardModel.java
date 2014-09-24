package scal.io.liger.model;

import android.content.Context;
import android.util.Log;

import com.fima.cardsui.objects.Card;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import scal.io.liger.Constants;

/**
 * Created by mnbogner on 7/10/14.
 */
public abstract class CardModel {
    public String type;
    public String id;
    public String title;
    public StoryPathModel storyPathReference;
    public ArrayList<String> references;
    public HashMap<String, String> values;

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

                if ((referencedValue != null) && (referencedValue.equals(Constants.EXTERNAL))) {
                    referencedValue = storyPathReference.getExternalReferencedValue(reference);
                }

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

    public void clearValues() {
        values = null;
    }

    public void addValue(String key, String value) {
        addValue(key, value, true);
    }

    public void addValue(String key, String value, boolean notify) {
        if (this.values == null)
            this.values = new HashMap<String, String>();

        this.values.put(key, value);

        if (notify) {
            // send notification that a value has been saved so that cards can re-check references
            if (storyPathReference != null) {
                storyPathReference.notifyActivity();
            } else {
                System.err.println("STORY PATH REFERENCE NOT FOUND, CANNOT SEND NOTIFICATION");
            }
        }
    }

    public boolean isKey(String key) {
        if (values != null) {
            if (values.keySet().contains(key)) {
                return true;
            }
        }

        return false;
    }

    public boolean isField(String key) {
        Class c = null;
        Field f = null;
        try {
            c = this.getClass();
            f = c.getField(key);
            return true;
        } catch (Exception e) {
            // field not found
        }

        return false;
    }

    public String getValueByKey(String key) {
        if (values != null) {
            if (values.keySet().contains(key)) {
                return values.get(key);
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
            //Log.d("TESTING", "EXCEPTION THROWN WHILE SEARCHING CLASS PROPERTIES FOR VALUE: " + e.getMessage());
        }

        //Log.e("TESTING", "VALUE " + key + " WAS NOT FOUND");
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

    public String fillReferences(String originalString) {
        if (originalString == null) {
            return originalString;
        }

        String newString = originalString;

        Pattern p = Pattern.compile("\\{\\{(.+?)\\}\\}");
        Matcher m = p.matcher(newString);

        while (m.find()) {
            String referenceString = m.group(1);
            if (referenceString != null) {
                referenceString = referenceString.trim();
                String referenceValue = storyPathReference.getReferencedValue(referenceString);

                if ((referenceValue != null) && (referenceValue.equals(Constants.EXTERNAL))) {
                    referenceValue = storyPathReference.getExternalReferencedValue(referenceString);
                }

                // doing a replace with a null seems to cause issues
                if (referenceValue == null)
                    referenceValue = "";
                newString = m.replaceFirst(referenceValue);
                m = p.matcher(newString);
            }
        }

        return newString;
    }
}
