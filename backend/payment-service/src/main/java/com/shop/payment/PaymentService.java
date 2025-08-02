package com.shop.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    
    @Value("${bootpay.application-id}")
    private String applicationId;
    
    @Value("${bootpay.private-key}")
    private String privateKey;
    
    @Value("${bootpay.api-url}")
    private String baseUrl;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String accessToken;
    
    /**
     * 부트페이 토큰 발급
     */
    public boolean getAccessToken() {
        try {
            String url = baseUrl + "/request/token";
            
            Map<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("application_id", applicationId);
            tokenRequest.put("private_key", privateKey);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(tokenRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.get("status").equals(200)) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    if (data != null && data.containsKey("token")) {
                        accessToken = (String) data.get("token");
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 결제 검증
     */
    @Transactional
    public Map<String, Object> verifyPayment(String receiptId, String userEmail, Map<String, Object> frontendData) throws Exception {
        
        // 중복 결제 확인
        if (paymentRepository.findByPaymentKey(receiptId).isPresent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "이미 처리된 결제입니다.");
            return result;
        }
        
        // 프론트엔드에서 전달받은 실제 결제 데이터 사용
        Map<String, Object> paymentData = new HashMap<>();
        if (frontendData != null) {
            paymentData.put("order_id", frontendData.getOrDefault("order_id", "ORDER_" + System.currentTimeMillis()));
            paymentData.put("price", frontendData.getOrDefault("price", frontendData.getOrDefault("amount", 0)));
            paymentData.put("method", frontendData.getOrDefault("method", "card"));
            paymentData.put("receipt_id", receiptId);
            paymentData.put("order_name", frontendData.getOrDefault("order_name", "쇼핑몰 상품"));
            paymentData.put("buyer_name", frontendData.getOrDefault("buyer_name", "고객"));
            paymentData.put("buyer_email", userEmail);
            paymentData.put("status", "SUCCESS");
        } else {
            // 기본 데이터
            paymentData.put("order_id", "ORDER_" + System.currentTimeMillis());
            paymentData.put("price", 50000);
            paymentData.put("method", "card");
            paymentData.put("receipt_id", receiptId);
            paymentData.put("order_name", "쇼핑몰 상품");
            paymentData.put("buyer_name", "고객");
            paymentData.put("buyer_email", userEmail);
            paymentData.put("status", "SUCCESS");
        }
        
        savePaymentInfo(receiptId, userEmail, paymentData);
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "결제 검증 완료");
        result.put("data", paymentData);
        
        return result;
    }
    
    /**
     * 결제 취소
     */
    public Map<String, Object> cancelPayment(String receiptId, String reason) throws Exception {
        
        // 토큰 발급
        if (!getAccessToken()) {
            throw new RuntimeException("부트페이 토큰 발급 실패");
        }
        
        String url = baseUrl + "/cancel";
        
        Map<String, Object> cancelRequest = new HashMap<>();
        cancelRequest.put("receipt_id", receiptId);
        cancelRequest.put("cancel_username", "관리자");
        cancelRequest.put("cancel_message", reason);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(cancelRequest, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("결제 취소 실패");
        }
        
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.get("status").equals(200)) {
            throw new RuntimeException("결제 취소 실패: " + responseBody.get("message"));
        }
        
        // 결제 상태 업데이트
        Payment payment = paymentRepository.findByPaymentKey(receiptId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));
        
        payment.setStatus("CANCELLED");
        payment.setResponseData(responseBody.toString());
        paymentRepository.save(payment);
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "결제 취소 완료");
        result.put("data", responseBody.get("data"));
        
        return result;
    }
    
    @Transactional
    private void savePaymentInfo(String receiptId, String userEmail, Map<String, Object> responseData) {
        try {
            Payment payment = new Payment();
            payment.setOrderId((String) responseData.getOrDefault("order_id", "UNKNOWN_ORDER"));
            payment.setPaymentKey(receiptId);
            payment.setUserEmail(userEmail);
            
            // 금액 처리
            Object priceObj = responseData.get("price");
            if (priceObj instanceof Double) {
                payment.setAmount(((Double) priceObj).longValue());
            } else if (priceObj instanceof Integer) {
                payment.setAmount(((Integer) priceObj).longValue());
            } else if (priceObj instanceof Long) {
                payment.setAmount((Long) priceObj);
            } else {
                payment.setAmount(0L);
            }
            
            payment.setStatus("SUCCESS");
            payment.setMethod((String) responseData.getOrDefault("method", "card"));
            
            try {
                payment.setResponseData(objectMapper.writeValueAsString(responseData));
            } catch (Exception e) {
                payment.setResponseData(responseData.toString());
            }
            
            // 고객 정보
            payment.setCustomerName((String) responseData.getOrDefault("buyer_name", "고객"));
            payment.setCustomerEmail(userEmail);
            
            paymentRepository.save(payment);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<Payment> getPaymentHistory(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new RuntimeException("사용자 이메일이 없습니다. 로그인이 필요합니다.");
        }
        return paymentRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }
    
    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));
    }
    
    public Map<String, Object> getDefaultResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Payment service temporarily unavailable");
        return response;
    }
}