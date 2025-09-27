package com.shop.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class PaymentsController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> paymentData) {
        try {
            // Circuit Breaker에서 호출하는 결제 처리 엔드포인트
            Long orderId = Long.valueOf(paymentData.get("orderId").toString());
            Integer amount = Integer.valueOf(paymentData.get("amount").toString());
            
            // 간단한 결제 처리 로직
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("orderId", orderId);
            response.put("amount", amount);
            response.put("message", "결제가 성공적으로 처리되었습니다");
            
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("FAILED");
        }
    }
}
//test