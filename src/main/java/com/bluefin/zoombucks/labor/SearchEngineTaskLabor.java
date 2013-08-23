package com.bluefin.zoombucks.labor;

import java.util.Map;

import org.openqa.selenium.WebDriver;

import com.bluefin.zoombucks.model.SearchEngineTask;

public class SearchEngineTaskLabor extends Thread{
	
	private WebDriver driver;
	
	private Map<String, SearchEngineTask> taskMap;
	
	public SearchEngineTaskLabor(WebDriver driver, Map<String, SearchEngineTask> taskMap){
		this.driver = driver;
		this.taskMap = taskMap;
	}
	
	public void run(){
		
	}
}
