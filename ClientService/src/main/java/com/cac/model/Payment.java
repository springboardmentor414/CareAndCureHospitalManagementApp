package com.cac.model;


import com.cac.model.Bill;

public class Payment {
	

	private Bill bill;

    private String paymentMethod;
    private Double amount;
    private String paymentStatus;
    private String paymentDate;
    private String razorpayOrderId;
    private String currency;
    
	public Payment() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public Payment(Bill bill, String paymentMethod, Double amount, String paymentStatus, String paymentDate,
			String razorpayOrderId, String currency) {
		super();
		this.bill = bill;
		this.paymentMethod = paymentMethod;
		this.amount = amount;
		this.paymentStatus = paymentStatus;
		this.paymentDate = paymentDate;
		this.razorpayOrderId = razorpayOrderId;
		this.currency = currency;
	}

	public Bill getBill() {
		return bill;
	}
	public void setBill(Bill bill) {
		this.bill = bill;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	public String getPaymentDate() {
		return paymentDate;
	}
	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}
	public String getRazorpayOrderId() {
		return razorpayOrderId;
	}
	public void setRazorpayOrderId(String razorpayOrderId) {
		this.razorpayOrderId = razorpayOrderId;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	@Override
	public String toString() {
		return "Payment [bill=" + bill + ", paymentMethod=" + paymentMethod + ", amount=" + amount + ", paymentStatus="
				+ paymentStatus + ", paymentDate=" + paymentDate + ", razorpayOrderId=" + razorpayOrderId
				+ ", currency=" + currency + "]";
	}
    
    
}
