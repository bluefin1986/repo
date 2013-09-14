package com.bluefin.qianbao.sign;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.safari.SafariDriver;

public class CheckinTest {

	private List<WebDriver> driverList;

	private String[] accounts = { "13859089698", "18677773586", "13860623747", "13075924899" };
	private String baseUrl;

	@Before
	public void init() {
		driverList = new ArrayList<WebDriver>();
		for (int i = 0; i < accounts.length; i++) {
//			driverList.add(new FirefoxDriver());
//			driverList.add(new InternetExplorerDriver());
			driverList.add(new SafariDriver());
//			driverList.add(new ChromeDriver());
		}
	}

	@Test
	public void checkinTest() {
		
		for (int i = 0; i < driverList.size(); i++) {
			final WebDriver driver = driverList.get(i);
			final String account = accounts[i];
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						checkin(account,driver);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void checkin(String account, WebDriver driver) throws InterruptedException{
		// 保持住初始的窗口句柄
		String currentWindow = driver.getWindowHandle();
		// driver = new SafariDriver();
		baseUrl = "http://www.qianwang365.com/cas/qianbaoLogin?service=http%3A%2F%2Fwww.qianbao666.com%2Fj_spring_cas_security_check";
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.get(baseUrl);
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys(account);
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("baoziazhu609");
		JOptionPane.showMessageDialog(null, account + " 输入一下验证码！");
		while (JOptionPane.showConfirmDialog(null, account + " 登录成功没？", "",
				JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			JOptionPane.showMessageDialog(null, account + " 输入一下验证码！");
			continue;
		}
		driver.get("http://www.qianbao666.com/account/toSignStart.html");
		Thread.sleep(1000);
		driver.findElement(
				By.xpath("/html/body/div[2]/div/div/div/div[2]/ul/li/div"))
				.click();
//		driver.findElement(
//				By.xpath("/html/body/div[2]/div/div/div/div[2]/ul/li/div"))
//				.click();
		Thread.sleep(1000);
		driver.findElement(
				By.xpath("/html/body/div[2]/div/div/div/div[2]/ul/li[2]/div"))
				.click();
		// driver.findElement(By.xpath("/html/body/div[2]/div/div/div/div[2]/ul/li[2]/div")).click();
		Thread.sleep(1000);
		driver.findElement(
				By.xpath("/html/body/div[2]/div/div/div/div[2]/ul/li[3]/div"))
				.click();
		try {
			driver.findElement(
					By.xpath("/html/body/div[2]/div/div/div/div[3]/ul/li/div/a"))
					.click();
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			driver.findElement(
					By.xpath("/html/body/div[2]/div/div/div/div[3]/ul/li[2]/div/a"))
					.click();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		Thread.sleep(1000);
		// new CloseWindows(driver).start();
		Set<String> windows = driver.getWindowHandles();
		for (String windowHandle : windows) {
			if (currentWindow.equals(windowHandle)) {
				continue;
			}
			WebDriver window = driver.switchTo().window(windowHandle);
			window.close();
		}
		driver = driver.switchTo().window(currentWindow);
		driver.findElement(By.id("nextstepBtn")).click();
		Thread.sleep(2000);
		WebElement element = driver.findElement(By
				.cssSelector("span.btn-help.btn-help-two"));
		element.click();
		Thread.sleep(500);
		for (int i = 0; i < 11; i++) {
			driver.findElement(By.id("step")).click();
			Thread.sleep(11000);
			String url = driver.getCurrentUrl();
			String fileName = getFileName(url);
			int pageIndex = 0;
			try {
				pageIndex = Integer.parseInt(fileName.replace("toSignNew", ""));
				System.out.println(account + "当前步骤：" + pageIndex);
				if (pageIndex != i + 1) {
					try {
						element = driver.findElement(By
								.cssSelector("span.btn-help.btn-help-two"));
						if (element != null && element.isDisplayed()) {
							element.click();
						}
					} catch (Exception ex) {
					}
					i--;
					continue;
				}
			} catch (NumberFormatException ex) {
				String errUrl = driver.getCurrentUrl();
				System.out.println("exception occoured current url:" + errUrl);
				String errFileName = getFileName(errUrl);
				if (!"toSignEnd".equals(errFileName)) {
					throw ex;
				}
			}
		}
		driver.findElement(By.cssSelector("div.help-three")).click();
		JOptionPane.showMessageDialog(null, "拼图时间到！");
	}

	private class CloseWindows extends Thread {

		private WebDriver driver;

		public CloseWindows(WebDriver driver) {
			this.driver = driver;
		}

		public void run() {
			String currentWindow = driver.getWindowHandle();
			Set<String> windows = driver.getWindowHandles();
			for (String windowHandle : windows) {
				if (currentWindow.equals(windowHandle)) {
					continue;
				}
				WebDriver window = driver.switchTo().window(windowHandle);
				window.close();
			}
		}
	}

	private String getFileName(String url) {
		return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
	}
}
