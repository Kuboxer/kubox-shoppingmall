#!/bin/bash

# 모든 서비스에 필요한 의존성 추가

SERVICES=("user-service" "product-service" "order-service" "payment-service" "cart-service")

echo "=== AWS 및 JSON 의존성 추가 ==="

for SERVICE in "${SERVICES[@]}"; do
    echo "처리 중: $SERVICE"
    
    # build.gradle에 의존성 추가 (중복 체크)
    if ! grep -q "software.amazon.awssdk:secretsmanager" backend/$SERVICE/build.gradle; then
        cat >> backend/$SERVICE/build.gradle << 'EOF'

    // AWS Secrets Manager
    implementation platform('software.amazon.awssdk:bom:2.20.26')
    implementation 'software.amazon.awssdk:secretsmanager'
    implementation 'software.amazon.awssdk:sts'
    
    // JSON 처리용 (이미 있을 수 있음)
    implementation 'com.fasterxml.jackson.core:jackson-databind'
EOF
    fi

    echo "$SERVICE 완료"
done

echo "=== 의존성 추가 완료 ==="
