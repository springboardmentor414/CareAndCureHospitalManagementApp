package com.cac.controller;

import com.cac.model.Payment;
import com.cac.service.EmailService;
import com.cac.service.PaymentService;
import com.cac.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.Utils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmailService emailNotificationService;

    @Autowired
    private RazorpayService razorpayService;

    @PostMapping("/create")
    @ResponseBody
    public Payment createPayment(@Valid @RequestBody Payment payment) {
        try {
            Order order = razorpayService.createOrder(payment.getAmount(), payment.getCurrency());
            payment.setRazorpayOrderId(order.get("id"));
            payment.setPaymentStatus("UnSuccess");
            paymentService.savePayment(payment);
            logger.info("Payment created successfully with ID: " + payment.getRazorpayOrderId());
        } catch (Exception e) {
            logger.severe("Error creating payment: " + e.getMessage());
            throw new RuntimeException("Payment creation failed", e);
        }
        return payment;
    }
    @PostMapping("/verify")
    @ResponseBody
    public Map<String, String> verifyPayment(@RequestBody Map<String, Object> payload) {
        String razorpayOrderId = (String) payload.get("razorpay_order_id");
        String razorpayPaymentId = (String) payload.get("razorpay_payment_id");
        String razorpaySignature = (String) payload.get("razorpay_signature");

        String secret = "Q4Cj1Fcp8DNd6V7D66GJqyoj";

        Map<String, String> response = new HashMap<>();
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            boolean isValidSignature = Utils.verifyPaymentSignature(attributes, secret);
            
            if (isValidSignature) {
            	
                Payment payment = paymentService.getPaymentByOrderId(razorpayOrderId);
                if (payment != null) {
                	payment.setPaymentStatus("Success");
                    paymentService.savePayment(payment);

                    // Send success email notification
                    String userEmail = "aathi22004@gmail.com"; 
                    emailNotificationService.sendPaymentSuccessEmail(
                            userEmail, razorpayPaymentId, razorpayOrderId, payment.getAmount()
                    );

                    response.put("message", "Payment verified successfully!");
                    response.put("status", "success");
                   
                } else {
                	
                    response.put("message", "Order not found!");
                    response.put("status", "failed");
                }
            } else {
            	
                Payment payment = paymentService.getPaymentByOrderId(razorpayOrderId);
                if (payment != null) {
                	payment.setPaymentStatus("Failed");
                    paymentService.savePayment(payment);
                    String userEmail = "aathi22004@gmail.com"; 
                    emailNotificationService.sendPaymentFailureEmail(
                            userEmail, razorpayOrderId, "Invalid payment signature."
                    );
                    
                }

                response.put("message", "Invalid payment signature!");
                response.put("status", "failed");
            }
        } catch (Exception e) {
            logger.severe("Payment verification failed: " + e.getMessage());
            Payment payment = paymentService.getPaymentByOrderId(razorpayOrderId);
            if (payment != null) {
                payment.setPaymentStatus("Failed");
                paymentService.savePayment(payment);
                String userEmail = "aathi22004@gmail.com"; 
                emailNotificationService.sendPaymentFailureEmail(
                        userEmail, razorpayOrderId, e.getMessage()
                );
            }

            response.put("message", "Payment verification failed!");
            response.put("status", "failed");
        }
        return response;
    }

    @PostMapping("/notifyFailure")
    @ResponseBody
    public Map<String, String> notifyFailure(@RequestBody Map<String, Object> payload) {
        String billId = (String) payload.get("billId");
        String message = (String) payload.get("message");

        Map<String, String> response = new HashMap<>();
        try {
            String userEmail = "aathi22004@gmail.com"; 
            emailNotificationService.sendPaymentFailureEmail(userEmail, billId, message);

            response.put("message", "Failure notification sent successfully!");
            response.put("status", "success");
        } catch (Exception e) {
            logger.severe("Error sending failure notification: " + e.getMessage());
            response.put("message", "Error sending failure notification");
            response.put("status", "failed");
        }
        return response;
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchPayments(
            @RequestParam(required = false) Integer billId,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {           
            LocalDate startLocalDate = null;
            LocalDate endLocalDate = null;            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (startDate != null) {
                startLocalDate = LocalDate.parse(startDate, formatter);
            }
            if (endDate != null) {
                endLocalDate = LocalDate.parse(endDate, formatter);
            }
            List<Payment> payments = new ArrayList<>();            
            if (startLocalDate != null && endLocalDate != null) {               
                payments = paymentService.getPaymentsByDateRange(startLocalDate, endLocalDate);
            } else if (billId != null) {               
                payments = paymentService.getPaymentsByBillId(billId);
            } else if (paymentMethod != null) {               
                payments = paymentService.getPaymentsByPaymentMethod(paymentMethod);
            } else {               
                payments = paymentService.getAllPayments();
            }
            if (payments.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No payments found for the provided criteria.");
            }

            return ResponseEntity.ok(payments);  

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Error searching payments: " + e.getMessage());
        }
    }




}
