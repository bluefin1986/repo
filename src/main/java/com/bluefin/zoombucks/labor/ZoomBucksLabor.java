package com.bluefin.zoombucks.labor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.bluefin.zoombucks.model.CompareSearchEngineTask;
import com.bluefin.zoombucks.model.ProfileSurvey;
import com.bluefin.zoombucks.model.SearchEngineTask;

public class ZoomBucksLabor extends Thread {
	
	private String account;
	
	private int totalEarned;
	
	private List<String> failedUrls;
	
	private WebDriver driver;
	
	public ZoomBucksLabor(String account, WebDriver driver){
		this.account = account;
		this.driver = driver;
	}
	
	public void run(){
		Date begin = new Date();
		login(account, driver);
		try {
			//tasks
			testZoomBucksTask(account,driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//surveys
			testZoomBucksSurvey(account,driver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//					testWatchVideo(account, driver);
		Date end = new Date();
		System.out.println(account + " all finished, cost time:" + (end.getTime() - begin.getTime()) / 60000 + " mins");
	}
	
	private void login(String account, WebDriver driver){
		driver.manage().deleteAllCookies();
		driver.navigate().refresh();
		String baseUrl = "http://www.zoombucks.com";
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
	
	private List<SearchEngineTask> loadTasks(WebDriver driver, WebElement taskListTable){
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
		return tasks;
	}
	
	public void testZoomBucksTask(String account, WebDriver driver) throws Exception{
		driver.get("http://www.zoombucks.com/tasks.php");
		driver.get("http://crowdflower.com/judgments/zoombucks?uid=" + account);
		Thread.sleep(5000);
		List<String> pageHrefs = new ArrayList<String>();
		List<WebElement> pageButtons = driver.findElements(By.xpath("//nav[@class='pagination']/span/a"));
		for (WebElement btn : pageButtons) {
			String content = btn.getText();
			try {
				Integer.parseInt(content.trim());
				pageHrefs.add(btn.getAttribute("href"));
			} catch (Exception e) {
			}
		}
		WebElement taskListTable = driver.findElement(By.xpath("//table[@class='task-listing']"));
		List<SearchEngineTask> tasks = loadTasks(driver, taskListTable);
		
		for (String href : pageHrefs) {
			driver.get(href);
			Thread.sleep(4000);
			taskListTable = driver.findElement(By.xpath("//table[@class='task-listing']"));
			tasks.addAll(loadTasks(driver, taskListTable));
		}
		System.out.println("total task count:" + tasks.size());
		Random ra = new Random();
		String content = loadInputContent();
		Collections.sort(tasks, new CompareSearchEngineTask());
		int totalBonus = 0;
		int failCount = 0;
		for (int i = 0; i< tasks.size(); i++) {
			SearchEngineTask searchEngineTask = tasks.get(i);
			driver.get(searchEngineTask.getTaskHref());
			Thread.sleep(1000);
			WebElement submit = null;
			try {
				submit = driver.findElement(By.xpath("//input[@type='submit']"));
			} catch (Exception e) {
				System.err.println("submit not exist");
				failCount++;
				System.out.println("rest tasks:" + (tasks.size() - i - 1) + ", failed:" + failCount);
				continue;
			}
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
					int beginIndex = ra.nextInt(content.length());
					if(beginIndex == content.length() || content.length() - beginIndex > 30){
						beginIndex = content.length() - 30;
					}
					textArea.sendKeys(content.substring(beginIndex));
				}
			} catch(Exception ex){
				System.out.println("text areas not found:" + ex.getMessage());
			}
			//填写input内容
			try{
				List<WebElement> questionDivs = driver.findElements(By.xpath("//div[@class='text cml_field']"));
				for (WebElement div : questionDivs) {
					WebElement input = div.findElement(By.tagName("input"));
					String answer = div.getAttribute("data-validates-regex");
					if(answer.startsWith("(")){
						System.out.println("found phone num task, may fail:" + searchEngineTask.getTaskHref());
					}
					input.sendKeys(answer);
				}
			} catch(Exception ex){
				System.out.println("inputs not found:" + ex.getMessage());
			}
			//提交
			submit.click();
			totalBonus += searchEngineTask.getBonus();
			System.out.println("[" + searchEngineTask.getTaskDesc() + "] finished. " + searchEngineTask.getBonus() + " bonus earned, total:" + totalBonus);
			System.out.println("rest tasks:" + (tasks.size() - i - 1) + ", failed:" + failCount);
			Thread.sleep(2000);
		}
		System.out.println("all tasks finished!");
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
		int earned = 0;
		for (int i = 0; i < surveys.size(); i++) {
			ProfileSurvey profileSurvey = surveys.get(i);
			String anchrText = profileSurvey.getAnchrText();
			if(anchrText.contains("Zoom Bucks")){
				int bonus = 0;
				try {
					bonus = Integer.parseInt(anchrText.replace("Zoom Bucks", "").trim());
				} catch (Exception e) {
				}
				System.out.println("[" + profileSurvey.getSurveyDesc() + "] begin");
				doSurvey(profileSurvey.getHref(), driver);
				earned += bonus;
				System.out.println("[" + profileSurvey.getSurveyDesc() + "] finished " + bonus + " earned, total:" + earned);
				System.out.println(surveys.size() - i - 1 + " surveys rest");
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
			System.err.println(e.getMessage());
		} catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	private void registerPeanuts(WebDriver driver) throws Exception {
	    driver.get("http://www.zoombucks.com/paymentwall.php");
	    driver.findElement(By.xpath("//div[@id='zb_payment_wall']/div/div[2]/div/div[2]/div/ul/li[3]/a/span/img")).click();
	    driver.findElement(By.cssSelector("div.pickerfbbuttontextcontent")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    new Select(driver.findElement(By.id("picker_date_yr1376567005604"))).selectByVisibleText("1980");
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    new Select(driver.findElement(By.id("picker_date_m1376567005604"))).selectByVisibleText("五月");
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    new Select(driver.findElement(By.id("picker_date_d1376567005604"))).selectByVisibleText("16");
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx1-1")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("txt_input")).sendKeys("32004");
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    new Select(driver.findElement(By.id("sel_input"))).selectByVisibleText("$150,000 - $199,999");
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx100-2")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx156-1")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx101-0")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx126-1")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx129-2")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx122-3")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx121-2")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx141-2")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("0")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx158-2")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx159-1")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx130-0")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    new Select(driver.findElement(By.id("sel_input"))).selectByVisibleText("Consumer Cellular");
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("qx132-1")).click();
	    // ERROR: Caught exception [ERROR: Unsupported command [selectWindow | name=&mid=0a72a848c5cc0b0b0876af15c2a00d47 | ]]
	    driver.findElement(By.id("button_0_75")).click();
	  }
}
