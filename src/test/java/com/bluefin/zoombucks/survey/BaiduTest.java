package com.bluefin.zoombucks.survey;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.bluefin.WebDriverFactory;

public class BaiduTest {
	
//	@Test
	public void testRemoveInput(){
		WebDriver driver = WebDriverFactory.generateFirefoxDriver();
		driver.get("http://www.baidu.com");
		WebElement input = driver.findElement(By.id("kw"));
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].parentNode.removeChild(arguments[0])", input);
	}
	
	@Test
	public void testMultiWindow(){
		WebDriver driver = WebDriverFactory.generateFirefoxDriver();
		driver.get("http://www.baidu.com");
		String windowHandleOrigin = driver.getWindowHandle();
		for (int i = 0; i < 5; i++) {
			driver.findElement(By.tagName("body")).sendKeys(Keys.chord(Keys.COMMAND, "n"));
		}
		for (String windowHandle : driver.getWindowHandles()) {
			if(windowHandle.equals(windowHandleOrigin)){
				continue;
			}
//			WebDriver driverNewTab = driver.switchTo().window(windowHandle);
			System.out.println(windowHandle);
			new Redirector(driver, "http://www.google.com", windowHandle).start();
		}
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		for (String windowHandle : driver.getWindowHandles()) {
//			if(windowHandle.equals(windowHandleOrigin)){
//				driver.switchTo().window(windowHandle).quit();
//				break;
//			}
//			WebDriver driverNewTab = driver.switchTo().window(windowHandle);
//			driverNewTab.close();
//		}
	}
	
	private class Redirector extends Thread{
		
		private WebDriver driver;
		
		private String url;
		
		private String windowHandle;
		
		public Redirector(WebDriver driver, String url, String windowHandle){
			this.driver = driver;
			this.url = url;
			this.windowHandle = windowHandle;
		}
		
		public void run(){
			driver.switchTo().window(windowHandle).get(url);
			driver.switchTo().window(windowHandle).get("http://baidu.com");
		}
	}
}
