package com.adroitsolutions.xlsmerger;

public enum BatchType {

	NIP_INFLOW_NOT_REPORTED (true),
	NIP_INFLOW_REPORTED (false),
	CASH_DEPOSIT_NOT_REPORTED (true),
	CASH_DEPOSIT_REPORTED (false),
	LOAN_NOT_REPORTED (true),
	LOAN_REPORTED (false),
	CASH_WITHDRAWAL_NOT_REPORTED (true),
	CASH_WITHDRAWAL_REPORTED (false),
	ALL (false);
	
  private final boolean isNotReported;

  BatchType(boolean isNotReported) {
    this.isNotReported = isNotReported;
  }

  public boolean isNotReported() {
    return this.isNotReported;
  }

}