package com.cac.repository;

import com.cac.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;




@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {	
	 List<Payment> findByBill_BillId(int billId);
	    List<Payment> findByPaymentMethod(String paymentMethod);
	    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
	    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
	    List<Payment> findByPaymentStatus(String paymentStatus);
	    List<Payment> findByBill_BillIdAndPaymentMethodAndPaymentStatus(Integer billId, String paymentMethod, String paymentStatus);
	    List<Payment> findByBill_BillIdAndPaymentMethod(int billId, String paymentMethod);
	    List<Payment> findByBill_BillIdAndPaymentStatus(Integer billId, String paymentStatus);
	    List<Payment> findByPaymentMethodAndPaymentStatus(String paymentMethod, String paymentStatus);



}