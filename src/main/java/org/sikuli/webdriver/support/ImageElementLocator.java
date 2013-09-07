package org.sikuli.webdriver.support;

import java.util.List;

import org.sikuli.webdriver.ImageElement;

interface ImageElementLocator {
	ImageElement findImageElement();
	List<ImageElement> findImageElements();
}