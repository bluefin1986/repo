package com.bluefin.zoombucks.labor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.bluefin.WebDriverFactory;
import com.bluefin.zoombucks.LaborTest;
import com.bluefin.zoombucks.TaskPool;
import com.bluefin.zoombucks.model.CompareSearchEngineTask;
import com.bluefin.zoombucks.model.ProfileSurvey;
import com.bluefin.zoombucks.model.SearchEngineTask;
import com.bluefin.zoombucks.model.TaskSummary;
import com.bluefin.zoombucks.model.ZoomBucksAccount;

public class ZoomBucksLabor extends Thread {

	protected ZoomBucksAccount zaccount;

	private boolean silentMode = false;

	private int totalEarned;

	private List<String> failedUrls;

	private WebDriver driver;
	
	private String originWindowHandle;

	private Map<String, SearchEngineTask> failedTaskMap = new HashMap<String, SearchEngineTask>();

	public ZoomBucksLabor(){
		
	}
	
	public ZoomBucksLabor(ZoomBucksAccount zaccount, WebDriver driver) {
		this.zaccount = zaccount;
		this.driver = driver;
	}

	public void run() {
		Date begin = new Date();
		try {
			ZoomBucksOperator.login(driver, zaccount);
		} catch (Exception e1) {
			System.out.println("login failed ,register account:"
					+ zaccount.getFullName());
			try {
				register();
				try {
					registerRewardTv();
				} catch (Exception e) {
					System.err.println("register rewardTv failed!"
							+ e.getMessage());
				}
				try {
					activateSurvey();
				} catch (Exception e) {
					e.printStackTrace();
					System.err
							.println("active survey failed!" + e.getMessage());
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
			// tasks
			runZoomBucksTask();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			// surveys
			runZoomBucksSurveys();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// testWatchVideo(account, driver);
		Date end = new Date();
		System.out.println(zaccount.getFullName() + " all finished, cost time:"
				+ (end.getTime() - begin.getTime()) / 60000 + " mins");
		driver.quit();
	}

	private void register() throws Exception {
		driver.get("http://www.zoombucks.com/");
		driver.findElement(By.id("email")).sendKeys(zaccount.getEmail());
		driver.findElement(By.id("fullname")).sendKeys(zaccount.getFullName());
		driver.findElement(By.id("textInput")).sendKeys(zaccount.getPassword());
		driver.findElement(By.id("accept_terms")).click();
		driver.findElement(By.xpath("//div[@class='submitbtn']/button"))
				.click();
		Thread.sleep(5000);
		String[] accountArray = zaccount.getFullName().split("_");
		driver.findElement(By.name("first_name")).sendKeys(accountArray[0]);
		driver.findElement(By.name("last_name")).sendKeys(accountArray[1]);
		driver.findElement(By.name("gender")).click();
		driver.findElement(By.id("date_of_birth")).sendKeys(
				zaccount.getBirthDateStr());
		driver.findElement(By.tagName("form")).submit();
		Thread.sleep(5000);
	}


	private List<String> loadTaskUrls(WebElement taskListTable) {
		List<WebElement> taskListTrs = taskListTable.findElements(By
				.xpath("tbody/tr"));
		List<String> urls = new ArrayList<String>();
		for (WebElement taskTr : taskListTrs) {
			WebElement taskDesc = taskTr.findElement(By
					.xpath("td[2]/section/h1/a"));
			if (taskDesc.getText().trim().startsWith("Find the search engine")) {
				urls.add(taskDesc.getAttribute("href"));
			}
		}
		return urls;
	}

	private List<SearchEngineTask> loadTasks(WebElement taskListTable) {
		List<WebElement> taskListTrs = taskListTable.findElements(By
				.xpath("tbody/tr"));
		List<SearchEngineTask> tasks = new ArrayList<SearchEngineTask>();
		for (WebElement taskTr : taskListTrs) {
			WebElement bonusValue = taskTr.findElement(By
					.xpath("td[1]/hgroup/a/h1"));
			WebElement taskDesc = taskTr.findElement(By
					.xpath("td[2]/section/h1/a"));
			if (taskDesc.getText().trim().startsWith("Find the search engine")) {
				int bonus = Integer.parseInt(bonusValue.getText());
				SearchEngineTask tsk = new SearchEngineTask(taskDesc.getText(),
						taskDesc.getAttribute("href"), bonus);
				tasks.add(tsk);
			}
		}
		return tasks;
	}

	
	
	/**
	 * 扫尾失败的任务
	 * @throws InterruptedException
	 */
	private void clearFailedSearchEngineTasks() throws Exception{
		Map<String, SearchEngineTask> goingonMap = new HashMap<String, SearchEngineTask>(failedTaskMap);
		failedTaskMap.clear();
		processTasks(goingonMap);
	}

	public void runZoomBucksTask() throws Exception {
		ZoomBucksOperator.ssoToTaskSite(driver, zaccount);
		List<String> pageHrefs = new ArrayList<String>();
		List<WebElement> pageButtons = driver.findElements(By
				.xpath("//nav[@class='pagination']/span/a"));
		for (WebElement btn : pageButtons) {
			String content = btn.getText();
			try {
				Integer.parseInt(content.trim());
				pageHrefs.add(btn.getAttribute("href"));
			} catch (Exception e) {
			}
		}
		WebElement taskListTable = driver.findElement(By
				.xpath("//table[@class='task-listing']"));
		 List<SearchEngineTask> tasks = loadTasks(taskListTable);
//		List<String> taskUrls = loadTaskUrls(taskListTable);
		// 载入预置的所有task列表
		Map<String, SearchEngineTask> goingOnTaskMap = new HashMap<String, SearchEngineTask>();
		goingOnTaskMap.putAll(LaborTest.taskMap);
		// 页面所有已显示的tasks
		for (String href : pageHrefs) {
			driver.get(href);
			Thread.sleep(4000);
			taskListTable = driver.findElement(By
					.xpath("//table[@class='task-listing']"));
			tasks.addAll(loadTasks(taskListTable));
//			taskUrls.addAll(loadTaskUrls(taskListTable));
		}
		// 筛除已显示的
		for (Iterator<SearchEngineTask> iterator = tasks.iterator(); iterator.hasNext();) {
			SearchEngineTask searchEngineTask = iterator.next();
			goingOnTaskMap.put(searchEngineTask.getTaskHref(), searchEngineTask);
		}
		processTasks(goingOnTaskMap);
		System.out.println("all tasks finished! now try clear failed tasks!");
		Thread.sleep(2000);
		clearFailedSearchEngineTasks();
	}
	
	private void processTasks(Map<String, SearchEngineTask> taskMap) throws InterruptedException{
		TaskPool taskPool = new TaskPool(taskMap);
		List<WebDriver> newTabs = openWindows(2);
		TaskSummary summary = new TaskSummary(taskMap.size());
		for (WebDriver webDriver : newTabs) {
			new SearchEngineTaskLabor(webDriver, taskPool, summary, zaccount.clone()).start();
		}
		//自己也别浪费了
		new SearchEngineTaskLabor(driver, taskPool, summary, zaccount).start();
		while (!taskPool.isTasksFinished()) {
			Thread.sleep(5000);
		}
	}
	
	private List<WebDriver> openWindows(int windowsCount){
		List<WebDriver> drivers = new ArrayList<WebDriver>();
		for (int i = 0; i < windowsCount; i++) {
			WebDriver driver = WebDriverFactory.generateFirefoxDriver();
			drivers.add(driver);
		}
		return drivers;
	}
	
	public void runZoomBucksSurveys() throws Exception {
		ZoomBucksOperator.ssoToSurveySite(driver, zaccount);
		List<WebElement> elements = null;
		try {
			elements = driver.findElements(By
					.xpath("//div[@id='divProfileList']/ul/li"));
		} catch (Exception e) {
			List<WebElement> optCountryIds = driver.findElements(By.id("optCountryId"));
			if (optCountryIds != null && optCountryIds.size() > 0) {
				System.out.println(zaccount.getFullName()
						+ " survey not activated yet, activate survey");
				activateSurvey();
				driver.get("http://surveys.zoombucks.com/dashboard.php");
				Thread.sleep(5000);
			}
		}
		TaskSummary taskSummary = new TaskSummary(elements.size());
		Map<String, SearchEngineTask> taskMap = new HashMap<String, SearchEngineTask>();
		for (WebElement webElement : elements) {
			WebElement desc = webElement.findElement(By.xpath("span"));
			WebElement anchr = webElement.findElement(By.xpath("a"));
			int bonus = 0;
			try {
				bonus = Integer.parseInt(anchr.getText()
						.replace("Zoom Bucks", "").trim());
			} catch (Exception e) {
			}
			String href = anchr.getAttribute("href");
			taskMap.put(href, new SearchEngineTask(desc.getText(), href, bonus));
		}
		
		TaskPool taskPool = new TaskPool(taskMap);
		new SurveySlave(driver, taskPool, taskSummary, zaccount).start();
		List<WebDriver> drivers = openWindows(2);
		for (WebDriver webDriver : drivers) {
			new SurveySlave(webDriver, taskPool, taskSummary, zaccount.clone()).start();
		}
		while (!taskPool.isTasksFinished()) {
			Thread.sleep(5000);
		}
		totalEarned += taskSummary.getTotalEarned();
		System.out.println("all survey finished!");
	}

	

	private void registerPeanuts() throws Exception {
		driver.get("http://www.zoombucks.com/peanutlabs.php");
		List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
		String url = null;
		for (WebElement webElement : iframes) {
			url = webElement.getAttribute("src");
			if (StringUtils.isNotBlank(url)) {
				break;
			}
		}
		driver.get(url);
		driver.findElement(By.cssSelector("div.pickerfbbuttontextcontent"))
				.click();
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
		new Select(driver.findElement(By.id("sel_input")))
				.selectByVisibleText("$150,000 - $199,999");
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
		new Select(driver.findElement(By.id("sel_input")))
				.selectByVisibleText("Consumer Cellular");
		driver.findElement(By.cssSelector(buttonCssSelector)).click();
		driver.findElement(By.id("qx132-1")).click();
		driver.findElement(By.cssSelector(buttonCssSelector)).click();
		System.out.println("peanuts account actived, 20 earned!");
		totalEarned += 20;
	}

	private void activateSurvey() throws Exception {
		System.out.println("activate survey.");
		driver.get("http://surveys.zoombucks.com");
		Thread.sleep(2000);
		WebElement optState = null;
		while (optState == null) {
			try {
				optState = driver.findElement(By.id("optStateId"));
			} catch (Exception e) {
				System.out.println("not found stateId, sleep for 1 second");
				Thread.sleep(1000);
			}
		}
		new Select(optState).selectByVisibleText("Florida");
		driver.findElement(By.id("txtZipPostal")).sendKeys("32004");
		new Select(driver.findElement(By.id("optMonthId")))
				.selectByVisibleText("June");
		new Select(driver.findElement(By.id("optDayId")))
				.selectByVisibleText("10");
		new Select(driver.findElement(By.id("optYearId")))
				.selectByVisibleText("1984");
		driver.findElement(By.id("rdGender")).click();
		new Select(driver.findElement(By.id("optAnnualHouseholdIncomeId")))
				.selectByIndex(5);
		new Select(driver.findElement(By.id("optEducationLevelId")))
				.selectByIndex(4);
		new Select(driver.findElement(By.id("optEmploymentStatusId")))
				.selectByIndex(4);
		new Select(driver.findElement(By.id("optIndustryId"))).selectByIndex(8);
		Thread.sleep(2000);
		new Select(driver.findElement(By.id("optRoleId"))).selectByIndex(4);
		Thread.sleep(2000);
		Select selectJobTitle = new Select(driver.findElement(By
				.id("optJobTitleId")));
		int jobTitleSize = selectJobTitle.getOptions().size();
		if (jobTitleSize == 1) {
			Thread.sleep(5000);
		}
		selectJobTitle.selectByIndex(selectJobTitle.getOptions().size() - 2);
		Thread.sleep(2000);
		new Select(driver.findElement(By.id("optMaritalStatusId")))
				.selectByIndex(2);
		new Select(driver.findElement(By.id("optEthnicityId")))
				.selectByIndex(10);
		new Select(driver.findElement(By.id("optMobilePhoneTypeId")))
				.selectByIndex(2);
		driver.findElement(By.id("rdChildrenUnder18_N")).click();
		driver.findElement(By.name("chbTermsAndConditions")).click();
		driver.findElement(By.xpath("//input[@type='submit']")).click();
		System.out.println("survey actived!");
		Thread.sleep(5000);
	}

	private void registerRewardTv() throws Exception {
		driver.get("http://www.zoombucks.com/flow.php");
		driver.findElement(By.id("top_nav_5")).click();
		String registerHref = driver.findElement(
				By.xpath("//div[@class='content']/a")).getAttribute("href");
		driver.get(registerHref);
		Thread.sleep(5000);
		driver.findElement(By.id("emailAddress")).sendKeys(zaccount.getEmail());
		WebElement form = driver.findElement(By.xpath("//form[@id='join']"));
		form.findElement(By.id("password")).sendKeys(zaccount.getPassword());
		driver.findElement(By.id("confirmPassword")).sendKeys(
				zaccount.getPassword());
		new Select(driver.findElement(By.id("state")))
				.selectByVisibleText("FL");
		driver.findElement(By.id("zipcode")).sendKeys("32004");
		new Select(driver.findElement(By.id("monthOfBirth")))
				.selectByVisibleText("APR");
		driver.findElement(By.id("yearOfBirth")).sendKeys("1982");
		driver.findElement(By.id("gender1")).click();
		new Select(driver.findElement(By.id("incomeList"))).selectByIndex(5);
		driver.findElement(By.name("rentOwnHomeResponseID")).click();
		driver.findElement(By.name("childrenInHomeResponseID")).click();
		driver.findElement(By.id("hispanic_7007")).click();
		new Select(driver.findElement(By.id("raceResponseID")))
				.selectByIndex(2);
		driver.findElement(By.id("rememberMe1")).click();
		driver.findElement(By.cssSelector("input.w100i")).click();
		String url = driver.getCurrentUrl();
		if ("http://www.rewardtv.com/verify/welcome_back.sdo?nextPage=%2Fplay%2Frelogin.sdo"
				.equals(url)) {
			loginRewardTv();
		}
		if ("http://www.rewardtv.com/play/relogin.sdo".equals(url)) {
			driver.findElement(By.xpath("/html/body/div/table/tbody/tr/td/div/a")).click();
			loginRewardTv();
		}
		Thread.sleep(2000);
		System.out.println("rewardTv registed!");
	}

	private void loginRewardTv() {
		driver.findElement(By.id("userName")).sendKeys(zaccount.getEmail());
		driver.findElement(By.id("password")).sendKeys(zaccount.getPassword());
		driver.findElement(By.id("btnAcctLogin")).click();
	}

	public void silentMode() {
		this.silentMode = true;
	}

	public static Map<String, SearchEngineTask> loadTaskMap() {
		Map<String, SearchEngineTask> map = new HashMap<String, SearchEngineTask>();
		try {
			String content = FileUtils.readFileToString(new File(
					"src/main/resources/tasks.txt"));
			content = content.replace("now:", "");
			String[] urls = content.split("\n");
			for (int i = 0; i < urls.length; i++) {
				String taskUrl = urls[i];
//				String taskId = taskUrl.substring(taskUrl.lastIndexOf("/") + 1);
				map.put(taskUrl, new SearchEngineTask("", taskUrl , 6));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
}
