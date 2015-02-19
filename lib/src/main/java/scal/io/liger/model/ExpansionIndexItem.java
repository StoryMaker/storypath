package scal.io.liger.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mnbogner on 11/24/14.
 */
public class ExpansionIndexItem extends BaseIndexItem  {

    // required
    String packageName;
    String expansionId;
    String patchOrder;
    String expansionFileName;
    String expansionFileVersion;
    String expansionFilePath; // relative to Environment.getExternalStorageDirectory()
    String expansionFileUrl;
    // String expansionThumbnail;

    // optional
    String author;
    // String title;
    // String description;
    String website;
    String date;
    ArrayList<String> languages;
    ArrayList<String> tags;
    HashMap<String, String> extras;

    public ExpansionIndexItem() {

    }

    public ExpansionIndexItem(String packageName, String expansionId, String patchOrder, String expansionFileName, String expansionFileVersion, String expansionFilePath, String expansionFileUrl, String expansionThumbnail) {
        this.packageName = packageName;
        this.expansionId = expansionId;
        this.patchOrder = patchOrder;
        this.expansionFileName = expansionFileName;
        this.expansionFileVersion = expansionFileVersion;
        this.expansionFilePath = expansionFilePath;
        this.expansionFileUrl = expansionFileUrl;
        this.thumbnailPath = expansionThumbnail;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getExpansionId() {
        return expansionId;
    }

    public void setExpansionId(String expansionId) {
        this.expansionId = expansionId;
    }

    public String getPatchOrder() {
        return patchOrder;
    }

    public void setPatchOrder(String patchOrder) {
        this.patchOrder = patchOrder;
    }

    public String getExpansionFileName() {
        return expansionFileName;
    }

    public void setExpansionFileName(String expansionFileName) {
        this.expansionFileName = expansionFileName;
    }

    public String getExpansionFileVersion() {
        return expansionFileVersion;
    }

    public void setExpansionFileVersion(String expansionFileVersion) {
        this.expansionFileVersion = expansionFileVersion;
    }

    public String getExpansionFilePath() {
        return expansionFilePath;
    }

    public void setExpansionFilePath(String expansionFilePath) {
        this.expansionFilePath = expansionFilePath;
    }

    public String getExpansionFileUrl() {
        return expansionFileUrl;
    }

    public void setExpansionFileUrl(String expansionFileUrl) {
        this.expansionFileUrl = expansionFileUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<String> getLanguages() {
        return languages;
    }

    public void setLanguages(ArrayList<String> languages) {
        this.languages = languages;
    }

    public void addLanguage(String language) {
        if (this.languages == null) {
            this.languages = new ArrayList<String>();
        }

        this.languages.add(language);
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<String>();
        }

        this.tags.add(tag);
    }

    public HashMap<String, String> getExtras() {
        return extras;
    }

    public void setExtras(HashMap<String, String> extras) {
        this.extras = extras;
    }

    public void addExtra(String key, String value) {
        if (this.extras == null) {
            this.extras = new HashMap<String, String>();
        }

        this.extras.put(key, value);
    }
}