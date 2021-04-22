package com.adroitsolutions.xlsmerger.controllers;

import java.util.Calendar;
import java.util.EnumSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.adroitsolutions.xlsmerger.BatchType;
import com.adroitsolutions.xlsmerger.XlsFileMerger;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

	private Log logger = LogFactory.getLog(this.getClass());
	@Autowired
	private XlsFileMerger xlsFileMerger;
	@Autowired
	private DashboardResponseXlsFileReader dashboardResponseXlsFileReader;

	public XlsFileMerger getXlsFileMerger() {
		return xlsFileMerger;
	}

	public void setXlsFileMerger(XlsFileMerger xlsFileMerger) {
		this.xlsFileMerger = xlsFileMerger;
	}

	@RequestMapping(value = "/createReport")
    @ResponseBody
	public DashboardResponse createReport(@RequestParam (required = false) String dateFrom, @RequestParam (required = false) String dateTo) {
		getXlsFileMerger().setTimeStamp(String.valueOf(Calendar.getInstance().getTime().getTime()));
		DashboardResponse response = new DashboardResponse();
		for (BatchType batchType : EnumSet.allOf(BatchType.class)) {
			String tmpStatus = xlsFileMerger.mergeExcelFiles(dateFrom, dateTo, batchType);
			if (!tmpStatus.equalsIgnoreCase("SUCCESS")) {
				response.setStatus(tmpStatus);
				break;
			} else {
				response.setStatus("SUCCESS");
			}
		}
		if (response.getStatus() == "SUCCESS") {
			response = dashboardResponseXlsFileReader.readXlsFileAndBuildReponse(getXlsFileMerger().getTimeStamp());
			return response;
		}
		return response;
	}
	
	@RequestMapping(value = "/test")
	@ResponseBody
	public String testService(@RequestParam (required = false) String param) {
		String status = "SUCCESS!"+"\n\nYou passed the following parameter: " +param;
		logger.info("You passed the following parameter:" +param);
		return status;
	}

	public DashboardResponseXlsFileReader getDashboardResponseXlsFileReader() {
		return dashboardResponseXlsFileReader;
	}

	public void setDashboardResponseXlsFileReader(DashboardResponseXlsFileReader dashboardResponseXlsFileReader) {
		this.dashboardResponseXlsFileReader = dashboardResponseXlsFileReader;
	}
}


