#!/bin/bash

# AWS 설정
AWS_REGION="us-east-2"  # 화면에서 확인된 리전
AWS_ACCOUNT_ID="862016452072"  # 화면에서 확인된 계정 ID
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

# 서비스 목록 (폴더명:태그명)
declare -A SERVICES
SERVICES["user-service"]="user"
SERVICES["product-service"]="prod"
SERVICES["order-service"]="order"
SERVICES["payment-service"]="pay"
SERVICES["cart-service"]="cart"

echo "=== ECR 로그인 ==="
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY

echo "=== Docker 이미지 빌드 및 푸시 ==="
for SERVICE_DIR in "${!SERVICES[@]}"; do
    SERVICE_TAG="${SERVICES[$SERVICE_DIR]}"
    
    echo "빌드 중: $SERVICE_DIR -> 태그: ${SERVICE_TAG}-v1"
    
    # 이미지 빌드
    docker build -t kubox:${SERVICE_TAG}-v1 ./backend/$SERVICE_DIR/
    
    # ECR 태그 설정 (kubox 리포지토리에 태그별로 저장)
    docker tag kubox:${SERVICE_TAG}-v1 $ECR_REGISTRY/kubox:${SERVICE_TAG}-v1
    
    # ECR에 푸시
    docker push $ECR_REGISTRY/kubox:${SERVICE_TAG}-v1
    
    echo "$SERVICE_DIR (${SERVICE_TAG}-v1) 완료!"
    echo "------------------------"
done

echo "=== 모든 이미지 빌드 완료 ==="
echo "kubox 리포지토리에 생성된 이미지 태그:"
echo "- user-v1"
echo "- prod-v1" 
echo "- order-v1"
echo "- pay-v1"
echo "- cart-v1"
