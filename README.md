# 프로젝트 1 클라우드 기반 무중단 확장형 MSA 구축

## 프로젝트 기간
**2025.07.25~2025.10.02**

## 프로젝트 개요
쇼핑몰 서비스를 위한 고가용성 마이크로서비스 아키텍처 환경 제공

## 구축 환경
AWS Cloud Platform 및 Multi-Cloud (AWS + GCP)

## 주요 사용 기술
**Container**: Docker, Kubernetes (EKS)  
**Service Mesh**: Istio  
**CI/CD**: GitHub Actions, ArgoCD, Helm  
**IaC**: Terraform  
**Database**: MySQL, Redis  
**Monitoring**: Prometheus, Grafana  
**Cloud**: AWS EKS, S3, API Gateway, CloudFront, GCP Cloud Storage  
**Application**: React, Spring boot  

## 고려사항
고가용성, 자동 확장성, 무중단 배포, 보안 강화, 비용 최적화

---

## AWS Architecture
<img width="2773" height="1860" alt="최종프로젝트-cloud-diagram drawio" src="https://github.com/user-attachments/assets/1ee7c8be-441b-4b53-8e3f-21719723a658" />

### [S3 정적 호스팅 + CloudFront CDN]
콘텐츠 전송 속도 향상

### [API Gateway]
마이크로서비스들의 단일 진입점으로 인증 및 트래픽 제어

### [EKS 클러스터]
AWS 관리형 Kubernetes 서비스 → 컨테이너 오케스트레이션 운영 부담 최소화

### [RDS MySQL]
백업, 패치, 모니터링이 자동화된 관리형 데이터베이스

### [ElastiCache Redis]
세션 정보 공유 및 데이터베이스 부하 감소를 위한 캐시

### [SSL 인증서 및 IAM]
암호화 통신 보장 및 세밀한 권한 관리

---

## Kubernetes Architecture
<img width="2371" height="1820" alt="최종프로젝트-EKS-diagram drawio" src="https://github.com/user-attachments/assets/c07f3167-8f31-4049-bb9b-6a5d0e930fd5" />

### [서비스 분리]
각 마이크로서비스의 독립적 운영으로 장애 영향 최소화

### [HPA 및 Cluster Autoscaler]
CPU/메모리 사용률에 따른 자동 Pod 및 노드 확장

### [Istio Service Mesh]
서킷 브레이커와 분산 추적으로 서비스 간 통신 안정성 확보

### [Istio Gateway]
외부 트래픽 통합 진입점 → Virtual Service, Destination Rule로 트래픽 제어

### [Blue-Green 배포]
신버전을 별도 환경에 배포 후 즉시 전환으로 무중단 서비스 제공

### [목적에 따른 Secret 분리]
민감 정보의 서비스별 분리로 보안 침해 영향 최소화

### [External Secrets Operator 연동]
AWS Secrets Manager와 자동 동기화로 중앙 집중식 보안 관리

### [RBAC 기반 권한 분리]
최소 권한 원칙 적용으로 무단 접근 방지

---

## CI/CD Architecture
![3차프로젝트-cicd아키텍처](https://github.com/user-attachments/assets/671bec3b-95e6-4e77-a8f2-866d4367943b)

### [GitHub Actions]
완전 관리형 빌드로 인프라 운영 비용 제거

### [AWS ECR]
AWS 통합 관리를 통한 운영 효율성 및 보안 강화

### [ArgoCD + Helm Chart]
Git 저장소 기반 선언적 배포로 배포 일관성 및 추적성 확보

### [GitHub Repository]
애플리케이션 코드와 인프라 코드의 통합 관리로 변경 추적 용이

---

## Monitoring Architecture
<img width="1274" height="891" alt="최종프로젝트-monitoring drawio" src="https://github.com/user-attachments/assets/bdc847f8-b8f4-4348-ae3a-ca7026f33459" />

### [Prometheus 메트릭 수집]
노드·앱 메트릭 자동 수집 → 시스템 전반 성능 모니터링

### [Thanos 분산 모니터링]
다중 Prometheus 통합 조회 → 대규모 클러스터 메트릭 중앙 관리

### [Grafana 시각화]
PromQL/LogQL 기반 대시보드 → 실시간 메트릭 및 로그 분석

### [Promtail + Loki]
마이크로서비스 로그 중앙 수집 → 분산 환경 로그 통합 분석

### [Alertmanager 알림]
임계치 초과 시 Email/Slack 알림 → 장애 조기 감지 및 신속 대응

### [S3 장기 저장]
메트릭·로그 압축 보관 → S3 Lifecycle 정책으로 비용 효율적 데이터 관리
