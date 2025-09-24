# Manual Approval 설정 가이드

## GitHub Environment 설정

수동 승인 기능을 사용하려면 GitHub Environment를 설정해야 합니다.

### 1. Environment 생성

1. GitHub 저장소로 이동
2. **Settings** 탭 클릭
3. 왼쪽 사이드바에서 **Environments** 클릭
4. **New environment** 클릭
5. Environment name: `production` 입력
6. **Configure environment** 클릭

### 2. Approval 규칙 설정

Environment 설정 페이지에서:

1. **Required reviewers** 체크박스 선택
2. **Add up to 6 reviewers** 아래에서 승인자 추가:
   - 본인 계정 추가
   - 팀원들 계정 추가 (예: 최윤하, 한승규, 백지영)
3. **Save protection rules** 클릭

### 3. 추가 설정 (선택사항)

- **Wait timer**: 승인 전 대기 시간 설정 (예: 5분)
- **Prevent self-review**: 본인이 푸시한 경우 자신이 승인할 수 없게 설정
- **Environment secrets**: Production 환경 전용 시크릿 설정 가능

## 워크플로우 동작 방식

### 1단계: SonarQube 분석
```
✅ SonarQube Code Analysis Completed - Awaiting Manual Approval
📋 Review the analysis results and approve deployment in GitHub Actions
🔗 GitHub Actions: https://github.com/repo/actions/runs/123
```

### 2단계: 수동 승인 대기
```
⏳ Manual Approval Required for Deployment
🔍 SonarQube analysis completed. Waiting for deployment approval.
👤 Please review and approve the deployment in GitHub Actions
```

### 3단계: 승인 후 배포
```
🚀 Deployment Approved - Starting Build and Deploy
✅ Manual approval received. Starting build and deployment process.
```

## 승인 프로세스

### 승인자가 해야 할 일:

1. **Slack 알림 확인**: SonarQube 분석 완료 알림 수신
2. **SonarQube 리포트 검토**: 제공된 링크에서 코드 품질 확인
3. **GitHub Actions 페이지 이동**: 알림의 GitHub Actions 링크 클릭
4. **Review deployments** 버튼 클릭
5. 검토 후 **Approve and deploy** 또는 **Reject** 선택
6. 코멘트 작성 (선택사항)
7. **Approve and deploy** 클릭

### 승인 기준 예시:

- ✅ **승인**: SonarQube Quality Gate 통과, 중요한 보안 이슈 없음
- ❌ **거부**: Critical/Major 버그 다수, 보안 취약점 발견, 코드 커버리지 기준 미달

## 알림 메시지 타임라인

```
🔄 워크플로우 시작
↓
✅ SonarQube 분석 완료 → Slack 알림
↓
⏳ 수동 승인 대기 → Slack 알림
↓
👤 승인자가 GitHub에서 승인/거부
↓
🚀 승인 시: 빌드 시작 → Slack 알림
↓
📦 ECR 푸시 완료 → Slack 알림
↓
🎉 전체 완료 → Slack 알림
```

## 긴급 배포 시

긴급한 경우 승인 없이 배포하려면:

1. **Workflow dispatch**로 수동 실행
2. 또는 별도의 `hotfix` 브랜치 사용하여 승인 단계 스킵
3. 또는 Environment 설정에서 일시적으로 Required reviewers 해제

## 테스트 방법

1. Backend 파일 수정 후 push
2. GitHub Actions에서 워크플로우 실행 확인
3. SonarQube 단계 완료 후 승인 대기 상태 확인
4. 승인 버튼 클릭하여 배포 진행
5. 각 단계별 Slack 알림 확인

이제 코드 품질을 검토한 후 안전하게 배포할 수 있습니다! 🚀
