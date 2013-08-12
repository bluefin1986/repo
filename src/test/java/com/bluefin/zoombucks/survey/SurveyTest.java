package com.bluefin.zoombucks.survey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.Select;

import com.bluefin.zoombucks.model.ProfileSurvey;
import com.bluefin.zoombucks.model.SearchEngineTask;

public class SurveyTest {

	private List<WebDriver> driverList;

	private String[] accounts = { "gingor" };
	private String baseUrl;

	@Before
	public void init() throws IOException {
		String PROXY = "127.0.0.1:9087";
		driverList = new ArrayList<WebDriver>();
		for (int i = 0; i < accounts.length; i++) {
			// driverList.add(new InternetExplorerDriver());
			org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
			proxy.setHttpProxy(PROXY)
			     .setFtpProxy(PROXY)
			     .setSslProxy(PROXY);
			DesiredCapabilities cap = new DesiredCapabilities();
			cap.setCapability(CapabilityType.PROXY, proxy);
			//selenium 专用profile
			File profilePath = new File("/Users/bluefin8603/Library/Application Support/Firefox/Profiles/pzodczhc.selenium");
			FirefoxProfile fp = new FirefoxProfile(profilePath);
//			fp.addExtension(proxyExtension);
			FirefoxDriver firefoxDriver = new FirefoxDriver(fp);
//			FirefoxDriver firefoxDriver = new FirefoxDriver(cap);
			firefoxDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
			driverList.add(firefoxDriver);
//			driverList.add(new SafariDriver(cap));
		}
	}
	
	@Test
	public void testZoombuckSurvey() throws Exception{
		for (int i = 0; i < driverList.size(); i++) {
			final WebDriver driver = driverList.get(i);
			final String account = accounts[i];
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
//					try {
			
					login(account, driver);
//					testZoomBucksTask(account,driver);
					//surveys
						testZoomBucksSurvey(account,driver);
//			testWatchVideo(account, driver);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					} catch (Exception ex){
//						ex.printStackTrace();
//					}
//				}
//			}).start();
		}
//		while (true) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}
	
	private void login(String account, WebDriver driver){
		baseUrl = "http://www.zoombucks.com";
		driver.get("http://www.zoombucks.com/login.php?logout");
		driver.get(baseUrl + "/");
		driver.findElement(By.linkText("Login")).click();
		driver.findElement(By.id("username")).clear();
		driver.findElement(By.id("username")).sendKeys(account);
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys("baoziazhu609");
		driver.findElement(By.cssSelector("button.btn")).click();
		System.out.println(account + "登录成功");
	}
	
	
	public void testWatchVideo(String account, WebDriver driver){
		login(account, driver);
		driver.get(baseUrl + "/includes/video_homepage.php?reward=2%20ZBucks");
		System.out.println("gogogo");
	}
	
	public void testZoomBucksTask(String account, WebDriver driver) throws Exception{
		driver.get("http://www.zoombucks.com/tasks.php");
//		driver.switchTo().frame(0);
		driver.get("http://crowdflower.com/judgments/zoombucks?uid=" + account);
		Thread.sleep(5000);
		WebElement taskListTable = driver.findElement(By.xpath("//table[@class='task-listing']"));
		List<WebElement> taskListTrs = taskListTable.findElements(By.xpath("tbody/tr"));
		
		List<SearchEngineTask> tasks = new ArrayList<SearchEngineTask>();
		for (WebElement taskTr : taskListTrs) {
			WebElement bonusValue = taskTr.findElement(By.xpath("td[1]/hgroup/a/h1"));
			WebElement taskDesc = taskTr.findElement(By.xpath("td[2]/section/h1/a"));
			if(taskDesc.getText().startsWith("Find the search engine")){
				int bonus = Integer.parseInt(bonusValue.getText());
				SearchEngineTask tsk = new SearchEngineTask(taskDesc.getText(), taskDesc.getAttribute("href"), bonus);
				System.out.println(tsk.getTaskDesc() + ", " + tsk.getTaskHref() + "," + tsk.getBonus());
				tasks.add(tsk);
			}
		}
		Collections.sort(tasks);
		for (SearchEngineTask searchEngineTask : tasks) {
			
		}
	}

	public void testZoomBucksSurvey(String account, WebDriver driver) throws Exception {
		driver.get("http://surveys.zoombucks.com/dashboard.php");
		Thread.sleep(5000);
		List<WebElement> elements = driver.findElements(By.xpath("//div[@id='divProfileList']/ul/li"));
		List<ProfileSurvey> surveys = new ArrayList<ProfileSurvey>();
		for (WebElement webElement : elements) {
			WebElement desc = webElement.findElement(By.xpath("span"));
			WebElement anchr = webElement.findElement(By.xpath("a"));
			String href = anchr.getAttribute("href");
			
			surveys.add(new ProfileSurvey(desc.getText(), href, anchr.getText()));
			
			
			
		}
		for (ProfileSurvey profileSurvey : surveys) {
//			if(profileSurvey.getAnchrText().contains("Zoom Bucks")){
				System.out.println(profileSurvey.getSurveyDesc() + " begin");
				doSurvey(profileSurvey.getHref(), driver);
				System.out.println(profileSurvey.getSurveyDesc() + " finished");
//			}
		}
		System.out.println("all survey finished!");
	}
	
	private void doSurvey(String url, WebDriver driver) throws InterruptedException{
		driver.get(url);
		By by = By.name("Next");
		WebElement nextButton;
		try{
			while ((nextButton = driver.findElement(by)) != null) {
				Thread.sleep(500);
				WebElement questionTableNode = driver.findElement(By.xpath("//td[@class='surveyInner-Table']"));
				
				WebElement typeDetect = questionTableNode.findElement(By.xpath("table/tbody/tr[2]/td/table/tbody/tr/td/*[1]"));
				String tagName = typeDetect.getTagName();
				Random ra = new Random();
				if("select".equals(tagName)){
					Select select = new Select(typeDetect);
					select.selectByIndex(ra.nextInt(select.getOptions().size()));
				} else {
					try{
						WebElement checkRadioContainer = driver.findElement(By.xpath("//td[@class='checkRadioContainer']"));
						if(checkRadioContainer != null){
							typeDetect = checkRadioContainer.findElement(By.tagName("input"));
							String type = typeDetect.getAttribute("type");
							if("checkbox".equals(type)){
								List<WebElement> checkboxList = driver.findElements(By.xpath("//input[@type='checkbox']"));
								int listSize = checkboxList.size();
								int totalChecked = ra.nextInt(listSize);
								while(totalChecked == 0){
									totalChecked = ra.nextInt(listSize);
								}
								for (int i = 0; i < totalChecked; i++) {
									checkboxList.get(ra.nextInt(listSize)).click();
								}
							} else {
								List<WebElement> radioList = driver.findElements(By.xpath("//input[@type='radio']"));
								int listSize = radioList.size();
								radioList.get(ra.nextInt(listSize)).click();
							}
						} 
					} catch(Exception e){
						e.printStackTrace();
						WebElement tableRadioElement = driver.findElement(By.xpath("//table[@class='tableBg']"));
						List<WebElement> cols = tableRadioElement.findElements(By.xpath("tbody/tr/td/table/tbody/tr[1]/td"));
						List<WebElement> rows = tableRadioElement.findElements(By.xpath("tbody/tr/td/table/tbody/tr"));
						int colsCount = cols.size();
						for (int i = 1; i < rows.size(); i++) {
							List<WebElement> inputs = rows.get(i).findElements(By.tagName("input"));
							inputs.get(ra.nextInt(colsCount - 1)).click();
						}
					}
				}
				Thread.sleep(600);
				nextButton.click();
			}
		} catch(NoSuchElementException e){
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
