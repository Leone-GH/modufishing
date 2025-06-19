# 베이스 이미지: Java 17 (Temurin)
FROM eclipse-temurin:17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# 호스트의 전체 프로젝트 복사
COPY . .

# Gradle 실행 권한 부여 및 빌드 실행
RUN chmod +x ./gradlew
RUN ./gradlew clean build --no-daemon

# Render에서는 포트를 환경변수로 받기 때문에 해당 포트로 바인딩해야 함
ENV PORT=8080
EXPOSE 8080

# JAR 파일 실행
CMD ["java", "-jar", "build/libs/fishtripdeploy.jar"]
