package com.bluefin.askmesurveys.model;

import com.bluefin.base.AbstractAccount;

public class AskmeSurveyAccount extends AbstractAccount{

	@Override
	public AskmeSurveyAccount clone() {
		AskmeSurveyAccount za = new AskmeSurveyAccount();
		za.setBirthDateStr(this.birthDateStr);
		za.setEmail(this.email);
		za.setFullName(this.fullName);
		za.setPassword(this.password);
		za.setGender(this.gender);
		return za;
	}

}
