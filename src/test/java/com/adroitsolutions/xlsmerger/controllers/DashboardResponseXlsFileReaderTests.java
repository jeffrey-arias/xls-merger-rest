package com.adroitsolutions.xlsmerger.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.adroitsolutions.xlsmerger.BatchType;
import com.adroitsolutions.xlsmerger.SystemConfigHelper;

@SpringBootTest
class DashboardResponseXlsFileReaderTests {
	
	private Log logger = LogFactory.getLog(getClass());
	@Autowired
	private SystemConfigHelper systemConfigHelper;

	private DashboardResponseXlsFileReaderImpl xlsFileReader;

	@BeforeEach
	void contextLoads() {
		xlsFileReader = new DashboardResponseXlsFileReaderImpl();
		xlsFileReader.setSystemConfigHelper(systemConfigHelper);
		xlsFileReader.setReadDirectory("c:/test");
	}
	

//	@Test
	public void testMergerDateNoFiles() throws Exception {
		DashboardResponse response = xlsFileReader.readXlsFileAndBuildReponse("1619045543560");
		assert (response.getStatus().equalsIgnoreCase("SUCCESS"));
	}
}
