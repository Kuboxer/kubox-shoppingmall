package com.shop.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    
    private static final String WIDGET_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    private static final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments/confirm";
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Transactional
    public Map<String, Object> confirmPayment(String orderId, String amount, String paymentKey, String userEmail) throws Exception {
        
        // 중복 결제 확인
        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new RuntimeException("이미 처리된 주문입니다.");
        }
        
        // 토스페이먼츠 결제 승인 요청
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("orderId", orderId);
        requestData.put("amount", amount);
        requestData.put("paymentKey", paymentKey);
        
        // Basic Auth 헤더 생성
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((WIDGET_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));
        String authorization = "Basic " + new String(encodedBytes);
        
        // HTTP 요청
        URL url = new URL(TOSS_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorization);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        
        try (OutputStream outputStream = connection.getOutputStream()) {
            String jsonRequest = objectMapper.writeValueAsString(requestData);
            outputStream.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
        }
        
        int responseCode = connection.getResponseCode();
        boolean isSuccess = responseCode == 200;
        
        InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
        
        Map<String, Object> responseData;
        
        try (InputStreamReader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            JsonNode jsonNode = objectMapper.readTree(reader);
            responseData = objectMapper.convertValue(jsonNode, Map.class);
        }
        
        // 결제 정보 저장
        savePaymentInfo(orderId, paymentKey, userEmail, Long.parseLong(amount), responseData, isSuccess);
        
        if (!isSuccess) {
            throw new RuntimeException("결제 승인 실패: " + responseData.get("message"));
        }
        
        return responseData;
    }
    
    @Transactional
    private void savePaymentInfo(String orderId, String paymentKey, String userEmail, Long amount, 
                                Map<String, Object> responseData, boolean isSuccess) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentKey(paymentKey);
        payment.setUserEmail(userEmail);
        payment.setAmount(amount);
        payment.setStatus(isSuccess ? "SUCCESS" : "FAIL");
        
        try {
            payment.setResponseData(objectMapper.writeValueAsString(responseData));
        } catch (Exception e) {
            payment.setResponseData(responseData.toString());
        }
        
        if (isSuccess && responseData.containsKey("method")) {
            payment.setMethod((String) responseData.get("method"));
        }
        
        // 고객 정보 추출
        if (responseData.containsKey("customerName")) {
            payment.setCustomerName((String) responseData.get("customerName"));
        }
        if (responseData.containsKey("customerEmail")) {
            payment.setCustomerEmail((String) responseData.get("customerEmail"));
        }
        if (responseData.containsKey("customerMobilePhone")) {
            payment.setCustomerPhone((String) responseData.get("customerMobilePhone"));
        }
        
        paymentRepository.save(payment);
    }
    
    public List<Payment> getPaymentHistory(String userEmail) {
        return paymentRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }
    
    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));
    }
    
    // 장애 전파 방지를 위한 기본 응답
    public Map<String, Object> getDefaultResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Payment service temporarily unavailable");
        return response;
    }
}
