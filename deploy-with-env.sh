#!/bin/bash

# EC2에서 Docker 컨테이너 실행 시 환경변수로 직접 전달하는 방법
# 이 방법이 더 간단하고 직관적입니다.

echo "=== 환경변수로 서비스 실행하는 방법 ==="

# 1. Secrets Manager에서 값들을 가져와서 환경변수 파일 생성
cat > /opt/kubox/.env << 'EOF'
JWT_SECRET_KEY=mySecretKeyForJWTTokenGeneration2024
JWT_EXPIRATION=86400000
DB_HOST=10.0.1.100
DB_PORT=3306
DB_USERNAME=shop_user
DB_PASSWORD=shop_password
REDIS_HOST=10.0.1.101
REDIS_PORT=6379
REDIS_PASSWORD=
BOOTPAY_APPLICATION_ID=688989d686cd66f61255b60b
BOOTPAY_PRIVATE_KEY=P4s4fN5fI3l1zRJNqvBdpKuSjW+dYx111VXXqWHayZU=
BOOTPAY_API_URL=https://api.bootpay.co.kr
CORS_ALLOWED_ORIGINS=https://kubox.shop,https://www.kubox.shop
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE
CORS_ALLOWED_HEADERS=*
LOG_LEVEL=INFO
EOF

# 2. Docker 컨테이너 실행
docker run -d \
  --name user-service \
  --env-file /opt/kubox/.env \
  -p 8080:8080 \
  862016452072.dkr.ecr.us-east-2.amazonaws.com/kubox:user-v1

echo "=== 완료! 이 방법이 더 간단합니다 ==="
