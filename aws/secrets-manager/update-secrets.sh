#!/bin/bash

# 비밀 업데이트 (값 변경 후 실행)

echo "🔄 AWS Secrets Manager 비밀 업데이트 중..."

aws secretsmanager update-secret \
  --secret-id "kubox/backend/production" \
  --secret-string file://kubox-backend-secrets.json \
  --region ap-northeast-2

echo "✅ 비밀 업데이트 완료"

# 업데이트된 내용 확인
aws secretsmanager get-secret-value \
  --secret-id "kubox/backend/production" \
  --region ap-northeast-2 \
  --query SecretString \
  --output text | jq .
