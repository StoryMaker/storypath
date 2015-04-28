package scal.io.liger;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import scal.io.liger.model.AudioClip;
import scal.io.liger.model.Card;
import scal.io.liger.model.Dependency;
import scal.io.liger.model.MediaFile;
import scal.io.liger.model.PublishProfile;
import scal.io.liger.model.StoryPath;
import scal.io.liger.model.StoryPathLibrary;
import scal.io.liger.model.VideoCaptureTypeCard;

/**
 * Created by mnbogner on 7/10/14.
 *
 * USAGE:
 *
 * GsonBuilder gBuild = new GsonBuilder();
 * gBuild.registerTypeAdapter(StoryPathModel.class, new StoryPathDeserializer());
 * Gson gson = gBuild.create();
 *
 * String json = loadJSON("story_path_1.json");
 * StoryPathModel spm = gson.fromJson(json, StoryPathModel.class);
 *
 * System.out.println("TEST: " + gson.toJson(spm));
 */
public class StoryPathLibraryDeserializer implements JsonDeserializer<StoryPathLibrary> {
    private static final String TAG = "StoryPathLibraryDeseria";

    @Override
    public StoryPathLibrary deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        StoryPathLibrary spl = new StoryPathLibrary();
        boolean errorFlag = false;

        JsonObject jObj = jsonElement.getAsJsonObject();

        String id = jObj.get("id").getAsString();
        String title = jObj.get("title").getAsString();
        String class_package = jObj.get("classPackage").getAsString();

        spl.setId(id);
        spl.setTitle(title);
        spl.setClassPackage(class_package);

        JsonElement tempElement = null;

        tempElement = jObj.get("fileLocation");
        if (tempElement != null) {
            String fileLocation = jObj.get("fileLocation").getAsString();
            spl.setFileLocation(fileLocation);
        }

        tempElement = jObj.get("savedFileName");
        if (tempElement != null) {
            String savedFileName = jObj.get("savedFileName").getAsString();
            spl.setSavedFileName(savedFileName);
        }

        tempElement = jObj.get("currentStoryPathFile");
        if (tempElement != null) {
            String currentStoryPathFile = tempElement.getAsString();
            spl.setCurrentStoryPathFile(currentStoryPathFile);
        }

        tempElement = jObj.get("publishProfile");
        if (tempElement != null) {
            JsonObject ppObj = tempElement.getAsJsonObject();
            PublishProfile publishProfile = new PublishProfile();

            if (ppObj.has("title")) {
                publishProfile.setTitle(ppObj.get("title").getAsString());
            }

            if (ppObj.has("titlePrefix")) {
                publishProfile.setTitlePrefix(ppObj.get("titlePrefix").getAsString());
            }

            if (ppObj.has("titlePostfix")) {
                publishProfile.setTitlePostfix(ppObj.get("titlePostfix").getAsString());
            }

            if (ppObj.has("description")) {
                publishProfile.setDescription(ppObj.get("description").getAsString());
            }

            if (ppObj.has("descriptionPrefix")) {
                publishProfile.setDescriptionPrefix(ppObj.get("descriptionPrefix").getAsString());
            }

            if (ppObj.has("descriptionPostfix")) {
                publishProfile.setDescriptionPostfix(ppObj.get("descriptionPostfix").getAsString());
            }

            if (ppObj.has("tags")) {
                JsonArray jArr = ppObj.get("tags").getAsJsonArray();
                ArrayList<String> tags = new ArrayList<String>();
                for (int i = 0; i < jArr.size(); i++) {
                    tags.add(i, jArr.get(i).getAsString());
                }
                publishProfile.setTags(tags);
            }

            if (ppObj.has("uploadSiteKeys")) {
                JsonArray jArr = ppObj.get("uploadSiteKeys").getAsJsonArray();
                ArrayList<String> uploadSiteKeys = new ArrayList<String>();
                for (int i = 0; i < jArr.size(); i++) {
                    uploadSiteKeys.add(i, jArr.get(i).getAsString());
                }
                publishProfile.setUploadSiteKeys(uploadSiteKeys);
            }

            if (ppObj.has("publishSiteKeys")) {
                JsonArray jArr = ppObj.get("publishSiteKeys").getAsJsonArray();
                ArrayList<String> publishSiteKeys = new ArrayList<String>();
                for (int i = 0; i < jArr.size(); i++) {
                    publishSiteKeys.add(i, jArr.get(i).getAsString());
                }
                publishProfile.setPublishSiteKeys(publishSiteKeys);
            }
            spl.setPublishProfile(publishProfile);
        }

        // additional metadata for publishing
        tempElement = jObj.get("metaTitle");
        if (tempElement != null) {
            String metaTitle = jObj.get("metaTitle").getAsString();
            spl.setMetaTitle(metaTitle);
        }

        tempElement = jObj.get("metaDescription");
        if (tempElement != null) {
            String metaDescription = jObj.get("metaDescription").getAsString();
            spl.setMetaDescription(metaDescription);
        }

        tempElement = jObj.get("metaThumbnail");
        if (tempElement != null) {
            String metaThumbnail = jObj.get("metaThumbnail").getAsString();
            spl.setMetaThumbnail(metaThumbnail);
        }

        tempElement = jObj.get("metaSection");
        if (tempElement != null) {
            String metaSection = jObj.get("metaSection").getAsString();
            spl.setMetaSection(metaSection);
        }

        tempElement = jObj.get("metaLocation");
        if (tempElement != null) {
            String metaLocation = jObj.get("metaLocation").getAsString();
            spl.setMetaLocation(metaLocation);
        }

        tempElement = jObj.get("language");
        if (tempElement != null) {
            String language = jObj.get("language").getAsString();
            spl.setLanguage(language);
        }

        tempElement = jObj.get("version");
        if (tempElement != null) {
            int version = jObj.get("version").getAsInt();
            spl.setVersion(version);
        }

        tempElement = jObj.get("templatePath");
        if (tempElement != null) {
            String templatePath = jObj.get("templatePath").getAsString();
            spl.setTemplatePath(templatePath);
        }

        GsonBuilder gBuild = new GsonBuilder();
        gBuild.registerTypeAdapter(MilestoneCardDeserializer.class, new MilestoneCardDeserializer());
        gBuild.registerTypeAdapter(VideoCaptureTypeCard.class, new VideoCaptureTypeCardDeserializer());
        Gson gson = gBuild.excludeFieldsWithoutExposeAnnotation().create();

        JsonObject tempObj = null;

        HashMap<String, String> storyPathTemplateFiles = new HashMap<String, String>();
        tempElement = jObj.get("storyPathTemplateFiles");
        if (tempElement != null) {
            tempObj = tempElement.getAsJsonObject();
            storyPathTemplateFiles = gson.fromJson(tempObj, storyPathTemplateFiles.getClass());
            spl.setStoryPathTemplateFiles(storyPathTemplateFiles);
        }

        HashMap<String, MediaFile> mediaFiles = new HashMap<String, MediaFile>();
        tempElement = jObj.get("mediaFiles");
        if (tempElement != null) {
            tempObj = tempElement.getAsJsonObject();
            mediaFiles = gson.fromJson(tempObj, new TypeToken<HashMap<String, MediaFile>>(){}.getType());
            spl.setMediaFiles(mediaFiles);
        }

        ArrayList<AudioClip> audioClips = new ArrayList<AudioClip>();
        tempElement = jObj.get("audioClips");
        if (tempElement != null) {
            JsonArray jArr = tempElement.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                AudioClip clip = gson.fromJson(jArr.get(i), new TypeToken<AudioClip>(){}.getType());
                audioClips.add(clip);
            }
            spl.setAudioClips(audioClips);
        }

        // additional metadata for publishing
        JsonElement jEle = jObj.get("metaTags");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                String metaTag = jArr.get(i).getAsString();
                spl.addMetaTag(metaTag);
            }
        }

        jEle = jObj.get("storyPathInstanceFiles");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                String storyPathInstanceFile = jArr.get(i).getAsString();
                spl.addStoryPathInstanceFile(storyPathInstanceFile);
            }
        }

        jEle = jObj.get("dependencies");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                Dependency dependency = (gson.fromJson(arrObj, Dependency.class));
                spl.addDependency(dependency);
            }
        }

        jEle = jObj.get("cards");
        if (jEle != null) {
            JsonArray jArr = jEle.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                JsonObject arrObj = jArr.get(i).getAsJsonObject();
                String cardType = arrObj.get("type").getAsString();
                String fqCardType;
                if (cardType.contains(".")) {
                    Log.d(TAG, "StoryPathLibrary JSON contains fully qualitfied card type: " + cardType);
                    fqCardType = cardType;
                } else {
                    fqCardType = class_package + "." + cardType;
                }
                try {
                    Class cardClass = Class.forName(fqCardType);
                    Card card = (Card)(gson.fromJson(arrObj, cardClass));
                    spl.addCard(card);
                } catch (ClassNotFoundException e) {
                    System.err.println("MODEL CLASS NOT FOUND FOR CARD TYPE: " + fqCardType);
                    errorFlag = true;
                }
            }
        }

        // don't want to return incomplete models
        if (errorFlag) {
            throw new JsonParseException("encountered cards with no corresponding model");
        }

        return spl;
    }
}
