package com.cac.controller;

import com.cac.model.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class PaymentClientController {

    private static final Logger logger = Logger.getLogger(PaymentClientController.class.getName());

    @Value("${base.url}")
    private String backendUrl;

    //payment home page added in main page as payement portal
    @GetMapping("/paymentManagement")
    public String home() {
        return "indexPayment.html";
    }

    @GetMapping("/payments")
    public String getAllPayments(Model model) {
        RestTemplate restTemplate = new RestTemplate();
        String jsonResponse = restTemplate.getForObject(backendUrl + "/search", String.class);
        logger.info("Backend JSON response: " + jsonResponse);
        try {
        	 Payment[] paymentArray = restTemplate.getForObject(backendUrl + "/search", Payment[].class);
             List<Payment> payments = Arrays.asList(paymentArray);
                     model.addAttribute("payments", payments);
        } catch (Exception e) {
            logger.severe("Error fetching payments: " + e.getMessage());
            model.addAttribute("error", "Error fetching payments");
        }
        return "payments";
    }

    @GetMapping("/create")
    public String createPage() {
        return "create";
    }
    
    @GetMapping("/viewpayments")
    public String viewpayments() {
        return "payments";
    }

    @PostMapping("/create")
    public String createPayment(
        @RequestParam int billId,
        @RequestParam String paymentMethod,
        @RequestParam Double amount
    ) {
        RestTemplate restTemplate = new RestTemplate();
        try {
        	Payment payment = new Payment();
            Bill bill = new Bill();
            bill.setBillId(billId);  
            payment.setBill(bill);
            payment.setPaymentMethod(paymentMethod);
            payment.setAmount(amount);

            restTemplate.postForObject(
                backendUrl + "/create",
                payment,
                String.class
            );
        } catch (Exception e) {
            logger.severe("Error creating payment: " + e.getMessage());
            return "redirect:/create?error=true";
        }
        return "redirect:/payments";
    }

    @GetMapping("/search")
    public String searchPayments(
            @RequestParam(required = false) Integer billId,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(backendUrl + "/search");
            if (billId != null) {
                builder.queryParam("billId", billId);
            }
            if (paymentMethod != null && !paymentMethod.isEmpty()) {
                builder.queryParam("paymentMethod", paymentMethod);
            }
            if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                builder.queryParam("startDate", startDate);
                builder.queryParam("endDate", endDate);
            }
            String queryUrl = builder.toUriString();
            logger.info("Query URL: " + queryUrl);
            RestTemplate restTemplate = new RestTemplate();
            Payment[] paymentsArray = restTemplate.getForObject(queryUrl, Payment[].class);
            
            if (paymentsArray != null && paymentsArray.length > 0) {
                List<Payment> payments = Arrays.asList(paymentsArray);
                model.addAttribute("payments", payments);
            } else {
                model.addAttribute("error", "No payments found matching the criteria.");
            }

        } catch (Exception e) {
            logger.severe("Error searching payments: " + e.getMessage());
            model.addAttribute("error", "Error searching payments: " + e.getMessage());
        }        
        return "payments";
    }

}
