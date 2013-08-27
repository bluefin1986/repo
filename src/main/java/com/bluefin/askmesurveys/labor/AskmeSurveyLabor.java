package com.bluefin.askmesurveys.labor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.bluefin.askmesurveys.model.AskmeSurveyAccount;
import com.bluefin.base.Task;

public class AskmeSurveyLabor{

	private AskmeSurveyAccount asAccount;
	
	private WebDriver driver;
	
	private AskmeSurveysOperator operator = new AskmeSurveysOperator();
	
	public AskmeSurveyLabor(WebDriver driver, AskmeSurveyAccount asAccount){
		this.driver = driver;
		this.asAccount = asAccount;
	}
	
	
	public void runProfileSurveys() throws Exception{
		operator.login(driver, asAccount);
		
		driver.get("https://www.askmesurveys.com/dashboard.php");
		List<WebElement> surveys = driver.findElements(By.xpath("//div[@id='divProfileList']/ul/li"));
		List<Task> taskList = new ArrayList<Task>();
		for (WebElement survey : surveys) {
			WebElement anchr = survey.findElement(By.tagName("a"));
			String bonusDesc = anchr.getText();
			if(!bonusDesc.startsWith("Earn $")){
				continue;
			}
			float bonus = 0.0f;
			try {
				bonus = Float.parseFloat(bonusDesc.replace("Earn $", "").trim());
			} catch (Exception e) {
				// TODO: handle exception
			}
			String href = anchr.getAttribute("href");
			String desc = survey.findElement(By.tagName("span")).getText();
			Task task = new Task(desc, href, bonus);
			taskList.add(task);
		}
		for (Task task : taskList) {
			try {
				doSurvey(task);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void doSurvey(Task task) throws InterruptedException {
//		taskSummary.taskPassed();
		System.out.println("[" + task.getTaskDesc() + "] " + task.getTaskHref() + " begin");
		driver.get(task.getTaskHref());
		Thread.sleep(3000);
		By by = By.name("Next");
		WebElement nextButton;
		try {
			while (true) {
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
//						taskSummary.plusEarned(task.getBonus());
//						System.out.println("[" + task.getTaskDesc()
//								+ "] finished " + task.getBonus() + " earned, total:"
//								+ taskSummary.getTotalEarned());
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
			}
		} catch (Exception e) {
//			taskPool.addFailTask(task);
			e.printStackTrace();
		}
//		taskSummary.plusFinished();
//		System.out.println(taskSummary.getRestTaskCount() + " surveys rest");
	}
}
