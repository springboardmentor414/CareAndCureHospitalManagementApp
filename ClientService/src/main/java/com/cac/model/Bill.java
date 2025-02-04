package com.cac.model;


import java.time.LocalDate;
import java.util.Set;

import com.cac.model.Payment;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;





public class Bill {

	

	private  int billId;
	private  LocalDate billDate;
	
  	private Appointment appointment;

	
	private  double miscellaneousCharge;
	private  String description;
	private  boolean isInsuranceApplicable;
	private  float discountPercentage;
	
	
//	private boolean tax;
	private  double consultationFees;
	private  double medicineFees;
	private  double testCharges;
	private double totalamount;
	private double finalamount;
	private String paymentstatus;
	
	private  Set<Payment> payList;
	
    private float taxPercentage;
    private double taxableamount;
	
	
	public Set<Payment> getPayList() {
		return payList;
	}
	public void setPayList(Set<Payment> payList) {
		this.payList = payList;
	}
	public String getPaymentstatus() {
		return paymentstatus;
	}
	public void setPaymentstatus(String paymentstatus) {
		this.paymentstatus = paymentstatus;
	}
	public Appointment getAppointment() {
		return appointment;
	}
	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}
	public double getTotalamount() {
		return totalamount;
	}
	public void setTotalamount(double totalamount) {
		this.totalamount = totalamount;
	}
	public double getFinalamount() {
		return finalamount;
	}
	public void setFinalamount(double finalamount) {
		this.finalamount = finalamount;
	}
	
	public int getBillId() {
		return billId;
	}
	public void setBillId(int billId) {
		this.billId = billId;
	}
	public LocalDate getBillDate() {
		return billDate;
	}
	public void setBillDate(LocalDate billDate) {
		this.billDate = billDate;
	}
	public double getConsultationFees() {
		return consultationFees;
	}
	public void setConsultationFees(double consultationFees) {
		this.consultationFees = consultationFees;
	}
	public double getMedicineFees() {
		return medicineFees;
	}
	public void setMedicineFees(double medicineFees) {
		this.medicineFees = medicineFees;
	}
	public double getTestCharges() {
		return testCharges;
	}
	public void setTestCharges(double testCharges) {
		this.testCharges = testCharges;
	}
	public double getMiscellaneousCharge() {
		return miscellaneousCharge;
	}
	public void setMiscellaneousCharge(double miscellaneousCharge) {
		this.miscellaneousCharge = miscellaneousCharge;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isInsuranceApplicable() {
		return isInsuranceApplicable;
	}
	public void setInsuranceApplicable(boolean isInsuranceApplicable) {
		this.isInsuranceApplicable = isInsuranceApplicable;
	}
	public float getDiscountPercentage() {
		return discountPercentage;
	}
	public void setDiscountPercentage(float discountPercentage) {
		this.discountPercentage = discountPercentage;
	}
	public float getTaxPercentage() {
		return taxPercentage;
	}
	public void setTaxPercentage(float taxPercentage) {
		this.taxPercentage = taxPercentage;
	}
	public double getTaxableamount() {
		return taxableamount;
	}
	public void setTaxableamount(double taxableamount) {
		this.taxableamount = taxableamount;
	}
//	public boolean isTax() {
//		return tax;
//	}
//	public void setTax(boolean tax) {
//		this.tax = tax;
//	}
	@Override
	public String toString() {
		return "Bill [billId=" + billId + ", billDate=" + billDate + ", appointment=" + appointment
				+ ", consultationFees=" + consultationFees + ", medicineFees=" + medicineFees + ", testCharges="
				+ testCharges + ", miscellaneousCharge=" + miscellaneousCharge + ", description=" + description
				+ ", isInsuranceApplicable=" + isInsuranceApplicable + ", discountPercentage=" + discountPercentage
				+ ", taxPercentage=" + taxPercentage + ", taxableamount=" + taxableamount + ", totalamount=" + totalamount + ", finalamount=" + finalamount + ", paymentstatus="
				+ paymentstatus + "]";
	}
	
	

}








