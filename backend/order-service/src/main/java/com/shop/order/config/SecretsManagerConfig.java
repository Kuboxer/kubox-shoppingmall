package com.shop.order.config;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecretsManagerConfig {
    
    private final ConfigurableEnvironment environment;
    
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
