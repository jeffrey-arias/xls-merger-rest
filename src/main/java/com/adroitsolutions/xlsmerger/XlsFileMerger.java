package com.adroitsolutions.xlsmerger;

public interface XlsFileMerger {

	public String mergeExcelFiles(String dateFrom, String dateTo, BatchType batchType);
	
	public void setTimeStamp(String timeStamp);
	
	public String getTimeStamp();
}
