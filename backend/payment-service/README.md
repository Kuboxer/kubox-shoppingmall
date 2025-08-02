# BootPay 결제 서비스

## 필요 환경
- Java 17+
- MySQL 8.0+
- Node.js 18+

## 설치 및 실행

### MySQL 설정
```bash
# MySQL 실행 후
mysql -u root
CREATE DATABASE payment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Backend (Payment Service)
```bash
cd backend/payment-service
chmod +x start_payment.sh
./start_payment.sh
```

### Frontend
```bash
cd frontend
npm install
npm start
```

## MySQL 연결 정보

- Database: `payment_db`
- Username: `root`
- Password: (없음)
- Port: `3306`

## API 엔드포인트

- `POST /api/payment/verify` - 부트페이 결제 검증
- `POST /api/payment/cancel` - 결제 취소
- `GET /api/payment/history` - 결제 내역
- `GET /api/payment/order/{orderId}` - 주문별 결제 정보
- `POST /api/payment/prepare` - 결제 준비
- `GET /api/payment/health` - 서비스 상태 확인

## BootPay 실제 키

- Application ID: `688989d686cd66f61255b60b`
- Private Key: `P4s4fN5fI3l1zRJNqvBdpKuSjW+dYx111VXXqWHayZU=`
- **주의: 실제 결제 키로 실제 금액 차감 가능**

## 사용법

1. `/cart` 페이지에서 "BootPay로 결제하기" 클릭
2. `/payment` 페이지에서 BootPay 위젯으로 결제 진행
3. 결제 완료 시 `/payment/success`로 이동
4. 결제 실패 시 `/payment/fail`로 이동

## 주의사항

- MySQL 서버가 실행 중이어야 함
- 샌드박스 환경에서만 사용
- 실제 금액 차감 없음
- 테스트 카드번호 사용 가능
