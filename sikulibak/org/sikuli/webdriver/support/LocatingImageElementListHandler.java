package org.sikuli.webdriver.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.sikuli.webdriver.ImageElement;

class LocatingImageElementListHandler implements InvocationHandler {
	private final ImageElementLocator locator;

	public LocatingImageElementListHandler(ImageElementLocator locator) {
		this.locator = locator;
	}

	public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
		List<ImageElement> elements = locator.findImageElements();

		try {
			return method.invoke(elements, objects);
		} catch (InvocationTargetException e) {
			// Unwrap the underlying exception
			throw e.getCause();
		}
	}
}