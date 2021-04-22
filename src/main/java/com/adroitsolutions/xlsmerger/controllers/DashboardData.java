package com.adroitsolutions.xlsmerger.controllers;

/** This is the basic unit of data needed by the Dashboard UI **/
public class DashboardData {

	private int id;
	
	private String name;

	private long value;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
}
