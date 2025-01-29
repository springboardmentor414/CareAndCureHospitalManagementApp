package com.cac.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import jakarta.annotation.PostConstruct;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.logging.Logger;

@Service
public class RazorpayService {

    private static final Logger logger = Logger.getLogger(RazorpayService.class.getName());

    private RazorpayClient razorpayClient;

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);
            logger.info("RazorpayClient initialized successfully.");
        } catch (Exception e) {
            logger.severe("Error initializing RazorpayClient: " + e.getMessage());
            throw new RazorpayServiceException("RazorpayClient initialization failed", e);
        }
    }

    public Order createOrder(double amount, String currency) throws RazorpayServiceException {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount * 100); 
            orderRequest.put("currency", currency);
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(orderRequest);
            logger.info("Razorpay order created successfully with ID: " + order.get("id"));
            return order;
        } catch (Exception e) {
            logger.severe("Error creating Razorpay order: " + e.getMessage());
            throw new RazorpayServiceException("Failed to create Razorpay order", e);
        }
    }

    public static class RazorpayServiceException extends RuntimeException {
        public RazorpayServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
