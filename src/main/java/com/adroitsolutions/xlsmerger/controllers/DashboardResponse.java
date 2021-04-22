package com.adroitsolutions.xlsmerger.controllers;

import java.util.ArrayList;
import java.util.List;

public class DashboardResponse {
	private String status;
	
	private List<DashboardData> data = new ArrayList<DashboardData>();
	
	public void addData (DashboardData dashboardData) {
		data.add(dashboardData);
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public List<DashboardData> getData() {
		return data;
	}

	public void setData(List<DashboardData> data) {
		this.data = data;
	}
}
