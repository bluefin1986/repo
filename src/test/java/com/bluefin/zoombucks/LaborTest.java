package com.bluefin.zoombucks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.bluefin.WebDriverFactory;
import com.bluefin.zoombucks.labor.ZoomBucksLabor;
import com.bluefin.zoombucks.labor.ZoomBucksOperator;
import com.bluefin.zoombucks.model.SearchEngineTask;
import com.bluefin.zoombucks.model.ZoomBucksAccount;

public class LaborTest {
	
	private List<WebDriver> driverList;
	
	private List<ZoomBucksAccount> accountList;

	private boolean[] finishFlags;
	
	public static Map<String, SearchEngineTask> taskMap = null;
	
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
		taskMap = ZoomBucksLabor.loadTaskMap();
		
		accountList = new ArrayList<ZoomBucksAccount>();
		
		ZoomBucksAccount acct0 = new ZoomBucksAccount();
		acct0.setBirthDateStr("19831201");
		acct0.setFullName("micojan_alee");
		acct0.setEmail("micojan_alee@hotmail.com");
		acct0.setGender("M");
		acct0.setPassword("baoziazhu609");
		accountList.add(acct0);
		ZoomBucksAccount acct = new ZoomBucksAccount();
		acct.setBirthDateStr("19861101");
		acct.setFullName("huston_chroose");
		acct.setEmail("huston_chroose@hotmail.com");
		acct.setGender("M");
		acct.setPassword("baoziazhu609");
		accountList.add(acct);
		ZoomBucksAccount acct1 = new ZoomBucksAccount();
		acct1.setBirthDateStr("19870221");
		acct1.setFullName("gwenn_archor");
		acct1.setEmail("gwenn_archor@hotmail.com");
		acct1.setGender("F");
		acct1.setPassword("baoziazhu609");
		accountList.add(acct1);
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
		for (int i = 0; i < accountList.size(); i++) {
			ZoomBucksAccount zaccount = accountList.get(i);
			ZoomBucksLabor zoombucksLabor = new ZoomBucksLabor(zaccount, driver);
			try {
				ZoomBucksOperator.login(driver, zaccount);
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
}
