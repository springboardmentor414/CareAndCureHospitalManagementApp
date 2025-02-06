package com.cac.model;

import jakarta.persistence.*;

import com.cac.model.Appointment;
import com.cac.model.Payment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
public class Bill {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private  int billId;
	private  LocalDate billDate;
	
	@OneToOne
	@JoinColumn(name = "appointment_id")
	@JsonManagedReference
	private Appointment appointment;

	private  double consultationFees;
	private  double medicineFees;
	private  double testCharges;
	private  double miscellaneousCharge;
	private  String description;
	private  boolean isInsuranceApplicable;
	private  float discountPercentage;
	private double amountPaid;   
	
	/*@OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
	//@OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JsonManagedReference
	private  Set<Payment> payList;*/
	
	@OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnoreProperties("bill") // Prevents recursion but allows payments in Bill response
	private Set<Payment> payList;

	
//	private boolean tax;
    private float taxPercentage;
    private double taxableamount;
	
	private double totalamount;
	private double finalamount;
	private String paymentstatus;
	
	
	
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
	public void setFinalamount(Double finalamount) {
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
//	public boolean isTax() {
//		return tax;
//	}
//	public void setTax(boolean tax) {
//		this.tax = tax;
//	}
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
	
	public double getAmountPaid() {
		return amountPaid;
	}
	public void setAmountPaid(double amountPaid) {
		this.amountPaid = amountPaid;
	}
	/*@Override
	public String toString() {
		return "Bill [billId=" + billId + ", billDate=" + billDate + ", appointment=" + appointment
				+ ", consultationFees=" + consultationFees + ", medicineFees=" + medicineFees + ", testCharges="
				+ testCharges + ", miscellaneousCharge=" + miscellaneousCharge + ", description=" + description
				+ ", isInsuranceApplicable=" + isInsuranceApplicable + ", discountPercentage=" + discountPercentage
				+ ", amountPaid=" + amountPaid + ", payList=" + payList + ", taxPercentage=" + taxPercentage
				+ ", taxableamount=" + taxableamount + ", totalamount=" + totalamount + ", finalamount=" + finalamount
				+ ", paymentstatus=" + paymentstatus + "]";
	}
	*/
	
	
}
