package org.sikuli.webdriver.examples;

import java.io.IOException;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.sikuli.webdriver.ImageElement;
import org.sikuli.webdriver.SikuliFirefoxDriver;
import org.sikuli.webdriver.support.FindByImage;
import org.sikuli.webdriver.support.SikuliPageFactory;

public class PageObjectsExample {
	public static void main(String[] args) throws IOException {

		SikuliFirefoxDriver driver = new SikuliFirefoxDriver();
		driver.get("http://map.google.com/");

		GoogleMapPage page = SikuliPageFactory.initElements(driver, GoogleMapPage.class);
		page.searchFor("Denver, CO");		
		
		DenverArea denverMap = SikuliPageFactory.initElements(driver, DenverArea.class);
		denverMap.lakewood.doubleClick();
		
		denverMap.searchFor("hotel");
		
		denverMap.zoomIn();

	}
	
	public static class DenverArea extends GoogleMapPage {
		@FindByImage(url = "https://dl.dropbox.com/u/5104407/lakewood.png")
		public ImageElement lakewood;				
	} 
	
	public static class GoogleMapPage {

		@FindBy(how = How.ID, using = "gbqfq")
		private WebElement searchInput;

		@FindByImage(url = "https://dl.dropbox.com/u/5104407/plus.png")
		private ImageElement plus;
		
		public void zoomIn(){
			plus.click();
		}

		public void searchFor(String text) {
			searchInput.clear();
			searchInput.sendKeys(text);
			searchInput.submit();
		}
	} 

}


