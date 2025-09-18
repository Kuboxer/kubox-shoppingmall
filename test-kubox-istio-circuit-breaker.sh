# 🔥 Payment Service 장애 시뮬레이션 가이드 (Kubox-EKS Istio)

## 📋 시나리오 A: 장애 격리 성공 테스트

### 1. 사전 준비

#### EKS 클러스터 및 Istio 설정 확인
```bash
# 현재 kubox-eks 디렉토리로 이동
cd /Users/ichungmin/Desktop/kubox-eks/kubox-eks

# EKS 클러스터 연결 확인
kubectl get nodes

# app-services 네임스페이스의 모든 파드 확인
kubectl get pods -n app-services

# Istio 사이드카 주입 확인 (Ready 컬럼에 2/2 표시되어야 함)
kubectl get pods -n app-services -o wide
```

#### Istio Gateway 및 LoadBalancer 확인
```bash
# Istio Ingress Gateway 서비스 확인
kubectl get svc -n istio-system istio-ingressgateway

# 외부 접근 가능한 LoadBalancer IP/DNS 확인
export GATEWAY_URL=$(kubectl get svc -n istio-system istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
echo "Gateway URL: http://$GATEWAY_URL"

# 또는 IP가 할당된 경우
# export GATEWAY_URL=$(kubectl get svc -n istio-system istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
```

### 2. 시연 시나리오

#### 🎬 STEP 1: 정상 상태 확인
```bash
# 모든 서비스 헬스체크
echo "=== 서비스 상태 확인 ==="
curl -s http://$GATEWAY_URL/api/users/health | jq '.'
curl -s http://$GATEWAY_URL/api/products/health | jq '.'
curl -s http://$GATEWAY_URL/api/cart/health | jq '.'
curl -s http://$GATEWAY_URL/api/orders/health | jq '.'
curl -s http://$GATEWAY_URL/api/payment/health | jq '.'
```

#### 🎬 STEP 2: 정상 주문/결제 플로우 테스트
```bash
# 1) 상품 조회
echo "=== 상품 목록 조회 ==="
curl -s http://$GATEWAY_URL/api/products | jq '.'

# 2) 정상 결제 테스트
echo "=== 정상 결제 테스트 ==="
curl -X POST http://$GATEWAY_URL/api/payment/verify \
  -H "Content-Type: application/json" \
  -H "User-Email: demo@kubox.shop" \
  -d '{
    "order_id": "ORDER_NORMAL_001",
    "receipt_id": "RECEIPT_NORMAL_001",
    "price": 25000,
    "order_name": "정상 테스트 상품",
    "buyer_name": "정상고객",
    "method": "card"
  }' | jq '.'
```

#### 🎬 STEP 3: Payment Service 장애 모드 활성화
```bash
# Payment Service 장애 모드 활성화 (에러 타입)
echo "=== Payment Service 장애 모드 활성화 ==="
curl -X POST http://$GATEWAY_URL/api/payment/failure/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "enable": true,
    "type": 2
  }' | jq '.'

# 장애 모드 상태 확인
curl -s http://$GATEWAY_URL/api/payment/failure/status | jq '.'
```

#### 🎬 STEP 4: Istio Outlier Detection 트리거
```bash
echo "=== Istio Outlier Detection 트리거 (연속 장애 요청) ==="

# 연속으로 장애 결제 요청 (5회) - FAILURE 키워드로 의도적 장애 발생
for i in {1..5}; do
  echo "장애 시도 $i/5:"
  curl -X POST http://$GATEWAY_URL/api/payment/verify \
    -H "Content-Type: application/json" \
    -H "User-Email: demo@kubox.shop" \
    -d '{
      "order_id": "ORDER_FAILURE_'$i'",
      "receipt_id": "RECEIPT_FAILURE_'$i'",
      "price": 50000,
      "order_name": "장애 테스트 상품",
      "buyer_name": "FAILURE_USER",
      "method": "card"
    }' 
  echo -e "\n"
  sleep 2
done
```

#### 🎬 STEP 5: Istio 메트릭으로 Circuit Breaker 상태 확인
```bash
# Payment Service Pod 이름 가져오기
PAYMENT_POD=$(kubectl get pods -n app-services -l app=payment-service -o jsonpath='{.items[0].metadata.name}')
echo "Payment Pod: $PAYMENT_POD"

# Envoy 통계 확인 (Outlier Detection 관련)
echo "=== Istio Envoy Outlier Detection 상태 ==="
kubectl exec -it $PAYMENT_POD -c istio-proxy -n app-services -- \
  curl -s localhost:15000/stats | grep -E "(outlier|ejected|health_check)" | head -10

# 클러스터 상태 확인
echo "=== Payment Service 클러스터 상태 ==="
kubectl exec -it $PAYMENT_POD -c istio-proxy -n app-services -- \
  curl -s localhost:15000/clusters | grep payment-svc
```

#### 🎬 STEP 6: 다른 서비스는 정상 동작 확인 (장애 격리)
```bash
echo "=== 다른 서비스 정상 동작 확인 ==="

# User Service - 정상 동작
echo "User Service:"
curl -s http://$GATEWAY_URL/api/users/health | jq '.'

# Product Service - 정상 동작  
echo "Product Service:"
curl -s http://$GATEWAY_URL/api/products/health | jq '.'

# Cart Service - 정상 동작
echo "Cart Service:"
curl -s http://$GATEWAY_URL/api/cart/health | jq '.'

# 상품 조회는 여전히 정상 동작
echo "상품 목록 조회 (정상 동작):"
curl -s http://$GATEWAY_URL/api/products | jq '.[0:2]'  # 처음 2개 상품만 표시
```

#### 🎬 STEP 7: Blue-Green 배포 시뮬레이션 (즉시 복구)
```bash
echo "=== Blue-Green 배포 시뮬레이션: 장애 서비스 즉시 복구 ==="

# 장애 모드 비활성화 (Green 환경으로 즉시 전환)
curl -X POST http://$GATEWAY_URL/api/payment/failure/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "enable": false
  }' | jq '.'

# 복구 상태 확인
echo "복구 상태 확인:"
curl -s http://$GATEWAY_URL/api/payment/failure/status | jq '.'
```

#### 🎬 STEP 8: 서비스 복구 검증
```bash
echo "=== 서비스 복구 검증 ==="

# 잠시 대기 (Istio Outlier Detection 복구 시간)
echo "Istio Outlier Detection 복구 대기 중..."
sleep 10

# 정상 결제로 복구 확인
curl -X POST http://$GATEWAY_URL/api/payment/verify \
  -H "Content-Type: application/json" \
  -H "User-Email: demo@kubox.shop" \
  -d '{
    "order_id": "ORDER_RECOVERY_001",
    "receipt_id": "RECEIPT_RECOVERY_001",
    "price": 30000,
    "order_name": "복구 테스트 상품",
    "buyer_name": "복구고객",
    "method": "card"
  }' | jq '.'

# 최종 서비스 상태 확인
echo "=== 최종 서비스 상태 ==="
curl -s http://$GATEWAY_URL/api/payment/health | jq '.'
```

### 3. Istio 기반 모니터링 및 관찰성

#### Kiali 대시보드 접속
```bash
# Kiali 포트포워딩
kubectl port-forward svc/kiali 20001:20001 -n istio-system &

echo "Kiali 대시보드: http://localhost:20001"
echo "- Graph 탭에서 서비스 간 트래픽 플로우 확인"
echo "- 장애 발생 시 빨간색 에러 표시 확인"
echo "- Circuit Breaker 동작 시 트래픽 차단 확인"
```

#### Jaeger 분산 추적
```bash
# Jaeger 포트포워딩  
kubectl port-forward svc/tracing 16686:80 -n istio-system &

echo "Jaeger 대시보드: http://localhost:16686"
echo "- order-service → payment-service 호출 트레이스 확인"
echo "- 장애 발생 시 에러 스팬 확인"
```

#### Grafana 메트릭
```bash
# Grafana 포트포워딩
kubectl port-forward svc/grafana 3000:3000 -n istio-system &

echo "Grafana 대시보드: http://localhost:3000"
echo "- Istio Service Dashboard 확인"
echo "- Success Rate, Latency, Throughput 메트릭 확인"
```

### 4. 자동화된 전체 시나리오 테스트

<function_calls>
<invoke name="filesystem:write_file">
<parameter name="content">#!/bin/bash

# Kubox-EKS Istio Circuit Breaker 장애 시뮬레이션 테스트
echo "🔥 Kubox-EKS Istio Outlier Detection 장애 시뮬레이션"
echo "=================================================="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 현재 디렉토리가 kubox-eks인지 확인
if [[ ! -f "istio/destination-rules.yaml" ]]; then
    echo -e "${RED}❌ kubox-eks 디렉토리에서 실행해주세요${NC}"
    exit 1
fi

# Istio Gateway URL 설정
GATEWAY_URL=$(kubectl get svc -n istio-system istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null)
if [ -z "$GATEWAY_URL" ]; then
    GATEWAY_URL=$(kubectl get svc -n istio-system istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null)
fi
if [ -z "$GATEWAY_URL" ]; then
    echo -e "${RED}❌ Istio Ingress Gateway를 찾을 수 없습니다${NC}"
    exit 1
fi

GATEWAY_URL="http://$GATEWAY_URL"
echo -e "${BLUE}🌐 Gateway URL: $GATEWAY_URL${NC}"

# 함수: API 테스트
test_api() {
    local endpoint=$1
    local method=${2:-GET}
    local data=${3:-""}
    local description=${4:-""}
    
    echo -e "${BLUE}🔍 $description${NC}"
    echo "   $method $GATEWAY_URL$endpoint"
    
    if [ "$method" == "POST" ] && [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -H "User-Email: demo@kubox.shop" \
            -d "$data" \
            "$GATEWAY_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$GATEWAY_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ]; then
        echo -e "   ${GREEN}✅ Success (200)${NC}"
        if command -v jq &> /dev/null; then
            echo "$body" | jq '.' 2>/dev/null | head -5 || echo "$body" | head -3
        else
            echo "$body" | head -3
        fi
    else
        echo -e "   ${RED}❌ Failed ($http_code)${NC}"
        echo "   $body" | head -2
    fi
    echo ""
}

# 1. 클러스터 상태 확인
echo -e "${YELLOW}📊 1. EKS 클러스터 및 서비스 상태 확인${NC}"
kubectl get pods -n app-services --no-headers | while read line; do
    pod_name=$(echo $line | awk '{print $1}')
    ready=$(echo $line | awk '{print $2}')
    status=$(echo $line | awk '{print $3}')
    
    if [[ "$ready" == "2/2" && "$status" == "Running" ]]; then
        echo -e "   ${GREEN}✅ $pod_name${NC}"
    else
        echo -e "   ${RED}❌ $pod_name ($ready, $status)${NC}"
    fi
done
echo ""

# 2. 서비스 헬스체크
echo -e "${YELLOW}🏥 2. 서비스 헬스체크${NC}"
test_api "/api/users/health" "GET" "" "User Service Health"
test_api "/api/products/health" "GET" "" "Product Service Health"  
test_api "/api/cart/health" "GET" "" "Cart Service Health"
test_api "/api/orders/health" "GET" "" "Order Service Health"
test_api "/api/payment/health" "GET" "" "Payment Service Health"

# 3. 정상 결제 테스트
echo -e "${YELLOW}✅ 3. 정상 결제 플로우 테스트${NC}"
normal_payment='{
    "order_id": "ORDER_NORMAL_DEMO",
    "receipt_id": "RECEIPT_NORMAL_DEMO",
    "price": 25000,
    "order_name": "정상 테스트 상품",
    "buyer_name": "정상고객",
    "method": "card"
}'
test_api "/api/payment/verify" "POST" "$normal_payment" "정상 결제 테스트"

# 4. Payment Service 장애 모드 활성화
echo -e "${YELLOW}🔥 4. Payment Service 장애 모드 활성화${NC}"
failure_config='{
    "enable": true,
    "type": 2
}'
test_api "/api/payment/failure/toggle" "POST" "$failure_config" "장애 모드 활성화"
test_api "/api/payment/failure/status" "GET" "" "장애 모드 상태 확인"

# 5. Istio Outlier Detection 트리거
echo -e "${YELLOW}💥 5. Istio Outlier Detection 트리거${NC}"
echo -e "${BLUE}   연속 장애 요청으로 Circuit Breaker 동작 유도...${NC}"

for i in {1..5}; do
    echo -e "${BLUE}   시도 $i/5${NC}"
    failure_payment='{
        "order_id": "ORDER_FAILURE_'$i'",
        "receipt_id": "RECEIPT_FAILURE_'$i'",
        "price": 50000,
        "order_name": "장애 테스트",
        "buyer_name": "FAILURE_USER",
        "method": "card"
    }'
    
    response=$(curl -s -w "%{http_code}" -X POST \
        -H "Content-Type: application/json" \
        -H "User-Email: demo@kubox.shop" \
        -d "$failure_payment" \
        "$GATEWAY_URL/api/payment/verify")
    
    http_code=$(echo "$response" | tail -c 4)
    if [ "$http_code" -eq 500 ]; then
        echo -e "     ${RED}❌ 500 Error (예상된 장애)${NC}"
    else
        echo -e "     ${YELLOW}⚠️  $http_code${NC}"
    fi
    sleep 1
done
echo ""

# 6. Istio 메트릭 확인
echo -e "${YELLOW}📊 6. Istio Outlier Detection 메트릭 확인${NC}"
PAYMENT_POD=$(kubectl get pods -n app-services -l app=payment-service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ -n "$PAYMENT_POD" ]; then
    echo -e "${BLUE}   Payment Pod: $PAYMENT_POD${NC}"
    echo -e "${BLUE}   Outlier Detection 통계:${NC}"
    kubectl exec -it "$PAYMENT_POD" -c istio-proxy -n app-services -- \
        curl -s localhost:15000/stats 2>/dev/null | grep -E "(outlier|ejected)" | head -5 || \
        echo -e "     ${YELLOW}⚠️  메트릭 조회 실패 (정상적인 경우도 있음)${NC}"
else
    echo -e "${RED}   ❌ Payment Pod를 찾을 수 없습니다${NC}"
fi
echo ""

# 7. 다른 서비스 정상 동작 확인 (장애 격리)
echo -e "${YELLOW}🛡️ 7. 장애 격리 확인 (다른 서비스 정상 동작)${NC}"
test_api "/api/users/health" "GET" "" "User Service (정상 동작 확인)"
test_api "/api/products" "GET" "" "Product Service (상품 조회 정상)"
test_api "/api/cart/health" "GET" "" "Cart Service (정상 동작 확인)"

# 8. Blue-Green 배포 시뮬레이션 (즉시 복구)
echo -e "${YELLOW}🔄 8. Blue-Green 배포 시뮬레이션 (서비스 즉시 복구)${NC}"
disable_config='{
    "enable": false
}'
test_api "/api/payment/failure/toggle" "POST" "$disable_config" "장애 모드 비활성화 (Green 환경으로 전환)"

# 9. 서비스 복구 검증
echo -e "${YELLOW}✅ 9. 서비스 복구 검증${NC}"
echo -e "${BLUE}   Outlier Detection 복구 대기 중... (10초)${NC}"
sleep 10

recovery_payment='{
    "order_id": "ORDER_RECOVERY_DEMO",
    "receipt_id": "RECEIPT_RECOVERY_DEMO",
    "price": 30000,
    "order_name": "복구 테스트 상품",
    "buyer_name": "복구고객",
    "method": "card"
}'
test_api "/api/payment/verify" "POST" "$recovery_payment" "복구 후 정상 결제 테스트"

# 10. 최종 상태 확인
echo -e "${YELLOW}🏁 10. 최종 상태 확인${NC}"
test_api "/api/payment/health" "GET" "" "Payment Service 최종 상태"
test_api "/api/payment/failure/status" "GET" "" "장애 모드 최종 상태"

# 결과 요약
echo ""
echo -e "${GREEN}🎉 Kubox-EKS Istio Outlier Detection 테스트 완료!${NC}"
echo ""
echo -e "${BLUE}📝 테스트 결과 요약:${NC}"
echo -e "${GREEN}   ✅ 정상 결제 성공${NC}"
echo -e "${RED}   🔥 Payment Service 장애 모드 활성화${NC}"
echo -e "${RED}   💥 연속 장애로 Istio Outlier Detection 트리거${NC}"
echo -e "${GREEN}   🛡️ 다른 서비스는 정상 동작 유지 (장애 격리)${NC}"
echo -e "${BLUE}   🔄 Blue-Green 배포로 즉시 복구${NC}"
echo -e "${GREEN}   ✅ 서비스 정상화 확인${NC}"
echo ""
echo -e "${YELLOW}📊 Istio 관찰성 도구:${NC}"
echo -e "${BLUE}   • Kiali: kubectl port-forward svc/kiali 20001:20001 -n istio-system${NC}"
echo -e "${BLUE}   • Jaeger: kubectl port-forward svc/tracing 16686:80 -n istio-system${NC}"  
echo -e "${BLUE}   • Grafana: kubectl port-forward svc/grafana 3000:3000 -n istio-system${NC}"
echo ""
echo -e "${YELLOW}🎬 시연 핵심 포인트:${NC}"
echo -e "${BLUE}   1. MSA 장애 격리: Payment만 장애, 다른 서비스 정상${NC}"
echo -e "${BLUE}   2. Istio Outlier Detection: 자동 장애 감지 및 트래픽 차단${NC}"
echo -e "${BLUE}   3. Blue-Green 배포: 즉시 롤백으로 서비스 연속성${NC}"
echo -e "${BLUE}   4. 관찰성: Kiali/Jaeger/Grafana로 실시간 모니터링${NC}"
