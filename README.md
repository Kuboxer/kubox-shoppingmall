# 🛒 클라우드 기반 무중단 확장형 MSA 쇼핑몰

<div align="center">

![MSA](https://img.shields.io/badge/MSA-Microservices-blue?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-green?style=flat-square)
![React](https://img.shields.io/badge/React-18.2-61DAFB?style=flat-square)
![Kubernetes](https://img.shields.io/badge/Kubernetes-EKS-326CE5?style=flat-square)
![AWS](https://img.shields.io/badge/AWS-Cloud-FF9900?style=flat-square)

**쇼핑몰 서비스를 위한 고가용성 마이크로서비스 아키텍처 환경 제공**

</div>

## 📋 프로젝트 개요

### 프로젝트 기간
**2025.07.25 ~ 2025.10.02** (2개월)

### 주요 목표
- **고가용성**: 무중단 서비스 제공
- **자동 확장성**: 트래픽에 따른 동적 스케일링  
- **무중단 배포**: Blue-Green 배포 전략
- **보안 강화**: 다층 보안 아키텍처
- **비용 최적화**: 클라우드 네이티브 최적화

## 🏗️ 아키텍처

### 마이크로서비스 구성
| 서비스 | 포트 | 기능 | 데이터베이스 |
|--------|------|------|--------------|
| **User Service** | 8080 | 회원가입/로그인, JWT 인증 | user_db |
| **Product Service** | 8081 | 상품 관리 | product_db |
| **Order Service** | 8082 | 주문 관리 | order_db |
| **Payment Service** | 8083 | 결제 처리 | payment_db |
| **Cart Service** | 8084 | 장바구니 관리 | cart_db |

### 기술 스택

#### Infrastructure
- **Container**: Docker, Kubernetes (EKS)
- **Service Mesh**: Istio
- **IaC**: Terraform
- **Cloud**: AWS EKS, S3, API Gateway, CloudFront

#### Backend
- **Framework**: Spring Boot 2.7.x
- **Database**: MySQL 8.0, Redis
- **Security**: JWT, AWS IAM, Secrets Manager

#### Frontend
- **Framework**: React.js 18.2
- **Styling**: Styled Components
- **API Client**: Axios

#### CI/CD & Monitoring
- **CI/CD**: GitHub Actions, ArgoCD, Helm
- **Monitoring**: Prometheus, Grafana, Thanos
- **Logging**: Promtail, Loki

## 🚀 빠른 시작

### 필수 요구사항
- **Docker Desktop** 4.0+
- **Node.js** 16+ LTS
- **kubectl** (Kubernetes CLI)
- **Git**

### 로컬 개발 환경 실행
```bash
# 프로젝트 클론
git clone https://github.com/IluvRiver/shopping-mall-msa
cd shopping-mall-msa

# 환경 설정
cp backend/.env.example backend/.env

# 자동 실행 (권장)
chmod +x quick-start.sh
./quick-start.sh
```

### 수동 실행
```bash
# 1. 백엔드 서비스 시작
docker-compose up --build -d

# 2. 프론트엔드 시작
cd frontend
npm install
npm start
```

### 접속 URL
- **쇼핑몰**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **MySQL**: localhost:3307 (shop_user/shop_password)

## 📁 프로젝트 구조

```
shopping-mall-msa/
├── 📂 aws/                    # AWS 인프라 구성
│   ├── terraform/             # Terraform IaC
│   └── cloudformation/        # CloudFormation 템플릿
├── 📂 backend/                # 마이크로서비스
│   ├── user-service/          # 사용자 서비스 (8080)
│   ├── product-service/       # 상품 서비스 (8081)
│   ├── order-service/         # 주문 서비스 (8082)
│   ├── payment-service/       # 결제 서비스 (8083)
│   ├── cart-service/          # 장바구니 서비스 (8084)
│   └── .env                   # 환경 설정
├── 📂 frontend/               # React 프론트엔드
├── 📂 helm-chart/             # Kubernetes 배포 차트
├── 📂 .github/workflows/      # CI/CD 파이프라인
├── 📄 docker-compose.yml      # 로컬 개발 환경
├── 📄 init-db.sql            # 데이터베이스 초기화
└── 📄 quick-start.sh         # 빠른 실행 스크립트
```

## 🔧 핵심 아키텍처

### AWS 클라우드 아키텍처
- **S3 정적 호스팅 + CloudFront CDN**: 콘텐츠 전송 속도 향상
- **API Gateway**: 마이크로서비스 단일 진입점, 인증 및 트래픽 제어
- **EKS 클러스터**: AWS 관리형 Kubernetes로 운영 부담 최소화
- **RDS MySQL**: 자동 백업, 패치, 모니터링
- **ElastiCache Redis**: 세션 공유 및 캐시

### Kubernetes 아키텍처
- **서비스 분리**: 마이크로서비스별 독립적 운영으로 장애 영향 최소화
- **HPA & Cluster Autoscaler**: CPU/메모리 기반 자동 확장
- **Istio Service Mesh**: 서킷 브레이커, 분산 추적으로 통신 안정성 확보
- **Blue-Green 배포**: 무중단 서비스 업데이트
- **RBAC 권한 분리**: 최소 권한 원칙 적용

### CI/CD 파이프라인
- **GitHub Actions**: 완전 관리형 빌드 및 테스트
- **AWS ECR**: 컨테이너 이미지 저장소
- **ArgoCD + Helm**: GitOps 기반 선언적 배포
- **External Secrets Operator**: AWS Secrets Manager 연동

### 모니터링 스택
- **Prometheus**: 메트릭 수집
- **Thanos**: 분산 모니터링 통합
- **Grafana**: 실시간 대시보드
- **Promtail + Loki**: 로그 중앙화
- **Alertmanager**: 장애 조기 감지 알림

## 🔐 보안 구성

- **JWT 기반 인증**: 무상태 토큰 인증
- **AWS IAM**: 세밀한 권한 관리
- **Secrets Manager**: 민감 정보 중앙 관리
- **SSL/TLS**: 전 구간 암호화 통신
- **VPC**: 네트워크 격리

## 🛠️ 개발 가이드

### API 엔드포인트
```
# 사용자 서비스
POST /api/users/register    # 회원가입
POST /api/users/login       # 로그인

# 상품 서비스
GET  /api/products          # 상품 목록
GET  /api/products/{id}     # 상품 상세

# 주문 서비스
POST /api/orders            # 주문 생성
GET  /api/orders/{userId}   # 주문 조회

# 결제 서비스
POST /api/payments          # 결제 처리

# 장바구니 서비스
GET  /api/cart/{userId}     # 장바구니 조회
POST /api/cart/add          # 장바구니 추가
```

### 로컬 개발 서버 실행
```bash
# 각 마이크로서비스 개발 모드
cd backend/[service-name]
./gradlew bootRun

# 프론트엔드 개발 서버
cd frontend
npm run dev
```

### 데이터베이스 초기화
```bash
# MySQL 컨테이너 접속
docker exec -it shopping-mysql mysql -u shop_user -p

# 각 서비스별 데이터베이스 확인
SHOW DATABASES;
USE user_db;
```

## 🏆 주요 특징

### 고가용성
- **Multi-AZ 배포**: 가용 영역 간 복제
- **Auto Scaling**: 자동 확장/축소
- **Health Check**: 인스턴스 상태 모니터링

### 확장성
- **수평 확장**: 트래픽 증가 시 Pod 자동 증가
- **서비스 분리**: 독립적 확장 가능

### 보안
- **네트워크 분리**: VPC, Security Group
- **암호화**: 전송/저장 데이터 암호화
- **접근 제어**: RBAC 기반 권한 관리

### 모니터링
- **실시간 메트릭**: 시스템 성능 모니터링
- **로그 중앙화**: 분산 환경 로그 통합
- **알림 시스템**: 장애 조기 감지

## 🛑 종료 방법

```bash
# 로컬 환경 종료
docker-compose down

# 쿠버네티스 환경 정리
kubectl delete -f helm-chart/

# 프론트엔드 종료 (Ctrl + C)
```

## 🤝 기여하기

1. Fork the Project
2. Create Feature Branch (`git checkout -b feature/amazing-feature`)
3. Commit Changes (`git commit -m 'Add amazing feature'`)
4. Push to Branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📞 문의

**이충민** - 프로젝트 팀장
- 📧 Email: leecm2468@gmail.com
- 🐙 GitHub: [@IluvRiver](https://github.com/IluvRiver)
- 📝 Blog: [https://lcm9243.tistory.com](https://lcm9243.tistory.com)

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다.

---

<div align="center">

**🚀 MSA 아키텍처로 구현된 확장 가능한 쇼핑몰 서비스**

*Made with ❤️ by Team Leader 이충민*

</div>