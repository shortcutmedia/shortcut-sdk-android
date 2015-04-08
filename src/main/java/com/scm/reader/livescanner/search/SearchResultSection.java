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

import java.util.LinkedList;

public class SearchResultSection {

  private long id;
  private long searchResultId;
  private String header;

  private LinkedList<SearchResultItem> items;
  
  public SearchResultSection() {
    items = new LinkedList<SearchResultItem>();
  }
  
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
  
  public long getSearchResultId() {
    return searchResultId;
  }

  public void setSearchResultId(long searchResultId) {
    this.searchResultId = searchResultId;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public boolean addItem(SearchResultItem item) {
    return items.add(item);
  }

  public LinkedList<SearchResultItem> getItems() {
    return items;
  }
  
  @Override
  public int hashCode() {
    int headerHash = header != null ? header.hashCode() : 1;
    int itemsHash = items.hashCode();

    return (int) (id + searchResultId + headerHash + itemsHash);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SearchResultSection) {
      SearchResultSection other = (SearchResultSection) o;

      return id == other.id &&
        searchResultId == other.searchResultId &&
        Utils.nullOrEqual(header, other.header) &&
        items.equals(other.items);
    }
    return false;
  }

}
