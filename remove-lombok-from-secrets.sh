#!/bin/bash

# 모든 서비스의 SecretsManagerConfig.java를 Lombok 없이 수정

SERVICES=("user-service" "product-service" "payment-service" "cart-service")

echo "=== SecretsManagerConfig Lombok 제거 중 ==="

# 기본 템플릿 생성
cat > /tmp/SecretsManagerConfig.template << 'EOF'
package com.shop.PACKAGE_NAME.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
public class SecretsManagerConfig {
    
    private static final Logger log = LoggerFactory.getLogger(SecretsManagerConfig.class);
    
    private final ConfigurableEnvironment environment;
    
    public SecretsManagerConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void loadSecretsOnStartup() {
        try {
            log.info("🔐 AWS Secrets Manager에서 설정 로드 중...");
            
            // Secrets Manager 클라이언트 생성
            SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                    .region(Region.US_EAST_2)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            
            // Secret 조회
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId("kubox-backend-secrets")
                    .build();
            
            GetSecretValueResponse getSecretValueResponse = secretsClient.getSecretValue(getSecretValueRequest);
            String secretString = getSecretValueResponse.secretString();
            
            // JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> secrets = mapper.readValue(secretString, Map.class);
            
            // 환경변수로 추가
            environment.getPropertySources().addFirst(new MapPropertySource("secrets-manager", secrets));
            
            log.info("✅ AWS Secrets Manager 설정 로드 완료");
            log.info("로드된 설정 개수: {}", secrets.size());
            
        } catch (Exception e) {
            log.error("❌ AWS Secrets Manager 설정 로드 실패", e);
            log.warn("⚠️ 환경변수를 사용합니다.");
        }
    }
}
EOF

for SERVICE in "${SERVICES[@]}"; do
    SERVICE_NAME="${SERVICE%-service}"
    TARGET_FILE="backend/$SERVICE/src/main/java/com/shop/$SERVICE_NAME/config/SecretsManagerConfig.java"
    
    echo "처리 중: $SERVICE"
    
    # 템플릿 복사하고 패키지명 수정
    sed "s/PACKAGE_NAME/$SERVICE_NAME/g" /tmp/SecretsManagerConfig.template > "$TARGET_FILE"
    
    echo "$SERVICE 완료"
done

# user-service도 동일하게 수정
sed "s/PACKAGE_NAME/user/g" /tmp/SecretsManagerConfig.template > "backend/user-service/src/main/java/com/shop/user/config/SecretsManagerConfig.java"

# 임시 파일 삭제
rm /tmp/SecretsManagerConfig.template

echo "=== SecretsManagerConfig Lombok 제거 완료 ==="
