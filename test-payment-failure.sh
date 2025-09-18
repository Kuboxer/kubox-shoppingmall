#!/bin/bash

# Payment Service 장애 테스트 스크립트
# Blue-Green 배포 및 Circuit Breaker 시연용

echo "🔥 Payment Service 장애 시뮬레이션 테스트 스크립트"
echo "=============================================="

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 설정
PAYMENT_SERVICE_URL="http://localhost:8083/api/payment"

# 함수: API 호출 테스트
test_api() {
    local url=$1
    local method=${2:-GET}
    local data=${3:-""}
    
    echo -e "${BLUE}Testing: $method $url${NC}"
    
    if [ "$method" == "POST" ] && [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST \
            -H "Content-Type: application/json" \
            -H "User-Email: test@example.com" \
            -d "$data" \
            "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url")
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
test_api "$PAYMENT_SERVICE_URL/health"

# 2. 장애 모드 상태 확인
echo -e "${YELLOW}📊 2. 장애 모드 상태 확인${NC}"
test_api "$PAYMENT_SERVICE_URL/failure/status"

# 3. 정상 결제 테스트
echo -e "${YELLOW}✅ 3. 정상 결제 테스트${NC}"
normal_payment='{
    "order_id": "ORDER_NORMAL_001",
    "receipt_id": "RECEIPT_NORMAL_001",
    "price": 10000,
    "order_name": "일반 상품",
    "buyer_name": "정상고객",
    "method": "card"
}'
test_api "$PAYMENT_SERVICE_URL/verify" "POST" "$normal_payment"

# 4. 장애 모드 활성화
echo -e "${YELLOW}🔥 4. 장애 모드 활성화 (타입 2: 에러)${NC}"
failure_config='{
    "enable": true,
    "type": 2
}'
test_api "$PAYMENT_SERVICE_URL/failure/toggle" "POST" "$failure_config"

# 5. 장애 결제 테스트 (FAILURE 키워드 포함)
echo -e "${YELLOW}💥 5. 장애 결제 테스트 (Circuit Breaker 트리거)${NC}"
failure_payment='{
    "order_id": "ORDER_FAILURE_001",
    "receipt_id": "RECEIPT_FAILURE_001",
    "price": 50000,
    "order_name": "장애 테스트 상품",
    "buyer_name": "FAILURE_USER",
    "method": "card"
}'
test_api "$PAYMENT_SERVICE_URL/verify" "POST" "$failure_payment"

# 6. 장애 모드에서 일반 결제 테스트 (키워드 없음)
echo -e "${YELLOW}🔄 6. 장애 모드에서 일반 결제 테스트${NC}"
normal_payment2='{
    "order_id": "ORDER_NORMAL_002",
    "receipt_id": "RECEIPT_NORMAL_002",
    "price": 20000,
    "order_name": "일반 상품 2",
    "buyer_name": "일반고객",
    "method": "card"
}'
test_api "$PAYMENT_SERVICE_URL/verify" "POST" "$normal_payment2"

# 7. 타임아웃 테스트 (장애 타입 1)
echo -e "${YELLOW}⏰ 7. 타임아웃 장애 모드로 변경${NC}"
timeout_config='{
    "enable": true,
    "type": 1
}'
test_api "$PAYMENT_SERVICE_URL/failure/toggle" "POST" "$timeout_config"

echo -e "${YELLOW}⏰ 8. 타임아웃 테스트 (10초 지연 - Ctrl+C로 중단 가능)${NC}"
timeout_payment='{
    "order_id": "ORDER_ERROR_TIMEOUT",
    "receipt_id": "RECEIPT_TIMEOUT_001",
    "price": 30000,
    "order_name": "타임아웃 테스트",
    "buyer_name": "ERROR_USER",
    "method": "card"
}'
echo "⚠️  이 테스트는 10초가 걸립니다. Ctrl+C로 중단하거나 기다려주세요..."
timeout 15 curl -s -X POST \
    -H "Content-Type: application/json" \
    -H "User-Email: test@example.com" \
    -d "$timeout_payment" \
    "$PAYMENT_SERVICE_URL/verify" || echo -e "${RED}❌ 타임아웃 발생 (예상된 동작)${NC}"
echo ""

# 9. 장애 모드 비활성화
echo -e "${YELLOW}✅ 9. 장애 모드 비활성화${NC}"
disable_config='{
    "enable": false
}'
test_api "$PAYMENT_SERVICE_URL/failure/toggle" "POST" "$disable_config"

# 10. 복구 확인
echo -e "${YELLOW}🔄 10. 서비스 복구 확인${NC}"
recovery_payment='{
    "order_id": "ORDER_RECOVERY_001",
    "receipt_id": "RECEIPT_RECOVERY_001",
    "price": 15000,
    "order_name": "복구 테스트 상품",
    "buyer_name": "복구고객",
    "method": "card"
}'
test_api "$PAYMENT_SERVICE_URL/verify" "POST" "$recovery_payment"

# 11. 최종 상태 확인
echo -e "${YELLOW}📊 11. 최종 상태 확인${NC}"
test_api "$PAYMENT_SERVICE_URL/health"
test_api "$PAYMENT_SERVICE_URL/failure/status"

echo ""
echo -e "${GREEN}🎉 장애 테스트 완료!${NC}"
echo ""
echo -e "${BLUE}📝 테스트 시나리오 요약:${NC}"
echo "1. ✅ 정상 결제 성공"
echo "2. 🔥 장애 모드 활성화"
echo "3. 💥 장애 결제 실패 (Circuit Breaker 트리거)"
echo "4. 🔄 장애 모드에서 일반 결제 성공 (키워드 없음)"
echo "5. ⏰ 타임아웃 장애 시뮬레이션"
echo "6. ✅ 장애 모드 비활성화"
echo "7. 🔄 서비스 복구 확인"
echo ""
echo -e "${YELLOW}💡 Circuit Breaker 테스트를 위해서는:${NC}"
echo "- Order Service나 다른 서비스에서 Payment Service 호출"
echo "- 연속된 실패 후 Circuit Open 확인"
echo "- Fallback 메커니즘 동작 확인"
