#!/bin/bash

# 모든 서비스에 Lombok 의존성 추가

SERVICES=("user-service" "product-service" "order-service" "payment-service" "cart-service")

echo "=== Lombok 의존성 추가 중 ==="

for SERVICE in "${SERVICES[@]}"; do
    echo "처리 중: $SERVICE"
    
    BUILD_FILE="backend/$SERVICE/build.gradle"
    
    # Lombok 의존성이 없다면 추가
    if ! grep -q "lombok" "$BUILD_FILE"; then
        # testImplementation 라인 앞에 Lombok 추가
        sed -i '' '/testImplementation.*spring-boot-starter-test/i\
    \
    // Lombok\
    compileOnly '"'"'org.projectlombok:lombok'"'"'\
    annotationProcessor '"'"'org.projectlombok:lombok'"'"'\
' "$BUILD_FILE"
    fi
    
    echo "$SERVICE 완료"
done

echo "=== Lombok 의존성 추가 완료 ==="
