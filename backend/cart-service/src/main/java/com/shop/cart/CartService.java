package com.shop.cart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class CartService {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public List<CartItem> getCartItems(Long userId) {
        try {
            return cartRepository.findByUserId(userId);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    public CartItem addToCart(Long userId, Long productId, String productName, int productPrice, int quantity) {
        try {
            Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(userId, productId);
            
            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + quantity);
                return cartRepository.save(item);
            } else {
                CartItem newItem = new CartItem(userId, productId, productName, productPrice, quantity);
                return cartRepository.save(newItem);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    public CartItem updateQuantity(Long itemId, int quantity) {
        try {
            Optional<CartItem> item = cartRepository.findById(itemId);
            if (item.isPresent()) {
                item.get().setQuantity(quantity);
                return cartRepository.save(item.get());
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean removeFromCart(Long itemId) {
        try {
            cartRepository.deleteById(itemId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Transactional
    public boolean clearCart(Long userId) {
        try {
            cartRepository.deleteByUserId(userId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 장바구니 결제 요청 (Payment Service 호출)
     * Istio Circuit Breaker 적용
     */
    public Map<String, Object> processCartPayment(Long userId, String userEmail, Map<String, Object> paymentData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 장바구니 아이템 조회
            List<CartItem> cartItems = getCartItems(userId);
            if (cartItems.isEmpty()) {
                response.put("status", "error");
                response.put("message", "장바구니가 비어있습니다");
                return response;
            }
            
            // 총 금액 계산
            int totalAmount = cartItems.stream()
                .mapToInt(item -> item.getProductPrice() * item.getQuantity())
                .sum();
            
            // 결제 데이터 준비
            Map<String, Object> requestData = new HashMap<>();
            if (paymentData != null) {
                requestData.putAll(paymentData);
            }
            
            // 기본 값 설정
            requestData.putIfAbsent("order_id", "CART_ORDER_" + userId + "_" + System.currentTimeMillis());
            requestData.putIfAbsent("price", totalAmount);
            requestData.putIfAbsent("order_name", getOrderName(cartItems));
            requestData.putIfAbsent("buyer_name", "고객");
            requestData.putIfAbsent("buyer_email", userEmail);
            requestData.putIfAbsent("method", "card");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("User-Email", userEmail);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestData, headers);
            
            logger.info("Payment Service 호출: userId={}, amount={}", userId, totalAmount);
            
            // Istio Service Mesh를 통한 Payment Service 호출
            ResponseEntity<Map> paymentResponse = restTemplate.exchange(
                "http://payment-svc.app-services.svc.cluster.local:8083/api/payment/verify", 
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (paymentResponse.getStatusCode().is2xxSuccessful() && paymentResponse.getBody() != null) {
                Map<String, Object> paymentResult = paymentResponse.getBody();
                String status = (String) paymentResult.get("status");
                
                if ("success".equals(status)) {
                    // 결제 성공 시 장바구니 비우기
                    clearCart(userId);
                    logger.info("장바구니 결제 성공: userId={}, amount={}", userId, totalAmount);
                    
                    response.put("status", "success");
                    response.put("message", "결제가 완료되었습니다");
                    response.put("data", paymentResult.get("data"));
                    response.put("totalAmount", totalAmount);
                } else {
                    logger.warn("장바구니 결제 실패: userId={}, response={}", userId, paymentResult);
                    response.put("status", "error");
                    response.put("message", "결제 처리에 실패했습니다");
                    response.put("details", paymentResult);
                }
            } else {
                logger.error("Payment Service 응답 오류: status={}", paymentResponse.getStatusCode());
                response.put("status", "error");
                response.put("message", "결제 서비스 응답 오류");
            }
            
        } catch (ResourceAccessException e) {
            logger.error("Payment Service 연결 실패 (Istio Circuit Breaker 동작 가능): {}", e.getMessage());
            response.put("status", "error");
            response.put("message", "결제 서비스에 일시적으로 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
            response.put("error_type", "circuit_breaker");
        } catch (Exception e) {
            logger.error("장바구니 결제 처리 중 예외: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 장바구니 아이템으로부터 주문명 생성
     */
    private String getOrderName(List<CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return "쇼핑몰 상품";
        }
        
        CartItem firstItem = cartItems.get(0);
        if (cartItems.size() == 1) {
            return firstItem.getProductName();
        } else {
            return firstItem.getProductName() + " 외 " + (cartItems.size() - 1) + "건";
        }
    }
}

//새 버전 테스트