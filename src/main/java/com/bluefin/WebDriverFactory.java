package com.bluefin;

import java.io.File;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class WebDriverFactory {
	
	private static String[] profilesMacOs = new String[]{
			"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/pzodczhc.selenium"
			,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/z8kseba1.selenium2"
			,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/sli4o4cr.selenium3"
			,"/Users/bluefin8603/Library/Application Support/Firefox/Profiles/hd8ge2oz.selenium4"};
	private static String[] profilesWin = new String[]{
			"C:/Documents and Settings/dvlp/Application Data/Mozilla/Firefox/Profiles/6v81xj5x.selenium2"
			,"C:/Documents and Settings/dvlp/Application Data/Mozilla/Firefox/Profiles/rx1c1n2i.selenium1"
	};
	
	public static FirefoxDriver generateFirefoxDriver(){
		
		Random ra = new Random();
		Properties prop = System.getProperties();
		String os = prop.getProperty("os.name");
		System.out.println(os);
		File profilePath;
		if(os.startsWith("win") || os.startsWith("Win")){
			int index = ra.nextInt(profilesWin.length);
			if(index == 0){
				index = 1;
			}
			profilePath = new File(profilesWin[index - 1]);
		} else {
			int index = ra.nextInt(profilesMacOs.length);
			if(index == 0){
				index = 1;
			}
			profilePath = new File(profilesMacOs[index]);
		}
		FirefoxProfile fp = new FirefoxProfile(profilePath);
		FirefoxDriver firefoxDriver = new FirefoxDriver(fp);
//		SikuliFirefoxDriver firefoxDriver = new SikuliFirefoxDriver(fp);
		firefoxDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		return firefoxDriver;
	}
}
