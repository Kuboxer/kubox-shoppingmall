# Kubox Shopping Mall Helm Chart

이 Helm Chart는 Kubox Shopping Mall의 마이크로서비스들을 Kubernetes에 배포합니다.

## 사용법

### 1. ECR 레지스트리 주소 설정
`values.yaml`에서 `global.imageRegistry`를 실제 ECR 주소로 변경하세요.

### 2. 설치
```bash
helm install kubox-shoppingmall ./helm-chart -n app-services --create-namespace
```

### 3. 업그레이드
```bash
helm upgrade kubox-shoppingmall ./helm-chart -n app-services
```

### 4. 삭제
```bash
helm uninstall kubox-shoppingmall -n app-services
```

## 구성 요소

- **User Service**: 사용자 관리
- **Product Service**: 상품 관리  
- **Cart Service**: 장바구니 관리
- **Order Service**: 주문 관리
- **Payment Service**: 결제 관리

## 설정

주요 설정은 `values.yaml`에서 수정할 수 있습니다:

- `global.imageRegistry`: ECR 레지스트리 주소
- `services.*.image.tag`: 각 서비스의 이미지 태그
- `resources`: CPU/메모리 리소스 제한
- `istio.virtualService.host`: 외부 도메인

## ArgoCD 연동

ArgoCD에서 이 Helm Chart를 모니터링하도록 설정하면 자동 배포가 가능합니다.
