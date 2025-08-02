#!/bin/bash

echo "=== User Service 시작 (8080) ==="

# Java 확인
if ! command -v java &> /dev/null; then
    echo "❌ Java가 설치되지 않았습니다."
    echo "설치: brew install openjdk@17"
    exit 1
fi

# Gradle 확인
if ! command -v gradle &> /dev/null; then
    echo "❌ Gradle이 설치되지 않았습니다."
    echo "설치: brew install gradle"
    exit 1
fi

echo "🔍 MySQL 연결 테스트 중..."

# MySQL 연결 테스트
if command -v mysql &> /dev/null; then
    mysql -u root -e "SELECT 1;" 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "✅ MySQL 연결 성공 - MySQL 사용"
        echo "🚀 MySQL로 서비스 시작..."
        gradle bootRun
    else
        echo "⚠️  MySQL 연결 실패 - H2 Database로 대체 실행"
        echo "🚀 H2로 서비스 시작..."
        gradle bootRun --args='--spring.profiles.active=h2'
    fi
else
    echo "⚠️  MySQL 클라이언트 없음 - H2 Database로 실행"
    echo "🚀 H2로 서비스 시작..."
    gradle bootRun --args='--spring.profiles.active=h2'
fi
