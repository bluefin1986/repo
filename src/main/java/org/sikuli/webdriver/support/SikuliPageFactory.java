package org.sikuli.webdriver.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.sikuli.webdriver.SikuliFirefoxDriver;

public class SikuliPageFactory {

	public static <T> T initElements(SikuliFirefoxDriver driver, Class<T> pageClassToProxy) {
	    T page = instantiatePage(driver, pageClassToProxy);
	    initElements(driver, page);
		return page;
	}

	public static void initElements(SikuliFirefoxDriver driverRef, Object page) {
		initElements(new DefaultImageElementLocatorFactory(driverRef), page);
	}

	static void initElements(ImageElementLocatorFactory factoryRef, Object page) {
		PageFactory.initElements(new ImageElementFieldDecorator(factoryRef), page);
	}


	private static <T> T instantiatePage(WebDriver driver, Class<T> pageClassToProxy) {
		try {
			try {
				Constructor<T> constructor = pageClassToProxy.getConstructor(WebDriver.class);
				return constructor.newInstance(driver);
			} catch (NoSuchMethodException e) {
				return pageClassToProxy.newInstance();
			}
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
