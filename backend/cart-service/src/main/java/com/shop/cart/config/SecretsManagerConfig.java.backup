package com.shop.cart.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class SecretsManagerConfig implements EnvironmentPostProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(SecretsManagerConfig.class);
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            log.info("🔐 AWS Secrets Manager에서 설정 로드 중...");
            
            SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                    .region(Region.US_EAST_2)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId("kubox-backend-secrets")
                    .build();
            
            GetSecretValueResponse getSecretValueResponse = secretsClient.getSecretValue(getSecretValueRequest);
            String secretString = getSecretValueResponse.secretString();
            
            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> secrets = mapper.readValue(secretString, Map.class);
            
            environment.getPropertySources().addFirst(new MapPropertySource("secrets-manager", secrets));
            
            log.info("✅ AWS Secrets Manager 설정 로드 완료");
            log.info("로드된 설정 개수: {}", secrets.size());
            
        } catch (Exception e) {
            log.error("❌ AWS Secrets Manager 설정 로드 실패", e);
            log.warn("⚠️ 환경변수를 사용합니다.");
        }
    }
}
