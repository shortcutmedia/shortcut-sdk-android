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
import android.util.Log;

import com.scm.reader.livescanner.sdk.KConfig;
import com.scm.reader.livescanner.util.LogUtils;
import com.scm.shortcutreadersdk.R;

import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;


public class ImageRecognizer {

    public static final String TAG = ImageRecognizer.class.getSimpleName();

    private static final String API_KEY_NAME = "com.shortcutmedia.shortcut.sdk.API_KEY";
    private static final String API_SECRET_NAME = "com.shortcutmedia.shortcut.sdk.API_SECRET";
	
  private static final String SHORTCUT_SERVER = "http://shortcut-service.shortcutmedia.com";
  private HttpPost searchRequest;
  private Object searchRequestLock = new Object();

  private boolean cancelled;

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
        Log.e(TAG, "Failed to load API_KEY or SECRET from meta-data, NameNotFound: " + e.getMessage());
        throw new RuntimeException("API_KEY or SECRET not found.");
      } catch (NullPointerException e) {
        Log.e(TAG, "Failed to load API_KEY or SECRET from meta-data, NullPointer: " + e.getMessage());
        throw new RuntimeException("API_KEY or SECRET not found.");
      }

      SearchRequestBuilder requestBuilderV4 =
              new SearchRequestBuilder(new SearchRequestData(
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
    	
    	Map<String, String> params = new HashMap<String, String>();
    	params.put("location:", dataToPopulate.getLatitude() + ", " + dataToPopulate.getLongitude());
        response = requestBuilderV4.query();
    	
	} catch (InvalidKeyException e1) {
		e1.printStackTrace();
	} catch (NoSuchAlgorithmException e1) {
		e1.printStackTrace();
	} catch (IOException e) {
      LogUtils.logWarn("IO exception while sending HTTP request and parsing response", e);
      throw e;
    }

    // Print the response to console
    System.out.println("HTTP Status: " + requestBuilderV4.getResponseStatus());
    System.out.println("HTTP Response: " + requestBuilderV4.getResponseBody());
    
    Search result = null;
	try {
		result = parseJSON(context, response, dataToPopulate);

	} catch (JSONException e) {
		e.printStackTrace();
	}
    return result;
    
  }
  
  private String getDeviceId(Context context){
	  String deviceId = Secure.getString(context.getContentResolver(),
              Secure.ANDROID_ID); 
	  return deviceId;
  }
  
  private PackageInfo getPackageInfo(Context context){
	  PackageInfo pInfo = null;
	  try {
		pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	} catch (NameNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return pInfo;
  }
  
  //parsing resultUrl string
  public String createResultUrl(String uuid, String recId){
	  String url = "";
	  url += SHORTCUT_SERVER + "/app#/results/";
	  url += uuid;
	  url += "_";
	  url += modifySH1String(recId);
	  
	  return url;
  }
  
  //modifying SH1 string for url
  public String modifySH1String(String shaString){
	  shaString = shaString.replace("image.sha1:", "");
	  return shaString;
  }
  

  private String extractFromLanguageString(JSONObject langObj) throws JSONException {
	  String userLang = Locale.getDefault().getLanguage();
	  String fallbackLang = "en";
	  return langObj.has(userLang) ? langObj.getString(userLang) : langObj.getString(fallbackLang);  
  }
  

  /**
   * @param context
   * @param data
   * @param dataToPopulate
   * @return
   * @throws IOException
   * @throws JSONException
   * @return Search object
   * 
   * TODO refactor and make this method more readable if time
   */
 public Search parseJSON(Context context, String data, Search dataToPopulate) throws IOException, JSONException{
	dataToPopulate.setSearchTime(new Date());
	dataToPopulate.setPending(false);
	JSONObject jObject;
	 	
	jObject = new JSONObject(data);
	
	dataToPopulate.setUuid(jObject.getString("query_id"));
	
	JSONArray resultArray = (JSONArray)jObject.get("results");
	
	//no results
	if(resultArray.length() == 0){
		dataToPopulate.setRecognized(false);
        dataToPopulate.setTitle(context.getResources().getString(R.string.shortcut_sdk_image_not_recognized_title));
        dataToPopulate.setUrl("http://");
	}
	
	//only one item result
	else if (resultArray.length() < 2){
		System.out.println("only one item result");
		JSONObject result = resultArray.getJSONObject(0);
		JSONArray recognitions = result.getJSONArray("recognitions");

		dataToPopulate.setRecognized(true);
        //dataToPopulate.setTitle(result.getString("title"));
		
        JSONArray metaDataArray = result.getJSONArray("metadata");
        JSONObject metaDataObj = metaDataArray.getJSONObject(0);
        JSONObject recognition = recognitions.getJSONObject(0);
        
        //setting right language
        String title = extractFromLanguageString(metaDataObj.getJSONObject("title"));
        dataToPopulate.setTitle(title);
        
        String subtitle = extractFromLanguageString(metaDataObj.getJSONObject("subtitle"));
        dataToPopulate.setDetail(subtitle);
        dataToPopulate.setItemUuid(metaDataObj.getString("uuid"));

        String url = "";
        if(metaDataObj.has("response")){
        	System.out.println("direct response");
        	JSONObject responseObject = metaDataObj.getJSONObject("response");
        	url = responseObject.getString("content");
            LogUtils.logDebug("response qurl " + url);
            System.out.println(url);
        }
        else {
        	System.out.println("normal response");
            url = createResultUrl(metaDataObj.getString("uuid"), recognition.getString("id"));
            LogUtils.logDebug("normal response response qurl " + url);

        }
        dataToPopulate.setUrl(url);
	}
	//multiple results
	else{
		//reading objects inside "results" node
		
		//inserting ads first
		for(int i = 0; i < resultArray.length(); i++){
			JSONObject result = resultArray.getJSONObject(i);
			JSONArray metaDataArray = result.getJSONArray("metadata");
			JSONObject metaDataObj = metaDataArray.getJSONObject(0);

			boolean hasKind = false;
			for(int ind = 0; ind < metaDataArray.length(); ind++)	{
				if(metaDataArray.getJSONObject(ind).has("kind")){
					hasKind = true;
					break;
				}
			}







			if(hasKind){
				if(!dataToPopulate.hasSections() && 
						metaDataObj.getString("kind").equals("Ad")){
					dataToPopulate.addSection(parseSectionJSON(resultArray, result));
				}
			}
		}
		
		//inserting everything else than ads
		
		for(int i = 0; i < resultArray.length(); i++){
			JSONObject result = resultArray.getJSONObject(i);
			
			JSONArray metaDataArray = result.getJSONArray("metadata");
			JSONObject metaDataObj = metaDataArray.getJSONObject(0);
			
			dataToPopulate.setTitle(context.getResources().getString(R.string.shortcut_sdk_multiple_matches));
				if(dataToPopulate.hasSections()){
					//checking that there is no two of same headers

					/**
					 * If any of the sections contains same header the insert boolean
					 * will be set false and loop is determined
					 */
					boolean insert = false;
					for(int index = 0; index < dataToPopulate.getSections().size(); index++){
						if(!metaDataObj.has("kind")){
							break;
						}
						SearchResultSection section = dataToPopulate.getSections().get(index);
						if(!metaDataObj.getString("kind").equals(section.getHeader())){
							insert = true;
						}else {
							insert = false;
							break;
						}
					}
					if(insert){
						dataToPopulate.addSection(parseSectionJSON(resultArray, result));
					}
				
			}else{
				dataToPopulate.addSection(parseSectionJSON(resultArray, result));
			}
		
		}

		dataToPopulate.setRecognized(true);
	}
	//TODO check here
	if (!dataToPopulate.hasSections()) {
	      if (dataToPopulate.getUrl() == null || dataToPopulate.getUrl().length() == 0) {
	        throw new JSONException("url not found");
	      }
	      if (dataToPopulate.getTitle() == null || dataToPopulate.getTitle().length() == 0) {
	    	  System.out.println(dataToPopulate.getTitle());
	        throw new JSONException("title not found");
	      }
	}
    else {
    	// If we have a section with a single object, set title to the item's title
    	// This is to avoid bug KOOABA-20
    	LinkedList<SearchResultSection> sections = dataToPopulate.getSections();
    	if (sections.size() == 1) {
    		LinkedList<SearchResultItem> items = sections.getFirst().getItems();
    		if (items.size() == 1) {
    			dataToPopulate.setTitle(items.getFirst().getTitle());
    		}
    	}
   }
   return dataToPopulate;
 }
 
 public SearchResultSection parseSectionJSON(JSONArray resultArray, JSONObject sResult) throws IOException, JSONException{
	 SearchResultSection section = new SearchResultSection();
	 JSONArray metaDataArray = sResult.getJSONArray("metadata");
	 JSONObject metaDataObj = metaDataArray.getJSONObject(0);
	 
	 //header is kind in metaData object
	if(metaDataObj.has("kind")){
		 String header = metaDataObj.getString("kind");
		 section.setHeader(header);
		 
		 for(int i=0; i < resultArray.length(); i++){
			 JSONObject result = resultArray.getJSONObject(i);
			 JSONArray metaArray = result.getJSONArray("metadata");
			 JSONObject meta = metaArray.getJSONObject(0);
			 
			 if(header.equals(meta.getString("kind"))){
				 section.addItem(parseSectionItemJSON(result));
			 }
		 }
	}else{
		//have to set some header even if we wouldn't show it
 		section.setHeader("");
 		for(int i=0; i < resultArray.length(); i++){
			 JSONObject result = resultArray.getJSONObject(i);
			 
			 section.addItem(parseSectionItemJSON(result));
		 }
 	}
	 return section;
 }
 
 
 public SearchResultItem parseSectionItemJSON(JSONObject result) throws IOException, JSONException{
	SearchResultItem item = new SearchResultItem();
		
	if(result != null){
		item.setTitle(result.getString("title"));
		JSONArray recognitions = result.getJSONArray("recognitions");
		JSONArray metaDataArray = result.getJSONArray("metadata");
		JSONObject metaDataObj = metaDataArray.getJSONObject(0);
		JSONObject recognition = recognitions.getJSONObject(0);
		
		item.setImageId(recognition.getString("id"));
		String url = createResultUrl(metaDataObj.getString("uuid"), recognition.getString("id"));

        //set up language settings
        String title = extractFromLanguageString(metaDataObj.getJSONObject("title"));
        item.setTitle(title);
        
        String subtitle = extractFromLanguageString(metaDataObj.getJSONObject("subtitle"));
        item.setDetail(subtitle);

        item.setItemUuid(metaDataObj.getString("uuid"));
      	item.setImageUrl(metaDataObj.getString("thumbnail_url"));

        if(metaDataObj.has("response")){
            System.out.println("direct response");
            JSONObject responseObject = metaDataObj.getJSONObject("response");
            url = responseObject.getString("content");
            LogUtils.logDebug("response qurl " + url);
            System.out.println(url);
        }
        else {
            System.out.println("normal response");
        }

        item.setResultUrl(url);





    }
	 return item;
 }

  public void cancelRequest() {
    synchronized (searchRequestLock) {
      cancelled = true;
      if (searchRequest != null) {
        searchRequest.abort();
      }
    }
  }

  public boolean isCancelled() {
    return cancelled;
  }
}
