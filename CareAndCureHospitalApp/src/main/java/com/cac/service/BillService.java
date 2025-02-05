package com.cac.service;

import com.cac.model.Bill;

import com.cac.repository.BillRepository;
import com.cac.exception.BillNotFoundException;
import com.cac.model.Appointment;
import com.cac.repository.AppointmentRepository;
import com.cac.repository.BillRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

	@Autowired
	private AppointmentRepository appointmentRepository;

    public Bill findByBillId(int billId) {
        return billRepository.findByBillId(billId);
    }
   
   
	
		
	public String displayWelcomeMessage() {
		return "welcome to billingSystem";
	}
	
	
	
	public Bill createBill(Bill bill, int appointmentId) {
		// Check if the appointment exists
		if (bill == null ||appointmentId <= 0) {
		    throw new IllegalArgumentException("Invalid Bill or Appointment ID.");
		}
       
	    Appointment appointment = appointmentRepository.findById(appointmentId)
	            .orElseThrow(() -> new BillNotFoundException("Appointment not found with ID: " + appointmentId));

	    
	    // Check if a bill is already associated with this appointment
	    Bill b=billRepository.findByAppointment_AppointmentId(appointmentId);
        if (b!=null) {
            throw new IllegalArgumentException("A bill already exists for this appointment.");
        }   
	    double totalAmount = bill.getConsultationFees() +
                bill.getMedicineFees() +
                bill.getTestCharges() +
                bill.getMiscellaneousCharge();

	    float discountPercentage = (totalAmount >= 100000) ? 20 : (totalAmount >= 1000) ? 10 : 0;

	    double discount = (totalAmount * discountPercentage) / 100;
	    double taxableAmount = totalAmount - discount;
	    float taxPercentage = 2;
	    double finalAmount = taxableAmount + (taxableAmount * taxPercentage) / 100;

	    bill.setTotalamount(totalAmount);
	    bill.setDiscountPercentage(discountPercentage);
	    bill.setTaxableamount(taxableAmount);
	    bill.setTaxPercentage(taxPercentage);
	    bill.setFinalamount(finalAmount);
	    // Set the appointment to the bill
	    bill.setAppointment(appointment);

	    
	    return billRepository.save(bill);
	}
	
	public Bill updatePaymentStatus(int billId, String paymentStatus) {
		if (billId <= 0) {
		    throw new IllegalArgumentException("Invalid Bill or Appointment ID.");
		}
       
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Bill not found with ID: " + billId));
        bill.setPaymentstatus(paymentStatus);
        return billRepository.save(bill);
    }

	
	public Bill getBillById(int billId) {
	    return billRepository.findById(billId)
	            .orElseThrow(() -> new BillNotFoundException("Bill not found with ID: " + billId));
	}
   
	public List<Bill> getBillsByPatientId(int patientId) {
	    return billRepository.findByAppointment_Patient_PatientId(patientId);
	}

	
	public List<Bill> getBillsByDate(LocalDate billDate) {
	    return billRepository.findByBillDate(billDate);
	}

	public Bill saveBill(Bill bill) {
	    return billRepository.save(bill);
	}
	
}
