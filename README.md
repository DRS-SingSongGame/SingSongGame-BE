# SingSong Game Backend

<div align="center">

**실시간 멀티플레이 노래 맞추기 게임의 백엔드 서버**<br><br>
_Spring Boot 기반의 실시간 게임 서버_<br>
</div>

<br/>

## 🔗 Links
- 🌐 **웹사이트**: [https://singsonggame.store](https://singsonggame.store)
- 📱 **프론트엔드**: [SingSongGame-FE](https://github.com/DRS-SingSongGame/SingSongGame-FE)

[//]: # (- 📼 **발표영상** : [바로가기]&#40;발표영상링크&#41;)

<br/>

## ✨ 주요 기능

### 🎮 실시간 멀티플레이어 게임
다양한 게임 모드를 지원하는 실시간 멀티플레이어 시스템

### 🏆 실시간 점수 시스템 & 랭킹
- Glicko 레이팅 시스템 기반의 MMR 계산
- 티어 시스템 (새내기 ~ 전설)
- 빠른 정답으로 높은 점수 획득

### 💬 실시간 채팅 시스템
- **로비 채팅**: 전체 사용자 대상 실시간 채팅
- **방 채팅**: 게임 방 내 플레이어 간 채팅
- **게임 중 채팅**: 게임 진행 중 실시간 소통

### 🎲 빠른 매칭 시스템
MMR 기반 자동 매칭으로 비슷한 실력의 플레이어들과 게임

### 🔐 OAuth2 인증
카카오 소셜 로그인을 통한 간편한 사용자 인증


## 🎯 게임 모드


### **키싱유**: 키워드에 맞는 노래를 10초간 불러서 점수를 획득하는 게임

### **랜덤 노래 맞추기 게임**: 키워드에 맞는 랜덤의 노래를 재생하고 빠르게 맞추는 게임

### **AI TTS 게임**: TTS가 읽어주는 가사로 노래 제목 맞추기


<br/>

## 🛠 기술 스택


### Back-end
![Spring Boot](https://img.shields.io/badge/SpringBoot-green?style=for-the-badge&logo=SpringBoot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-green?style=for-the-badge&logo=SpringSecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-green?style=for-the-badge&logo=SpringSecurity&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=FastAPI&logoColor=white)

### Database
![MySQL](https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-FF4438?style=for-the-badge&logo=Redis&logoColor=white)


### External Services
![Google](https://img.shields.io/badge/Google_Cloud_tts-white?style=for-the-badge&logo=Google&logoColor=black)
![Kakao OAuth2](https://img.shields.io/badge/Kakao_OAuth2-FEE500?style=for-the-badge&logo=kakao&logoColor=black)
![ACR CLOUD](https://img.shields.io/badge/ACR_CLOUD-skyblue?style=for-the-badge&logo=ACR_CLOUD&logoColor=white)


### DevOps
![AWS](https://img.shields.io/badge/AWS-white?style=for-the-badge&logo=Amazon&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-skyblue?style=for-the-badge&logo=Docker&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-green?style=for-the-badge&logo=Gradle&logoColor=white)

<br/>

## 🚀 시작하기

### 환경 요구사항
- Java 17+
- MySQL 8.0+
- Redis 6.0+

### 설치 및 실행

```bash
# 레포지토리 클론
git clone https://github.com/DRS-SingSongGame/SingSongGame-BE.git
cd SingSongGame-BE

# 환경 변수 설정 (.env 파일 생성, 배포 시에는 Github Secrets 사용)
JWT_SECRET_KEY=your-jwt-secret-key
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
DB_URL=jdbc:mysql://localhost:3306/singsong
DB_USERNAME=your-db-username
DB_PASSWORD=your-db-password
REDIS_HOST=localhost
REDIS_PORT=6379

# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### Docker로 실행

```bash
# Docker 이미지 빌드 및 실행
docker build -t singsong-backend .
docker run -p 8080:8080 singsong-backend
```

<br/>

## 📁 프로젝트 구조

```
src/main/java/SingSongGame/BE/
├── auth/                    # OAuth2 인증 시스템
├── chat/                    # 실시간 채팅 시스템
├── in_game/                 # 게임 로직 및 상태 관리
├── quick_match/             # 빠른 매칭 시스템
├── room/                    # 게임 방 관리
├── song/                    # 노래 데이터 관리
├── user/                    # 사용자 관리
├── ai_game/                 # AI TTS 게임 모드
├── online/                  # 온라인 사용자 관리
├── redis/                   # Redis 구독자 및 설정
├── common/                  # 공통 유틸리티
├── config/                  # 스프링 설정
└── exception/               # 예외 처리
```

<br/>

## 🎮 게임 플로우

### 1. 사용자 인증
```
카카오 로그인 → JWT 토큰 발급 → 사용자 세션 생성
```

### 2. 게임 매칭
```
빠른 매칭 요청 → MMR 기반 매칭 → 게임 방 생성 → 플레이어 배치
```

### 3. 게임 진행
```
게임 시작 → 문제 출제 → 정답 제출 → 점수 계산 → 다음 문제
```

### 4. 게임 종료
```
최종 점수 계산 → MMR 업데이트 → 티어 변동 → 결과 저장
```

<br/>

## 📚 API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`


<br/>

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "TestClassName"
```

<br/>

## 🌐 배포

### Production 환경 설정

```bash
# Production 프로파일로 실행
java -jar -Dspring.profiles.active=prod build/libs/BE-0.0.1-SNAPSHOT.jar
```

<br/>

## 👥 팀 정보

**Team DRS**

이 프로젝트는 크래프톤 정글 8기 Team DRS에서 개발한 실시간 멀티플레이어 노래 맞추기 게임입니다.

<br/>
