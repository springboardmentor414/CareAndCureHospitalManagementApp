package com.cac.model;

public  class Bill {
	  private int billId;

      public Bill(int billId) {
          this.billId = billId;
      }

      public Bill() {
		// TODO Auto-generated constructor stub
	}

	public int getBillId() {
          return billId;
      }

      public void setBillId(int billId) {
          this.billId = billId;
      }
}