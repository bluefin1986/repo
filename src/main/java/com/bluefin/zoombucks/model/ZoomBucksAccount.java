package com.bluefin.zoombucks.model;

public class ZoomBucksAccount implements Cloneable{

	private String fullName;
	
	private String email;
	
	private String password;
	
	private String birthDateStr;
	
	private String gender;
	
	private boolean loggedIn;

	public String getBirthDateStr() {
		return birthDateStr;
	}

	public void setBirthDateStr(String birthDateStr) {
		this.birthDateStr = birthDateStr;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName.trim();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email.trim();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

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
