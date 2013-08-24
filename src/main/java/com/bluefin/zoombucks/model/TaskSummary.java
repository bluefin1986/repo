package com.bluefin.zoombucks.model;


public class TaskSummary {

	private int taskCount;
	
	private int totalEarned = 0;
	
	private int progressCount = 0;
	
	private int failCount = 0;
	
	public TaskSummary(int taskCount){
		this.taskCount = taskCount;
	}
	
	public int getTaskCount() {
		return taskCount;
	}
	
	public int getTotalEarned() {
		return totalEarned;
	}
	
	public int getProgressCount() {
		return progressCount;
	}
	
	public int getFailCount() {
		return failCount;
	}
	
	public int getRestTaskCount(){
		return taskCount - progressCount;
	}
	
	
	public synchronized void plusEarned(int bonus){
		this.totalEarned += bonus;
	}
	
	public synchronized void plusFailed(){
		this.failCount++;
	}
	
	public synchronized void taskPassed(){
		this.progressCount++;
	}
}
