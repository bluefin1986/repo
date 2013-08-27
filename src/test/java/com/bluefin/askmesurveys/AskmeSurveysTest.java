package com.bluefin.askmesurveys;

import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.bluefin.WebDriverFactory;
import com.bluefin.askmesurveys.labor.AskmeSurveyLabor;
import com.bluefin.askmesurveys.model.AskmeSurveyAccount;

public class AskmeSurveysTest {
	
	@Test
	public void testSurvey() throws Exception{
		AskmeSurveyAccount account = new AskmeSurveyAccount();
		account.setFullName("hookglurey@hotmail.com");
		account.setPassword("baoziazhu609");
		WebDriver driver = WebDriverFactory.generateFirefoxDriver();
		AskmeSurveyLabor labor = new AskmeSurveyLabor(driver, account);
		labor.runProfileSurveys();
	}
}
