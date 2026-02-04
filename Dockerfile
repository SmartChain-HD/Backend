# 1단계: 빌드 스테이지
FROM gradle:8-jdk17-alpine AS builder
WORKDIR /app

# 의존성 정의 파일만 먼저 복사
COPY build.gradle settings.gradle ./

# 라이브러리 다운로드만 먼저 실행 (이 단계에서 캐시를 생성하여 네트워크 부하 감소)
# 실패하더라도 재시도할 수 있도록 처리
RUN gradle dependencies --no-daemon || true

# 전체 소스 복사
COPY . .

# 실제 빌드 실행 (네트워크 타임아웃 방지를 위해 --info 추가 가능)
RUN gradle bootJar -x test --no-daemon --stacktrace

# 2단계: 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# 메모리 설정 추가 (컨테이너 환경에서 안정적 구동)
ENTRYPOINT ["java", "-jar", "-Xmx512M", "-Dspring.profiles.active=prod", "app.jar"]