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
