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

import android.content.pm.PackageInfo;

import com.scm.reader.livescanner.sdk.KConfig;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class SearchRequestBuilder {

    private static final String AUTHENTICATION_METHOD = "KA";
    private String mApiKey;
    private String mApiSecret;

  
  private static final String MULTIPART_BOUNDARY = "----------ThIs_Is_tHe_bouNdaRY_";
  private static final String CRLF = "\r\n";
  
  private final SearchRequestData data;
  private final PackageInfo pInfo;
  
  private int responseStatus = -1;
  /**Get the http status code of the last API request.*/
  public int getResponseStatus() {return this.responseStatus;}

  private String responseBody = null;
  /**Get the http response of the last API request.*/
  public String getResponseBody() {return this.responseBody;}

  public SearchRequestBuilder(SearchRequestData data, PackageInfo pInfo, String apiKey, String apiSecret) {
	this.pInfo = pInfo;
    this.data = data;
      mApiKey = apiKey;
      mApiSecret = apiSecret;
  }
 
  private String getUserData(){
	  String dataString = "{";
	  if(data.getLocationLatitude() != 0 && data.getLocationLongitude() != 0){
		  dataString += "\"longitude\":" + data.getLocationLongitude() + "," + "\"latitude\":" + data.getLocationLatitude() + ",";
	  }
	  dataString += "\"device_id\": " + "\"" + data.getDeviceId() + "\"";
	  dataString += ",\"application_id\":\""+parseAppId()+"\"";
	  dataString += "}";
	  System.out.println(dataString);
	  return dataString;
  }
  
  private String parseAppId(){
	  String appId ="Shortcut-Android/";
	  appId +=pInfo.versionName; 
	  appId += "(" +pInfo.versionCode + ")";
	  return appId;
  }

  public String query() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
      String response = "";
      Map<String, String> params = new HashMap<String, String>();
	  params.put("user_data", getUserData());
      response = query(params);
      return response;
  }
  
  public String query(Map<String, String> params) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
      this.responseStatus = -1;
      this.responseBody = null;
      String contentType = "multipart/form-data";

      byte[] image = data.getRequestContent();
      byte[] requestBody = createMultipartRequest(image, params);
      final String dateStr = data.getFormattedDate();

      HttpURLConnection conn = (HttpURLConnection) (new URL(getQueryUrl())).openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setDoInput(true);

      conn.setRequestProperty("Content-Type", contentType + "; boundary=" + MULTIPART_BOUNDARY);
      //KA-sign auth
      conn.setRequestProperty("Authorization", getAuthorizationHeader(AUTHENTICATION_METHOD, "POST", requestBody, contentType, dateStr, KConfig.getConfig().getPath()));
      //Token auth
      //conn.setRequestProperty("Authorization", "Token " + SECRET_TOKEN);
      
      conn.setRequestProperty("Accept", "application/json; charset=utf-8");
      conn.setRequestProperty("Date", dateStr);

      System.out.println("REQUESTBODY: " + requestBody.toString());
      conn.getOutputStream().write(requestBody);
      return readHttpResponse(conn);
  }
  
  private String readHttpResponse(HttpURLConnection conn) throws IOException {
      InputStream is = null;
      try {
          is = conn.getInputStream();
          this.responseStatus = ((HttpURLConnection)conn).getResponseCode();
      } catch (IOException e) {
          try {
              this.responseStatus = ((HttpURLConnection)conn).getResponseCode();
              is = ((HttpURLConnection)conn).getErrorStream();
          } catch(IOException ex) {
              throw ex;
          }
      }

      BufferedReader bin = new BufferedReader(new InputStreamReader(is));

      StringBuilder sb = new StringBuilder();
      String inputLine;
      while ((inputLine = bin.readLine()) != null)
          sb.append(inputLine);
      bin.close();

      this.responseBody = sb.toString();
      return this.responseBody;
  }


  private String getAuthorizationHeader(String authenticationMethod, String verb, byte[] requestBody, String contentType, String date, String queryPath) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
      String auth = null;
      if("Token".equals(authenticationMethod)) {
          auth = "Token " + mApiSecret;
      }
      else if ("KA".equals(authenticationMethod)) {
          auth = "KA " + mApiKey + ":" + sign("POST", requestBody, contentType, date, KConfig.getConfig().getPath());
      }
      return auth;
  }
  
  private byte[] createMultipartRequest(byte[] image, Map<String, String> params) throws IOException {
      ByteArrayOutputStream bodyOutputStream = new ByteArrayOutputStream();

      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String> entry : params.entrySet()) {
          sb.append("--" + MULTIPART_BOUNDARY + CRLF);
          sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + CRLF);
          sb.append(CRLF);
          sb.append(entry.getValue());
          sb.append(CRLF);
      }

      sb.append("--" + MULTIPART_BOUNDARY + CRLF);
      sb.append("Content-Disposition: form-data; name=\"image\"" + CRLF);
      sb.append("Content-Type: appliation/octet-stream" + CRLF);
      sb.append(CRLF);
      
      bodyOutputStream.write(sb.toString().getBytes());
      bodyOutputStream.write(image);
      bodyOutputStream.write((CRLF + "--" + MULTIPART_BOUNDARY + "--").getBytes());
      return bodyOutputStream.toByteArray();
  }
 
  private String sign(String verb, byte[] content, String contentType, String date, String requestPath) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException {
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] thedigest = md.digest(content);
      String md5sum = new String(Hex.encodeHex(thedigest));

      String message = verb + "\n" + md5sum + "\n" + contentType + "\n" + date + "\n" + requestPath;
      String signature = signHmacSha1(mApiSecret, message);
      return signature;
  }

  private static String signHmacSha1(String key, String message) throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException {
      SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(keySpec);
      byte[] result = mac.doFinal(message.getBytes());

      return new String(Base64.encodeBase64(result));
  }

  private String getQueryUrl() {
    KConfig config = KConfig.getConfig();
    String protocol = "KA".equals(AUTHENTICATION_METHOD) ? "http://" : "https://";
    return protocol + config.getServer() + config.getPath();
  }
}
