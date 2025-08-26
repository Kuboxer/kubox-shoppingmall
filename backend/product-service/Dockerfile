# Spring Boot 서비스용 Dockerfile
FROM openjdk:17-jdk-slim

# 버전 정보 인자
ARG VERSION=unknown
ENV APP_VERSION=$VERSION

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 설정 파일들 복사
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src/ src/

# Gradle 래퍼에 실행 권한 부여
RUN chmod +x ./gradlew

# 애플리케이션 빌드
RUN ./gradlew build -x test

# 빌드된 JAR 파일을 app.jar로 복사
RUN find build/libs -name '*.jar' ! -name '*plain*' -exec cp {} app.jar \;

# 포트 노출
EXPOSE 8081

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
