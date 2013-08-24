package com.bluefin.zoombucks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.bluefin.zoombucks.model.SearchEngineTask;

public class TaskPool {

	private Map<String, SearchEngineTask> taskMap;
	
	private Map<String, SearchEngineTask> failedTaskMap = new HashMap<String, SearchEngineTask>();
	
	private Iterator<SearchEngineTask> it;
	
	public TaskPool(Map<String, SearchEngineTask> taskMap){
		this.taskMap = taskMap;
		it = this.taskMap.values().iterator();
	}
	
	public boolean isTasksFinished(){
		return this.taskMap.size() == 0;
	}
	
	public Map<String, SearchEngineTask> getFailedTaskMap(){
		return this.failedTaskMap;
	}
	
	public synchronized SearchEngineTask getSearchEngineTask(){
		if(it.hasNext()){
			SearchEngineTask task = it.next();
			it.remove();
			return task;
		}
		return null;
	}
	
	public synchronized void addFailTask(SearchEngineTask task){
		this.failedTaskMap.put(task.getTaskHref(), task);
	}
	
}
