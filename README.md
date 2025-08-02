# 🛒 MSA 쇼핑몰 프로젝트

Spring Boot + React.js를 활용한 마이크로서비스 아키텍처(MSA) 쇼핑몰입니다.

## 🏗️ 아키텍처

### 마이크로서비스 구성
- **User Service** (8080): 회원가입/로그인, JWT 인증
- **Product Service** (8081): 상품 관리
- **Order Service** (8082): 주문 관리
- **Payment Service** (8083): 결제 처리
- **Cart Service** (8084): 장바구니 관리

### 기술 스택
- **Backend**: Spring Boot 2.7.x, MySQL 8.0, JWT
- **Frontend**: React.js, Styled Components
- **Infrastructure**: Docker, Docker Compose
- **Database**: MySQL 8.0 (각 서비스별 독립 DB)

## 🚀 빠른 시작

### 필수 요구사항
- Docker Desktop
- Node.js 16+
- Git

### 실행 방법
```bash
# 프로젝트 클론
git clone [your-repo-url]
cd shopping-mall-msa

# 환경 설정 파일 복사
cp backend/.env.example backend/.env

# 자동 실행 스크립트
chmod +x quick-start.sh
./quick-start.sh
```

또는 수동 실행:
```bash
# 백엔드 서비스 시작
docker-compose up --build -d

# 프론트엔드 시작 (새 터미널)
cd frontend
npm install
npm start
```

### 접속 URL
- **쇼핑몰**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **MySQL**: localhost:3307

## 📁 프로젝트 구조

```
shopping-mall-msa/
├── backend/
│   ├── user-service/     # 회원 서비스
│   ├── product-service/  # 상품 서비스
│   ├── order-service/    # 주문 서비스
│   ├── payment-service/  # 결제 서비스
│   ├── cart-service/     # 장바구니 서비스
│   └── .env             # 환경 설정 (비공개)
├── frontend/            # React 프론트엔드
├── docs/               # 문서 및 스크립트
├── docker-compose.yml  # Docker 설정
└── quick-start.sh     # 빠른 실행 스크립트
```

## 🔧 개발 가이드

### 환경 설정
1. `backend/.env.example`을 복사하여 `backend/.env` 생성
2. 필요한 값들을 실제 값으로 변경

### API 엔드포인트
- **User**: `/api/users` - 회원가입, 로그인
- **Product**: `/api/products` - 상품 조회, 관리
- **Order**: `/api/orders` - 주문 생성, 조회
- **Payment**: `/api/payments` - 결제 처리
- **Cart**: `/api/cart` - 장바구니 관리

### 개발 서버 실행
```bash
# 각 서비스별 개발 서버
cd backend/[service-name]
./gradlew bootRun

# 프론트엔드 개발 서버
cd frontend
npm start
```

## 🛑 종료 방법

```bash
# Docker 서비스 종료
docker-compose down

# 프론트엔드 종료 (Ctrl + C)
```

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다.

## 📞 문의

프로젝트 관련 문의사항이 있으시면 Issue를 생성해주세요.
