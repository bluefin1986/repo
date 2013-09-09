package com.bluefin.zoombucks.labor;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.bluefin.base.Task;
import com.bluefin.zoombucks.TaskPool;
import com.bluefin.zoombucks.model.TaskSummary;
import com.bluefin.zoombucks.model.ZoomBucksAccount;

public class SurveySlave extends Thread{
	
	private WebDriver driver;

	private TaskPool taskPool;

	private TaskSummary taskSummary;
	
	private ZoomBucksAccount zaccount;
	
	private ZoomBucksOperator operator;
	
	public SurveySlave(WebDriver driver, TaskPool taskPool, TaskSummary taskSummary,ZoomBucksAccount zaccount) {
		this.driver = driver;
		this.taskPool = taskPool;
		this.zaccount = zaccount;
		this.taskSummary = taskSummary;
		operator = new ZoomBucksOperator();
	}
	
	public void run(){
		Task task;
		try {
			boolean isSlaveThread = false;
			if(!zaccount.isLoggedIn()){
				operator.login(driver, zaccount);
				operator.ssoToSurveySite(driver, zaccount);
				isSlaveThread = true;
			}
			while ((task = taskPool.getSearchEngineTask()) != null) {
				try {
					doSurvey(task);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(isSlaveThread){
				driver.quit();
			}
		} catch (Exception e1) {
			throw new RuntimeException("login failed!");
		}
	}
	
	private void doSurvey(Task task) throws InterruptedException {
		taskSummary.taskPassed();
		System.out.println("[" + task.getTaskDesc() + "] " + task.getTaskHref() + " begin");
		driver.get(task.getTaskHref());
		Thread.sleep(3000);
		By by = By.name("Next");
		WebElement nextButton;
		try {
			Date begin = new Date();
			while (true) {
				Date end =  new Date();
				//survey经常挂掉，超时刷新一下。
				if(end.getTime() - begin.getTime() >= 60000){
					driver.get(driver.getCurrentUrl());
					begin = new Date();
					continue;
				}
				try {
					nextButton = driver.findElement(by);
				} catch (Exception e) {
					boolean successed = false;
					if(driver.findElements(By.xpath("//div[@class='confirmBox']")).size() > 0){
						successed = true;
					}
					if(!successed && driver.findElements(By.id("profiles")).size() > 0){
						successed = true;
					}
					if(successed){
						taskSummary.plusEarned(task.getBonus());
						System.out.println("[" + task.getTaskDesc()
								+ "] finished " + task.getBonus() + " earned, total:"
								+ taskSummary.getTotalEarned());
						break;
					}
					continue;
				}
				Thread.sleep(500);
				WebElement questionTableNode = driver.findElement(By
						.xpath("//td[@class='surveyInner-Table']"));

				WebElement typeDetect = questionTableNode.findElement(By
						.xpath("table/tbody/tr[2]/td/table/tbody/tr/td/*[1]"));
				String tagName = typeDetect.getTagName();
				Random ra = new Random();
				if ("select".equals(tagName)) {
					Select select = new Select(typeDetect);
					int optionsCount = select.getOptions().size();
					int selectIndex = ra.nextInt(optionsCount);
					if (selectIndex < 2) {
						selectIndex = optionsCount - 1;
					}
					select.selectByIndex(selectIndex);
				} else if ("textarea".equals(tagName)) {
					typeDetect.sendKeys("N/A");
				} else {
					try {
						WebElement checkRadioContainer = driver.findElement(By
								.xpath("//td[@class='checkRadioContainer']"));
						if (checkRadioContainer != null) {
							typeDetect = checkRadioContainer.findElement(By
									.tagName("input"));
							String type = typeDetect.getAttribute("type");
							if ("checkbox".equals(type)) {
								List<WebElement> checkboxList = driver
										.findElements(By
												.xpath("//input[@type='checkbox']"));
								int listSize = checkboxList.size();
								int totalChecked = ra.nextInt(listSize);
								while (totalChecked == 0) {
									totalChecked = ra.nextInt(listSize);
								}
								// 最多选5个提高效率
								if (totalChecked > 5) {
									totalChecked = 5;
								}

								for (int i = 0; i < totalChecked; i++) {
									WebElement chkBox = checkboxList.get(ra.nextInt(listSize));
									if(chkBox.isSelected()){
										continue;
									}
									chkBox.click();
								}
							} else {
								List<WebElement> radioList = driver
										.findElements(By
												.xpath("//input[@type='radio']"));
								int listSize = radioList.size();
								radioList.get(ra.nextInt(listSize)).click();
							}
						}
					} catch (Exception e) {
						System.err.println(e.getMessage());
						WebElement tableRadioElement = driver.findElement(By
								.xpath("//table[@class='tableBg']"));
						List<WebElement> cols = tableRadioElement
								.findElements(By
										.xpath("tbody/tr/td/table/tbody/tr[1]/td"));
						List<WebElement> rows = tableRadioElement
								.findElements(By
										.xpath("tbody/tr/td/table/tbody/tr"));
						int colsCount = cols.size();
						for (int i = 1; i < rows.size(); i++) {
							List<WebElement> inputs = rows.get(i).findElements(
									By.tagName("input"));
							inputs.get(ra.nextInt(colsCount - 1)).click();
						}
					}
				}
				nextButton.click();
				Thread.sleep(600);
//				try {
//					List<WebElement> inputs = driver.findElements(By.id("quest_no"));
//					if(inputs.size() > 0){
//						inputs.get(0).click();
//						driver.findElement(By.xpath("//button[@class='button-dashboard']")).click();
//						taskSummary.plusEarned(task.getBonus());
//						System.out.println("[" + task.getTaskDesc()
//								+ "] finished " + task.getBonus() + " earned, total:"
//								+ taskSummary.getTotalEarned());
//						break;
//					}
//				} catch (Exception e) {
//					Thread.sleep(5000);
//				}
			}
		} catch (Exception e) {
			taskPool.addFailTask(task);
			e.printStackTrace();
		}
		taskSummary.plusFinished();
		System.out.println(taskSummary.getRestTaskCount() + " surveys rest");
	}
}
