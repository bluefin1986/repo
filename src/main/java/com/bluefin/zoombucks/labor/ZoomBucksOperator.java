package com.bluefin.zoombucks.labor;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.bluefin.base.Operator;
import com.bluefin.zoombucks.model.ZoomBucksAccount;

public class ZoomBucksOperator implements Operator<ZoomBucksAccount> {

	public void login(WebDriver driver, ZoomBucksAccount zaccount)
			throws Exception {
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
		System.out
				.println(zaccount.getFullName() + " 登录成功 , points: " + points);
		zaccount.setLoggedIn(true);
	}

	public void ssoToTaskSite(WebDriver driver, ZoomBucksAccount zaccount)
			throws InterruptedException {
		driver.get("http://www.zoombucks.com/tasks.php");
		driver.get("http://crowdflower.com/judgments/zoombucks?uid="
				+ zaccount.getFullName());
		Thread.sleep(5000);
	}

	public void signInToTaskSite(WebDriver driver, ZoomBucksAccount zaccount)
			throws Exception {
		ssoToTaskSite(driver,zaccount);
		driver.get("https://tasks.crowdflower.com/auth_central/login/new");
		try {
			driver.findElement(By.id("auth_central_login_username")).sendKeys(zaccount.getEmail());
			driver.findElement(By.id("auth_central_login_password")).sendKeys(
					zaccount.getPassword());
			driver.findElement(
					By.xpath("//form[@action='https://tasks.crowdflower.com/auth_central/login']"))
					.submit();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("已经登录crownflower");
			return;
		}
		try {
			boolean needLoginToHotMail = false;
			WebElement alertContent = driver.findElement(By
					.xpath("/html/body/div[2]/div/div/p"));
			if ("Login failed. Username or password is incorrect.".equals(alertContent.getText()) ) {
				driver.findElement(By.id("auth_central_account_name"))
						.sendKeys(zaccount.getFullName());
				driver.findElement(By.id("auth_central_account_email"))
						.sendKeys(zaccount.getEmail());
				driver.findElement(By.id("auth_central_account_password"))
						.sendKeys(zaccount.getPassword());
				driver.findElement(
						By.id("auth_central_account_password_confirmation"))
						.sendKeys(zaccount.getPassword());
				driver.findElement(By.id("auth_central_account_accepts_terms"))
						.click();
				driver.findElement(By.id("new_auth_central_account")).submit();
				needLoginToHotMail = true;
			}
			if("You must confirm your account before continuing. Check your email for the confirmation link.".equals(alertContent.getText())){
				needLoginToHotMail = true;
			}
			if(needLoginToHotMail){
				loginIntoHotmail(driver, zaccount);
				while (true) {
					try {
						Thread.sleep(10000);
						List<WebElement> trashLinks = driver.findElements(By
								.xpath("//a[contains(@title,'垃圾邮件')]"));
						for (WebElement trashLink : trashLinks) {
							if(trashLink.isDisplayed() && trashLink.getAttribute("title").startsWith("垃圾邮件")){
								trashLink.click();
							}
						}
						break;
					} catch (Exception e) {
						Thread.sleep(1000);
						e.printStackTrace();
						continue;
					}
				}
				WebElement messageListContainer = driver.findElement(By
						.id("messageListContentContainer"));
				List<WebElement> messages = messageListContainer
						.findElements(By.xpath("div/ul/li"));
				for (WebElement msg : messages) {
					WebElement sender = msg.findElement(
							By.cssSelector("span.Lt")).findElement(
							By.xpath("span/div/a/span"));
					WebElement title = msg.findElement(
							By.cssSelector("span.Sb")).findElement(
							By.tagName("a"));
					if ("support@crowdflower.com".equals(sender
							.getAttribute("email"))) {
						if ("Confirmation instructions‏"
								.equals(title.getText())) {
							while (true) {
								try {
									title.click();
									WebElement msgBody = driver.findElement(By
											.id("mpf0_readMsgBodyContainer"));
									WebElement confirmAnchr = msgBody
											.findElement(By.tagName("a"));
									driver.get(confirmAnchr
											.getAttribute("href"));
								} catch (Exception e) {
									System.err.println(e.getMessage());
									continue;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("已经注册过sunflower");
		}

	}
	
	public void loginIntoHotmail(WebDriver driver, ZoomBucksAccount zaccount){
		driver.get("https://login.live.com/login.srf?wa=wsignin1.0&ct=1378536452&rver=6.1.6206.0&sa=1&ntprob=-1&wp=MBI_SSL_SHARED&wreply=https:%2F%2Fmail.live.com%2F%3Fowa%3D1%26owasuffix%3Dowa%252f&id=64855&snsc=1&cbcxt=mail");
		driver.findElement(By.xpath("//input[@type='email']"))
				.sendKeys(zaccount.getEmail());
		driver.findElement(By.xpath("//input[@type='password']"))
				.sendKeys(zaccount.getPassword());
		driver.findElement(By.id("idSIButton9")).click();
	}

	public void ssoToSurveySite(WebDriver driver, ZoomBucksAccount zaccount)
			throws InterruptedException {
		driver.get("http://surveys.zoombucks.com/dashboard.php");
		Thread.sleep(5000);
	}
}
