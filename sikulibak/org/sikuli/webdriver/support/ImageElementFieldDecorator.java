package org.sikuli.webdriver.support;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.DefaultFieldDecorator;
import org.openqa.selenium.support.pagefactory.FieldDecorator;
import org.sikuli.webdriver.ImageElement;

class ImageElementFieldDecorator extends DefaultFieldDecorator implements FieldDecorator {

	final private ImageElementLocatorFactory factory;

	public ImageElementFieldDecorator(ImageElementLocatorFactory factoryRef) {
		super(factoryRef);
		this.factory = factoryRef;
	}

	@Override
	public Object decorate(ClassLoader loader, Field field) {
		if (WebElement.class.isAssignableFrom(field.getType())){
			return super.decorate(loader,  field);
		}else{
			return decorateAsImageElement(loader, field);
		}
	}

	private Object decorateAsImageElement(ClassLoader loader, Field field){
		if (!(ImageElement.class.isAssignableFrom(field.getType())
				|| isDecoratableList(field))) {
			return null;
		}

		ImageElementLocator locator = factory.createImageElementLocator(field);
		if (locator == null) {
			return null;
		}

		if (ImageElement.class.isAssignableFrom(field.getType())) {
			return proxyForImageElementLocator(loader, locator);
		} else if (List.class.isAssignableFrom(field.getType())) {
			return proxyForImageElementListLocator(loader, locator);
		} else {
			return null;
		}
	}


	private boolean isDecoratableList(Field field) {
		if (!List.class.isAssignableFrom(field.getType())) {
			return false;
		}

		// Type erasure in Java isn't complete. Attempt to discover the generic
		// type of the list.
		Type genericType = field.getGenericType();
		if (!(genericType instanceof ParameterizedType)) {
			return false;
		}

		Type listType = ((ParameterizedType) genericType).getActualTypeArguments()[0];

		if (!ImageElement.class.equals(listType)) {
			return false;
		}


		//		if (field.getAnnotation(FindBy.class) == null &&
		//				field.getAnnotation(FindBys.class) == null) {
		//			return false;
		//		}

		if (field.getAnnotation(FindBy.class) == null){
			return false;
		}
		return true;
	}

	protected ImageElement proxyForImageElementLocator(ClassLoader loader, ImageElementLocator locator) {
		InvocationHandler handler = new LocatingImageElementHandler(locator);

		ImageElement proxy;
		//		proxy = (ImageElement) Proxy.newProxyInstance(
		//				loader, new Class[] {ImageElement.class, WrapsElement.class, Locatable.class}, handler);
		proxy = (ImageElement) Proxy.newProxyInstance(
				loader, new Class[] {ImageElement.class}, handler);

		return proxy;
	}

	@SuppressWarnings("unchecked")
	protected List<ImageElement> proxyForImageElementListLocator(ClassLoader loader, ImageElementLocator locator) {
		InvocationHandler handler = new LocatingImageElementListHandler(locator);

		List<ImageElement> proxy;
		proxy = (List<ImageElement>) Proxy.newProxyInstance(
				loader, new Class[] {List.class}, handler);
		return proxy;
	}
}