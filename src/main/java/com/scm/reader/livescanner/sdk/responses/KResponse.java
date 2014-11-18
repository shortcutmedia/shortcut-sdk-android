package com.scm.reader.livescanner.sdk.responses;

import android.util.Log;
import com.scm.reader.livescanner.sdk.KInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * KResponse class wrapping the JSON response received from the Kooaba servers. It is a response of
 * of a Kooaba recognition Query.
 */
public class KResponse implements Response{
  private static final String TAG = KResponse.class.getSimpleName();
  
  private boolean recognized = false;
  private String title = "";
  private String rawResponse;

  // stores some statistical data about the query
  private KInfo info;

  /**
   * Initializes a new KResponse object.
   */
  public KResponse() {
    this.info = new KInfo();
  }

  /**
   * The KResponse is used to wrap the JSON string received from the Kooaba recognition servers.
   * This function parses a raw JSON string and fills in the corresponding fields into the KResponse object.
   * @param rawJSONResponse a JSON string which is a kooaba response.
   * @throws JSONException if the returned result from the server was invalid JSON.
   */
  public void parse(String rawJSONResponse) throws JSONException {
    Log.d(TAG, "Raw response: " + rawJSONResponse);
    rawResponse = rawJSONResponse;

    JSONObject parsed = new JSONObject(rawJSONResponse);
    JSONArray results = parsed.getJSONArray("results");

    if (results.length() > 0) {
      recognized  = true;
      try {
        title = results.getJSONObject(0).get("reference_id").toString();
      } catch (JSONException e) {
        title = results.getJSONObject(0).get("title").toString();
      }
    } else {
      recognized  = false;
      title       = "not recognized";
    }
  }

  /**
   * Get the title of first result
   * @return the title of first recognized result as a String
   */
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * Function which returns true if image is recognized, false if image was not recognized
   * @return true if image is recognized, false if image was not recognized
   */
  @Override
  public boolean isRecognized() {
    return recognized;
  }
  
  /**
   * Function to change recognized state
   */
  public void setRecognizedState(boolean recState) {
	  this.recognized = recState;
  }

  /**
   * Returns a string representation of the response. Essentially this is the raw JSON object
   * as a string
   * @return the raw response received from server in JSON format
   */
  public String toString() {
    return rawResponse;
  }

  /**
   * Returns some statistical information as a KInfo object.
   */
  @Override
  public KInfo getInfo() {
    return info;
  }
}
