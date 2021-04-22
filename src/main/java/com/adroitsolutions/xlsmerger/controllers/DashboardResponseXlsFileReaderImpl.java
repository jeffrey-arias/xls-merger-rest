package com.adroitsolutions.xlsmerger.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adroitsolutions.xlsmerger.BatchType;
import com.adroitsolutions.xlsmerger.SystemConfigHelper;

@Component
public class DashboardResponseXlsFileReaderImpl implements DashboardResponseXlsFileReader {
	
	private Log logger = LogFactory.getLog(this.getClass()); 
	
	private String readDirectory = "";
	
	@Autowired
	private SystemConfigHelper systemConfigHelper;
	
	@Override
	public DashboardResponse readXlsFileAndBuildReponse(String timeStamp) {
		String fileLocation = systemConfigHelper.getFileLocation();
		
		DashboardResponse response = new DashboardResponse();
		response.setStatus("SUCCESS");
		int id = 1;
		for (BatchType batchType : EnumSet.allOf(BatchType.class)) {
			if (batchType == BatchType.ALL) {
				continue;
			}
			DashboardData dashboardData = new DashboardData();
			long numberOfRows = 0;
			try {
				numberOfRows = countXlsLines(batchType, timeStamp, fileLocation);
			} catch (IOException e) {
				logger.error("Error encountered while uploading data for: "+batchType.toString()+"\n"+e.getMessage());
				response.setData(null);
				response.setStatus("Error encountered while uploading data for: "+batchType.toString());
				break;
			} catch (Exception e) {
				logger.error("Error encountered while uploading data for: "+batchType.toString()+"\n"+e.getMessage());
				response.setData(null);
				response.setStatus("Error encountered while uploading data for: "+batchType.toString());
				break;
			}
			dashboardData.setId(id);
			id++;
			dashboardData.setName(batchType.toString());
			dashboardData.setValue(numberOfRows);
			
			response.addData(dashboardData);
			logger.info("Data loading for "+batchType.toString()+" is a SUCCESS");
		}
		return response;
	}
	
	private long countXlsLines(BatchType batchType, String timeStamp, String fileLocation) throws IOException {
		long numberOfRows = 0;

		if (fileLocation.isBlank()) {
			logger.error("Property file is not properly configured! Please set the file.localtion properly in config.properties");
		} else {
			
			String fileName = fileLocation+System.getProperty("file.separator")
								+timeStamp
								+"_tmpdata_"
								+batchType.toString()
								+"_Merged_Not_Reported.xls";
			logger.info("Reading "+fileName+"...");
			FileInputStream fin = null;			 
            HSSFWorkbook workbook = null;
			try {
				fin = new FileInputStream(fileName);				 
	            workbook = new HSSFWorkbook(fin);
	            HSSFSheet sheet = workbook.getSheetAt(0);
	            int totalAmtRows = getNumberOfTotalAmtRows(sheet);
	            logger.debug("Raw number of rows: "+sheet.getPhysicalNumberOfRows());
	            logger.debug("Total AMT rows: "+totalAmtRows);
	            numberOfRows = sheet.getPhysicalNumberOfRows() - 1 - totalAmtRows;
	            logger.debug("Total DATA rows: "+numberOfRows);

			} catch (FileNotFoundException e) {
				logger.error("Error encountered while reading file: "+fileName+" "+e.getMessage());
			} catch (IOException e) {
				logger.error("Error encountered while reading file: "+fileName+" "+e.getMessage());
			} finally {
				fin.close();
				workbook.close();
			}
		}
		return numberOfRows;
	}
    private int getNumberOfTotalAmtRows (HSSFSheet sheet) {
    	int counter = 0;
    	for (int i = 0; i <= sheet.getLastRowNum(); i++) {
    	    Row row = sheet.getRow(i);

    	    if (row.getCell(0).getStringCellValue().equalsIgnoreCase("TOTAL TRANSACTIONS")) {
    	    	counter++;
    	    }
    	}
	    return counter;
    }
    
	public String getReadDirectory() {
		return readDirectory;
	}

	public void setReadDirectory(String readDirectory) {
		this.readDirectory = readDirectory;
	}

	public SystemConfigHelper getSystemConfigHelper() {
		return systemConfigHelper;
	}

	public void setSystemConfigHelper(SystemConfigHelper systemConfigHelper) {
		this.systemConfigHelper = systemConfigHelper;
	}

}
