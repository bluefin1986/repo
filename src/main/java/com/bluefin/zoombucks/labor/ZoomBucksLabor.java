package com.bluefin.zoombucks.labor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.bluefin.zoombucks.model.CompareSearchEngineTask;
import com.bluefin.zoombucks.model.ProfileSurvey;
import com.bluefin.zoombucks.model.SearchEngineTask;
import com.bluefin.zoombucks.model.ZoomBucksAccount;

public class ZoomBucksLabor extends Thread {
	
	private ZoomBucksAccount zaccount;
	
	private String email;
	
	private int totalEarned;
	
	private List<String> failedUrls;
	
	private WebDriver driver;
	
	public ZoomBucksLabor(ZoomBucksAccount zaccount, WebDriver driver){
		this.zaccount = zaccount;
		this.driver = driver;
		this.email = zaccount.getEmail();
	}
	
	public void run(){
		Date begin = new Date();
		try {
			login();
		} catch (Exception e1) {
			System.out.println("login failed ,register account:" + zaccount.getFullName());
			try {
				register();
				try {
					registerRewardTv();
				} catch (Exception e) {
					System.err.println("register rewardTv failed!" + e.getMessage());
				}
				try {
					activateSurvey();
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("active survey failed!" + e.getMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("registed failed!");
				return;
			}
			
		}
		try {
			registerPeanuts();
		} catch (Exception e) {
			System.out.println("peanuts account already actived。");
		}
		try {
			//tasks
			runZoomBucksTask();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			//surveys
			runZoomBucksSurveys();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//					testWatchVideo(account, driver);
		Date end = new Date();
		System.out.println(zaccount.getFullName() + " all finished, cost time:" + (end.getTime() - begin.getTime()) / 60000 + " mins");
	}
	
	private void register() throws Exception{
		driver.get("http://www.zoombucks.com/");
		driver.findElement(By.id("email")).sendKeys(zaccount.getEmail());
		driver.findElement(By.id("fullname")).sendKeys(zaccount.getFullName());
		driver.findElement(By.id("textInput")).sendKeys(zaccount.getPassword());
		driver.findElement(By.id("accept_terms")).click();
		driver.findElement(By.xpath("//div[@class='submitbtn']/button")).click();
		Thread.sleep(5000);
		String[] accountArray = zaccount.getFullName().split("_");
		driver.findElement(By.name("first_name")).sendKeys(accountArray[0]);
		driver.findElement(By.name("last_name")).sendKeys(accountArray[1]);
		driver.findElement(By.name("gender")).click();
		driver.findElement(By.id("date_of_birth")).sendKeys(zaccount.getBirthDateStr());
		driver.findElement(By.tagName("form")).submit();
		Thread.sleep(5000);
	}
	
	private void login() throws Exception{
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
		if("http://www.zoombucks.com/login.php".equals(url)){
			throw new Exception(zaccount.getFullName() + "not registed yet!");
		}
		System.out.println(zaccount.getFullName() + "登录成功");
	}
	
	private List<SearchEngineTask> loadTasks(WebElement taskListTable){
		List<WebElement> taskListTrs = taskListTable.findElements(By.xpath("tbody/tr"));
		List<SearchEngineTask> tasks = new ArrayList<SearchEngineTask>();
		for (WebElement taskTr : taskListTrs) {
			WebElement bonusValue = taskTr.findElement(By.xpath("td[1]/hgroup/a/h1"));
			WebElement taskDesc = taskTr.findElement(By.xpath("td[2]/section/h1/a"));
			if(taskDesc.getText().trim().startsWith("Find the search engine")){
				int bonus = Integer.parseInt(bonusValue.getText());
				SearchEngineTask tsk = new SearchEngineTask(taskDesc.getText(), taskDesc.getAttribute("href"), bonus);
				tasks.add(tsk);
			}
		}
		return tasks;
	}
	
	private void runZoomBucksTask() throws Exception{
		driver.get("http://www.zoombucks.com/tasks.php");
		driver.get("http://crowdflower.com/judgments/zoombucks?uid=" + zaccount.getFullName());
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
		List<SearchEngineTask> tasks = loadTasks(taskListTable);
		
		for (String href : pageHrefs) {
			driver.get(href);
			Thread.sleep(4000);
			taskListTable = driver.findElement(By.xpath("//table[@class='task-listing']"));
			tasks.addAll(loadTasks(taskListTable));
		}
		System.out.println("total task count:" + tasks.size());
		Random ra = new Random();
		String content = loadInputContent();
		Collections.sort(tasks, new CompareSearchEngineTask());
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
			boolean isInput = false;
			boolean meetRegexError = false;
			//填写input内容
			try{
				List<WebElement> questionDivs = driver.findElements(By.xpath("//div[@class='text cml_field']"));
				JavascriptExecutor executor = (JavascriptExecutor)driver;
				for (WebElement div : questionDivs) {
					WebElement input = div.findElement(By.tagName("input"));
					if(input == null){
						System.out.println("not exist");
					}
					String answer = div.getAttribute("data-validates-regex");
					if(answer.startsWith("(")){
//						executor.executeScript("document.getElementById('" + input.get"')");
						System.out.println("found phone num task, may fail:" + searchEngineTask.getTaskHref());
						failedUrls.add(searchEngineTask.getTaskHref());
						meetRegexError = true;
						break;
					}
					input.sendKeys(answer);
				}
				isInput = true;
			} catch(Exception ex){
				System.out.println("inputs not found:" + ex.getMessage());
			}
			//填写textarea内容
			try{
				if(isInput){
					throw new Exception("not textarea task");
				}
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
			if(meetRegexError){
				failCount++;
			} else {
				//提交
				submit.click();
				totalEarned += searchEngineTask.getBonus();
				System.out.println("[" + searchEngineTask.getTaskDesc() + "] finished. " + searchEngineTask.getBonus() + " bonus earned, total:" + totalEarned);
			}
			System.out.println("total tasks: " + tasks.size() + " rest tasks:" + (tasks.size() - i - 1) + ", failed:" + failCount);
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

	public void runZoomBucksSurveys() throws Exception {
		driver.get("http://surveys.zoombucks.com/dashboard.php");
		Thread.sleep(5000);
		List<WebElement> elements = null;
		try {
			elements = driver.findElements(By.xpath("//div[@id='divProfileList']/ul/li"));
		} catch (Exception e) {
			WebElement element = driver.findElement(By.id("optCountryId"));
			if(element != null){
				System.out.println(zaccount.getFullName() + " survey not activated yet, activate survey");
				activateSurvey();
				driver.get("http://surveys.zoombucks.com/dashboard.php");
				Thread.sleep(5000);
			}
		}
		List<ProfileSurvey> surveys = new ArrayList<ProfileSurvey>();
		for (WebElement webElement : elements) {
			WebElement desc = webElement.findElement(By.xpath("span"));
			WebElement anchr = webElement.findElement(By.xpath("a"));
			String href = anchr.getAttribute("href");
			surveys.add(new ProfileSurvey(desc.getText(), href, anchr.getText()));
		}
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
				totalEarned += bonus;
				System.out.println("[" + profileSurvey.getSurveyDesc() + "] finished " + bonus + " earned, total:" + totalEarned);
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
	
	private void registerPeanuts() throws Exception {
	    driver.get("http://www.zoombucks.com/peanutlabs.php");
	    List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
	    String url = null;
	    for (WebElement webElement : iframes) {
	    	url = webElement.getAttribute("src");
	    	if(StringUtils.isNotBlank(url)){
	    		break;
	    	}
		}
	    driver.get(url);
	    driver.findElement(By.cssSelector("div.pickerfbbuttontextcontent")).click();
	    Thread.sleep(500);
	    WebElement queryBody = driver.findElement(By.id("picker_q_body"));
	    List<WebElement> selects = queryBody.findElements(By.tagName("select"));
	    new Select(selects.get(0)).selectByVisibleText("1980");
	    new Select(selects.get(1)).selectByVisibleText("五月");
	    new Select(selects.get(2)).selectByVisibleText("14");
	    String buttonCssSelector = "button.fbbuttonfornav";
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx1-1")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("txt_input")).sendKeys("32004");
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    new Select(driver.findElement(By.id("sel_input"))).selectByVisibleText("$150,000 - $199,999");
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx100-2")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx156-1")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx101-0")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx126-1")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx129-2")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx122-3")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx121-2")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx141-2")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("0")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx158-2")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx159-1")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx130-0")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    new Select(driver.findElement(By.id("sel_input"))).selectByVisibleText("Consumer Cellular");
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    driver.findElement(By.id("qx132-1")).click();
	    driver.findElement(By.cssSelector(buttonCssSelector)).click();
	    System.out.println("peanuts account actived, 20 earned!");
	    totalEarned += 20;
	  }
	
	private void activateSurvey() throws Exception{
		System.out.println("activate survey.");
		driver.get("http://surveys.zoombucks.com");
		Thread.sleep(2000);
		WebElement optState = null;
		while(optState == null){
			try {
				optState = driver.findElement(By.id("optStateId"));
			} catch (Exception e) {
				System.out.println("not found stateId, sleep for 1 second");
				Thread.sleep(1000);
			}
		}
		new Select(optState).selectByVisibleText("Florida");
		driver.findElement(By.id("txtZipPostal")).sendKeys("32004");
		new Select(driver.findElement(By.id("optMonthId"))).selectByVisibleText("June");
		new Select(driver.findElement(By.id("optDayId"))).selectByVisibleText("10");
		new Select(driver.findElement(By.id("optYearId"))).selectByVisibleText("1984");
		driver.findElement(By.id("rdGender")).click();
		new Select(driver.findElement(By.id("optAnnualHouseholdIncomeId"))).selectByIndex(5);
		new Select(driver.findElement(By.id("optEducationLevelId"))).selectByIndex(4);
		new Select(driver.findElement(By.id("optEmploymentStatusId"))).selectByIndex(4);
		new Select(driver.findElement(By.id("optIndustryId"))).selectByIndex(8);
		Thread.sleep(2000);
		new Select(driver.findElement(By.id("optRoleId"))).selectByIndex(4);
		Thread.sleep(2000);
		Select selectJobTitle = new Select(driver.findElement(By.id("optJobTitleId")));
		int jobTitleSize = selectJobTitle.getOptions().size();
		if(jobTitleSize == 1){
			Thread.sleep(5000);
		}
		selectJobTitle.selectByIndex(selectJobTitle.getOptions().size() - 2);
		Thread.sleep(2000);
		new Select(driver.findElement(By.id("optMaritalStatusId"))).selectByIndex(2);
		new Select(driver.findElement(By.id("optEthnicityId"))).selectByIndex(10);
		new Select(driver.findElement(By.id("optMobilePhoneTypeId"))).selectByIndex(2);
		driver.findElement(By.id("rdChildrenUnder18_N")).click();
		driver.findElement(By.name("chbTermsAndConditions")).click();
		driver.findElement(By.xpath("//input[@type='submit']")).click();
		System.out.println("survey actived!");
		Thread.sleep(5000);
	}
	
	private void registerRewardTv() throws Exception{
		driver.get("http://www.zoombucks.com/flow.php");
		driver.findElement(By.id("top_nav_5")).click();
		String registerHref = driver.findElement(By.xpath("//div[@class='content']/a")).getAttribute("href");
		driver.get(registerHref);
		Thread.sleep(5000);
		driver.findElement(By.id("emailAddress")).sendKeys(zaccount.getEmail());
		WebElement form = driver.findElement(By.xpath("//form[@id='join']"));
		form.findElement(By.id("password")).sendKeys(zaccount.getPassword());
		driver.findElement(By.id("confirmPassword")).sendKeys(zaccount.getPassword());
		new Select(driver.findElement(By.id("state"))).selectByVisibleText("FL");
		driver.findElement(By.id("zipcode")).sendKeys("32004");
		new Select(driver.findElement(By.id("monthOfBirth"))).selectByVisibleText("APR");
		driver.findElement(By.id("yearOfBirth")).sendKeys("1982");
		driver.findElement(By.id("gender1")).click();
		new Select(driver.findElement(By.id("incomeList"))).selectByIndex(5);
		driver.findElement(By.name("rentOwnHomeResponseID")).click();
		driver.findElement(By.name("childrenInHomeResponseID")).click();
		driver.findElement(By.id("hispanic_7007")).click();
		new Select(driver.findElement(By.id("raceResponseID"))).selectByIndex(2);
		driver.findElement(By.id("rememberMe1")).click();
		driver.findElement(By.cssSelector("input.w100i")).click();
		String url = driver.getCurrentUrl();
		if("http://www.rewardtv.com/verify/welcome_back.sdo?nextPage=%2Fplay%2Frelogin.sdo".equals(url)){
			loginRewardTv();
		}
		if("http://www.rewardtv.com/play/relogin.sdo".equals(url)){
			loginRewardTv();
		}
		System.out.println("rewardTv registed!");
	}
	
	private void loginRewardTv(){
		driver.findElement(By.id("userName")).sendKeys(zaccount.getEmail());
		driver.findElement(By.id("password")).sendKeys(zaccount.getPassword());
		driver.findElement(By.id("btnAcctLogin")).click();
	}
}
