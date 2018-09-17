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

import com.scm.reader.livescanner.util.Utils;

import java.util.ArrayList;

public class SearchResultItem {

    private long id;
    private long searchResultSectionId;
    private String title;
    private String detail;
    private String detail2;
    private boolean hidden;
    private String imageId;
    private String imageUrl;
    private String resultUrl;
    private String itemUuid;
    private ArrayList<Integer> oids = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSearchResultSectionId() {
        return searchResultSectionId;
    }

    public void setSearchResultSectionId(long searchResultSectionId) {
        this.searchResultSectionId = searchResultSectionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getDetail2() {
        return detail2;
    }

    public void setDetail2(String detail2) {
        this.detail2 = detail2;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getResultUrl() {
        return resultUrl;
    }

    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }

    public String getItemUuid() {
        return itemUuid;
    }

    public void setItemUuid(String itemUuid) {
        this.itemUuid = itemUuid;
    }

    @Override
    public int hashCode() {
        int titleHash = title != null ? title.hashCode() : 1;
        int detailHash = detail != null ? detail.hashCode() : 1;
        int detail2Hash = detail2 != null ? detail2.hashCode() : 1;
        int imageIdHash = imageId != null ? imageId.hashCode() : 1;
        int imageUrlHash = imageUrl != null ? imageUrl.hashCode() : 1;
        int resultUrlHash = resultUrl != null ? resultUrl.hashCode() : 1;

        return (int) (id + searchResultSectionId + titleHash +
                detailHash + detail2Hash + imageIdHash + imageUrlHash + resultUrlHash);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SearchResultItem) {
            SearchResultItem other = (SearchResultItem) o;

            return id == other.id &&
                    searchResultSectionId == other.searchResultSectionId &&
                    Utils.nullOrEqual(title, other.title) &&
                    Utils.nullOrEqual(detail, other.detail) &&
                    Utils.nullOrEqual(detail2, other.detail2) &&
                    hidden == other.hidden &&
                    Utils.nullOrEqual(imageId, other.imageId) &&
                    Utils.nullOrEqual(imageUrl, other.imageUrl) &&
                    Utils.nullOrEqual(resultUrl, other.resultUrl);
        }
        return false;
    }

    public ArrayList<Integer> getOids() {
        return oids;
    }

    public void setOids(ArrayList<Integer> oids) {
        this.oids = oids;
    }
}
