package com.shop.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Order createOrder(Long userId, int totalAmount) {
        try {
            Order order = new Order(userId, "PENDING", totalAmount);
            Order savedOrder = orderRepository.save(order);
            
            // 결제 서비스 호출
            String paymentResult = processPayment(savedOrder.getId(), totalAmount);
            
            if ("SUCCESS".equals(paymentResult)) {
                savedOrder.setStatus("COMPLETED");
            } else {
                savedOrder.setStatus("FAILED");
            }
            
            return orderRepository.save(savedOrder);
        } catch (Exception e) {
            return null;
        }
    }
    
    public String processPayment(Long orderId, int amount) {
        try {
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("orderId", orderId);
            paymentData.put("amount", amount);
            
            String response = restTemplate.postForObject(
                "http://payment-service:8083/api/payments/process", 
                paymentData, 
                String.class
            );
            
            return response != null ? "SUCCESS" : "FAILED";
        } catch (Exception e) {
            throw new RuntimeException("Payment service unavailable", e);
        }
    }

    
    public List<Order> getOrdersByUserId(Long userId) {
        try {
            return orderRepository.findByUserId(userId);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public Order updateOrderStatus(Long orderId, String status) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus(status);
                return orderRepository.save(order);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
