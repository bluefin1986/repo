package com.bluefin.base;

import org.openqa.selenium.WebDriver;

public interface Operator<T extends AbstractAccount> {
	
	public void login(WebDriver driver, T account) throws Exception;
}
