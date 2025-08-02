package com.shop.payment;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class SimplePaymentController {
    
    @GetMapping("/")
    public String home() {
        return "Payment Service is running on port 8083!";
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-service");
        response.put("port", 8083);
        response.put("message", "Payment Service is healthy");
        return response;
    }
    
    @PostMapping("/test")
    public Map<String, Object> test(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Payment Service API is working");
        response.put("received", request);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
