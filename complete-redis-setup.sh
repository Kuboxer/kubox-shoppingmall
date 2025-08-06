#!/bin/bash

# 나머지 서비스들(order, payment, cart)에 Redis 설정 추가

SERVICES=("order-service" "payment-service" "cart-service")

echo "=== 나머지 서비스들에 Redis 설정 추가 ==="

for SERVICE in "${SERVICES[@]}"; do
    echo "처리 중: $SERVICE"
    
    # 1. build.gradle에 Redis 의존성 추가
    if ! grep -q "spring-boot-starter-data-redis" backend/$SERVICE/build.gradle; then
        sed -i '' '/spring-boot-starter-actuator/a\
    implementation '\''org.springframework.boot:spring-boot-starter-data-redis'\''
' backend/$SERVICE/build.gradle
    fi
    
    # 2. application.properties에 Redis 설정 추가 (중복 체크)
    if ! grep -q "spring.data.redis.host" backend/$SERVICE/src/main/resources/application.properties; then
        cat >> backend/$SERVICE/src/main/resources/application.properties << 'EOF'

# Redis 설정
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.password=${REDIS_PASSWORD}
spring.data.redis.timeout=2000ms
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
EOF
    fi
    
    echo "$SERVICE 완료"
done

echo "=== 모든 서비스 Redis 설정 완료 ==="
