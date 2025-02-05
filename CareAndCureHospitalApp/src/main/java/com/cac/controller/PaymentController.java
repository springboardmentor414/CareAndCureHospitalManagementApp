package com.cac.controller;

import com.cac.exception.UserNotFoundException;
import com.cac.model.Bill;
import com.cac.model.Payment;
import com.cac.repository.BillRepository;
import com.cac.service.EmailService;
import com.cac.service.PaymentService;
import com.cac.service.RazorpayService;
import com.cac.controller.PaymentController;
import com.cac.exception.UserNotFoundException;
import com.cac.model.Bill;
import com.cac.repository.BillRepository;
import com.cac.service.EmailService;
import com.razorpay.Order;
import com.razorpay.Utils;

import jakarta.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
	private BillRepository billingRepository;

    @Autowired
    private EmailService emailNotificationService;

    @Autowired
    private RazorpayService razorpayService;
    
        
   
    
    @CrossOrigin(origins = "http://localhost:9093") // Allow CORS for this controller method
    @PostMapping("/create/{billId}")
    @ResponseBody
    public ResponseEntity<?> createPayment(@PathVariable int billId, @Valid @RequestBody Payment payment) {
        logger.info("Inside payment controller create method for Bill ID: " + billId);
        try {
            // Fetch bill from repository
            Bill fetchedBill = billingRepository.findByBillId(billId);
            if (fetchedBill == null) {
                logger.warning("Bill not found for ID: " + billId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Bill not found for ID: " + billId));
            }

            if (fetchedBill.getFinalamount() == 0) {
                logger.warning("Total amount for Bill ID " + billId + " is null.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Bill total amount cannot be null."));
            }

            double remainingBalance = fetchedBill.getFinalamount() - fetchedBill.getAmountPaid();
            if (payment.getAmount() > remainingBalance) {
                logger.warning("Payment amount exceeds remaining balance for Bill ID " + billId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Payment amount " + payment.getAmount() + " exceeds remaining balance " + remainingBalance + " for Bill ID " + billId
                ));
            }

            // Associate payment with fetched bill
            payment.setBill(fetchedBill);
            Order order = razorpayService.createOrder(payment.getAmount(), payment.getCurrency());
            payment.setRazorpayOrderId(order.get("id"));
            payment.setPaymentStatus("UnSuccess");
            paymentService.savePayment(payment);
            logger.info("Payment created successfully with ID: " + payment.getRazorpayOrderId());
            return ResponseEntity.ok(payment); 
        } catch (IllegalArgumentException e) {
            logger.severe("Invalid payment input: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.severe("Error creating payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Payment creation failed: " + e.getMessage()));
        }
    }
 
    @CrossOrigin(origins = "http://localhost:9093") // Allow CORS for this controller method
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
                     Bill bill = payment.getBill();
                     paymentService.updatePaymentStatus(bill.getBillId());
                     double balanceAmount = bill.getFinalamount() - bill.getAmountPaid();
                     String userEmail = "aathi22004@gmail.com"; 
                     emailNotificationService.sendPaymentSuccessEmail(
                             userEmail, razorpayPaymentId, razorpayOrderId, payment.getAmount(), bill.getBillId(), balanceAmount
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
                     payment.setPaymentStatus("UnSuccess");
                     paymentService.savePayment(payment);

                     String userEmail = "aathi22004@gmail.com"; 
                     emailNotificationService.sendPaymentFailureEmail(
                             userEmail, razorpayOrderId, "Invalid payment signature.", payment.getBill().getBillId()
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
                         userEmail, razorpayOrderId, e.getMessage(), payment.getBill().getBillId()
                 );
             }

             response.put("message", "Payment verification failed!");
             response.put("status", "failed");
         }
         return response;
    }

    @CrossOrigin(origins = "http://localhost:9093") // Allow CORS for this controller method
    @PostMapping("/notifyFailure")
    @ResponseBody
    public Map<String, String> notifyFailure(@RequestBody Map<String, Object> payload) {
        Map<String, String> response = new HashMap<>();
        try {
            
        	int billId = Integer.parseInt(payload.get("billId").toString());
            String orderId = (String) payload.get("orderId");
            String failureReason = (String) payload.get("message");
            String userEmail = "aathi22004@gmail.com"; 
            emailNotificationService.sendPaymentFailureEmail(userEmail, orderId, failureReason, billId);

            response.put("message", "Failure notification sent successfully!");
            response.put("status", "success");
        } catch (Exception e) {
            logger.severe("Error sending failure notification: " + e.getMessage());
            response.put("message", "Error sending failure notification");
            response.put("status", "failed");
        }
        return response;
    }
    
    @CrossOrigin(origins = "http://localhost:9093") // Allow CORS for this controller method
    @GetMapping("/searchbypayment")
    public ResponseEntity<?> searchByCriteria(
            @RequestParam(required = false) Integer billId,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String paymentStatus) {
        try {
            if (paymentMethod != null) {
                paymentMethod = URLDecoder.decode(paymentMethod, StandardCharsets.UTF_8);
            }
            if (paymentStatus != null) {
                paymentStatus = URLDecoder.decode(paymentStatus, StandardCharsets.UTF_8);
            }
            if (billId != null) {
                List<Payment> payments = paymentService.getPaymentsByBillId(billId);
                if (payments.isEmpty()) {
                    throw new UserNotFoundException("No payments found for the provided Bill ID: " + billId);
                }
                return ResponseEntity.ok(payments);
            } else if (paymentMethod != null && !paymentMethod.isEmpty()) {                
                List<Payment> payments = paymentService.getPaymentsByPaymentMethod(paymentMethod);
                if (payments.isEmpty()) {
                    throw new UserNotFoundException("No payments found for the payment method: " + paymentMethod);
                }
                return ResponseEntity.ok(payments);
            } else if (paymentStatus != null && !paymentStatus.isEmpty()) {
                List<Payment> payments = paymentService.getPaymentsByPaymentStatus(paymentStatus);
                if (payments.isEmpty()) {
                    throw new UserNotFoundException("No payments found for the payment status: " + paymentStatus);
                }
                return ResponseEntity.ok(payments);
            } else if (billId == null && (paymentMethod == null || paymentMethod.isEmpty()) && (paymentStatus == null || paymentStatus.isEmpty())) {
                List<Payment> allPayments = paymentService.getAllPayments();
                if (allPayments.isEmpty()) {
                    throw new UserNotFoundException("No payments found in the system.");
                }
                return ResponseEntity.ok(allPayments);
            } else {
                return ResponseEntity.badRequest()
                        .body("Error! Please provide valid search criteria (Bill ID, Payment Method, or Payment Status). ");
            }
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        } catch (Exception e) {            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing your request.");
        }
    }


    @CrossOrigin(origins = "http://localhost:9093")
    @GetMapping("/searchByDate")
    public ResponseEntity<?> searchByDate(
            @RequestParam(required = true) String startDate,
            @RequestParam(required = true) String endDate) {
        try {
            // Validate and parse dates
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
            LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

            if (startLocalDate.isAfter(endLocalDate)) {
                return ResponseEntity.badRequest()
                        .body("Error! Start date cannot be after the end date.");
            }

            List<Payment> payments = paymentService.getPaymentsByDateRange(startLocalDate, endLocalDate);
            if (payments.isEmpty()) {
                throw new UserNotFoundException("No payments found for the date range: " 
                                                 + startDate + " to " + endDate);
            }
            return ResponseEntity.ok(payments);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        } catch (DateTimeParseException dtpe) {
            
            return ResponseEntity.badRequest()
                    .body("Error! Invalid date format. Please use 'yyyy-MM-dd'.");
        } catch (Exception e) {       
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching payments: " + e.getMessage());
        }
    }





}
