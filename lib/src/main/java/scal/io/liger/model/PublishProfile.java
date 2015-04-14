package scal.io.liger.model;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

/**
 * Created by josh on 4/12/15.
 */
public class PublishProfile {

    @Expose protected ArrayList<String> uploadSiteKeys;  // if defined, only allow upload to these sites
    @Expose protected ArrayList<String> publishSiteKeys; // if defined, only allow publish to these sites
    @Expose protected ArrayList<String> tags;
    @Expose protected String title;               // if this field is set, it will override any user defined title
    @Expose protected String titlePrefix;         // add this text to the start of any user provided title
    @Expose protected String titlePostfix;        // add this text to end end of any user provided title
    @Expose protected String description;         // if this field is set, it will override any user defined description
    @Expose protected String descriptionPrefix;   // add this text to the start of any user provided description
    @Expose protected String descriptionPostfix;  // add this text to end end of any user provided description

    public String getDescriptionPostfix() {
        return descriptionPostfix;
    }

    public void setDescriptionPostfix(String descriptionPostfix) {
        this.descriptionPostfix = descriptionPostfix;
    }

    public ArrayList<String> getUploadSiteKeys() {
        return uploadSiteKeys;
    }

    public void setUploadSiteKeys(ArrayList<String> uploadSiteKeys) {
        this.uploadSiteKeys = uploadSiteKeys;
    }

    public ArrayList<String> getPublishSiteKeys() {
        return publishSiteKeys;
    }

    public void setPublishSiteKeys(ArrayList<String> publishSiteKeys) {
        this.publishSiteKeys = publishSiteKeys;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitlePrefix() {
        return titlePrefix;
    }

    public void setTitlePrefix(String titlePrefix) {
        this.titlePrefix = titlePrefix;
    }

    public String getTitlePostfix() {
        return titlePostfix;
    }

    public void setTitlePostfix(String titlePostfix) {
        this.titlePostfix = titlePostfix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionPrefix() {
        return descriptionPrefix;
    }

    public void setDescriptionPrefix(String descriptionPrefix) {
        this.descriptionPrefix = descriptionPrefix;
    }
}
