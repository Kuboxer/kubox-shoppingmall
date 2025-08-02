#!/bin/bash

echo "=== 쇼핑몰 MSA Docker 명령어 가이드 ==="
echo ""

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}📦 Docker 빌드 및 실행 명령어들${NC}"
echo ""

echo -e "${GREEN}1. 전체 시스템 빌드 및 실행:${NC}"
echo "docker-compose up --build"
echo ""

echo -e "${GREEN}2. 백그라운드 실행:${NC}"
echo "docker-compose up -d --build"
echo ""

echo -e "${GREEN}3. 특정 서비스만 실행:${NC}"
echo "docker-compose up user-service product-service"
echo ""

echo -e "${GREEN}4. 로그 확인:${NC}"
echo "docker-compose logs -f [서비스명]"
echo "예: docker-compose logs -f user-service"
echo ""

echo -e "${GREEN}5. 서비스 상태 확인:${NC}"
echo "docker-compose ps"
echo ""

echo -e "${GREEN}6. 컨테이너 내부 접속:${NC}"
echo "docker-compose exec [서비스명] bash"
echo "예: docker-compose exec user-service bash"
echo ""

echo -e "${GREEN}7. 전체 중지 및 정리:${NC}"
echo "docker-compose down"
echo ""

echo -e "${GREEN}8. 볼륨까지 삭제:${NC}"
echo "docker-compose down -v"
echo ""

echo -e "${GREEN}9. 이미지 재빌드 (캐시 무시):${NC}"
echo "docker-compose build --no-cache"
echo ""

echo -e "${YELLOW}📋 실행 순서:${NC}"
echo "1. 프로젝트 루트에서 실행"
echo "2. docker-compose up --build"
echo "3. 브라우저에서 http://localhost:3000 접속"
echo ""

echo -e "${YELLOW}🔍 서비스 접속 포트:${NC}"
echo "- Frontend: http://localhost:3000"
echo "- User Service: http://localhost:8080"
echo "- Product Service: http://localhost:8081"  
echo "- Order Service: http://localhost:8082"
echo "- Payment Service: http://localhost:8083"
echo "- Cart Service: http://localhost:8084"
echo "- MySQL: localhost:3306"
echo ""

echo -e "${RED}⚠️  주의사항:${NC}"
echo "- 처음 실행 시 MySQL 초기화로 시간이 걸릴 수 있습니다"
echo "- 각 서비스별 Dockerfile이 각 디렉토리에 있어야 합니다"
echo "- 포트 충돌 시 docker-compose.yml에서 포트 변경 가능"
