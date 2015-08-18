package com.kickmogu.yodobashi.community.resource.domain;

import java.util.ArrayList;
import java.util.List;

public class SearchResult<T> extends com.kickmogu.lib.core.domain.SearchResult<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -434865733094505081L;

	private boolean hasAdult=false;

	private long stopContentsCount;
	
	private String sku;

	public T first;

	public T last;

	public SearchResult(com.kickmogu.lib.core.domain.SearchResult<T> searchResult) {
		super(searchResult.getNumFound(), searchResult.getDocuments());
	}

	public SearchResult(long numFound, List<T> documents) {
		super(numFound, documents);
	}

	public SearchResult() {
		super(0L, new ArrayList<T>());
	}

	/**
	 * @return the hasAdult
	 */
	public boolean isHasAdult() {
		return hasAdult;
	}

	/**
	 * @param hasAdult the hasAdult to set
	 */
	public void setHasAdult(boolean hasAdult) {
		this.hasAdult = hasAdult;
	}
	
	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return hasStop
	 */
	public boolean isHasStop() {
		return stopContentsCount > 0;
	}

	public void countUpStopContents() {
		stopContentsCount++;
	}

	/**
	 * @return first
	 */
	public T getFirst() {
		if (first == null) {
			return null;
		} else {
			if (getDocuments() == null || getDocuments().isEmpty() || first != getDocuments().get(0)) {
				return first;
			} else {
				return null;
			}
		}
	}

	/**
	 * @return last
	 */
	public T getLast() {
		if (last == null) {
			return null;
		} else {
			if (getDocuments() == null || getDocuments().isEmpty() ||  last != getDocuments().get(getDocuments().size() - 1)) {
				return last;
			} else {
				return null;
			}
		}
	}

	public void updateFirstAndLast(T instance) {
		if (first == null) {
			first = instance;
		}
		last = instance;
	}

}
