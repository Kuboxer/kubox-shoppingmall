#!/bin/bash

# AWS Secrets Manager에 비밀 등록

echo "🔐 AWS Secrets Manager에 KUBOX 비밀 등록 중..."

# 1. 백엔드 비밀 등록
aws secretsmanager create-secret \
  --name "kubox/backend/production" \
  --description "KUBOX 백엔드 서비스 비밀 정보" \
  --secret-string file://kubox-backend-secrets.json \
  --region ap-northeast-2

echo "✅ 백엔드 비밀 등록 완료"

# 2. 비밀 확인
aws secretsmanager describe-secret \
  --secret-id "kubox/backend/production" \
  --region ap-northeast-2

echo "🎯 등록된 비밀:"
aws secretsmanager get-secret-value \
  --secret-id "kubox/backend/production" \
  --region ap-northeast-2 \
  --query SecretString \
  --output text | jq .

echo "📋 다음 단계:"
echo "1. kubox-backend-secrets.json 파일의 값들을 실제 운영 값으로 변경"
echo "2. aws secretsmanager update-secret 명령으로 업데이트"
echo "3. EKS 클러스터에 ConfigMap과 Secret 적용"
