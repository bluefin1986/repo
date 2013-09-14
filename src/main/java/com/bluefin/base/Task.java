package com.bluefin.base;

public class Task implements Comparable<Task>{

	private String taskDesc;
	
	private String taskHref;
	
	private float bonus;
	
	public Task(String taskDesc, String taskHref, float bonus){
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

	public float getBonus() {
		return bonus;
	}

	public void setBonus(float bonus) {
		this.bonus = bonus;
	}

	@Override
	public int compareTo(Task o) {
		if(o.getBonus() > this.getBonus()){
			return 1;
		} else if(o.getBonus() < this.getBonus()){
			return -1;
		}
		return 0;
	}
	
	public boolean equals(Object obj){
		Task task = (Task)obj;
		
		return this.getTaskHref().equals(task.getTaskHref());
	}
	
}
