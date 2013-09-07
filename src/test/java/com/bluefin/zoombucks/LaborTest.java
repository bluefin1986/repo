package com.bluefin.zoombucks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.bluefin.WebDriverFactory;
import com.bluefin.base.Operator;
import com.bluefin.base.Task;
import com.bluefin.zoombucks.labor.ZoomBucksLabor;
import com.bluefin.zoombucks.labor.ZoomBucksOperator;
import com.bluefin.zoombucks.model.ZoomBucksAccount;

public class LaborTest {
	
	private List<WebDriver> driverList;
	
	private List<ZoomBucksAccount> accountList;

	private boolean[] finishFlags;
	
	public static Map<String, Task> taskMap = null;
	
	public static Map<String, String> ANSWER_MAP = new HashMap<String, String>();
	
	private void initDrivers(int accountCount){
		driverList = new ArrayList<WebDriver>();
		String[] profiles = new String[]{
				"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/pzodczhc.selenium"
				,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/z8kseba1.selenium2"
				,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/sli4o4cr.selenium3"
				,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/hd8ge2oz.selenium4"};
//		String[] profiles = new String[]{
//				"C:/Documents and Settings/dvlp/Application Data/Mozilla/Firefox/Profiles/6v81xj5x.selenium2"
//				,"C:/Documents and Settings/dvlp/Application Data/Mozilla/Firefox/Profiles/rx1c1n2i.selenium1"
//		};
		if(accountList.size() > profiles.length){
			throw new RuntimeException("profile数量不够");
		}
		finishFlags = new boolean[accountList.size()];
		for (int i = 0; i < accountCount; i++) {
			finishFlags[i] = false;
		}
		for (int i = 0; i < accountList.size(); i++) {
			File profilePath = new File(profiles[i]);
			FirefoxProfile fp = new FirefoxProfile(profilePath);
			FirefoxDriver firefoxDriver = new FirefoxDriver(fp);
			firefoxDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			driverList.add(firefoxDriver);
		}
	}
	
	@Before
	public void init() throws IOException {
		LaborTest.ANSWER_MAP = loadAnswerMap();
		taskMap = ZoomBucksLabor.loadTaskMap();
		
		accountList = new ArrayList<ZoomBucksAccount>();
		
		ZoomBucksAccount acct0 = new ZoomBucksAccount();
		acct0.setBirthDateStr("19811101");
		acct0.setFullName("hookglurey");
		acct0.setEmail("hookglurey@hotmail.com");
		acct0.setGender("M");
		acct0.setPassword("baoziazhu609");
		accountList.add(acct0);
		ZoomBucksAccount acct = new ZoomBucksAccount();
		acct.setBirthDateStr("19761101");
		acct.setFullName("hooji_claixor");
		acct.setEmail("hooji_claixor@hotmail.com");
		acct.setGender("M");
		acct.setPassword("baoziazhu609");
		accountList.add(acct);
//		ZoomBucksAccount acct1 = new ZoomBucksAccount();
//		acct1.setBirthDateStr("19810211");
//		acct1.setFullName("broono_lawe");
//		acct1.setEmail("broono_lawe@hotmail.com");
//		acct1.setGender("F");
//		acct1.setPassword("baoziazhu609");
//		accountList.add(acct1);
//		ZoomBucksAccount acct2 = new ZoomBucksAccount();
//		acct2.setBirthDateStr("19860511");
//		acct2.setFullName("james_dingous");
//		acct2.setEmail("james_dingous@hotmail.com");
//		acct2.setGender("F");
//		acct2.setPassword("baoziazhu609");
//		accountList.add(acct2);
//		ZoomBucksAccount acct3 = new ZoomBucksAccount();
//		acct3.setBirthDateStr("19820511");
//		acct3.setFullName("giibson_proono");
//		acct3.setEmail("giibson_proono@hotmail.com");
//		acct3.setGender("F");
//		acct3.setPassword("baoziazhu609");
//		accountList.add(acct3);
	}
	
	@Test
	public void testZoombuckSurvey() throws Exception{
		WebDriver driver = null;
		for (int i = 0; i < accountList.size(); i++) {
			driver = WebDriverFactory.generateFirefoxDriver();
			ZoomBucksAccount account = accountList.get(i);
//			new ZoomBucksLabor(account, driver).start();
			new ZoomBucksLabor(account, driver).run();
			Thread.sleep(2000);
		}
//		startDaemon();
	}
	
//	@Test
	public void testZoombucksTasks(){
		WebDriver driver = WebDriverFactory.generateFirefoxDriver();
		Operator<ZoomBucksAccount> operator = new ZoomBucksOperator();
		for (int i = 0; i < accountList.size(); i++) {
			ZoomBucksAccount zaccount = accountList.get(i);
			ZoomBucksLabor zoombucksLabor = new ZoomBucksLabor(zaccount, driver);
			try {
				operator.login(driver, zaccount);
				zoombucksLabor.runZoomBucksTask();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void startDaemon(){
		while (true) {
			try {
				Thread.sleep(10000);
				int finishCount = 0;
				for (int i = 0; i < finishFlags.length; i++) {
					if (finishFlags[i]) {
						finishCount++;
					}
					if(finishCount == finishFlags.length){
						break;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Map<String, String> loadAnswerMap() {
		Map<String, String> map = new HashMap<String, String>();
		try {
			String content = FileUtils.readFileToString(new File(
					"src/main/resources/answers.txt"));
			String[] answers = content.split("\n");
			for (String answer : answers) {
				if(StringUtils.isBlank(answer)){
					continue;
				}
				String[] answerSplit = answer.split("&&");
				map.put(answerSplit[0], answerSplit[1]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	
//	@Test
	public void testLoginIntoHotmail(){
		WebDriver driver = WebDriverFactory.generateFirefoxDriver();
		ZoomBucksAccount acct0 = new ZoomBucksAccount();
		acct0.setBirthDateStr("19811101");
		acct0.setFullName("hookglurey");
		acct0.setEmail("hookglurey@hotmail.com");
		acct0.setGender("M");
		acct0.setPassword("baoziazhu609");
		accountList.add(acct0);
		new ZoomBucksOperator().loginIntoHotmail(driver, acct0);
	}
	
//	@Test
	public void testSignIntoTaskSite() throws Exception{
		WebDriver driver = WebDriverFactory.generateFirefoxDriver();
		ZoomBucksAccount acct0 = new ZoomBucksAccount();
		acct0.setBirthDateStr("19811101");
		acct0.setFullName("hookglurey");
		acct0.setEmail("hookglurey@hotmail.com");
		acct0.setGender("M");
		acct0.setPassword("baoziazhu609");
		accountList.add(acct0);
		new ZoomBucksOperator().signInToTaskSite(driver, acct0);
	}
}
