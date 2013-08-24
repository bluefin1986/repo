package com.bluefin.zoombucks.labor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.bluefin.zoombucks.TaskPool;
import com.bluefin.zoombucks.model.SearchEngineTask;
import com.bluefin.zoombucks.model.TaskSummary;
import com.bluefin.zoombucks.model.ZoomBucksAccount;

public class SearchEngineTaskLabor extends Thread {

	private WebDriver driver;

	private TaskPool taskPool;

	private TaskSummary taskSummary;
	
	private ZoomBucksAccount zaccount;
	
	public SearchEngineTaskLabor(WebDriver driver, TaskPool taskPool, TaskSummary taskSummary, ZoomBucksAccount zaccount) {
		this.driver = driver;
		this.taskPool = taskPool;
		this.taskSummary = taskSummary;
		this.zaccount = zaccount;
	}

	public void run() {
		SearchEngineTask task;
		try {
			if(!zaccount.isLoggedIn()){
				ZoomBucksOperator.login(driver, zaccount);
				ZoomBucksOperator.ssoToTaskSite(driver, zaccount);
			}
			
		} catch (Exception e1) {
			throw new RuntimeException("login failed!");
		}
		while ((task = taskPool.getSearchEngineTask()) != null) {
			try {
				doSearchEngineTasks(task);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		driver.quit();
	}

	/**
	 * 执行搜索引擎task
	 * 
	 * @param searchEngineTaskMap
	 * @throws InterruptedException
	 */
	private void doSearchEngineTasks(SearchEngineTask searchEngineTask)
			throws InterruptedException {
		String content = loadInputContent();
		Random ra = new Random();
		System.out.println("now:" + searchEngineTask.getTaskHref());
		driver.get(searchEngineTask.getTaskHref());
		Thread.sleep(1000);
		WebElement submit = null;
		try {
			List<WebElement> elements = driver.findElements(By
					.xpath("//div[@class='hero-unit']/h1"));
			if (elements.size() > 0) {
				WebElement ele = elements.get(0);
				String text = ele.getText();
				if (text.startsWith("You've")) {
					throw new Exception("already done!");
				}
				System.out.println("text:" + text);
				if (text.startsWith("There is") || text.startsWith("This task")) {
					throw new Exception("unknown");
				}
			}
			submit = driver.findElement(By.xpath("//input[@type='submit']"));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			taskSummary.plusFailed();
			taskSummary.taskPassed();
			taskPool.addFailTask(searchEngineTask);
			System.out.println("total tasks: " + taskSummary.getTaskCount()
					+ ", rest tasks:" + taskSummary.getRestTaskCount()
					+ ", failed:" + taskSummary.getFailCount());
			return;
		}
		// 选择所有的下拉
		List<WebElement> selects = driver.findElements(By.xpath("//select"));
		for (WebElement selElement : selects) {
			Select sel = new Select(selElement);
			int length = sel.getOptions().size();
			int pickIndex = ra.nextInt(length);
			if (pickIndex < 2) {
				pickIndex = 2;
			}
			sel.selectByIndex(pickIndex);
		}
		boolean isInput = false;
		boolean meetRegexError = false;
		// 填写input内容
		try {
			List<WebElement> questionDivs = driver.findElements(By
					.xpath("//div[@class='text cml_field']"));
			if (questionDivs.size() == 0) {
				throw new Exception("not found text inputs");
			}
			for (WebElement div : questionDivs) {
				WebElement input = div.findElement(By.tagName("input"));
				if (input == null) {
					System.out.println("not exist");
				}
				String answer = div.getAttribute("data-validates-regex");
				if (answer.startsWith("(")) {
					JavascriptExecutor executor = (JavascriptExecutor) driver;
					executor.executeScript(
							"arguments[0].parentNode.removeChild(arguments[0])",
							input);
					// if(silentMode){
					// failedUrls.add(searchEngineTask.getTaskHref());
					// meetRegexError = true;
					// break;
					// }
					// JOptionPane.showMessageDialog(null,
					// "found phone num task, should be handled manually");
				} else {
					input.sendKeys(answer);
				}
			}
			isInput = true;
		} catch (Exception ex) {
			System.out.println("inputs not found:" + ex.getMessage());
		}
		// 填写textarea内容
		try {
			if (isInput) {
				throw new Exception("not textarea task");
			}
			List<WebElement> textAreas = driver.findElements(By
					.xpath("//textarea"));
			for (WebElement textArea : textAreas) {
				int beginIndex = ra.nextInt(content.length());
				if (beginIndex == content.length()
						|| content.length() - beginIndex > 30) {
					beginIndex = content.length() - 30;
				}
				textArea.sendKeys(content.substring(beginIndex));
			}
		} catch (Exception ex) {
			System.out.println("text areas not found:" + ex.getMessage());
		}
		taskSummary.taskPassed();
		if (meetRegexError) {
			taskSummary.plusFailed();
			taskPool.addFailTask(searchEngineTask);
		} else {
			// 提交
			submit.click();
			taskSummary.plusEarned(searchEngineTask.getBonus());
			String desc = searchEngineTask.getTaskDesc().length() == 0 ? searchEngineTask
					.getTaskHref() : searchEngineTask.getTaskDesc();
			System.out.println("[" + desc + "] finished. "
					+ searchEngineTask.getBonus() + " bonus earned, total:"
					+ taskSummary.getTotalEarned());
			System.out.println("total tasks: " + taskSummary.getTaskCount() + ", rest tasks:"
					+ taskSummary.getRestTaskCount() + ", failed:"
					+ taskSummary.getFailCount());
		}
	}

	private String loadInputContent() {
		try {
			String content = FileUtils.readFileToString(new File(
					"src/main/resources/InputContent.txt"));
			return content;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Presidents Day may refer to: Presidents Day (United States), a holiday in some regions";
	}
}
