package com.bluefin.zoombucks.model;

public class ProfileSurvey {

	private String surveyDesc;
	
	private String href;
	
	private String anchrText;

	public ProfileSurvey(String surveyDesc, String href, String anchrText){
		this.surveyDesc = surveyDesc;
		this.href = href;
		this.anchrText = anchrText;
	}
	
	public String getSurveyDesc() {
		return surveyDesc;
	}

	public void setSurveyDesc(String surveyDesc) {
		this.surveyDesc = surveyDesc;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getAnchrText() {
		return anchrText;
	}

	public void setAnchrText(String anchrText) {
		this.anchrText = anchrText;
	}
	
	
}
