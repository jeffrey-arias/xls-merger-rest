package com.adroitsolutions.xlsmerger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class XlsFileMergerImpl implements XlsFileMerger {
	
	private Log logger = LogFactory.getLog(this.getClass()); 
	
	@Autowired
	private SystemConfigHelper systemConfigHelper;
	
	private String readDirectory = "";

    private String mergedFileLoc = "";
    
    private String timeStamp = "";

	public String mergeExcelFiles(String dateFrom, String dateTo, BatchType batchType) {
		String status = "SUCCESS"; 
		logger.info("Creating merged file of type: "+batchType.toString());
		String propertyFileLocation = systemConfigHelper.getFileLocation();
		if (getReadDirectory().isBlank()) {
			if (propertyFileLocation.isBlank()) {
				logger.error("Property file is not properly configured! Please set the file.localtion properly in config.properties");
				return "Property file is not correctly configured!";
			}
			setReadDirectory(propertyFileLocation);
		}
		File fi = new File(getReadDirectory());
		Calendar c = Calendar.getInstance();

		Date dateFromDtObj = new Date(0);
		Date dateToDtObj = 	c.getTime();
		try {
			if (StringUtils.hasText(dateFrom)) {
				dateFromDtObj = new SimpleDateFormat("yyyy-MM-dd").parse(dateFrom);
			}
			if (StringUtils.hasText(dateTo)) {
				dateToDtObj = new SimpleDateFormat("yyyy-MM-dd").parse(dateTo);
			}
			
			
			// add 1 day to dateToDtObj
			c.setTime(dateToDtObj);
			c.add(Calendar.DATE, 1);
			dateToDtObj = c.getTime();
			
			// subtract 1 day from dateFromDtObj
			c.setTime(dateFromDtObj);
			c.add(Calendar.DATE, -1);
			dateFromDtObj = c.getTime();

		} catch (ParseException e) {
			logger.error("Unable to read dateFrom and/or dateTo parameters\n"+e.getMessage());
			status = "Unable to read dateFrom and/or dateTo parameters\n"+e.getMessage();
			return status;
		}
		
		String xlsSearchString = "";
		String xlsSearchStringReported = "";

		if (batchType == BatchType.ALL) {
			xlsSearchString = ""; 
			xlsSearchStringReported = "";
		} else {
			if (batchType.isNotReported()) {
				xlsSearchString = batchType.toString().replaceAll("_NOT_REPORTED", "").replaceAll("_"," ").toUpperCase();
				xlsSearchStringReported = "NOT";
			} else {
				xlsSearchString = batchType.toString().replaceAll("_REPORTED", "").replaceAll("_"," ").toUpperCase();
			}
		}
		
        if (!fi.exists()) {
            logger.error("Please set the read directory properly first!");
            return "Please set the read directory properly first!";
        } else {
            logger.info("Reading excel files in "+getReadDirectory());
            File mergedFile = new File(createFileName(batchType));
            setMergedFileLoc(mergedFile.getAbsolutePath());
            logger.info("Writing output to: "+mergedFile.getAbsolutePath());
            
            List<String> fileNames = new ArrayList<String>();
            try {
                fileNames = getAllFiles(getReadDirectory());
            } catch (Exception e) {
            	logger.error(e.getMessage());
            	status = e.getMessage();
            	return status;
            }
            List<FileInputStream> files = new ArrayList<FileInputStream>();
            logger.info("Merging the following files: ");
            for (String fileName : fileNames) {
                try {
                	// Ignore previously merged files and only pick up files with a .XLS extension
                    if (!fileName.contains("_Merged_Reported") && fileName.endsWith(".xls") 
                    		// Do not include files that are created by the same request
                    		&& !fileName.contains(getTimeStamp())
                    		// Also do not add temporary data files
                    		&& !fileName.contains("_tmpdata_")
                    		// Add either all files or specific batch types
                    		&& fileName.toUpperCase().contains(xlsSearchString)
                    		&& fileName.toUpperCase().contains(xlsSearchStringReported)) {
                    	if (!xlsSearchString.isEmpty() && xlsSearchStringReported.isEmpty() && fileName.toUpperCase().contains("NOT")) {
                    		continue;
                    	}
                        FileInputStream nfis = new FileInputStream(fileName);
                        Path file = Paths.get(fileName);
                        BasicFileAttributes attr =
                            Files.readAttributes(file, BasicFileAttributes.class);
                        Date fileLastAccessDate = new Date(attr.lastModifiedTime().toMillis());
                        // Only add files that are within the date range
                        if (fileLastAccessDate.after(dateFromDtObj) && fileLastAccessDate.before(dateToDtObj)) {
	                        if (nfis.available() > 0) {
	                            files.add(new FileInputStream(fileName));
	                            logger.info(fileName);
	    
	                        } else {
	                            logger.debug(fileName+"=====> Invalid file. Skipping...");
	
	                        }
                        } else {
                        	logger.debug(fileName+" is not within " +dateFromDtObj+" "+dateToDtObj+"====> Invalid file. Skipping...");
                        }
                        nfis.close(); 
                    } else {
                        logger.debug(fileName+"=====> Invalid file. Skipping...");
                    }
                    
                } catch (FileNotFoundException e) {
                	logger.error(e.getMessage());
                } catch (IOException e) {
                	logger.error(e.getMessage());
                }
            }
            try {
                mergeExcelFiles(mergedFile, files);
            } catch (Exception e) {
            	logger.error(e.getMessage());
            }

        }
        logger.info("Mering file of type "+batchType.toString()+" has a status of "+status);
        return status;
    }
    
	private String createFileName(BatchType batchType) {
		if (batchType == BatchType.ALL) {
			return getReadDirectory()+System.getProperty("file.separator")+getTimeStamp()+"_Merged_Reported.xls";
		} else {
			return getReadDirectory()+System.getProperty("file.separator")+getTimeStamp()+"_tmpdata_"+batchType.toString()+"_Merged_Not_Reported.xls";
		}
	}
    private static List<String> getAllFiles(String readDirectory) throws Exception {
        List<String> fileNames = new ArrayList<String>();
        try (Stream<Path> paths = Files.walk(Paths.get(readDirectory))) {
          paths
              .filter(Files::isRegularFile)
              .forEach(file -> fileNames.add(file.toAbsolutePath().toString()));
      } catch (Exception e) {
          throw e;
      }
        return fileNames;
    }
	
	private void mergeExcelFiles(File file, List<FileInputStream> list) throws Exception {
        HSSFWorkbook book = new HSSFWorkbook();
        HSSFSheet sheet = book.createSheet(file.getName());
        try {
        	boolean isFirstFile = true;
	        for (FileInputStream fin : list) {
	          HSSFWorkbook b = new HSSFWorkbook(fin);
	          for (int i = 0; i < b.getNumberOfSheets(); i++) {
	            copySheets(book, sheet, b.getSheetAt(i), isFirstFile);
	          }
	          isFirstFile = false;
	        }
	        writeFile(book, file);
        } catch (Exception e) {
        	throw e;
        }
      }
      
      protected static void writeFile(HSSFWorkbook book, File file) throws Exception {
        FileOutputStream out = new FileOutputStream(file);
        book.write(out);
        out.close();
      }
      
      private static void copySheets(HSSFWorkbook newWorkbook, HSSFSheet newSheet, HSSFSheet sheet, boolean isFirstFile){     
        copySheets(newWorkbook, newSheet, sheet, true, isFirstFile);
      }     

      private static void copySheets(HSSFWorkbook newWorkbook, HSSFSheet newSheet, HSSFSheet sheet, boolean copyStyle, boolean isFirstFile){     
        int newRownumber = newSheet.getLastRowNum() + 1;
        int maxColumnNum = 0;     
        Map<Integer, HSSFCellStyle> styleMap = (copyStyle) ? new HashMap<Integer, HSSFCellStyle>() : null;    
        boolean isfirstLine = true;
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
          if (!isFirstFile && isfirstLine) {
        	  isfirstLine = false;
        	  continue;
          }
          HSSFRow srcRow = sheet.getRow(i);
          HSSFRow destRow = null;
          if (isFirstFile) {
              destRow = newSheet.createRow(i + newRownumber);   
          } else {
        	  destRow = newSheet.createRow(i - 1 + newRownumber); 
          }
          
          if (srcRow != null) {     
            copyRow(newWorkbook, sheet, newSheet, srcRow, destRow, styleMap);     
            if (srcRow.getLastCellNum() > maxColumnNum) {     
                maxColumnNum = srcRow.getLastCellNum();     
            }     
          }     
        }     
        for (int i = 0; i <= maxColumnNum; i++) {     
          newSheet.setColumnWidth(i, sheet.getColumnWidth(i));     
        }     
      }     
      
      public static void copyRow(HSSFWorkbook newWorkbook, HSSFSheet srcSheet, HSSFSheet destSheet, HSSFRow srcRow, HSSFRow destRow, Map<Integer, HSSFCellStyle> styleMap) {     
        destRow.setHeight(srcRow.getHeight());
        for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {     
          HSSFCell oldCell = srcRow.getCell(j);
          HSSFCell newCell = destRow.getCell(j);
          if (oldCell != null) {     
            if (newCell == null) {     
              newCell = destRow.createCell(j);     
            }     
            copyCell(newWorkbook, oldCell, newCell, styleMap);
          }     
        }                
      }
      
      public static void copyCell(HSSFWorkbook newWorkbook, HSSFCell oldCell, HSSFCell newCell, Map<Integer, HSSFCellStyle> styleMap) {      
        if(styleMap != null) {     
          int stHashCode = oldCell.getCellStyle().hashCode();     
          HSSFCellStyle newCellStyle = styleMap.get(stHashCode);     
          if(newCellStyle == null){     
            newCellStyle = newWorkbook.createCellStyle();     
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());     
            styleMap.put(stHashCode, newCellStyle);     
          }     
          newCell.setCellStyle(newCellStyle);   
        } 
      
        switch(oldCell.getCellType()) {     
          case STRING:     
            newCell.setCellValue(oldCell.getRichStringCellValue());     
            break;     
          case NUMERIC:     
            newCell.setCellValue(oldCell.getNumericCellValue());     
            break;     
          case BLANK:     
            newCell.setCellType(CellType.BLANK);     
            break;     
          case BOOLEAN:     
            newCell.setCellValue(oldCell.getBooleanCellValue());     
            break;     
          case ERROR:     
            newCell.setCellErrorValue(oldCell.getErrorCellValue());    
            break;     
          case FORMULA:     
            newCell.setCellFormula(oldCell.getCellFormula());     
            break;     
          default:     
            break;     
        }
      }

    // Getters and Setters
	public Log getLogger() {
		return logger;
	}

	public void setLogger(Log logger) {
		this.logger = logger;
	}
	public SystemConfigHelper getSystemConfigHelper() {
		return systemConfigHelper;
	}

	public void setSystemConfigHelper(SystemConfigHelper systemConfigHelper) {
		this.systemConfigHelper = systemConfigHelper;
	}
	
	public String getMergedFileLoc() {
		return mergedFileLoc;
	}

	public void setMergedFileLoc(String mergedFileLoc) {
		this.mergedFileLoc = mergedFileLoc;
	}

	public String getReadDirectory() {
		return readDirectory;
	}

	public void setReadDirectory(String readDirectory) {
		this.readDirectory = readDirectory;
	}
	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
}
