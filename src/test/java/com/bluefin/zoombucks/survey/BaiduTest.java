package com.bluefin.zoombucks.survey;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.bluefin.WebDriverFactory;

public class BaiduTest {
	
	@Test
	public void testRemoveInput(){
		WebDriver driver = WebDriverFactory.generateFirefoxDriver();
		driver.get("http://www.baidu.com");
		WebElement input = driver.findElement(By.id("kw"));
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].parentNode.removeChild(arguments[0])", input);
	}
}
