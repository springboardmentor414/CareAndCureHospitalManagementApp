package com.cac.model;

import java.sql.Date;

import com.cac.model.Bill;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Payment {

	 @Id
	 private String razorpayOrderId;

	/*@ManyToOne
    @JoinColumn(name = "billid", nullable = false)
	@JsonBackReference
    private Bill bill;*/
	
	
	/*@ManyToOne(fetch = FetchType.EAGER) // Use EAGER fetching
    @JoinColumn(name = "bill_id", nullable = false)
    //@JsonIgnoreProperties("payments") // Ignore recursive serialization
    private Bill bill;*/
	 
	 @ManyToOne(fetch = FetchType.EAGER) // Ensures bill is loaded with Payment
	 @JoinColumn(name = "billid", nullable = false)
	 @JsonIgnoreProperties("payList") // Prevents recursion but allows bill in Payment response
	 private Bill bill;

	
	/*@OneToOne(mappedBy = "appointment")
	@JsonBackReference
	private Bill bill;*/

    private String paymentMethod;
    private Double amount;
    private String paymentStatus;
    private Date paymentDate;
   
    private String currency;

    // Getters and setters
   

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

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
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
		return "Payment [razorpayOrderId=" + razorpayOrderId + ", bill=" + bill + ", paymentMethod=" + paymentMethod
				+ ", amount=" + amount + ", paymentStatus=" + paymentStatus + ", paymentDate=" + paymentDate
				+ ", currency=" + currency + "]";
	}
    
}
