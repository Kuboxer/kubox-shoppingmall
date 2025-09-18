package com.shop.payment.failure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 장애 테스트용 컨트롤러
 * Circuit Breaker 및 장애 격리 시연을 위한 장애 모드 제어
 */
@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class PaymentFailureController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    // 장애 모드 플래그
    private static final AtomicBoolean failureMode = new AtomicBoolean(false);
    private static volatile int failureType = 1; // 1: timeout, 2: error, 3: exception
    
    /**
     * 장애 모드 활성화/비활성화
     */
    @PostMapping("/failure/toggle")
    public ResponseEntity<Map<String, Object>> toggleFailureMode(
            @RequestBody(required = false) Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 요청에서 모드 설정
            if (request != null && request.containsKey("enable")) {
                boolean enable = (Boolean) request.get("enable");
                failureMode.set(enable);
                
                if (request.containsKey("type")) {
                    failureType = (Integer) request.get("type");
                }
            } else {
                // 토글
                failureMode.set(!failureMode.get());
            }
            
            response.put("status", "success");
            response.put("failureMode", failureMode.get());
            response.put("failureType", failureType);
            response.put("message", failureMode.get() ? "장애 모드 활성화" : "장애 모드 비활성화");
            
            logger.warn("Payment 서비스 장애 모드: {} (타입: {})", 
                    failureMode.get() ? "활성화" : "비활성화", failureType);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("장애 모드 토글 실패: {}", e.getMessage());
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 장애 모드 상태 확인
     */
    @GetMapping("/failure/status")
    public ResponseEntity<Map<String, Object>> getFailureStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("failureMode", failureMode.get());
        response.put("failureType", failureType);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 장애 시뮬레이션 메서드
     * PaymentService에서 호출하여 장애 발생
     */
    public static void simulateFailure() throws Exception {
        if (!failureMode.get()) {
            return; // 장애 모드가 아니면 정상 진행
        }
        
        switch (failureType) {
            case 1:
                // 타임아웃 시뮬레이션 (10초 지연)
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Payment service interrupted", e);
                }
                break;
                
            case 2:
                // HTTP 500 에러 시뮬레이션
                throw new RuntimeException("Payment service internal error - 결제 시스템 장애 발생");
                
            case 3:
                // 네트워크 에러 시뮬레이션
                throw new Exception("Payment gateway connection failed - 외부 결제 서비스 연결 실패");
                
            default:
                throw new RuntimeException("Payment service error - 알 수 없는 결제 오류");
        }
    }
    
    /**
     * 특정 키워드가 포함된 요청에 대해서만 장애 발생
     */
    public static boolean shouldFail(Map<String, Object> requestData) {
        if (!failureMode.get()) {
            return false;
        }
        
        if (requestData == null) {
            return false;
        }
        
        // 특정 키워드 체크
        String orderId = (String) requestData.getOrDefault("order_id", "");
        String orderName = (String) requestData.getOrDefault("order_name", "");
        String buyerName = (String) requestData.getOrDefault("buyer_name", "");
        
        // "FAILURE", "ERROR", "TEST" 키워드가 포함된 경우 장애 발생
        return orderId.contains("FAILURE") || 
               orderName.contains("FAILURE") || 
               buyerName.contains("FAILURE") ||
               orderId.contains("ERROR") ||
               orderName.contains("ERROR") ||
               buyerName.contains("ERROR") ||
               orderId.contains("TEST_FAIL") ||
               orderName.contains("TEST_FAIL");
    }
}
