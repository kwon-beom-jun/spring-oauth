# OAuth2 & JWT 기반 Spring Boot 프로젝트

이 프로젝트는 **Spring Boot 3.2.9**, **Java 17**, **Spring Security** 및 **OAuth2 Client**를 활용하여 일반 로그인(JWT)과 소셜 로그인(구글, 카카오)을 통합하여 인증하는 시스템입니다.

## 📌 주요 기능
- 일반 로그인 (JWT 기반 인증)
- Google OAuth2 로그인
- Kakao OAuth2 로그인
- JWT 토큰 발급 및 검증
- Spring Security 기반 보안 설정
- PostgreSQL을 사용한 사용자 관리

---

## 🔧 기술 스택
### 🛠️ 개발 환경
- **Java**: 17
- **Spring Boot**: 3.2.9
- **Gradle**: Groovy
- **PostgreSQL**: 데이터베이스
- **Thymeleaf**: 프론트엔드 (테스트 페이지용)
- **Lombok**: 코드 간소화

### 📦 주요 라이브러리
- **Spring Boot Starter Web**: REST API 개발
- **Spring Boot Starter Security**: 인증 및 보안 관리
- **Spring Boot Starter OAuth2 Client**: 구글 및 카카오 소셜 로그인
- **Spring Boot Starter Data JPA**: JPA (Hibernate) 기반 DB 관리
- **PostgreSQL Driver**: PostgreSQL 연동
- **JWT (io.jsonwebtoken:jjwt)**: JWT 토큰 생성 및 검증
- **Lombok**: Getter, Setter, 생성자 자동 생성

---

## 🏗️ 프로젝트 설정
### 📜 Gradle 설정 (`build.gradle`)
```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.9'
    id 'io.spring.dependency-management' version '1.1.6'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Web
    implementation 'org.springframework.boot:spring-boot-starter-web'

    // Spring Security
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // Spring Security OAuth2 Client (구글 로그인, 카카오 로그인)
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'

    // JPA와 DB 연결을 위한 의존성
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.postgresql:postgresql'

    // JWT 사용을 위한 라이브러리
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    
    // Lombok 추가
    compileOnly 'org.projectlombok:lombok:1.18.36'
    annotationProcessor 'org.projectlombok:lombok:1.18.36'
    testCompileOnly 'org.projectlombok:lombok:1.18.36'
    testAnnotationProcessor 'org.projectlombok:lombok:1.18.36'
    
    // Thymeleaf (일반 로그인 페이지 테스트 용도)
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
}

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'  // UTF-8 인코딩 설정
    options.compilerArgs << "-parameters"  // 파라미터 유지 설정 추가
}
```

---

## 🔑 인증 & 보안 구조

### 1️⃣ **JWT 기반 일반 로그인**
- `/auth/login` API를 통해 사용자가 `username/password`로 로그인
- 로그인 성공 시 **JWT 토큰**을 발급 (Spring Security + JJWT 사용)
- 토큰을 사용해 인증이 필요한 API 요청 가능
- JWT는 **헤더의 Authorization: Bearer <TOKEN>** 으로 전달

### 2️⃣ **OAuth2 기반 소셜 로그인 (구글 & 카카오)**
- `/oauth2/authorization/google` → 구글 로그인 페이지로 이동 후 인증
- `/oauth2/authorization/kakao` → 카카오 로그인 페이지로 이동 후 인증
- 인증이 완료되면 **OAuth2 로그인 성공 페이지**(`/auth/oauth2/success`)로 리다이렉트
- 해당 OAuth2 정보를 기반으로 사용자 정보를 DB에 저장 후 JWT 발급

### 3️⃣ **Spring Security 설정 (`SecurityConfig.java`)**
- `@EnableWebSecurity` 를 사용하여 Spring Security 설정 적용
- `/auth/**` 및 `/oauth2/**` 경로는 인증 없이 접근 가능
- JWT를 이용한 API 보호 및 OAuth2 로그인 성공 후 처리
- 로그아웃 시 `/auth/logout` 호출하면 세션 만료

---

## 📂 주요 API 엔드포인트
| HTTP Method | URL | 설명 |
|------------|-------------------------------|---------------------------|
| **POST** | `/auth/login` | 일반 로그인 (JWT 발급) |
| **GET** | `/auth/token` | 로그인된 사용자의 JWT 조회 |
| **GET** | `/auth/logout` | 로그아웃 (세션 종료) |
| **GET** | `/oauth2/authorization/google` | 구글 로그인 시작 |
| **GET** | `/oauth2/authorization/kakao` | 카카오 로그인 시작 |
| **GET** | `/auth/oauth2/success` | OAuth2 로그인 성공 처리 |

---

## 🔌 환경 설정 (`application.properties`)
```properties
# 서버 기본 설정
server.port=8080
spring.application.name=oauth

# PostgreSQL 설정
spring.datasource.url=jdbc:postgresql://localhost:5432/{DB Name}
spring.datasource.username={username}
spring.datasource.password={password}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA 설정
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT 설정
jwt.secret=THIS_IS_SECRET_KEY_FOR_JWT_1234567890
jwt.expiration=3600000  # 1시간 (밀리초 기준)

# Google OAuth2 설정
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_SECRET
spring.security.oauth2.client.registration.google.scope=email, profile
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google

# Kakao OAuth2 설정
spring.security.oauth2.client.registration.kakao.client-id=YOUR_KAKAO_CLIENT_ID
spring.security.oauth2.client.registration.kakao.client-secret=YOUR_KAKAO_SECRET
spring.security.oauth2.client.registration.kakao.scope=profile_nickname, profile_image, account_email
spring.security.oauth2.client.registration.kakao.redirect-uri={baseUrl}/login/oauth2/code/kakao
```

---

## 🚀 실행 방법
1. **PostgreSQL 실행 및 `centgate` 데이터베이스 생성**
2. `application.properties` 파일에 **Google/Kakao OAuth2 Client 정보 입력**
3. **브라우저에서 로그인 페이지 테스트:**
   - [http://localhost:8080/auth/login-page](http://localhost:8080/auth/login-page)

---

