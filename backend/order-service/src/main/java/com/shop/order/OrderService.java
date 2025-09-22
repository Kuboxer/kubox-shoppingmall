package com.shop.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * 주문 생성 (결제는 별도 처리)
     * 결제 완료 후 주문 상태를 업데이트하는 용도
     */
    public Order createOrder(Long userId, int totalAmount) {
        try {
            Order order = new Order(userId, "PENDING", totalAmount);
            Order savedOrder = orderRepository.save(order);
            
            logger.info("주문 생성: ID={}, UserId={}, Amount={}", savedOrder.getId(), userId, totalAmount);
            return savedOrder;
        } catch (Exception e) {
            logger.error("주문 생성 중 오류: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 결제 완료 후 주문 상태 업데이트
     */
    public Order completeOrder(Long orderId, String paymentId) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus("COMPLETED");
                Order updated = orderRepository.save(order);
                logger.info("주문 완료: ID={}, PaymentID={}", orderId, paymentId);
                return updated;
            }
            logger.warn("주문을 찾을 수 없음: ID={}", orderId);
            return null;
        } catch (Exception e) {
            logger.error("주문 완료 처리 실패: ID={}, error={}", orderId, e.getMessage());
            return null;
        }
    }
    
    /**
     * 결제 실패 시 주문 상태 업데이트
     */
    public Order failOrder(Long orderId, String reason) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus("FAILED");
                Order updated = orderRepository.save(order);
                logger.warn("주문 실패: ID={}, Reason={}", orderId, reason);
                return updated;
            }
            logger.warn("주문을 찾을 수 없음: ID={}", orderId);
            return null;
        } catch (Exception e) {
            logger.error("주문 실패 처리 실패: ID={}, error={}", orderId, e.getMessage());
            return null;
        }
    }

    public List<Order> getOrdersByUserId(Long userId) {
        try {
            return orderRepository.findByUserId(userId);
        } catch (Exception e) {
            logger.error("주문 조회 실패: userId={}, error={}", userId, e.getMessage());
            return List.of();
        }
    }
    
    public Order updateOrderStatus(Long orderId, String status) {
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setStatus(status);
                Order updated = orderRepository.save(order);
                logger.info("주문 상태 업데이트: ID={}, status={}", orderId, status);
                return updated;
            }
            logger.warn("주문을 찾을 수 없음: ID={}", orderId);
            return null;
        } catch (Exception e) {
            logger.error("주문 상태 업데이트 실패: ID={}, error={}", orderId, e.getMessage());
            return null;
        }
    }
    
    public Order getOrderById(Long orderId) {
        try {
            return orderRepository.findById(orderId).orElse(null);
        } catch (Exception e) {
            logger.error("주문 조회 실패: ID={}, error={}", orderId, e.getMessage());
            return null;
        }
    }
}
//새 버전 테스트