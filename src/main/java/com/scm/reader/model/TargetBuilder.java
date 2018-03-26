package com.scm.reader.model;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


/**
 * Created by franco on 21/06/16.
 */
public class TargetBuilder {

    private static final String VUFORIA_QUERY_RESPONSE_TARGET_ID_KEY = "target_id";
    private static final String VUFORIA_QUERY_RESPONSE_TARGET_DATA_KEY = "target_data";
    private static final String VUFORIA_QUERY_RESPONSE_TARGET_METADATA = "application_metadata";

    private static final String METADATA_VERSION = "1";

    private static final String VUFORIA_METADATA_VERSION_KEY = "version";
    private static final String VUFORIA_METADATA_KIND_KEY = "kind";
    private static final String VUFORIA_METADATA_TITLE_KEY = "title";
    private static final String VUFORIA_METADATA_SUBTITLE_KEY = "subtitle";
    private static final String VUFORIA_METADATA_UUID_KEY = "uuid";
    private static final String VUFORIA_METADATA_THUMBNAIL_KEY = "thumbnail_url";
    private static final String VUFORIA_METADATA_RESPONSE_KEY = "response";
    private static final String VUFORIA_METADATA_RESPONSE_TARGET_KEY = "target";
    private static final String VUFORIA_METADATA_RESPONSE_CONTENT_KEY = "content";


    public static Target fromVuforiaJson(JSONObject jsonTarget) throws JSONException {
        Target target = new Target();

        target.name = jsonTarget.getString(VUFORIA_QUERY_RESPONSE_TARGET_ID_KEY);

        JSONObject targetDataJSON = jsonTarget.getJSONObject(VUFORIA_QUERY_RESPONSE_TARGET_DATA_KEY);


        target.metadata = new String(
                Base64.decode(
                        targetDataJSON.getString(VUFORIA_QUERY_RESPONSE_TARGET_METADATA),
                        Base64.DEFAULT));


        JSONArray targetMetadataArray = new JSONArray(target.metadata);

        JSONObject targetMetadata = null;

        // Get the relevant version.
        for (int i=0; i < targetMetadataArray.length(); i++) {
            targetMetadata = targetMetadataArray.getJSONObject(i);

            if (METADATA_VERSION.equals( targetMetadata.getString(VUFORIA_METADATA_VERSION_KEY) )) {
                // Correct version found. Do not look further.
                break;
            }
        }

        if (targetMetadata != null) {
            switch (targetMetadata.getString(VUFORIA_METADATA_KIND_KEY)) {
                case Target.KIND_AD:
                    target.kind = Target.KIND_AD;
                    break;
                case Target.KIND_PUBLICATION:
                    target.kind = Target.KIND_PUBLICATION;
                    break;
                default:
                    throw new JSONException("kind not found in json");
            }
        }

        target.title = extractFromLanguageString(targetMetadata.getJSONObject(VUFORIA_METADATA_TITLE_KEY));
        target.subtitle = extractFromLanguageString(targetMetadata.getJSONObject(VUFORIA_METADATA_SUBTITLE_KEY));
        target.uuid = targetMetadata.getString(VUFORIA_METADATA_UUID_KEY);
        target.thumbnailUrl = targetMetadata.getString(VUFORIA_METADATA_THUMBNAIL_KEY);

        JSONObject responseJSON = targetMetadata.getJSONObject(VUFORIA_METADATA_RESPONSE_KEY);
        switch (responseJSON.getString(VUFORIA_METADATA_RESPONSE_TARGET_KEY)) {
            case "result_page":
                target.responseTarget = Target.RESULT_PAGE_TARGET;
                break;
            case "web":
                target.responseTarget = Target.WEB_TARGET;
                break;
        }

        target.responseContent = responseJSON.getString(VUFORIA_METADATA_RESPONSE_CONTENT_KEY);
        return target;
    }


    private static String extractFromLanguageString(JSONObject langObj) throws JSONException {
        String userLang = Locale.getDefault().getLanguage();
        String fallbackLang = "en";
        return langObj.has(userLang) ? langObj.getString(userLang) : langObj.getString(fallbackLang);
    }

}
