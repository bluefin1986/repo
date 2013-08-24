package com.bluefin.zoombucks.labor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.bluefin.zoombucks.model.ZoomBucksAccount;

public class ZoomBucksOperator {

	public static void login(WebDriver driver, ZoomBucksAccount zaccount) throws Exception{
		System.out.println("login " + zaccount.getFullName());
		driver.manage().deleteAllCookies();
		driver.navigate().refresh();
		String baseUrl = "http://www.zoombucks.com";
		driver.get("http://www.zoombucks.com/login.php?logout");
		driver.get(baseUrl + "/");
		driver.findElement(By.linkText("Login")).click();
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys(zaccount.getFullName());
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys(zaccount.getPassword());
		driver.findElement(By.cssSelector("button.btn")).click();
		String url = driver.getCurrentUrl();
		if ("http://www.zoombucks.com/login.php".equals(url)) {
			throw new Exception(zaccount.getFullName() + "not registed yet!");
		}
		String points = driver.findElement(By.id("points")).getText();
		System.out.println(zaccount.getFullName() + " 登录成功 , points: " + points);
		zaccount.setLoggedIn(true);
	}
	
	public static void ssoToTaskSite(WebDriver driver, ZoomBucksAccount zaccount) throws InterruptedException{
		driver.get("http://www.zoombucks.com/tasks.php");
		driver.get("http://crowdflower.com/judgments/zoombucks?uid="
				+ zaccount.getFullName());
		Thread.sleep(5000);
	}
}
