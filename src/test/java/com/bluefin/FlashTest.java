package com.bluefin;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FlashTest {

	@Test
	public void testFlash(){
		String url = "http://v.youku.com/v_show/id_XNjAxODk4NzQ0_ev_2.html";
		
		WebDriver driver = WebDriverFactory.generateFirefoxDriver();
		
		driver.get(url);
		
		JavascriptExecutor jsExecutor = (JavascriptExecutor)driver;
		
		WebElement moviePlayer = driver.findElement(By.id("movie_player"));
		jsExecutor.executeScript("Play", moviePlayer);
		
		jsExecutor.executeScript("StopPlay", moviePlayer);
	}
}
