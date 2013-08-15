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

public class LaborTest {
	
	private List<WebDriver> driverList;

	private String[] accounts = { "olivemacqueen" };
	
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
		if(accountCount > profiles.length){
			throw new RuntimeException("profile数量不够");
		}
		finishFlags = new boolean[accountCount];
		for (int i = 0; i < accountCount; i++) {
			finishFlags[i] = false;
		}
		for (int i = 0; i < accountCount; i++) {
			File profilePath = new File(profiles[i]);
			FirefoxProfile fp = new FirefoxProfile(profilePath);
			FirefoxDriver firefoxDriver = new FirefoxDriver(fp);
			firefoxDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			driverList.add(firefoxDriver);
		}
	}
	
	@Before
	public void init() throws IOException {
		initDrivers(accounts.length);
	}
	
	@Test
	public void testZoombuckSurvey() throws Exception{
		for (int i = 0; i < accounts.length; i++) {
			final WebDriver driver = driverList.get(i);
			final String account = accounts[i];
			new ZoomBucksLabor(account, driver, finishFlags, i).start();
		}
		startDaemon();
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
