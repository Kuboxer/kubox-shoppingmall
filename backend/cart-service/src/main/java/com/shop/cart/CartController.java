package com.shop.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class CartController {
    @Autowired
    private CartService cartService;
    
    @Value("${APP_VERSION:unknown}")
    private String appVersion;
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItem>> getCartItems(@PathVariable Long userId) {
        try {
            List<CartItem> items = cartService.getCartItems(userId);
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request) {
        try {
            CartItem item = cartService.addToCart(
                Long.valueOf(request.get("userId").toString()),
                Long.valueOf(request.get("productId").toString()),
                request.get("productName").toString(),
                Integer.valueOf(request.get("productPrice").toString()),
                Integer.valueOf(request.get("quantity").toString())
            );
            
            if (item != null) {
                return ResponseEntity.ok(item);
            }
            return ResponseEntity.badRequest().body(Map.of("message", "장바구니 추가 실패"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류"));
        }
    }
    
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long itemId, @RequestBody Map<String, Integer> request) {
        try {
            CartItem item = cartService.updateQuantity(itemId, request.get("quantity"));
            if (item != null) {
                return ResponseEntity.ok(item);
            }
            return ResponseEntity.badRequest().body(Map.of("message", "수량 변경 실패"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류"));
        }
    }
    
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long itemId) {
        try {
            if (cartService.removeFromCart(itemId)) {
                return ResponseEntity.ok(Map.of("message", "삭제 완료"));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "삭제 실패"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류"));
        }
    }
    
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        try {
            if (cartService.clearCart(userId)) {
                return ResponseEntity.ok(Map.of("message", "장바구니 비우기 완료"));
            }
            return ResponseEntity.badRequest().body(Map.of("message", "장바구니 비우기 실패"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류"));
        }
    }
    
    /**
     * 🛒 장바구니 결제 요청 (Circuit Breaker 적용)
     */
    @PostMapping("/{userId}/payment")
    public ResponseEntity<Map<String, Object>> processPayment(
            @PathVariable Long userId,
            @RequestHeader(value = "User-Email") String userEmail,
            @RequestBody Map<String, Object> paymentData) {
        
        try {
            Map<String, Object> result = cartService.processCartPayment(userId, userEmail, paymentData);
            
            if ("success".equals(result.get("status"))) {
                return ResponseEntity.ok(result);
            } else {
                // Circuit Breaker가 열린 경우 503 응답
                if ("circuit_breaker".equals(result.get("error_type"))) {
                    return ResponseEntity.status(503).body(result);
                }
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "결제 처리 중 서버 오류가 발생했습니다"
            ));
        }
    }
    
    /**
     * 서비스 상태 체크
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = Map.of(
            "status", "UP",
            "service", "cart-service",
            "port", "8084",
            "timestamp", System.currentTimeMillis(),
            "version", appVersion,
            "description", "장바구니 서비스 - Circuit Breaker 지원 결제 기능"
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * 서비스 버전 정보 조회
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        Map<String, String> version = Map.of(
            "service", "cart-service",
            "version", appVersion,
            "description", "장바구니 서비스 - Payment Service Circuit Breaker 연동",
            "lastUpdated", "2025-09-18",
            "feature", "Istio Circuit Breaker 지원 결제"
        );
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
