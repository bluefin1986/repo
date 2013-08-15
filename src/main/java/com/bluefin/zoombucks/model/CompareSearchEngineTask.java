package com.bluefin.zoombucks.model;

import java.util.Comparator;

public class CompareSearchEngineTask implements Comparator<SearchEngineTask> {

	@Override
	public int compare(SearchEngineTask o1, SearchEngineTask o2) {
		
		return o1.compareTo(o2);
	}

}
