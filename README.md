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
- 권한별 접근 제어 (일반 사용자, 관리자)

## 기술 스택

### 백엔드
- Java 17
- Spring Boot 3.4.4
- Spring Security 6
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

### Docker Compose를 활용한 실행 방법

1. 프로젝트 클론 (IDE Terminal)
   ```bash
   git clone https://github.com/GrayOM/sim_board.git
   cd sim_board
   ```

2. IDE Terminal -> Gradle로 빌드 (Linux/Mac)
   ```bash
   ./gradlew clean build -x test
   ```
   
   Gradle로 빌드 (Windows)
   ```bash
   gradlew.bat clean build
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

### Docker-Compose 수정 후 적용 방법

1. 재 빌드
```bash
   gradlew clean build -x test
```
2. Docker-Compose 종료
```bash
   docker-compose down
```
3. Docker-Compose 시작
```bash
   docker-compose up --build
```
### 볼륨 및 데이터 관리
- MySQL 데이터는 Docker 볼륨을 통해 보존됩니다.
- 업로드된 파일은 `./uploads` 디렉토리에 저장됩니다.

### Upload_files 조회
```bash
  docker ps -> 컨테이너 ID 조회
```
```bash
   docker exec -it [컨테이너 ID] /bin bash
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
``
## 프로젝트 구조

```
board/
├── src/
│   ├── main/
│   │   ├── java/com/sim/board/
│   │   │   ├── config/          # Spring 설정 파일
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
└── README.md                    # 프로젝트 설명
```

## 기본 계정 정보

시스템 시작 시 자동으로 생성되는 기본 계정입니다:

### 관리자 계정
- 관리자 ID: admin
- 비밀번호: admin
- DB 관리자 ID : root
- DB 비밀번호 : root  

이 계정은 모든 게시글과 댓글에 대한 수정/삭제 권한을 가지고 있습니다.

