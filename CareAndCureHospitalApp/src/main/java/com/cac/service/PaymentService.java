package com.cac.service;

import com.cac.model.Bill;
import com.cac.model.Payment;
import com.cac.repository.BillRepository;
import com.cac.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PaymentService {

    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private BillRepository billRepository;

    public void savePayment(Payment payment) {
        Bill bill = billRepository.findByBillId(payment.getBill().getBillId());
        if (bill == null) {
            bill = new Bill();
            bill.setBillId(payment.getBill().getBillId());
            bill = billRepository.save(bill);
        }
        payment.setBill(bill);
        paymentRepository.save(payment);
        logger.info("Payment saved successfully for bill ID: " + payment.getBill().getBillId());
        logger.info("Payment saved successfully ");
    }

    public List<Payment> getPaymentsByBillId(int billId) {
        return paymentRepository.findByBill_BillId(billId);
    }

    public List<Payment> getPaymentsByPaymentMethod(String paymentMethod) {
        return paymentRepository.findByPaymentMethod(paymentMethod);
    }
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    public Payment getPaymentByOrderId(String razorpayOrderId) {
        return paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment with Order ID " + razorpayOrderId + " not found"));
    }

    // Custom Exception Class
    public static class PaymentException extends RuntimeException {
        public PaymentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
