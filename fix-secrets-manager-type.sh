#!/bin/bash

# 모든 서비스의 SecretsManagerConfig.java 타입 오류 수정

SERVICES=("product-service" "order-service" "payment-service" "cart-service")

echo "=== SecretsManagerConfig.java 타입 오류 수정 중 ==="

for SERVICE in "${SERVICES[@]}"; do
    SERVICE_NAME="${SERVICE%-service}"
    CONFIG_FILE="backend/$SERVICE/src/main/java/com/shop/$SERVICE_NAME/config/SecretsManagerConfig.java"
    
    echo "처리 중: $SERVICE"
    
    if [ -f "$CONFIG_FILE" ]; then
        # Map<String, String> -> Map<String, Object> 변경
        sed -i '' 's/Map<String, String> secrets = mapper.readValue(secretString, Map.class);/@SuppressWarnings("unchecked")\
            Map<String, Object> secrets = mapper.readValue(secretString, Map.class);/' "$CONFIG_FILE"
        
        echo "$SERVICE 완료"
    else
        echo "$SERVICE: 파일이 존재하지 않음"
    fi
done

echo "=== SecretsManagerConfig.java 수정 완료 ==="
