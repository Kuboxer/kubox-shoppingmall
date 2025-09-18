# 🔥 Payment Service 장애 시뮬레이션 가이드

## 📋 시나리오 A: 장애 격리 성공 테스트

### 1. 사전 준비

#### 필요한 서비스 실행 확인
```bash
# 모든 서비스가 실행 중인지 확인
docker-compose ps

# 또는 개별 포트 확인
curl http://localhost:8081/api/user/health     # User Service
curl http://localhost:8082/api/product/health  # Product Service  
curl http://localhost:8083/api/payment/health  # Payment Service
curl http://localhost:8084/api/cart/health     # Cart Service
curl http://localhost:8085/api/order/health    # Order Service
```

#### 테스트 스크립트 실행 권한 부여
```bash
cd /Users/ichungmin/Desktop/shopping-mall-msa
chmod +x test-payment-failure.sh
```

### 2. 시연 시나리오

#### 🎬 STEP 1: 정상 상태 확인
```bash
# 모든 서비스 정상 동작 확인
curl http://localhost:8083/api/payment/health
curl http://localhost:8083/api/payment/version
```

#### 🎬 STEP 2: 정상 결제 테스트
```bash
# 정상 결제 요청
curl -X POST http://localhost:8083/api/payment/verify \
  -H "Content-Type: application/json" \
  -H "User-Email: demo@example.com" \
  -d '{
    "order_id": "ORDER_DEMO_001",
    "receipt_id": "RECEIPT_DEMO_001", 
    "price": 25000,
    "order_name": "데모 상품",
    "buyer_name": "데모고객",
    "method": "card"
  }'
```

#### 🎬 STEP 3: 장애 모드 활성화
```bash
# Payment Service 장애 모드 ON (에러 타입)
curl -X POST http://localhost:8083/api/payment/failure/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "enable": true,
    "type": 2
  }'

# 장애 모드 상태 확인
curl http://localhost:8083/api/payment/failure/status
```

#### 🎬 STEP 4: 장애 발생 시뮬레이션
```bash
# FAILURE 키워드가 포함된 결제 요청 → 장애 발생
curl -X POST http://localhost:8083/api/payment/verify \
  -H "Content-Type: application/json" \
  -H "User-Email: demo@example.com" \
  -d '{
    "order_id": "ORDER_FAILURE_DEMO",
    "receipt_id": "RECEIPT_FAILURE_001",
    "price": 50000,
    "order_name": "장애 테스트 상품",
    "buyer_name": "FAILURE_USER",
    "method": "card"
  }'
```

**예상 결과**: HTTP 500 에러, Circuit Breaker 트리거

#### 🎬 STEP 5: 다른 서비스는 정상 동작 확인
```bash
# User Service - 정상 동작
curl http://localhost:8081/api/user/health

# Product Service - 정상 동작  
curl http://localhost:8082/api/product/health

# Cart Service - 정상 동작
curl http://localhost:8084/api/cart/health

# Order Service - 정상 동작 (Payment 호출 제외)
curl http://localhost:8085/api/order/health
```

#### 🎬 STEP 6: Payment 서비스 장애 중에도 일부 기능 동작
```bash
# 건강 체크는 여전히 동작
curl http://localhost:8083/api/payment/health

# FAILURE 키워드가 없는 일반 결제는 정상 처리
curl -X POST http://localhost:8083/api/payment/verify \
  -H "Content-Type: application/json" \
  -H "User-Email: demo@example.com" \
  -d '{
    "order_id": "ORDER_NORMAL_DEMO", 
    "receipt_id": "RECEIPT_NORMAL_002",
    "price": 15000,
    "order_name": "일반 상품",
    "buyer_name": "일반고객",
    "method": "card"
  }'
```

#### 🎬 STEP 7: 타임아웃 장애 테스트
```bash
# 타임아웃 모드로 변경
curl -X POST http://localhost:8083/api/payment/failure/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "enable": true,
    "type": 1
  }'

# 타임아웃 테스트 (10초 지연)
curl -X POST http://localhost:8083/api/payment/verify \
  -H "Content-Type: application/json" \
  -H "User-Email: demo@example.com" \
  -d '{
    "order_id": "ORDER_ERROR_TIMEOUT",
    "receipt_id": "RECEIPT_TIMEOUT_001",
    "price": 30000,
    "order_name": "타임아웃 테스트",
    "buyer_name": "ERROR_USER",
    "method": "card"
  }'
```

#### 🎬 STEP 8: 장애 복구 (Blue-Green 배포 시뮬레이션)
```bash
# 장애 모드 비활성화 (서비스 복구)
curl -X POST http://localhost:8083/api/payment/failure/toggle \
  -H "Content-Type: application/json" \
  -d '{
    "enable": false
  }'

# 복구 확인
curl -X POST http://localhost:8083/api/payment/verify \
  -H "Content-Type: application/json" \
  -H "User-Email: demo@example.com" \
  -d '{
    "order_id": "ORDER_RECOVERY_DEMO",
    "receipt_id": "RECEIPT_RECOVERY_001", 
    "price": 20000,
    "order_name": "복구 테스트 상품",
    "buyer_name": "복구고객",
    "method": "card"
  }'
```

### 3. 자동화된 테스트 실행
```bash
# 전체 시나리오 자동 실행
./test-payment-failure.sh
```

### 4. 시연 포인트

#### ✅ 장애 격리 성공 증명
1. **Payment Service만 장애 발생** - 다른 서비스 정상 동작
2. **Circuit Breaker 작동** - 장애 감지 후 빠른 실패
3. **부분 기능 유지** - Health Check, 일반 결제는 정상
4. **빠른 복구** - 장애 모드 해제 즉시 정상화

#### ⚡ Blue-Green 배포 효과
- **즉시 롤백**: 장애 감지 즉시 이전 버전으로 전환 가능
- **무중단 서비스**: 조회/장바구니 기능은 계속 제공
- **격리된 장애**: Payment 장애가 전체 시스템에 영향 없음

### 5. 추가 테스트 옵션

#### Circuit Breaker 패턴 테스트
```bash
# Order Service에서 Payment 호출 시뮬레이션
for i in {1..5}; do
  echo "시도 $i:"
  curl -X POST http://localhost:8085/api/order/create \
    -H "Content-Type: application/json" \
    -H "User-Email: demo@example.com" \
    -d '{
      "items": [{"productId": 1, "quantity": 1}],
      "totalAmount": 50000,
      "paymentMethod": "card",
      "test_failure": true
    }'
  echo -e "\n"
done
```

#### 모니터링 및 알람 테스트
```bash
# 서비스 메트릭 확인
curl http://localhost:8083/actuator/metrics 2>/dev/null | jq '.names[]' | grep -i error

# 로그 패턴 확인
docker logs payment-service 2>&1 | grep -i "error\|failure\|exception"
```

### 6. 문제 해결

#### Payment Service 응답 없음
```bash
# 서비스 재시작
docker-compose restart payment-service

# 포트 확인
netstat -tulpn | grep 8083
```

#### 장애 모드 해제 안됨
```bash
# 강제 해제
curl -X POST http://localhost:8083/api/payment/failure/toggle \
  -H "Content-Type: application/json" \
  -d '{"enable": false}'

# 또는 서비스 재시작
docker-compose restart payment-service
```

---

## 🎯 시연 시 강조 포인트

1. **MSA 장점**: 하나의 서비스 장애가 전체에 영향 없음
2. **Circuit Breaker**: 빠른 장애 감지와 격리
3. **Blue-Green 배포**: 즉시 롤백으로 서비스 연속성
4. **모니터링**: 실시간 장애 감지 및 알림
5. **자동 복구**: 최소한의 수동 개입으로 서비스 정상화
