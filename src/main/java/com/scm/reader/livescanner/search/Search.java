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

import android.graphics.Bitmap;

import com.scm.reader.livescanner.sdk.KConfig;

import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.ByteArrayOutputStream;

public class Search {

  private static final String OLD_URL_REGEX = "^http:\\/\\/my\\.kooaba\\.com\\/q\\/([a-zA-Z0-9]*)\\?image_id=([0-9]*)$";


  private long id;
  private String uuid; // query_uuid
  private String itemUuid;
  private String url;
  private String detail;
  private boolean recognized;
  private boolean isQrcode;
  private long selectedSearchResultId;

  protected String title;
  protected byte[] image;
  protected Date searchTime;
  private boolean pending;

  private LinkedList<SearchResultSection> sections = new LinkedList<SearchResultSection>();
  private double latitude;
  private double longitude;



  public Search() {
	  this.isQrcode = false;
  }
  

  public Search(String title, byte[] image, Date searchTime, boolean pending) {
    this.title = title;
    this.image = image;
    this.searchTime = searchTime;
    this.pending = pending;
    this.isQrcode = false;
  }

  /**
   * @deprecated used for search only
   */
  public Search(String title, String url, boolean recognized) {
    this.title = title;
    this.url = url;
    this.recognized = recognized;
    this.isQrcode = false;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getUrl() {
	  if (url != null) {

          // needed to maintain compatibility with clients which have taken pictures before
          // Shortcut 2.0. See Search.getUrl() method
		  Pattern oldUrlPattern = Pattern.compile(OLD_URL_REGEX);
		  Matcher matcher = oldUrlPattern.matcher(url);

		  if (matcher.matches()) {
			  String newUrl = KConfig.getConfig().getServer() + "/app/#/results/$1_$2";
			  return matcher.replaceFirst(newUrl);
		  } else {
			  return url;
		  }
	  } else {
		  return url;
	  }
  }

  public String getDetail() {
    return detail;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public boolean isRecognized() {
    return recognized;
  }

  public void setRecognized(boolean recognized) {
    this.recognized = recognized;
  }

  public long getSelectedSearchResultId() {
    return selectedSearchResultId;
  }

  public void setSelectedSearchResultId(long selectedSearchResultId) {
    this.selectedSearchResultId = selectedSearchResultId;
  }

  public boolean addSection(SearchResultSection section) {
    return sections.add(section);
  }

  public boolean hasSections() {
    return sections.size() > 0;
  }

  public LinkedList<SearchResultSection> getSections() {
    return sections;
  }

  public String getTitle() {
    return title;
  }

  public Date getSearchTime() {
    return searchTime;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setSearchTime(Date searchTime) {
    this.searchTime = searchTime;
  }

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public boolean isPending() {
    return pending;
  }

  public void setPending(boolean pending) {
    this.pending = pending;
  }
  
  public boolean isQrcode(){
	  return this.isQrcode;
  }
  
  public void setIsQrcode(boolean isQrcode){
	  this.isQrcode = isQrcode;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((detail == null) ? 0 : detail.hashCode());
    result = prime * result + (pending ? 1231 : 1237);
    result = prime * result + (int) (id ^ (id >>> 32));
    long temp;
    temp = Double.doubleToLongBits(latitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(longitude);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (recognized ? 1231 : 1237);
    result = prime * result + ((searchTime == null) ? 0 : searchTime.hashCode());
    result = prime * result + (int) (selectedSearchResultId ^ (selectedSearchResultId >>> 32));
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Search other = (Search) obj;
    if (detail == null) {
      if (other.detail != null)
        return false;
    } else if (!detail.equals(other.detail))
      return false;
    if (pending != other.pending)
      return false;
    if (id != other.id)
      return false;
    if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
      return false;
    if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
      return false;
    if (recognized != other.recognized)
      return false;
    if (searchTime == null) {
      if (other.searchTime != null)
        return false;
    } else if (!searchTime.equals(other.searchTime))
      return false;
    if (selectedSearchResultId != other.selectedSearchResultId)
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    if (uuid == null) {
      if (other.uuid != null)
        return false;
    } else if (!uuid.equals(other.uuid))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Search [id=" + id + ", uuid=" + uuid + ", url=" + url + ", detail=" + detail + ", recognized=" + recognized
        + ", selectedSearchResultId=" + selectedSearchResultId + ", title=" + title + ", searchTime=" + searchTime
        + ", pending=" + pending + ", latitude=" + latitude + ", longitude=" + longitude + "]";
  }
  
  public void modifyToQRSearch(Search qrSearch, Bitmap barcodeThumbnail){
	  this.setIsQrcode(true);
	  this.setTitle(qrSearch.getTitle());
	  this.setUrl(qrSearch.getUrl());
	  this.setPending(false);
	  this.setRecognized(true);
	  this.setImage(createByteArray(barcodeThumbnail));
  }
  
  public byte[] createByteArray(Bitmap barcodeBitmap){
	  ByteArrayOutputStream stream = new ByteArrayOutputStream();
	  barcodeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
	  return stream.toByteArray();
  }

    public String getItemUuid() {
        return itemUuid;
    }

    public void setItemUuid(String itemUuid) {
        this.itemUuid = itemUuid;
    }

}
