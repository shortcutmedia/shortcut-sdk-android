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

import com.scm.reader.livescanner.util.DateUtils;

import java.util.Date;

public class SearchRequestData {

  private final byte[] jpgImage;
  private final String formattedDate;

  private final Double locationLatitude;
  private final Double locationLongitude;

  private final String searchUrl;
  
  private final String deviceId;
  

  public SearchRequestData(byte[] jpgImage, Double latitude, Double longitude, String searchUrl, String deviceId) {
    this.jpgImage = jpgImage;
    this.locationLatitude = latitude;
    this.locationLongitude = longitude;
    this.searchUrl = searchUrl;
    this.formattedDate = DateUtils.formatDate(new Date());
    this.deviceId = deviceId;
  }

  public byte[] getRequestContent() {
    return jpgImage;
  }

//  public URI getRequestPath() {
//    String url = searchUrl != null ? searchUrl : KConfig.getConfig().getServer();
//    return NetUtils.createUri(url + "/" + PaperboyApplication.QUERY_PATH);
//  }
  
  //new
//  public String getRequestPathV4(){
//	  String url = searchUrl != null ? searchUrl : PaperboyApplication.QUERY_SERVER_URL;
//	  url += "/" + PaperboyApplication.QUERY_PATH;
//	  return url;
//  }

  public String getFormattedDate() {
    return formattedDate;
  }

  public Double getLocationLatitude() {
    return locationLatitude;
  }

  public Double getLocationLongitude() {
    return locationLongitude;
  }

  public String getDeviceId(){
	  return this.deviceId;
  }
}
