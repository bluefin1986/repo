package org.sikuli.webdriver;

import org.openqa.selenium.firefox.FirefoxDriver;

public class SikuliFirefoxDriver extends FirefoxDriver {

	static Logger logger = LoggerFactory.getLogger(SikuliFirefoxDriver.class);

	private static final int DEFAULT_WAIT_TIMEOUT_MSECS = 10000;
	ScreenRegion webdriverRegion;

	public SikuliFirefoxDriver(){
		WebDriverScreen webDriverScreen;
		try {
			webDriverScreen = new WebDriverScreen(this);
		} catch (IOException e1) {
			throw new RuntimeException("unable to initialize SikuliFirefoxDriver");
		}
		webdriverRegion = new DefaultScreenRegion(webDriverScreen);
	}
	
	public SikuliFirefoxDriver(FirefoxProfile fp){
		super(new FirefoxBinary(), fp);
		WebDriverScreen webDriverScreen;
		try {
			webDriverScreen = new WebDriverScreen(this);
		} catch (IOException e1) {
			throw new RuntimeException("unable to initialize SikuliFirefoxDriver");
		}
		webdriverRegion = new DefaultScreenRegion(webDriverScreen);
	}

	public WebElement findElementByLocation(int x, int y){
		return (WebElement) ((JavascriptExecutor) this).executeScript("return document.elementFromPoint(" + x + "," + y + ")");
	}

	public ImageElement findImageElement(URL imageUrl) {
		ImageTarget target = new ImageTarget(imageUrl);
		final ScreenRegion imageRegion = webdriverRegion.wait(target, DEFAULT_WAIT_TIMEOUT_MSECS);
		
		
		if (imageRegion != null){
			Rectangle r = imageRegion.getBounds();
			logger.debug("image is found at {} {} {} {}", r.x, r.y, r.width, r.height);
		}else{
			logger.debug("image is not found");
			return null;
		}


		ScreenLocation center = imageRegion.getCenter();
		WebElement foundWebElement = findElementByLocation(center.getX(), center.getY());
		Rectangle r = imageRegion.getBounds();
		return new DefaultImageElement(this, foundWebElement, 
				r.x,
				r.y,
				r.width,
				r.height);
	}
}
