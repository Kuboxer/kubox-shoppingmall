# KUBOX AWS Secrets Manager & EKS 배포 가이드

## 🔐 1단계: 비밀 정보 등록

### 1-1. 실제 운영 값으로 변경
```bash
# kubox-backend-secrets.json 파일 수정
vi aws/secrets-manager/kubox-backend-secrets.json

# 다음 값들을 실제 운영 값으로 변경:
# - JWT_SECRET_KEY: 강력한 비밀키로 변경
# - DB_PASSWORD: RDS 비밀번호
# - MYSQL_ROOT_PASSWORD: MySQL 루트 비밀번호  
# - MYSQL_PASSWORD: MySQL 사용자 비밀번호
# - BOOTPAY_APPLICATION_ID: 실제 BootPay ID
# - BOOTPAY_PRIVATE_KEY: 실제 BootPay 개인키
```

### 1-2. AWS Secrets Manager에 등록
```bash
cd aws/secrets-manager
chmod +x create-secrets.sh
./create-secrets.sh
```

## ⚙️ 2단계: ConfigMap 설정

### 2-1. RDS 엔드포인트 변경
```bash
# configmap-backend.yaml에서 DB_HOST 수정
vi aws/k8s/configmap-backend.yaml
# DB_HOST를 실제 RDS 엔드포인트로 변경
```

### 2-2. 도메인 설정 확인
```bash
# CORS_ALLOWED_ORIGINS가 실제 도메인인지 확인
# https://kubox.shop,https://www.kubox.shop
```

## 🚀 3단계: EKS 배포

### 3-1. ConfigMap 적용
```bash
kubectl apply -f aws/k8s/configmap-backend.yaml
kubectl apply -f aws/k8s/configmap-frontend.yaml
```

### 3-2. Secret Provider 적용
```bash
kubectl apply -f aws/k8s/secret-provider-class.yaml
```

### 3-3. 서비스 배포 시 환경변수 사용
```yaml
# Deployment 예시
envFrom:
- configMapRef:
    name: kubox-backend-config
- secretRef:
    name: kubox-backend-secrets
```

## 🔄 4단계: 비밀 업데이트 (필요시)

```bash
# 비밀 값 변경 후
./update-secrets.sh

# Pod 재시작으로 새 비밀 적용
kubectl rollout restart deployment/user-service
kubectl rollout restart deployment/product-service
# ... 기타 서비스들
```

## ✅ 분류 결과

### 🔐 AWS Secrets Manager (민감정보)
- JWT_SECRET_KEY
- DB_PASSWORD  
- MYSQL_ROOT_PASSWORD
- MYSQL_PASSWORD
- BOOTPAY_APPLICATION_ID
- BOOTPAY_PRIVATE_KEY

### ⚙️ ConfigMap (일반설정)
- DB_HOST, DB_PORT, DB_USERNAME
- MYSQL_DATABASE, MYSQL_USER
- BOOTPAY_API_URL
- CORS 설정들
- JWT_EXPIRATION
- LOG_LEVEL
- 프론트엔드 API URLs
