package org.sikuli.webdriver.support;

import java.lang.reflect.Field;

import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

interface ImageElementLocatorFactory extends ElementLocatorFactory {
	ImageElementLocator createImageElementLocator(Field field);
}