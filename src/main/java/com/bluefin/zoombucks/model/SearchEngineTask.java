package com.bluefin.zoombucks.model;

public class SearchEngineTask implements Comparable<SearchEngineTask>{

	private String taskDesc;
	
	private String taskHref;
	
	private int bonus;
	
	public SearchEngineTask(String taskDesc, String taskHref, int bonus){
		this.taskDesc = taskDesc;
		this.taskHref = taskHref;
		this.bonus = bonus;
	}

	public String getTaskDesc() {
		return taskDesc;
	}

	public void setTaskDesc(String taskDesc) {
		this.taskDesc = taskDesc;
	}

	public String getTaskHref() {
		return taskHref;
	}

	public void setTaskHref(String taskHref) {
		this.taskHref = taskHref;
	}

	public int getBonus() {
		return bonus;
	}

	public void setBonus(int bonus) {
		this.bonus = bonus;
	}

	@Override
	public int compareTo(SearchEngineTask o) {
		if(o.getBonus() > this.getBonus()){
			return 1;
		} else if(o.getBonus() < this.getBonus()){
			return -1;
		}
		return 0;
	}
	
	
}
