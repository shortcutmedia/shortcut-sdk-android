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
