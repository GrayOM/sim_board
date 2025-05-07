# Spring Boot 게시판 프로젝트

Spring Boot 3.4.4와 MySQL 8.0을 활용한 게시판 웹 애플리케이션입니다.

## 목차
- [기능 소개](#기능-소개)
- [기술 스택](#기술-스택)
- [실행 방법](#실행-방법)
- [프로젝트 구조](#프로젝트-구조)
- [기본 계정 정보](#기본-계정-정보)
## 기능 소개

### 게시판 기능
- 게시글 CRUD (작성, 조회, 수정, 삭제)
- 페이지네이션 지원
- 게시글에 여러 파일 첨부 기능
- 댓글 시스템 (작성, 조회, 수정, 삭제)
- 관리자 기능 (모든 게시글 및 댓글 관리)

### 계정 기능
- 회원가입 및 로그인
- 소셜 로그인 지원 (Google, Kakao, Naver)
- 권한별 접근 제어 (일반 사용자, 관리자)
- 소셜 계정 이메일 주소를 사용자명으로 연동

## 기술 스택

### 백엔드
- Java 17
- Spring Boot 3.4.4
- Spring Security 6
- Spring Security OAuth2 Client
- Spring Data JPA
- Hibernate
- MySQL 8.0

### 프론트엔드
- Thymeleaf
- Bootstrap 5
- HTML5/CSS3
- JavaScript

### 개발 도구 및 환경
- Gradle 8.13
- Docker & Docker Compose
- Git

## 실행 방법

### 사전 요구사항
- [Docker Desktop](https://www.docker.com/products/docker-desktop) 설치
- [Git](https://git-scm.com/downloads) 설치

### OAuth2 설정

이 프로젝트는 Google, Kakao, Naver를 통한 소셜 로그인을 지원합니다.

### 소셜 로그인 설정 방법

1. 각 서비스의 개발자 콘솔에서 OAuth 애플리케이션 등록
   - [Google Cloud Console](https://console.cloud.google.com/)
   - [Kakao 개발자 센터](https://developers.kakao.com/)
   - [Naver 개발자 센터](https://developers.naver.com/main/)


2. 리디렉션 URI 설정 (application.yml)
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            redirect-uri: http://localhost:8080/login/oauth2/code/google
          kakao:
            redirect-uri: http://localhost:8080/login/oauth2/code/kakao
          naver:
            redirect-uri: http://localhost:8080/login/oauth2/code/naver
```

   
3. 발급받은 클라이언트 ID와 시크릿을 `.env` 파일에 설정
```bash
# Google OAuth2
GOOGLE_CLIENT_ID=[발급 받은 클라이언트 ID 입력]
GOOGLE_CLIENT_SECRET=[발급 받은 클라이언트 시크릿 입력]
# Kakao OAuth2
KAKAO_CLIENT_ID=[발급 받은 REST API 키 입력]
KAKAO_CLIENT_SECRET=[발급 받은 Admin 키 입력]
# Naver OAuth2
NAVER_CLIENT_ID=[발급 받은 클라이언트 ID 입력]
NAVER_CLIENT_SECRET=[발급 받은 클라이언트 시크릿 입력]
```

## Docker Compose를 활용한 실행 방법

1. 프로젝트 클론 (IDE Terminal)
   ```bash
   git clone https://github.com/GrayOM/sim_board.git
   cd sim_board
   ```
2. IDE Terminal -> Gradle로 빌드
   ```bash
   ./gradlew clean build -x test
   ```
   
3. Docker Compose로 실행
   ```bash
   docker-compose up --build
   ```

4. 브라우저에서 접속
   ```bash
   http://localhost:8080
   ```

5. 종료하기
   ```bash
   docker-compose down
   ```

## Docker-Compose 수정 후 적용 방법

1. 재 빌드
```bash
   ./gradlew clean build -x test
```
2. Docker-Compose 종료
```bash
   docker-compose down
```
3. Docker-Compose 시작
```bash
   docker-compose up --build
```
## 볼륨 및 데이터 관리
- MySQL 데이터는 Docker 볼륨을 통해 보존됩니다.
- 업로드된 파일은 `./uploads` 디렉토리에 저장됩니다.

### Upload_files 조회
```bash
  docker ps -> 컨테이너 ID 조회
```
```bash
   docker exec -it [컨테이너 ID] /bin/bash
```
```bash
   cd upload
   ls -l
```
### DB 조회
```bash
   docker exec -it [DB name] mysql -u root -p
   password : root
```
```sql
   USE board_db;
   SHOW tables;
   SELECT * FROM users;
```
## API 문서 (Swagger-UI)
Swagger UI에서 API를 테스트할 때 인증이 필요한 엔드포인트의 경우 다음 단계를 따라 인증을 설정할 수 있습니다.
```bash
   http://localhost:8080/swagger-ui.html
```
### 제공 되는 API 그룹
- 게시글 API : 게시글 CRUD 기능
- 댓글 API : 댓글 CRUD 기능
- Schemas : board,comment,fileupload,user,page ....
### OAuth2 Authorize
```
bearerAuth (http,Bearer)
value : 현재 JSESSIONID

OAuth2 인증 => [google,kakao,naver] (authorizationCode)
client_id : [발급 받은 클라이언트 ID 입력]
client_secret : [발급 받은 클라이언트 시크릿 입력]
```
## 프로젝트 구조

```
board/
├── src/
│   ├── main/
│   │   ├── java/com/sim/board/
│   │   │   ├── config/          # Spring 설정 파일
│   │   │   │   └── oauth/       # OAuth2 설정 및 핸들러
│   │   │   ├── controller/      # 컨트롤러
│   │   │   ├── domain/          # 엔티티 클래스
│   │   │   ├── repository/      # JPA 리포지토리
│   │   │   ├── service/         # 비즈니스 로직
│   │   │   └── BoardApplication.java
│   │   └── resources/
│   │       ├── static/          # 정적 리소스 (CSS, JS)
│   │       ├── templates/       # Thymeleaf 템플릿
│   │       └── application.yml  # 애플리케이션 설정
│   └── test/                    # 테스트 코드
├── uploads/                     # 업로드 파일 저장 디렉토리
├── build.gradle                 # Gradle 빌드 스크립트
├── Dockerfile                   # Docker 이미지 빌드 설정
├── docker-compose.yml           # Docker Compose 설정
├── .env                         # 환경 변수 설정 파일 (OAuth2 클라이언트 정보)
└── README.md                    # 프로젝트 설명
```

## 기본 계정 정보

시스템 시작 시 자동으로 생성되는 기본 계정입니다:

###  계정 정보 (datalnitializer.java)
- 게시판 관리자 ID: admin
- 비밀번호: admin
- DB 관리자 ID : root
- DB 비밀번호 : root  

이 계정은 모든 게시글과 댓글에 대한 수정/삭제 권한을 가지고 있습니다.