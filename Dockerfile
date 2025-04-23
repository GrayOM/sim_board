# OpenJDK 17 기반 이미지
FROM openjdk:17-jdk-slim
# 작업 디렉토리 설정
WORKDIR /app
# 빌드된 JAR 파일 복사 , 경로 확인
COPY build/libs/board-0.0.1-SNAPSHOT.jar app.jar
# 복사한 app.jar 를 실행시킴
ENTRYPOINT ["java", "-jar", "app.jar"]