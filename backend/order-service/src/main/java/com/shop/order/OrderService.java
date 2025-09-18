package com.shop.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public Order createOrder(Long userId, int totalAmount) {
        return createOrder(userId, totalAmount, null);
    }
    
    public Order createOrder(Long userId, int totalAmount, Map<String, Object> paymentData) {
        try {
            Order order = new Order(userId, "PENDING", totalAmount);
            Order savedOrder = orderRepository.save(order);
            
            logger.info("주문 생성: ID={}, UserId={}, Amount={}", savedOrder.getId(), userId, totalAmount);
            
            // 결제 서비스 호출 (Istio Circuit Breaker가 처리)
            String paymentResult = processPayment(savedOrder.getId(), totalAmount, paymentData);
            
            if ("SUCCESS".equals(paymentResult)) {
                savedOrder.setStatus("COMPLETED");
                logger.info("주문 완료: ID={}", savedOrder.getId());
            } else {
                savedOrder.setStatus("FAILED");
                logger.error("주문 실패: ID={}", savedOrder.getId());
            }
            
            return orderRepository.save(savedOrder);
        } catch (Exception e) {
            logger.error("주문 생성 중 오류: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 결제 서비스 호출 (Istio Service Mesh를 통한 호출)
     */
    public String processPayment(Long orderId, int amount, Map<String, Object> paymentData) {
        try {
            Map<String, Object> requestData = new HashMap<>();
            
            if (paymentData != null) {
                requestData.putAll(paymentData);
            } else {
                // 기본 결제 데이터
                requestData.put("order_id", "ORDER_" + orderId);
                requestData.put("receipt_id", "RECEIPT_" + orderId + "_" + System.currentTimeMillis());
                requestData.put("price", amount);
                requestData.put("order_name", "쇼핑몰 상품 주문");
                requestData.put("buyer_name", "고객");
                requestData.put("method", "card");
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("User-Email", "order-service@shop.com");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            logger.info("결제 서비스 호출: orderId={}, amount={}", orderId, amount);
            
            // ✅ 수정: Kubernetes DNS를 통한 호출 (Istio가 자동으로 라우팅)
            ResponseEntity<Map> response = restTemplate.exchange(
                "http://payment-svc.app-services.svc.cluster.local:8083/api/payment/verify", 
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String status = (String) responseBody.get("status");
                
                if ("success".equals(status)) {
                    logger.info("결제 성공: orderId={}", orderId);
                    return "SUCCESS";
                } else {
                    logger.warn("결제 실패: orderId={}, response={}", orderId, responseBody);
                    return "FAILED";
                }
            } else {
                logger.error("결제 서비스 응답 오류: status={}", response.getStatusCode());
                return "FAILED";
            }
            
        } catch (ResourceAccessException e) {
            logger.error("결제 서비스 연결 실패 (Istio Outlier Detection 동작 가능): {}", e.getMessage());
            return "FAILED";
        } catch (Exception e) {
            logger.error("결제 처리 중 예외: {}", e.getMessage(), e);
            return "FAILED";
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
