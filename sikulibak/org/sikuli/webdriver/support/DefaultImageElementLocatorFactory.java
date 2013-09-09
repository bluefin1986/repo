package org.sikuli.webdriver.support;

import java.lang.reflect.Field;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.DefaultElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.sikuli.webdriver.SikuliFirefoxDriver;

class DefaultImageElementLocatorFactory implements ImageElementLocatorFactory {

	final private SikuliFirefoxDriver driver;
	public DefaultImageElementLocatorFactory(SikuliFirefoxDriver driverRef) {
		driver = driverRef;
	}

	@Override
	public ImageElementLocator createImageElementLocator(Field field) {
		return new DefaultImageElementLocator(driver, field);
	}

	@Override
	public ElementLocator createLocator(Field field) {
		return new DefaultElementLocator(driver, field);
	}

}