#!/bin/bash

# 모든 백엔드 서비스에 AWS Secrets Manager 의존성 추가

SERVICES=("user-service" "product-service" "order-service" "payment-service" "cart-service")

echo "=== AWS Secrets Manager 의존성 추가 ==="

for SERVICE in "${SERVICES[@]}"; do
    echo "처리 중: $SERVICE"
    
    # build.gradle에 AWS SDK 의존성 추가
    cat >> backend/$SERVICE/build.gradle << 'EOF'

// AWS Secrets Manager
implementation platform('software.amazon.awssdk:bom:2.20.26')
implementation 'software.amazon.awssdk:secretsmanager'
implementation 'software.amazon.awssdk:sts'
EOF

    echo "$SERVICE 완료"
done

echo "=== AWS 의존성 추가 완료 ==="
