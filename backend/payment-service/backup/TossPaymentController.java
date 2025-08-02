package com.shop.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private PaymentService paymentService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 토스페이먼츠 결제 승인
     */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmPayment(@RequestBody String jsonBody,
                                                   @RequestHeader(value = "User-Email", required = false) String userEmail) {
        try {
            JsonNode requestData = objectMapper.readTree(jsonBody);
            
            String paymentKey = requestData.get("paymentKey").asText();
            String orderId = requestData.get("orderId").asText();
            String amount = requestData.get("amount").asText();
            
            // 기본 사용자 이메일 설정 (테스트용)
            if (userEmail == null || userEmail.isEmpty()) {
                userEmail = "test@example.com";
            }
            
            logger.info("결제 승인 요청 - orderId: {}, amount: {}, userEmail: {}", orderId, amount, userEmail);
            
            Map<String, Object> result = paymentService.confirmPayment(orderId, amount, paymentKey, userEmail);
            
            logger.info("결제 승인 성공 - orderId: {}", orderId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("결제 승인 실패: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 결제 내역 조회
     */
    @GetMapping("/history")
    public ResponseEntity<List<Payment>> getPaymentHistory(
            @RequestHeader(value = "User-Email", defaultValue = "test@example.com") String userEmail) {
        try {
            List<Payment> payments = paymentService.getPaymentHistory(userEmail);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            logger.error("결제 내역 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 특정 주문 결제 정보 조회
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable String orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            logger.error("결제 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 서비스 상태 체크 (장애 전파 방지)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "payment-service");
            response.put("port", "8083");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(paymentService.getDefaultResponse());
        }
    }
    
    /**
     * 결제 준비 (장바구니 -> 결제)
     */
    @PostMapping("/prepare")
    public ResponseEntity<Map<String, Object>> preparePayment(
            @RequestBody Map<String, Object> requestData,
            @RequestHeader(value = "User-Email", defaultValue = "test@example.com") String userEmail) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // 기본 결제 준비 정보 생성
            String orderId = "ORDER_" + System.currentTimeMillis();
            Object amountObj = requestData.getOrDefault("amount", 50000);
            Long amount = amountObj instanceof Integer ? ((Integer) amountObj).longValue() : (Long) amountObj;
            
            response.put("orderId", orderId);
            response.put("amount", amount);
            response.put("customerKey", userEmail);
            response.put("clientKey", "test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm");
            
            logger.info("결제 준비 완료 - orderId: {}, amount: {}", orderId, amount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("결제 준비 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(paymentService.getDefaultResponse());
        }
    }
}
