# SonarQube GitHub Actions 연동 가이드

## GitHub Secrets 설정

GitHub Repository → Settings → Secrets and variables → Actions에서 다음 Secrets를 추가하세요:

### 필수 Secrets

1. **SONAR_TOKEN**
   - Value: `sqp_7b6942b7495e6202ed3c5c2f0907b5466582a080`
   - Description: SonarQube 인증 토큰

2. **SONAR_HOST_URL**
   - Value: `http://ac6144560e7204124b21fa5016f6a3d0-1879842916.ap-northeast-2.elb.amazonaws.com:9000`
   - Description: SonarQube 서버 URL

### 기존 Secrets (확인 필요)

3. **AWS_ACCESS_KEY_ID**
   - AWS 접근 키 ID

4. **AWS_SECRET_ACCESS_KEY**
   - AWS 비밀 접근 키

5. **AWS_REGION**
   - AWS 리전 (예: ap-northeast-2)

6. **HELM_REPO_TOKEN**
   - Helm Chart 저장소 접근 토큰

## SonarQube 프로젝트 설정

1. SonarQube 웹 인터페이스에 접속:
   http://ac6144560e7204124b21fa5016f6a3d0-1879842916.ap-northeast-2.elb.amazonaws.com:9000

2. 로그인 후 새 프로젝트 생성:
   - Project Key: `kubox-shopping-mall`
   - Project Name: `Kubox Shopping Mall MSA`

3. Quality Gate 설정 (옵션):
   - Administration → Quality Gates
   - 커스텀 Quality Gate 생성 또는 기본값 사용

## 테스트 방법

1. 코드 변경 후 푸시:
   ```bash
   git add .
   git commit -m "Add SonarQube integration"
   git push origin main
   ```

2. GitHub Actions에서 워크플로우 실행 확인

3. SonarQube 대시보드에서 분석 결과 확인

## 주요 기능

- **코드 품질 분석**: 버그, 취약점, 코드 스멜 감지
- **테스트 커버리지**: 테스트 커버리지 측정
- **중복 코드 감지**: 코드 중복 분석
- **품질 게이트**: 품질 기준 미달 시 빌드 실패

## 문제 해결

### SonarQube 스캔 실패 시:
1. SONAR_TOKEN과 SONAR_HOST_URL 확인
2. SonarQube 서버 접근 가능 여부 확인
3. 프로젝트 키 중복 여부 확인

### Maven 빌드 실패 시:
1. pom.xml 파일 존재 여부 확인
2. Java 버전 호환성 확인
3. 의존성 문제 해결
