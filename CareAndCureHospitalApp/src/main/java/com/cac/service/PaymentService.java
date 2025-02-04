package com.cac.service;

import com.cac.model.Bill;

import com.cac.model.Payment;
import com.cac.repository.BillRepository;
import com.cac.repository.PaymentRepository;
import com.cac.exception.UserNotFoundException;
import com.cac.repository.BillRepository;
import com.cac.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
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
            logger.log(Level.SEVERE, "Bill with ID {0} does not exist.", payment.getBill().getBillId());
            throw new UserNotFoundException("Bill ID not found!");
        }
        double remainingBalance = bill.getFinalamount() - bill.getAmountPaid();
        if (payment.getAmount() > remainingBalance) {
            logger.log(Level.WARNING, "Cannot process payment. Payment amount {0} exceeds the remaining balance {1} for Bill ID {2}.",
                    new Object[]{payment.getAmount(), remainingBalance, bill.getBillId()});
            throw new UserNotFoundException("Payment amount exceeds the remaining balance of " + remainingBalance);
        }
        if (remainingBalance <= 0) {
            logger.log(Level.WARNING, "Cannot process payment. Bill ID {0} is already fully paid.", bill.getBillId());
            throw new UserNotFoundException("This bill has already been fully paid.");
        }
        payment.setBill(bill);
        paymentRepository.save(payment);
        logger.info("Payment saved successfully for Bill ID: " + payment.getBill().getBillId());
    }
    public List<Payment> getPaymentsByBillId(int billId) {
    	List<Payment> payments = paymentRepository.findByBill_BillId(billId);
        payments.sort(Comparator.comparing(payment -> payment.getBill().getBillId()));
        return payments;
    }

    public List<Payment> getPaymentsByPaymentMethod(String paymentMethod) {
    	List<Payment> payments = paymentRepository.findByPaymentMethod(paymentMethod);
        payments.sort(Comparator.comparing(payment -> payment.getBill().getBillId()));
        return payments;
    }
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);
        payments.sort(Comparator.comparing(payment -> payment.getBill().getBillId()));
        return payments;
    }
    public List<Payment> getPaymentsByPaymentStatus(String paymentStatus) {
        return paymentRepository.findByPaymentStatus(paymentStatus);
    }

    public List<Payment> getAllPayments() {
    	 List<Payment> payments = paymentRepository.findAll();
         payments.sort(Comparator.comparing(payment -> payment.getBill().getBillId()));
         return payments;
    }
    public Payment getPaymentByOrderId(String razorpayOrderId) {
        return paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new UserNotFoundException("Payment with Order ID " + razorpayOrderId + " not found"));
    }
    public void updatePaymentStatus(int billId) {
        Bill bill = billRepository.findByBillId(billId);
        if (bill == null) {
            logger.log(Level.SEVERE, "Bill with ID {0} does not exist.", billId);
            throw new UserNotFoundException("Bill ID not found!");
        }

     // Calculate the total paid amount for the bill by accumulating each successful payment's amount
        List<Payment> payments = paymentRepository.findByBill_BillId(billId);
        Double totalPaidAmount = payments.stream()
                                         .filter(payment -> "Success".equals(payment.getPaymentStatus())) // Filter only successful payments
                                         .mapToDouble(Payment::getAmount)
                                         .sum();


        // Update the amount paid in the bill object (cumulative)
        bill.setAmountPaid(totalPaidAmount);
        if (totalPaidAmount >= bill.getFinalamount()) {
            bill.setPaymentstatus("Paid");
        } else if (totalPaidAmount > 0) {
            bill.setPaymentstatus("Partially Paid");
        } else {
            bill.setPaymentstatus("Unpaid");
        }
        billRepository.save(bill);
        logger.info("Updated payment status for Bill ID " + billId + " to: " + bill.getPaymentstatus() + ", Amount Paid: " + totalPaidAmount);
    }

}
