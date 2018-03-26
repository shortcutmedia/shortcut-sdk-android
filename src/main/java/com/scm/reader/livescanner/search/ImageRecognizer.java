/*
 * Copyright 2015 Shortcut Media AG.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.scm.reader.livescanner.search;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.annotation.StringDef;
import android.util.Log;

import com.scm.reader.livescanner.sdk.KConfig;
import com.scm.reader.livescanner.util.LogUtils;
import com.scm.reader.model.Target;
import com.scm.reader.model.TargetBuilder;
import com.scm.shortcutreadersdk.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ImageRecognizer {

    public static final String TAG = ImageRecognizer.class.getSimpleName();

    private static final String API_KEY_NAME = "com.shortcutmedia.shortcut.sdk.API_KEY";
    private static final String API_SECRET_NAME = "com.shortcutmedia.shortcut.sdk.API_SECRET";

    private static final String RESULT_CODE_SUCCESSS = "Success";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({RESULT_CODE_SUCCESSS})
    private @interface ResultCode {
    }

    private static final String VUFORIA_QUERY_ID = "query_id";
    private static final String VUFORIA_QUERY_RESULT_CODE = "result_code";
    private static final String VUFORIA_QUERY_RESULTS = "results";


    private static final String SHORTCUT_SERVER = "http://shortcut-service.shortcutmedia.com";


    public Search query(final Context context, Search dataToPopulate) throws IOException {

        String searchUrl = KConfig.getConfig().getServer();

        LogUtils.logDebug("qurl " + searchUrl);

        String apiKey = null;
        String apiSecret = null;

        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            apiKey = bundle.getString(API_KEY_NAME);
            apiSecret = bundle.getString(API_SECRET_NAME);

        } catch (NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        VuforiaSearchBuilderRequest requestBuilder = new VuforiaSearchBuilderRequest(
                new SearchRequestData(
                        dataToPopulate.getImage(),
                        dataToPopulate.getLatitude(),
                        dataToPopulate.getLongitude(),
                        searchUrl,
                        getDeviceId(context)),
                getPackageInfo(context),
                apiKey,
                apiSecret);


        String response = "";

        try {
            response = requestBuilder.query();

        } catch (InvalidKeyException | NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            LogUtils.logWarn("IO exception while sending HTTP request and parsing response", e);
            throw e;
        }

        Log.d(TAG, "HTTP Status: " + requestBuilder.getResponseStatus());
        Log.d(TAG, "HTTP Response: " + requestBuilder.getResponseBody());

        Search result = null;
        try {
            result = parseJSON(context, response, dataToPopulate);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;

    }

    private String getDeviceId(Context context) {
        return Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
    }

    private PackageInfo getPackageInfo(Context context) {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pInfo;
    }


    /**
     * @param context        Context
     * @param data           Data from query response
     * @param dataToPopulate Data to populate object
     * @return Search object
     * <p>
     * TODO catch JSONException and log it to firebase
     * <p>
     * - If two or more targets have the same metadata uuid pick the best (first) result. This
     * emulates the behaviour of the legacy kooaba system where a kooaba item could have
     * multiple images associated.
     * - If targets are of mixed kind and one kind is an Ad it makes sure that Ads are displayed
     * first.
     * - If result contains only one item immediately open the result
     * @throws IOException
     * @throws JSONException
     */
    public Search parseJSON(Context context, String data, Search dataToPopulate) throws IOException, JSONException {

        JSONObject jObject = new JSONObject(data);

        dataToPopulate.setSearchTime(new Date());
        dataToPopulate.setPending(false);
        dataToPopulate.setUuid(jObject.getString(VUFORIA_QUERY_ID));

        @ResultCode String resultCode = jObject.getString(VUFORIA_QUERY_RESULT_CODE);

        if (RESULT_CODE_SUCCESSS.equals(resultCode)) {

            JSONArray results = jObject.getJSONArray(VUFORIA_QUERY_RESULTS);

            if (results.length() > 0) {
                dataToPopulate.setRecognized(true);
                dataToPopulate.setTitle(context.getResources().getString(R.string.shortcut_sdk_multiple_matches));

                // LinkedHasMap preserves insertion order so when iterated Ad will be always first
                Map<String, List<Target>> groupedTargets = new LinkedHashMap<>();

                // Prepopulate map with kind 'Ad'. This way items of kind 'Ad' are always shown
                // first.
                groupedTargets.put(Target.KIND_AD, new ArrayList<Target>());

                Set<String> processedUUIDs = new HashSet<>();

                // group results into sections
                for (int i = 0; i < results.length(); i++) {
                    Target target = TargetBuilder.fromVuforiaJson(results.getJSONObject(i));

                    // Query results are sorted by matching confidence (highest to lowest). If
                    // multiple results have the same uuid we use just the best matching result.
                    // This heuristic emulates the former behaviour where an item could have
                    // multiple images assigned which is not the case with vuforia currently.

                    if (processedUUIDs.contains(target.uuid)) {
                        continue;
                    } else {
                        processedUUIDs.add(target.uuid);
                    }

                    if (!groupedTargets.containsKey(target.kind)) {
                        groupedTargets.put(target.kind, new ArrayList<Target>());
                    }
                    groupedTargets.get(target.kind).add(target);
                }

                for (Map.Entry<String, List<Target>> entry : groupedTargets.entrySet()) {

                    List<Target> targets = entry.getValue();

                    if (!targets.isEmpty()) {

                        SearchResultSection srSection = new SearchResultSection();
                        srSection.setHeader(entry.getKey());

                        for (Target target : targets) {
                            SearchResultItem srItem = new SearchResultItem();
                            srItem.setTitle(target.title);
                            srItem.setDetail(target.subtitle);
                            srItem.setItemUuid(target.uuid);
                            srItem.setImageUrl(target.thumbnailUrl);
                            //                     srItem.setImageId(... ); // mandatory?
                            String url;
                            if (Target.RESULT_PAGE_TARGET == target.responseTarget) {
                                url = createResultUrl(target.uuid, target.responseContent);
                                srItem.setImageId(target.responseContent);
                            } else {
                                url = target.responseContent;
                            }
                            srItem.setResultUrl(url);
                            srSection.addItem(srItem);
                        }
                        dataToPopulate.addSection(srSection);
                    }
                }
            } else {
                // no match
                dataToPopulate.setRecognized(false);
                dataToPopulate.setTitle(context.getResources().getString(R.string.shortcut_sdk_image_not_recognized_title));
                dataToPopulate.setUrl("http://");
            }
        }

        return dataToPopulate;
    }

    //parsing resultUrl string
    private String createResultUrl(String uuid, String recId) {
        String url = "";
        url += SHORTCUT_SERVER + "/app#/results/";
        url += uuid;
        url += "_";
        url += modifySH1String(recId);

        return url;
    }

    //modifying SH1 string for url
    private String modifySH1String(String shaString) {
        shaString = shaString.replace("image.sha1:", "");
        return shaString;
    }

}
