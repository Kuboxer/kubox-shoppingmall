package com.shop.payment;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class SimpleTestController {
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-service");
        response.put("port", 8085);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @PostMapping("/prepare")
    public Map<String, Object> preparePayment(@RequestBody Map<String, Object> request) {
        System.out.println("받은 결제 요청: " + request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("paymentId", 1);
        
        Map<String, Object> bootpayConfig = new HashMap<>();
        bootpayConfig.put("application_id", "656c9fc3e57a7e001b59ff36");
        bootpayConfig.put("price", request.get("amount"));
        bootpayConfig.put("order_name", request.get("orderName"));
        bootpayConfig.put("order_id", "ORDER_" + request.get("orderId") + "_" + System.currentTimeMillis());
        bootpayConfig.put("pg", "nicepay");
        bootpayConfig.put("method", "card,kakao");
        
        response.put("bootpayConfig", bootpayConfig);
        
        return response;
    }
    
    @PostMapping("/verify")
    public Map<String, Object> verifyPayment(@RequestBody Map<String, Object> request) {
        System.out.println("결제 검증 요청: " + request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "결제 검증 완료 (테스트)");
        
        return response;
    }
    
    @PostMapping("/process")
    public Map<String, Object> processPayment(@RequestBody Map<String, Object> request) {
        System.out.println("결제 처리 요청: " + request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", 1);
        response.put("status", "SUCCESS");
        
        return response;
    }
    
    @PutMapping("/{paymentId}/status")
    public Map<String, Object> updateStatus(@PathVariable Long paymentId, @RequestBody Map<String, String> request) {
        System.out.println("상태 업데이트: " + paymentId + " -> " + request.get("status"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", paymentId);
        response.put("status", request.get("status"));
        
        return response;
    }
}
