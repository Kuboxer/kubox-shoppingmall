#!/bin/bash
chmod +x "$0"

echo "🔥 Istio Circuit Breaker 실시간 테스트"
echo "======================================"

# Payment Service Pod 찾기
PAYMENT_POD=$(kubectl get pods -n app-services -l app=payment-service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)

if [ -z "$PAYMENT_POD" ]; then
    echo "❌ Payment Service Pod를 찾을 수 없습니다"
    exit 1
fi

echo "📍 Payment Pod: $PAYMENT_POD"
echo ""

# 5번 연속 장애 요청 시뮬레이션
echo "🔄 연속 장애 요청 시뮬레이션 (5회)"
echo "--------------------------------------"

for i in {1..5}; do
    echo "요청 $i/5..."
    
    # API 요청 (백그라운드)
    curl -s -w "%{http_code}" -X POST https://api.kubox.shop/api/payment/verify \
        -H "Content-Type: application/json" \
        -H "User-Email: test@example.com" \
        -d '{
            "order_id": "ORDER_CIRCUIT_TEST_'$i'",
            "receipt_id": "RECEIPT_CIRCUIT_TEST_'$i'",
            "price": 50000,
            "order_name": "Circuit Breaker 테스트",
            "buyer_name": "FAILURE_USER",
            "method": "card"
        }' > /tmp/response_$i.txt &
    
    sleep 2
done

echo ""
echo "⏳ 요청 완료 대기 중..."
wait

echo ""
echo "📊 응답 코드 확인:"
for i in {1..5}; do
    response=$(cat /tmp/response_$i.txt 2>/dev/null)
    echo "  요청 $i: HTTP $response"
done

echo ""
echo "🔍 Istio Outlier Detection 통계 확인:"
echo "--------------------------------------"

# Envoy 통계 확인
kubectl exec -it $PAYMENT_POD -c istio-proxy -n app-services -- \
    curl -s localhost:15000/stats 2>/dev/null | grep -E "(outlier|ejected)" | head -10 || \
    echo "❌ 통계 조회 실패 (정상적인 경우도 있음)"

echo ""
echo "🎯 Circuit Breaker 동작 확인:"
echo "--------------------------------------"

# Cluster 상태 확인
kubectl exec -it $PAYMENT_POD -c istio-proxy -n app-services -- \
    curl -s localhost:15000/clusters 2>/dev/null | grep -A 5 -B 5 "payment-svc" | head -20 || \
    echo "❌ 클러스터 상태 조회 실패"

echo ""
echo "✅ 테스트 완료!"
echo ""
echo "🎬 다음 단계:"
echo "1. Kiali UI: kubectl port-forward svc/kiali 20001:20001 -n istio-system"
echo "2. 브라우저: http://localhost:20001"
echo "3. Graph → app-services 네임스페이스에서 Circuit 상태 확인"

# 임시 파일 정리
rm -f /tmp/response_*.txt
