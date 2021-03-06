package com.bluefin.zoombucks.model;

import com.bluefin.base.AbstractAccount;

public class ZoomBucksAccount extends AbstractAccount{

	public ZoomBucksAccount clone(){
		ZoomBucksAccount za = new ZoomBucksAccount();
		za.setBirthDateStr(this.birthDateStr);
		za.setEmail(this.email);
		za.setFullName(this.fullName);
		za.setPassword(this.password);
		za.setGender(this.gender);
		return za;
	}
}
