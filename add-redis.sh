#!/bin/bash

# 모든 백엔드 서비스에 Redis 의존성 추가
SERVICES=("product-service" "order-service" "payment-service" "cart-service")

echo "=== Redis 의존성 추가 중 ==="

for SERVICE in "${SERVICES[@]}"; do
    echo "처리 중: $SERVICE"
    
    # build.gradle에 Redis 의존성 추가
    sed -i '' "s/implementation 'org.springframework.boot:spring-boot-starter-actuator'/implementation 'org.springframework.boot:spring-boot-starter-actuator'\
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'/g" backend/$SERVICE/build.gradle
    
    # application.properties에 Redis 설정 추가
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

    # RedisConfig.java 복사
    cp backend/user-service/src/main/java/com/shop/user/config/RedisConfig.java backend/$SERVICE/src/main/java/com/shop/${SERVICE%-service}/config/RedisConfig.java
    
    # 패키지명 수정
    sed -i '' "s/package com.shop.user.config;/package com.shop.${SERVICE%-service}.config;/g" backend/$SERVICE/src/main/java/com/shop/${SERVICE%-service}/config/RedisConfig.java
    
    echo "$SERVICE 완료"
done

echo "=== Redis 설정 완료 ==="
