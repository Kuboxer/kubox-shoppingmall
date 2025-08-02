# MSA 쇼핑몰 실행 가이드

## 🚀 빠른 시작 (다른 컴퓨터에서)

### 1단계: 필수 소프트웨어 설치
- **Docker Desktop**: https://docs.docker.com/get-docker/
- **Node.js**: https://nodejs.org (LTS 버전)

### 2단계: 프로젝트 복사
```bash
# 이 폴더를 다른 컴퓨터로 복사
shopping-mall-msa/
```

### 3단계: 실행
```bash
# 터미널에서 프로젝트 폴더로 이동
cd shopping-mall-msa

# 실행 권한 부여 (Mac/Linux)
chmod +x quick-start.sh

# 자동 실행 스크립트
./quick-start.sh
```

### 수동 실행 (Windows)
```bash
# 1. 백엔드 서비스 시작
docker-compose up --build -d

# 2. 프론트엔드 시작 (새 터미널)
cd frontend
npm install
npm start
```

## 📋 접속 URL
- **쇼핑몰**: http://localhost:3000
- **MySQL**: localhost:3307 (shop_user/shop_password)

## 🛑 종료 방법
```bash
# Docker 서비스 종료
docker-compose down

# Ctrl + C로 프론트엔드 종료
```

## ⚠️ 주의사항
- Docker Desktop이 실행 중이어야 합니다
- 포트 3000, 8080-8084, 3307이 사용 중이면 안됩니다
- 첫 실행시 `npm install`이 필요합니다
