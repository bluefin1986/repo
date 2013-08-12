package com.bluefin.qianbao.sign;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class QianbaoCheckin {

	public static void checkin(WebDriver driver, String baseUrl){
		driver.get(baseUrl + "/cas/qianbaoLogin?service=http%3A%2F%2Fwww.qianbao666.com%2Fj_spring_cas_security_check");
	    driver.findElement(By.id("username")).clear();
	    driver.findElement(By.id("username")).sendKeys("13859089698");
	    driver.findElement(By.id("password")).clear();
	    driver.findElement(By.id("password")).sendKeys("baoziazhu609");
	    driver.findElement(By.id("captcha")).clear();
	    driver.findElement(By.id("captcha")).sendKeys("ywqs");
	    driver.findElement(By.name("submit")).click();
	    driver.findElement(By.id("userSign")).click();
	    driver.findElement(By.cssSelector("a")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [waitForPopUp |  | 30000]]
	    driver.findElement(By.xpath("//div[@id='container']/div/div/div/div[2]/ul/li[2]/div/a")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [waitForPopUp |  | 30000]]
	    driver.findElement(By.xpath("//div[@id='container']/div/div/div/div[2]/ul/li[3]/div/a")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [waitForPopUp |  | 30000]]
	    driver.findElement(By.id("nextstepBtn")).click();
	    driver.findElement(By.cssSelector("span.btn-help.btn-help-two")).click();
	    driver.findElement(By.id("step")).click();
	    driver.findElement(By.id("step")).click();
	    driver.findElement(By.id("step")).click();
	    driver.findElement(By.id("step")).click();
	    driver.findElement(By.cssSelector("a")).click();
	    driver.findElement(By.id("step")).click();
	    driver.findElement(By.id("step")).click();
	    driver.findElement(By.id("step")).click();
	    driver.findElement(By.id("step")).click();
	    driver.findElement(By.id("step")).click();
	    driver.findElement(By.cssSelector("div.help-three")).click();
	    driver.findElement(By.id("subMitBtn")).click();
	    driver.findElement(By.linkText("确定")).click();
	}
}
