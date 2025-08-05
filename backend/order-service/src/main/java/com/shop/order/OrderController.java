package com.shop.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "${CORS_ALLOWED_ORIGINS}")
public class OrderController {
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Order order = orderService.createOrder(
                Long.valueOf(request.get("userId").toString()),
                Integer.valueOf(request.get("totalAmount").toString())
            );
            
            if (order != null) {
                return ResponseEntity.ok(order);
            }
            return ResponseEntity.badRequest().body(Map.of("message", "주문 생성 실패"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류"));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestBody Map<String, String> request) {
        try {
            Order order = orderService.updateOrderStatus(orderId, request.get("status"));
            if (order != null) {
                return ResponseEntity.ok(order);
            }
            return ResponseEntity.badRequest().body(Map.of("message", "주문 상태 변경 실패"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "서버 오류"));
        }
    }
}
