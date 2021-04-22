package com.adroitsolutions.xlsmerger;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XlsMergerApplicationTests {
	
	private Log logger = LogFactory.getLog(getClass());
	@Autowired
	private SystemConfigHelper systemConfigHelper;
 
	private XlsFileMergerImpl merger;

	@BeforeEach
	void contextLoads() {
		merger = new XlsFileMergerImpl();
		merger.setSystemConfigHelper(systemConfigHelper);
		merger.setLogger(logger);
		merger.setReadDirectory("c:/Read");
		merger.setTimeStamp(String.valueOf(Calendar.getInstance().getTime().getTime()));
	}
	

//	@Test
	public void testMergerDateNoFiles() throws Exception {
		
		String dateFrom = LocalDate.now().toString();
		String dateTo = LocalDate.now().toString();

		merger.mergeExcelFiles(dateFrom, dateTo, BatchType.ALL);
		
        FileInputStream nfis = new FileInputStream(merger.getMergedFileLoc());
		assert(nfis.available() == 4096);
		
		nfis.close();
	}

	@Test
	public void testMergerDateAllFiles() throws Exception {
		
		merger.setReadDirectory("C:/unittestread");
		String dateFrom = "2019-01-01";

		String dateTo = LocalDate.now().toString();

		merger.mergeExcelFiles(dateFrom, dateTo, BatchType.ALL);
		
		
        FileInputStream nfis = new FileInputStream(merger.getMergedFileLoc());
		assert(nfis.available() > 4096);
		
		nfis.close();
	}
	
//	@Test
	public void testMergerDateNIP_INFLOW_NOT_REPORTED() throws Exception {
		
		merger.setReadDirectory("C:/unittestread2");
		String dateFrom = "2019-01-01";

		String dateTo = LocalDate.now().toString();

		merger.mergeExcelFiles(dateFrom, dateTo, BatchType.NIP_INFLOW_NOT_REPORTED);
		
        FileInputStream nfis = new FileInputStream(merger.getMergedFileLoc());
		assert(nfis.available() > 4096);
		
		nfis.close();
	}


}
