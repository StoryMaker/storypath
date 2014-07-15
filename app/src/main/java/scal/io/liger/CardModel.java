package scal.io.liger;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by mnbogner on 7/10/14.
 */
public class CardModel {
    public String type;
    public String id;
    public String title;

    public StoryPathModel storyPathReference;
    public ArrayList<ReferenceModel> references;

    public ArrayList<String> values;

    public CardModel(){

    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<ReferenceModel> getReferences() {
        return references;
    }

    public void setReferences(ArrayList<ReferenceModel> references) {
        this.references = references;
    }

    public void addReference(ReferenceModel reference) {
        if (this.references == null)
            this.references = new ArrayList<ReferenceModel>();

        this.references.add(reference);
    }

    public StoryPathModel getStoryPathReference() {
        return storyPathReference;
    }

    public void setStoryPathReference(StoryPathModel storyPathReference) {
        this.storyPathReference = storyPathReference;
    }

    public boolean checkReferencedValues() {
        boolean result = true;

        for (ReferenceModel rm : references) {
            String[] pathParts = rm.getReference().split("::");
            if (storyPathReference.getReferencedValue(rm.getReference()).equals(pathParts[3])) {
                System.out.println(rm.getReference() + " MATCHES " + storyPathReference.getReferencedValue(rm.getReference()));
            } else {
                System.out.println(rm.getReference() + " DOESN'T MATCH " + storyPathReference.getReferencedValue(rm.getReference()));
                result = false;
            }
        }

        // what status should be returned if a value is specified and no value is found?
        return result;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public void addValue(String value) {
        if (this.values == null)
            this.values = new ArrayList<String>();

        this.values.add(value);
    }

    public String getValueById (String fullPath) {
        String[] pathParts = fullPath.split("::");

        // sanity check
        if (!this.id.equals(pathParts[1]))
        {
            System.out.println("WRONG CARD ID " + this.id + " vs. " + pathParts[1]);
            return null;
        }

        // assumes the format story::card::field::value
        for (String value : values)
        {
            String[] valueParts = value.split("::");
            if (valueParts[0].equals(pathParts[2]))
            {
                System.out.println("FOUND VALUE " + valueParts[0] + " -> " + valueParts[1]);
                return valueParts[1];
            }
        }

        System.out.println("VALUE NOT FOUND FOR " + pathParts[2]);
        return null;
    }
}
