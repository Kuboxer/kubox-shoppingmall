# Slack 알림 설정 가이드

## 개요
CI/CD 파이프라인에 Slack 알림이 추가되어 다음 상황에서 자동으로 Slack 메시지를 받을 수 있습니다:

### Backend Deploy 알림
1. **SonarQube 코드 분석 완료** 시 알림
2. **ECR 이미지 푸시 완료** 시 알림
3. **전체 워크플로우 완료** 시 알림 (성공/실패 상관없이)

### Frontend Deploy 알림
1. **S3 배포 완료** 시 알림
2. **전체 워크플로우 완료** 시 알림 (성공/실패 상관없이)

## Slack 웹훅 설정 방법

### 1. Slack App 생성 및 웹훅 URL 생성

1. [Slack API](https://api.slack.com/apps)에 접속
2. "Create New App" 클릭
3. "From scratch" 선택
4. App 이름 입력 (예: "Kubox CI/CD Bot")
5. 워크스페이스 선택
6. "Incoming Webhooks" 메뉴로 이동
7. "Activate Incoming Webhooks" 토글을 ON으로 변경
8. "Add New Webhook to Workspace" 클릭
9. 알림을 받을 채널 선택 (예: #dev, #cicd, #alerts)
10. 생성된 Webhook URL 복사 (https://hooks.slack.com/services/... 형태)

### 2. GitHub Secrets 설정

1. GitHub 저장소로 이동
2. Settings > Secrets and variables > Actions
3. "New repository secret" 클릭
4. 다음 시크릿 추가:

```
Name: SLACK_WEBHOOK_URL
Secret: https://hooks.slack.com/services/YOUR/WEBHOOK/URL
```

## 알림 메시지 예시

### SonarQube 분석 완료
```
✅ SonarQube Code Analysis Completed
Repository: Kuboxer/shopping-mall-msa
Branch: main
Commit: abc1234
Analysis Report: [SonarQube Dashboard Link]
```

### ECR 푸시 완료
```
🚀 Docker Images Successfully Pushed to ECR
Repository: Kuboxer/shopping-mall-msa
Branch: main
Built Services: user-service product-service
Updated Images: user-service:user-service-v2 product-service:product-service-v3
ECR Repository: 123456789.dkr.ecr.us-east-2.amazonaws.com/kubox
```

### S3 배포 완료
```
🌐 Frontend Successfully Deployed to S3
Repository: Kuboxer/shopping-mall-msa
Branch: main
Commit: abc1234
S3 Bucket: kubox-frontend-bucket
Deployment Status: ✅ Build and S3 sync completed successfully
```

### 전체 워크플로우 완료
```
GitHub Action Bot
Repository: Kuboxer/shopping-mall-msa
Message: [Commit message]
Commit: abc1234
Author: ichungmin
Action: push
Event Name: push
Ref: refs/heads/main
Workflow: Backend MSA Deploy
Job: build
Duration: 5m 32s
Status: ✅ success / ❌ failure / ⚠️ cancelled
```

## 설정 테스트

1. GitHub Secrets에 `SLACK_WEBHOOK_URL` 추가 후
2. 코드 변경 후 main 브랜치에 push
3. Slack 채널에서 알림 확인

## 커스터마이징

알림 메시지를 수정하려면 워크플로우 파일의 `custom_payload` 섹션을 편집하세요:

### Backend: `.github/workflows/backend-deploy.yml`
- Line 90-113: SonarQube 완료 알림
- Line 196-223: ECR 푸시 완료 알림
- Line 291-299: 최종 워크플로우 결과 알림

### Frontend: `.github/workflows/frontend-deploy.yml`
- Line 51-75: S3 배포 완료 알림
- Line 91-99: 최종 워크플로우 결과 알림

## 문제 해결

### 알림이 오지 않는 경우
1. `SLACK_WEBHOOK_URL` 시크릿 값 확인
2. Slack 웹훅 URL이 활성 상태인지 확인
3. GitHub Actions 로그에서 Slack 스텝 에러 확인
4. 채널 권한 및 봇 권한 확인

### 웹훅 URL 테스트
```bash
curl -X POST -H 'Content-type: application/json' \
--data '{"text":"테스트 메시지입니다!"}' \
YOUR_WEBHOOK_URL
```
