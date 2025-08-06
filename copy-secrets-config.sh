#!/bin/bash

# 모든 서비스에 SecretsManagerConfig 복사 및 패키지명 수정

SERVICES=("product-service" "order-service" "payment-service" "cart-service")

echo "=== SecretsManagerConfig 복사 중 ==="

for SERVICE in "${SERVICES[@]}"; do
    SERVICE_NAME="${SERVICE%-service}"
    SOURCE_FILE="backend/user-service/src/main/java/com/shop/user/config/SecretsManagerConfig.java"
    TARGET_FILE="backend/$SERVICE/src/main/java/com/shop/$SERVICE_NAME/config/SecretsManagerConfig.java"
    
    echo "복사 중: $SERVICE"
    
    # 파일 복사
    cp "$SOURCE_FILE" "$TARGET_FILE"
    
    # 패키지명 수정
    sed -i '' "s/package com.shop.user.config;/package com.shop.$SERVICE_NAME.config;/g" "$TARGET_FILE"
    
    echo "$SERVICE 완료"
done

echo "=== SecretsManagerConfig 복사 완료 ==="
