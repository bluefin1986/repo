package org.sikuli.webdriver.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sikuli.webdriver.ImageElement;

class LocatingImageElementHandler implements InvocationHandler {
	private final ImageElementLocator locator;

	public LocatingImageElementHandler(ImageElementLocator locator) {
		this.locator = locator;
	}

	public Object invoke(Object object, Method method, Object[] objects) throws Throwable {
		ImageElement element = locator.findImageElement();

		if ("getWrappedElement".equals(method.getName())) {
			return element;
		}

		try {
			return method.invoke(element, objects);
		} catch (InvocationTargetException e) {
			// Unwrap the underlying exception
			throw e.getCause();
		}
	}
}