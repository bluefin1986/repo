package com.bluefin.base;

import org.openqa.selenium.WebDriver;

public abstract class AbstractLabor extends Thread{

	protected AbstractAccount account;
	
	protected WebDriver driver;
	
	public abstract void login();
}
