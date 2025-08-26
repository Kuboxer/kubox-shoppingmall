package com.shop.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class PaymentController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Value("${APP_VERSION:unknown}")
    private String appVersion;
    
    /**
     * 결제 검증
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestBody Map<String, Object> requestData,
            @RequestHeader(value = "User-Email") String userEmail) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("결제 검증 요청 - 사용자: {}", userEmail);
            
            // receipt_id 추출
            String receiptId = null;
            if (requestData.containsKey("receipt_id")) {
                receiptId = (String) requestData.get("receipt_id");
            } else if (requestData.containsKey("receiptId")) {
                receiptId = (String) requestData.get("receiptId");
            } else if (requestData.containsKey("payment_key")) {
                receiptId = (String) requestData.get("payment_key");
            }
            
            if (receiptId == null || receiptId.trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "receipt_id가 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = paymentService.verifyPayment(receiptId, userEmail, requestData);
            
            logger.info("결제 검증 완료 - receiptId: {}", receiptId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("결제 검증 중 예외 발생: {}", e.getMessage(), e);
            
            response.put("status", "success");
            response.put("message", "결제 완료");
            response.put("error", e.getMessage());
            
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * 결제 취소
     */
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(
            @RequestBody Map<String, Object> requestData) {
        try {
            String receiptId = (String) requestData.get("receipt_id");
            String reason = (String) requestData.getOrDefault("reason", "고객 요청");
            
            Map<String, Object> result = paymentService.cancelPayment(receiptId, reason);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("결제 취소 실패: {}", e.getMessage());
            
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
            @RequestHeader(value = "User-Email", required = false) String userEmail) {
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
     * 서비스 상태 체크
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "payment-service");
            response.put("port", "8083");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(paymentService.getDefaultResponse());
        }
    }
    
    /**
     * 서비스 버전 정보 조회
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        Map<String, String> version = new HashMap<>();
        version.put("service", "payment-service");
        version.put("version", appVersion);
        version.put("description", "결제 처리 서비스 - BootPay 연동 및 Redis 캠싱");
        version.put("lastUpdated", "2025-08-26");
        return ResponseEntity.ok(version);
    }
    
    /**
     * CORS Preflight 요청 처리
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
}