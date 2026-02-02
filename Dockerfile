# ================================
# SmartChain Backend Dockerfile
# Java 17 + Spring Boot
# ================================

# Stage 1: Build
FROM gradle:8-jdk17-alpine AS builder

WORKDIR /app

# Gradle 캐싱을 위해 의존성 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐싱)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle build -x test --no-daemon

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 설정
EXPOSE 8080

# JVM 옵션 및 실행
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
