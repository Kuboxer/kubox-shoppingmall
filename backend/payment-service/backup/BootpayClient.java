package com.shop.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

// Bootpay SDK 임포트
import kr.co.bootpay.Bootpay;
import kr.co.bootpay.model.request.Cancel;
import kr.co.bootpay.model.response.ResDefault; // API 응답 래퍼 클래스

@Component
public class BootpayClient {

    @Value("${bootpay.application-id}")
    private String applicationId;

    @Value("${bootpay.private-key}")
    private String privateKey;

    /**
     * Bootpay API 인스턴스를 생성하고 Access Token을 발급받아 반환합니다.
     * 발급된 토큰은 Bootpay 객체 내부에 자동으로 설정됩니다.
     * @return Access Token이 설정된 Bootpay 객체
     * @throws Exception 토큰 발급 실패 시 예외 발생
     */
    private Bootpay getBootpayApi() throws Exception {
        Bootpay bootpay = new Bootpay(applicationId, privateKey);

        ResDefault<HashMap<String, Object>> resDefault = bootpay.getAccessToken();

        // ResDefault 객체의 필드에 직접 접근합니다.
        if (resDefault != null && resDefault.status == 200) { // status 필드 직접 접근
            System.out.println("Bootpay API Access Token 발급 성공: " + resDefault.data); // data 필드 직접 접근
        } else {
            String errorMessage = (resDefault != null && resDefault.message != null) ? resDefault.message : "알 수 없는 토큰 발급 오류"; // message 필드 직접 접근
            System.err.println("Bootpay API Access Token 발급 실패: " + errorMessage);
            throw new RuntimeException("Bootpay API Access Token 발급 실패: " + errorMessage);
        }
        return bootpay;
    }

    /**
     * BootPay를 통해 결제된 내역을 검증합니다.
     * @param receiptId BootPay에서 발급한 영수증 ID
     * @return BootPay 검증 API 응답의 'data' 부분 (Map 형태)
     */
    public Map<String, Object> verifyPayment(String receiptId) {
        try {
            Bootpay bootpay = getBootpayApi();

            // 결제 검증은 bootpay.getReceipt() 메서드를 사용합니다.
            ResDefault<HashMap<String, Object>> resDefault = bootpay.getReceipt(receiptId);

            if (resDefault != null && resDefault.status == 200) { // status 필드 직접 접근
                System.out.println("BootPay 결제 검증 성공: " + resDefault.data); // data 필드 직접 접근
                return resDefault.data; // SDK 응답의 data 부분을 Map으로 반환
            } else {
                String errorMessage = (resDefault != null && resDefault.message != null) ? resDefault.message : "BootPay 검증 응답 오류"; // message 필드 직접 접근
                System.err.println("BootPay 결제 검증 실패: " + errorMessage);
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("status", resDefault != null ? resDefault.status : 500); // status 필드 직접 접근
                errorMap.put("message", errorMessage);
                return errorMap;
            }
        } catch (Exception e) {
            System.err.println("BootPay 검증 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", 500);
            errorMap.put("message", "서버 내부 오류: " + e.getMessage());
            return errorMap;
        }
    }

    /**
     * BootPay를 통해 결제된 내역을 취소합니다.
     * @param receiptId 취소할 결제의 영수증 ID
     * @param reason 취소 사유
     * @return 취소 결과 메시지를 포함한 Map
     */
    public Map<String, Object> cancelPayment(String receiptId, String reason) {
        try {
            Bootpay bootpay = getBootpayApi();

            Cancel cancel = new Cancel();
            // Cancel 객체의 필드에 직접 접근하여 값을 설정합니다.
            cancel.receiptId = receiptId;
            cancel.name = "관리자";
            cancel.reason = reason;

            ResDefault<HashMap<String, Object>> resDefault = bootpay.cancel(cancel);

            if (resDefault != null && resDefault.status == 200) { // status 필드 직접 접근
                System.out.println("BootPay 결제 취소 성공: " + resDefault.message); // message 필드 직접 접근
                return Map.of("status", "200", "message", "결제 취소 완료");
            } else {
                String errorMessage = (resDefault != null && resDefault.message != null) ? resDefault.message : "BootPay 취소 응답 오류"; // message 필드 직접 접근
                System.err.println("BootPay 결제 취소 실패: " + errorMessage);
                return Map.of("status", resDefault != null ? resDefault.status : "500", "message", errorMessage); // status 필드 직접 접근
            }
        } catch (Exception e) {
            System.err.println("BootPay 취소 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return Map.of("status", "500", "message", "서버 오류: " + e.getMessage());
        }
    }
}
