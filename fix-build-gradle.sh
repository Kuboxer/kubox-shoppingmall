#!/bin/bash

# 모든 서비스의 build.gradle 파일에서 잘못 추가된 AWS 의존성 정리

SERVICES=("product-service" "order-service" "payment-service" "cart-service")

echo "=== build.gradle 파일 정리 중 ==="

for SERVICE in "${SERVICES[@]}"; do
    echo "처리 중: $SERVICE"
    
    BUILD_FILE="backend/$SERVICE/build.gradle"
    
    # dependencies 블록 밖에 있는 AWS 의존성 제거
    sed -i '' '/^    \/\/ AWS Secrets Manager/,$d' "$BUILD_FILE"
    
    # dependencies 블록 안에 AWS 의존성 추가
    if ! grep -q "software.amazon.awssdk:secretsmanager" "$BUILD_FILE"; then
        # testImplementation 라인을 찾아서 그 앞에 AWS 의존성 추가
        sed -i '' '/testImplementation.*spring-boot-starter-test/i\
    \
    // AWS Secrets Manager\
    implementation platform('"'"'software.amazon.awssdk:bom:2.20.26'"'"')\
    implementation '"'"'software.amazon.awssdk:secretsmanager'"'"'\
    implementation '"'"'software.amazon.awssdk:sts'"'"'\
' "$BUILD_FILE"
    fi
    
    echo "$SERVICE 완료"
done

echo "=== build.gradle 정리 완료 ==="
