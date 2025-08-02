package com.shop.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    public Order createOrder(Long userId, int totalAmount) {
        try {
            Order order = new Order(userId, "PENDING", totalAmount);
            return orderRepository.save(order);
        } catch (Exception e) {
            return null;
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
