package com.shop.order;

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
@RequestMapping("/api/orders")  // ✅ 수정: VirtualService와 일치하도록 변경
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class OrderController {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private OrderService orderService;
    
    @Value("${APP_VERSION:unknown}")
    private String appVersion;
    
    /**
     * 주문 생성 (장애 테스트 지원)
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("주문 생성 요청: {}", request);
            
            Long userId = Long.valueOf(request.getOrDefault("userId", "1").toString());
            Integer totalAmount = Integer.valueOf(request.getOrDefault("totalAmount", "50000").toString());
            
            // 결제 데이터 추출 (장애 테스트용)
            Map<String, Object> paymentData = null;
            if (request.containsKey("test_failure") && (Boolean) request.get("test_failure")) {
                paymentData = new HashMap<>();
                paymentData.put("order_id", "ORDER_FAILURE_" + System.currentTimeMillis());
                paymentData.put("receipt_id", "RECEIPT_FAILURE_" + System.currentTimeMillis());
                paymentData.put("price", totalAmount);
                paymentData.put("order_name", "장애 테스트 상품");
                paymentData.put("buyer_name", "FAILURE_USER");
                paymentData.put("method", "card");
            }
            
            Order order = orderService.createOrder(userId, totalAmount, paymentData);
            
            if (order != null) {
                response.put("status", "success");
                response.put("message", "주문이 생성되었습니다");
                response.put("data", order);
                
                logger.info("주문 생성 성공: ID={}, Status={}", order.getId(), order.getStatus());
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "주문 생성에 실패했습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("주문 생성 중 오류: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 사용자별 주문 목록 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("주문 목록 조회 실패: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 주문 상태 업데이트
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long orderId, 
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String status = request.get("status");
            Order order = orderService.updateOrderStatus(orderId, status);
            
            if (order != null) {
                response.put("status", "success");
                response.put("message", "주문 상태가 업데이트되었습니다");
                response.put("data", order);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "주문 상태 업데이트에 실패했습니다");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            logger.error("주문 상태 업데이트 실패: orderId={}, error={}", orderId, e.getMessage());
            response.put("status", "error");
            response.put("message", "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("주문 조회 실패: orderId={}, error={}", orderId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 서비스 상태 체크
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "order-service");
        response.put("port", "8082");  // ✅ 수정: VirtualService의 포트와 일치
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", appVersion);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 서비스 버전 정보 조회
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        Map<String, String> version = new HashMap<>();
        version.put("service", "order-service");
        version.put("version", appVersion);
        version.put("description", "주문 처리 서비스 - Istio Outlier Detection 연동");
        version.put("lastUpdated", "2025-09-18");
        version.put("feature", "Payment Service 장애 테스트 지원");
        
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
