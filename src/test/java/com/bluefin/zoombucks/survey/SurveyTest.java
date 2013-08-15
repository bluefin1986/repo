package com.bluefin.zoombucks.survey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
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

	private String[] accounts = { "alben_nick" };
	private String baseUrl;
	
	private void initDrivers(int accountCount){
		driverList = new ArrayList<WebDriver>();
		String[] profiles = new String[]{
				"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/pzodczhc.selenium"
				,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/z8kseba1.selenium2"
				,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/sli4o4cr.selenium3"
				,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/hd8ge2oz.selenium4"};
		if(accountCount > profiles.length){
			throw new RuntimeException("profile数量不够");
		}
		for (int i = 0; i < accountCount; i++) {
			File profilePath = new File(profiles[i]);
			FirefoxProfile fp = new FirefoxProfile(profilePath);
			FirefoxDriver firefoxDriver = new FirefoxDriver(fp);
			firefoxDriver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			driverList.add(firefoxDriver);
		}
	}

	@Before
	public void init() throws IOException {
		initDrivers(accounts.length);
		for (int i = 0; i < accounts.length; i++) {
//			String PROXY = "127.0.0.1:9087";
			// driverList.add(new InternetExplorerDriver());
//			org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
//			proxy.setHttpProxy(PROXY)
//			     .setFtpProxy(PROXY)
//			     .setSslProxy(PROXY);
//			DesiredCapabilities cap = new DesiredCapabilities();
//			cap.setCapability(CapabilityType.PROXY, proxy);
			//selenium 专用profile
			
			
			
//			driverList.add(new SafariDriver(cap));
		}
	}
	
	@Test
	public void testZoombuckSurvey() throws Exception{
		for (int i = 0; i < accounts.length; i++) {
			final WebDriver driver = driverList.get(i);
			final String account = accounts[i];
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
			
					login(account, driver);
					testZoomBucksTask(account,driver);
					//surveys
					testZoomBucksSurvey(account,driver);
//					testWatchVideo(account, driver);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (Exception ex){
						ex.printStackTrace();
					}
				}
			}).start();
		}
		startDaemon();
	}
	
	private void startDaemon(){
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void login(String account, WebDriver driver){
		driver.manage().deleteAllCookies();
		driver.navigate().refresh();
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
		String earnUrl = "http://www.zoombucks.com/includes/video_homepage.php?reward=2%20ZBucks";
		String url = "https://embed.jungroup.com/embedded_videos/catalog_frame?uid=" + account + "&site=ZoomBucks&pid=4716234&sub_id=&reward=2%20ZBucks";
		boolean finished = false;
		int count = 0;
		while(count < 10 && !finished){
			try {
				driver.get(earnUrl);
				WebElement bodyContent = driver.findElement(By.tagName("body"));
				String content = bodyContent.getText();
				if("No Videos available.".equals(content.trim())){
					break;
				}
				WebElement earnButton = driver.findElement(By.id("webtraffic_start_button_text"));
				earnButton.click();
				count++;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
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
				tasks.add(tsk);
			}
		}
		Random ra = new Random();
		String content = loadInputContent();
		Collections.sort(tasks);
		for (SearchEngineTask searchEngineTask : tasks) {
			driver.get(searchEngineTask.getTaskHref());
			Thread.sleep(1000);
			//选择所有的下拉
			List<WebElement> selects = driver.findElements(By.xpath("//select"));
			for (WebElement selElement : selects) {
				Select sel = new Select(selElement);
				int length = sel.getOptions().size();
				int pickIndex = ra.nextInt(length);
				if(pickIndex < 2){
					pickIndex = 2;
				}
				sel.selectByIndex(pickIndex);
			}
			try{
				List<WebElement> textAreas = driver.findElements(By.xpath("//textarea"));
				for (WebElement textArea : textAreas) {
					textArea.sendKeys(content.substring(ra.nextInt(content.length())));
				}
			} catch(Exception ex){
				System.out.println("text areas not found:" + ex.getMessage());
			}
			//填写input内容
			try{
				List<WebElement> questionDivs = driver.findElements(By.xpath("//div[@class='text cml_field']"));
				for (WebElement div : questionDivs) {
					WebElement input = div.findElement(By.tagName("input"));
					input.sendKeys(div.getAttribute("data-validates-regex"));
				}
			} catch(Exception ex){
				System.out.println("inputs not found:" + ex.getMessage());
			}
			//提交
			try {
				WebElement submit = driver.findElement(By.xpath("//input[@type='submit']"));
				submit.click();
			} catch (Exception e) {
				System.err.println("submit failed");
			}
			
			Thread.sleep(5000);
		}
	}
	
	public String loadInputContent(){
		try {
			String content = FileUtils.readFileToString(new File("src/main/resources/InputContent.txt"));
			return content;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Presidents Day may refer to: Presidents Day (United States), a holiday in some regions";
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
			if(profileSurvey.getAnchrText().contains("Zoom Bucks")){
				System.out.println(profileSurvey.getSurveyDesc() + " begin");
				doSurvey(profileSurvey.getHref(), driver);
				System.out.println(profileSurvey.getSurveyDesc() + " finished");
			}
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
					int optionsCount = select.getOptions().size();
					int selectIndex = ra.nextInt(optionsCount);
					if (selectIndex < 2) {
						selectIndex = optionsCount - 1;
					}
					select.selectByIndex(selectIndex);
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
								//最多选5个提高效率
								if(totalChecked > 5){
									totalChecked = 5;
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
						System.err.println(e.getMessage());
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
