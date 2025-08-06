#!/bin/bash

echo "🚀 MSA 쇼핑몰 실행 스크립트"
echo "========================="

# 1. Docker 설치 확인
if ! command -v docker &> /dev/null; then
    echo "❌ Docker가 설치되지 않았습니다."
    echo "https://docs.docker.com/get-docker/ 에서 설치하세요."
    exit 1
fi

# 2. Node.js 설치 확인
if ! command -v node &> /dev/null; then
    echo "❌ Node.js가 설치되지 않았습니다."
    echo "https://nodejs.org 에서 설치하세요."
    exit 1
fi

# 3. .env 파일 확인
if [ ! -f "backend/.env" ]; then
    echo "❌ backend/.env 파일이 없습니다."
    exit 1
fi

echo "✅ 모든 필수 소프트웨어가 설치되어 있습니다."

# 4. Docker 서비스 시작
echo "🐳 Docker 서비스 시작 중..."
docker-compose down
docker-compose up --build -d

# 5. 서비스 시작 대기
echo "⏳ 서비스 시작 대기 중... (30초)"
sleep 30

# 6. 프론트엔드 의존성 설치 및 시작
echo "📦 프론트엔드 의존성 설치 중..."
cd frontend
npm install

echo "🌐 프론트엔드 시작 중..."
echo "브라우저에서 http://localhost:3000 을 열어주세요."
npm start
