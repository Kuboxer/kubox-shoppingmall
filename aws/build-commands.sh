# GitHub Actions 또는 CodeBuild에서
# 환경별로 다른 이미지 빌드

# 개발환경 빌드
docker build \
  --build-arg REACT_APP_USER_API_URL=https://dev-api.kubox.shop \
  --build-arg REACT_APP_PRODUCT_API_URL=https://dev-api.kubox.shop \
  -t kubox/frontend:dev .

# 운영환경 빌드
docker build \
  --build-arg REACT_APP_USER_API_URL=https://api.kubox.shop \
  --build-arg REACT_APP_PRODUCT_API_URL=https://api.kubox.shop \
  -t kubox/frontend:prod .
