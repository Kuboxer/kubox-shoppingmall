package com.shop.cart;

import org.springframework.beans.factory.annotation.Autowired;
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
     * 서비스 버전 정보 조회s
     */
    @GetMapping("/version")
    public ResponseEntity<Map<String, String>> getVersion() {
        Map<String, String> version = Map.of(
            "service", "cart-service",
            "version", "v2.0.0",
            "description", "장바구니 서비스 - 새로운 기능 추가",
            "lastUpdated", "2025-08-26"
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
