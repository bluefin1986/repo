package com.bluefin.askmesurveys.labor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.bluefin.askmesurveys.model.AskmeSurveyAccount;
import com.bluefin.base.Operator;

public class AskmeSurveysOperator implements Operator<AskmeSurveyAccount> {

	@Override
	public void login(WebDriver driver, AskmeSurveyAccount account)
			throws Exception {
		driver.get("https://www.askmesurveys.com/index.php?mode=logout");
		driver.findElement(By.name("txtEmail")).sendKeys(account.getEmail());
		driver.findElement(By.name("txtPassword")).sendKeys(account.getPassword());
		driver.findElement(By.name("bt_submit")).click();
		
	}

}
