package com.bluefin.zoombucks.model;

import java.util.Comparator;

import com.bluefin.base.Task;

public class CompareSearchEngineTask implements Comparator<Task> {

	@Override
	public int compare(Task o1, Task o2) {
		
		return o1.compareTo(o2);
	}

}
