package com.bluefin.zoombucks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.bluefin.base.Task;

public class TaskPool {

	private Map<String, Task> taskMap;
	
	private Map<String, Task> failedTaskMap = new HashMap<String, Task>();
	
	private Iterator<Task> it;
	
	public TaskPool(Map<String, Task> taskMap){
		this.taskMap = taskMap;
		it = this.taskMap.values().iterator();
	}
	
	public Map<String, Task> getFailedTaskMap(){
		return this.failedTaskMap;
	}
	
	public synchronized Task getSearchEngineTask(){
		if(it.hasNext()){
			Task task = it.next();
			it.remove();
			return task;
		}
		return null;
	}
	
	public synchronized void addFailTask(Task task){
		this.failedTaskMap.put(task.getTaskHref(), task);
	}
	
}
