package com.bluefin.zoombucks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import com.bluefin.zoombucks.labor.ZoomBucksLabor;
import com.bluefin.zoombucks.model.ZoomBucksAccount;

public class LaborTest {
	
	private List<WebDriver> driverList;
	
	private List<ZoomBucksAccount> accountList;

	private boolean[] finishFlags;
	
	private void initDrivers(int accountCount){
		driverList = new ArrayList<WebDriver>();
		String[] profiles = new String[]{
				"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/pzodczhc.selenium"
				,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/z8kseba1.selenium2"
				,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/sli4o4cr.selenium3"
				,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/hd8ge2oz.selenium4"};
//		String[] profiles = new String[]{
//				"C:/Documents and Settings/dvlp/Application Data/Mozilla/Firefox/Profiles/rx1c1n2i.selenium1"
//				,"C:/Documents and Settings/dvlp/Application Data/Mozilla/Firefox/Profiles/6v81xj5x.selenium2"
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
	
	private FirefoxDriver generateFirefoxDriver(){
		File profilePath = new File("/Users/bluefin8603/Library/Application Support/Firefox/Profiles/pzodczhc.selenium");
		FirefoxProfile fp = new FirefoxProfile(profilePath);
		FirefoxDriver firefoxDriver = new FirefoxDriver(fp);
		firefoxDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		return firefoxDriver;
	}
	
	@Before
	public void init() throws IOException {
		accountList = new ArrayList<ZoomBucksAccount>();
		ZoomBucksAccount acct0 = new ZoomBucksAccount();
		acct0.setBirthDateStr("19820501");
		acct0.setFullName("harry_nocora");
		acct0.setEmail("harry_nocora@hotmail.com");
		acct0.setGender("M");
		acct0.setPassword("baoziazhu609");
		accountList.add(acct0);
		ZoomBucksAccount acct = new ZoomBucksAccount();
		acct.setBirthDateStr("19860501");
		acct.setFullName("nucle_hobson");
		acct.setEmail("nucle_hobson@hotmail.com");
		acct.setGender("M");
		acct.setPassword("baoziazhu609");
		accountList.add(acct);
		ZoomBucksAccount acct1 = new ZoomBucksAccount();
		acct1.setBirthDateStr("19870511");
		acct1.setFullName("nico_albin0011");
		acct1.setEmail("nico_albin0011@hotmail.com");
		acct1.setGender("F");
		acct1.setPassword("baoziazhu609");
		accountList.add(acct0);
	}
	
	@Test
	public void testZoombuckSurvey() throws Exception{
		WebDriver driver = null;
		for (int i = 0; i < accountList.size(); i++) {
			driver = generateFirefoxDriver();
			ZoomBucksAccount account = accountList.get(i);
//			new ZoomBucksLabor(account, driver).start();
			new ZoomBucksLabor(account, driver).run();
			driver.close();
			Thread.sleep(2000);
			driver = generateFirefoxDriver();
		}
//		startDaemon();
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
