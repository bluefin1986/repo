package org.sikuli.webdriver.support;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.Annotations;
import org.openqa.selenium.support.pagefactory.DefaultElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.sikuli.webdriver.ImageElement;
import org.sikuli.webdriver.SikuliFirefoxDriver;

class DefaultImageElementLocator extends DefaultElementLocator
	implements ImageElementLocator, ElementLocator {

	final private SikuliFirefoxDriver driver;
	final private FindByImage findBy;
	public DefaultImageElementLocator(SikuliFirefoxDriver driverRef, Field field) {
		super(driverRef, field);		
		
	    findBy = field.getAnnotation(FindByImage.class);
	    driver = driverRef;
	}

	@Override
	public ImageElement findImageElement() {
		ImageElement element;
		try {
			element = driver.findImageElement(new URL(findBy.url()));
		} catch (MalformedURLException e) {
			return null;
		}
		return element;
	}

	@Override
	public List<ImageElement> findImageElements() {
		throw new UnsupportedOperationException();
	}

}