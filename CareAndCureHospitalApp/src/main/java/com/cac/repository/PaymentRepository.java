package com.cac.repository;

import com.cac.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByBill_BillId(int billId);
    List<Payment> findByPaymentMethod(String paymentMethod);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
}
