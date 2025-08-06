#!/bin/bash

# EC2 IAM 역할 생성 및 정책 연결 스크립트 (수정됨)

echo "🔐 EC2 IAM 역할 생성 중..."

# 1. 신뢰 정책 파일 생성
cat > ec2-trust-policy.json << 'EOF'
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF

# 2. IAM 역할 생성 (영어 설명으로 변경)
echo "Creating IAM role..."
aws iam create-role \
    --role-name KuboxEC2SecretsRole \
    --assume-role-policy-document file://ec2-trust-policy.json \
    --description "Role for Kubox EC2 instances to access Secrets Manager"

# 3. 역할에 기존 정책 연결 (이미 생성됨)
echo "Attaching policy to role..."
aws iam attach-role-policy \
    --role-name KuboxEC2SecretsRole \
    --policy-arn arn:aws:iam::862016452072:policy/KuboxSecretsManagerPolicy

# 4. 역할을 기존 인스턴스 프로파일에 추가
echo "Adding role to instance profile..."
aws iam add-role-to-instance-profile \
    --instance-profile-name KuboxEC2SecretsProfile \
    --role-name KuboxEC2SecretsRole

# 임시 파일 정리
rm ec2-trust-policy.json

echo ""
echo "✅ IAM 역할 생성 완료!"
echo ""
echo "📋 다음 단계:"
echo "1. EC2 콘솔에서 백엔드 서비스용 EC2 인스턴스 선택"
echo "2. 작업 → 보안 → IAM 역할 수정"
echo "3. 'KuboxEC2SecretsProfile' 인스턴스 프로파일 연결"
echo "4. 인스턴스 재부팅"
echo ""
echo "🚀 이제 EC2에서 Docker 컨테이너를 실행하면 Secrets Manager를 자동으로 읽습니다!"
