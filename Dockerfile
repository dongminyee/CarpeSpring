# 1단계: 빌드 환경 설정
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# 소스 코드 복사 및 권한 부여
COPY . .
RUN chmod +x ./gradlew

# 테스트를 제외하고 프로젝트 빌드 (.jar 파일 생성)
RUN ./gradlew clean build -x test

# 2단계: 실행 환경 설정 (가벼운 JRE만 사용하여 용량 최적화)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 빌드된 .jar 파일을 실행 환경으로 복사
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

# Spring Boot 기본 포트 노출
EXPOSE 8080

# 서버 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]