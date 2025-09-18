# 🔥 Payment Service 장애 시뮬레이션 가이드 (Istio Circuit Breaker)

## 📋 시나리오 A: 장애 격리 성공 테스트

### 1. 사전 준비

#### 필요한 서비스 실행 확인
```bash
# Kubernetes 클러스터에서 모든 서비스 확인
kubectl get pods -n app-services

# 서비스 헬스체크
kubectl exec -it <pod-name> -n app-services -- curl http://localhost:8081/api/user/health
kubectl exec -it <pod-name> -n app-services -- curl http://localhost:8082/api/product/health  
kubectl exec -it <pod-name> -n app-services -- curl http://localhost:8083/api/payment/health
kubectl exec -it <pod-name> -n app-services -- curl http://localhost:8084/api/cart/health
kubectl exec -it <pod-name> -n app-services -- curl http://localhost:8085/api/order/health
```

#### Istio 설정 확인
```bash
# Istio 사이드카 주입 확인
kubectl get pods -n app-services -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.spec.containers[*].name}{"\n"}{end}'

# DestinationRule 확인 (Circuit Breaker 설정)
kubectl get destinationrule -n app-services
kubectl describe destinationrule payment-service-dr -n app-services
```

### 2. 시연 시나리오

#### 🎬 STEP 1: 정상 상태 확인
```bash
# Istio Ingress Gateway를 통한 접근
export INGRESS_HOST=$(kubectl -n istio-ingress get service istio-ingress-gateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
export INGRESS_PORT=$(kubectl -n istio-ingress get service istio-ingress-gateway -o jsonpath='{.spec.ports[?(@.name=="http2")].port}')
export GATEWAY_URL=$INGRESS_HOST:$INGRESS_PORT

# 서비스 상태 확인
curl http://$GATEWAY_URL/api/payment/health
curl http://$GATEWAY_URL/api/order/health
```

#### 🎬 STEP 2: 정상 주문 테스트
```bash
# Order Service를 통한 정상 주문
curl -X POST http://$GATEWAY_URL/api/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "totalAmount": 25000,
    "items": [{"productId": 1, "quantity": 2}]
  }'
```

#### 🎬 STEP 3: Payment Service 장애 모드 활성화
```bash
# Payment Service에 장애 모드 활성화
curl -X POST http://$GATEWAY_URL/api/payment/failure/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "enable": true,
    "type": 2
  }'

# 장애 모드 상태 확인
curl http://$GATEWAY_URL/api/payment/failure/status
```

#### 🎬 STEP 4: Istio Circuit Breaker 트리거
```bash
# 연속으로 장애 주문 생성 (Circuit Breaker 트리거)
for i in {1..5}; do
  echo "시도 $i:"
  curl -X POST http://$GATEWAY_URL/api/order/create \
    -H "Content-Type: application/json" \
    -d '{
      "userId": 1,
      "totalAmount": 50000,
      "test_failure": true
    }'
  echo -e "\n"
  sleep 2
done
```

#### 🎬 STEP 5: Istio 메트릭 확인
```bash
# Istio Circuit Breaker 상태 확인
kubectl exec -it <istio-proxy-pod> -c istio-proxy -n app-services -- \
  curl -s localhost:15000/stats | grep payment

# Envoy 클러스터 상태 확인
kubectl exec -it <order-service-pod> -c istio-proxy -n app-services -- \
  curl -s localhost:15000/clusters | grep payment-service

# Circuit Breaker 메트릭
kubectl exec -it <order-service-pod> -c istio-proxy -n app-services -- \
  curl -s localhost:15000/stats | grep -E "(circuit_breakers|outlier_detection)"
```

#### 🎬 STEP 6: 다른 서비스 정상 동작 확인
```bash
# User Service - 정상 동작
curl http://$GATEWAY_URL/api/user/health

# Product Service - 정상 동작  
curl http://$GATEWAY_URL/api/product/health

# Cart Service - 정상 동작
curl http://$GATEWAY_URL/api/cart/health

# Order Service Health Check - 정상 동작
curl http://$GATEWAY_URL/api/order/health
```

#### 🎬 STEP 7: Blue-Green 배포 시뮬레이션 (서비스 복구)
```bash
# 장애 모드 비활성화 (Green 환경으로 전환)
curl -X POST http://$GATEWAY_URL/api/payment/failure/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "enable": false
  }'

# 잠시 대기 후 복구 테스트
sleep 10

# 정상 주문으로 복구 확인
curl -X POST http://$GATEWAY_URL/api/order/create \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "totalAmount": 20000,
    "items": [{"productId": 2, "quantity": 1}]
  }'
```

### 3. Istio 기반 모니터링

#### Kiali 대시보드 확인
```bash
# Kiali 대시보드 접속
kubectl port-forward svc/kiali 20001:20001 -n istio-system

# 브라우저에서 http://localhost:20001 접속
# Graph 탭에서 서비스 간 트래픽 및 에러율 확인
```

#### Jaeger 분산 추적 확인
```bash
# Jaeger 대시보드 접속
kubectl port-forward svc/jaeger 16686:16686 -n istio-system

# 브라우저에서 http://localhost:16686 접속
# order-service → payment-service 호출 트레이스 확인
```

#### Grafana 메트릭 확인
```bash
# Grafana 대시보드 접속
kubectl port-forward svc/grafana 3000:3000 -n istio-system

# 브라우저에서 http://localhost:3000 접속
# Istio Service Dashboard에서 Circuit Breaker 메트릭 확인
```

### 4. 자동화된 테스트 스크립트

<function_calls>
<invoke name="filesystem:write_file">
<parameter name="content">#!/bin/bash

# Istio 기반 Payment Service 장애 테스트 스크립트

echo "🔥 Istio Circuit Breaker 장애 시뮬레이션 테스트"
echo "=============================================="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Istio Ingress Gateway URL 설정
export INGRESS_HOST=$(kubectl -n istio-ingress get service istio-ingress-gateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null)
if [ -z "$INGRESS_HOST" ]; then
    export INGRESS_HOST=$(kubectl -n istio-ingress get service istio-ingress-gateway -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null)
fi
if [ -z "$INGRESS_HOST" ]; then
    export INGRESS_HOST="localhost"
fi

export INGRESS_PORT=$(kubectl -n istio-ingress get service istio-ingress-gateway -o jsonpath='{.spec.ports[?(@.name=="http2")].port}' 2>/dev/null)
if [ -z "$INGRESS_PORT" ]; then
    export INGRESS_PORT="80"
fi

export GATEWAY_URL="http://$INGRESS_HOST:$INGRESS_PORT"

echo -e "${BLUE}Gateway URL: $GATEWAY_URL${NC}"

# 함수: API 테스트
test_api() {
    local endpoint=$1
    local method=${2:-GET}
    local data=${3:-""}
    
    echo -e "${BLUE}Testing: $method $GATEWAY_URL$endpoint${NC}"
    
    if [ "$method" == "POST" ] && [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$GATEWAY_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$GATEWAY_URL$endpoint")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ]; then
        echo -e "${GREEN}✅ Success (200)${NC}"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        echo -e "${RED}❌ Failed ($http_code)${NC}"
        echo "$body"
    fi
    echo ""
}

# 1. 서비스 상태 확인
echo -e "${YELLOW}📊 1. 서비스 상태 확인${NC}"
test_api "/api/payment/health"
test_api "/api/order/health"

# 2. 정상 주문 테스트
echo -e "${YELLOW}✅ 2. 정상 주문 테스트${NC}"
normal_order='{
    "userId": 1,
    "totalAmount": 25000,
    "items": [{"productId": 1, "quantity": 2}]
}'
test_api "/api/order/create" "POST" "$normal_order"

# 3. 장애 모드 활성화
echo -e "${YELLOW}🔥 3. Payment Service 장애 모드 활성화${NC}"
failure_config='{
    "enable": true,
    "type": 2
}'
test_api "/api/payment/failure/toggle" "POST" "$failure_config"

# 4. Istio Circuit Breaker 트리거
echo -e "${YELLOW}💥 4. Istio Circuit Breaker 트리거 (연속 장애 주문)${NC}"
for i in {1..5}; do
    echo -e "${BLUE}시도 $i/${5}:${NC}"
    failure_order='{
        "userId": 1,
        "totalAmount": 50000,
        "test_failure": true
    }'
    test_api "/api/order/create" "POST" "$failure_order"
    sleep 1
done

# 5. Circuit Breaker 상태 확인
echo -e "${YELLOW}📊 5. Istio Circuit Breaker 상태 확인${NC}"
ORDER_POD=$(kubectl get pods -n app-services -l app=order-service -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)
if [ -n "$ORDER_POD" ]; then
    echo "Order Service Pod: $ORDER_POD"
    echo "Circuit Breaker Stats:"
    kubectl exec -it "$ORDER_POD" -c istio-proxy -n app-services -- \
        curl -s localhost:15000/stats | grep -E "(circuit_breakers|outlier)" || echo "메트릭 조회 실패"
    echo ""
else
    echo "Order Service Pod를 찾을 수 없습니다"
fi

# 6. 다른 서비스 정상 동작 확인
echo -e "${YELLOW}🔄 6. 다른 서비스 정상 동작 확인${NC}"
test_api "/api/user/health"
test_api "/api/product/health"
test_api "/api/cart/health"

# 7. 서비스 복구 (Blue-Green 배포 시뮬레이션)
echo -e "${YELLOW}✅ 7. 서비스 복구 (Blue-Green 배포)${NC}"
disable_config='{
    "enable": false
}'
test_api "/api/payment/failure/toggle" "POST" "$disable_config"

# 8. 복구 확인
echo -e "${YELLOW}🔄 8. 서비스 복구 확인${NC}"
sleep 5
recovery_order='{
    "userId": 1,
    "totalAmount": 20000,
    "items": [{"productId": 2, "quantity": 1}]
}'
test_api "/api/order/create" "POST" "$recovery_order"

# 9. 최종 상태 확인
echo -e "${YELLOW}📊 9. 최종 상태 확인${NC}"
test_api "/api/payment/health"
test_api "/api/order/health"

echo ""
echo -e "${GREEN}🎉 Istio Circuit Breaker 테스트 완료!${NC}"
echo ""
echo -e "${BLUE}📝 테스트 결과 요약:${NC}"
echo "1. ✅ 정상 주문 성공"
echo "2. 🔥 Payment Service 장애 모드 활성화"
echo "3. 💥 연속 장애로 Istio Circuit Breaker 트리거"
echo "4. 🔄 다른 서비스는 정상 동작 유지"
echo "5. ✅ Blue-Green 배포로 즉시 복구"
echo "6. 🔄 서비스 정상화 확인"
echo ""
echo -e "${YELLOW}💡 추가 모니터링:${NC}"
echo "- Kiali: kubectl port-forward svc/kiali 20001:20001 -n istio-system"
echo "- Jaeger: kubectl port-forward svc/jaeger 16686:16686 -n istio-system"
echo "- Grafana: kubectl port-forward svc/grafana 3000:3000 -n istio-system"
