package org.sikuli.webdriver.support;

import java.util.List;

import org.openqa.selenium.By;
import org.sikuli.webdriver.ImageElement;

interface ImageSearchContext {
	List<ImageElement> findImageElements(By by);
	ImageElement findImageElement(By by);
}