# SingSongGame-BE

## 프로젝트 개요
SingSong 게임의 백엔드 서버입니다. Spring Boot를 기반으로 하며, WebSocket을 통한 실시간 채팅 기능을 제공합니다.

## 주요 기능

### 1. 로비 채팅 시스템
- **실시간 채팅**: WebSocket과 STOMP를 사용한 실시간 로비 채팅
- **사용자 입장/퇴장 알림**: 사용자가 로비에 입장하거나 퇴장할 때 자동 알림
- **메시지 타입**: ENTER, TALK, LEAVE 메시지 타입 지원

### 2. 채팅 관련 API

#### WebSocket 연결
- **연결 엔드포인트**: `/ws/chat`
- **구독 토픽**: `/topic/lobby` (로비 채팅)

#### REST API
- **로비 입장**: `POST /api/lobby/enter`
- **로비 퇴장**: `POST /api/lobby/leave`

#### STOMP 메시지
- **채팅 메시지 전송**: `/api/lobby/chat`

### 3. 테스트 방법

#### 웹 브라우저에서 테스트
1. 애플리케이션을 실행합니다
2. 브라우저에서 `http://localhost:8080/lobby-chat.html` 접속
3. 여러 브라우저 창을 열어 실시간 채팅 테스트

#### WebSocket 연결 예시
```javascript
// SockJS 연결
const socket = new SockJS('/ws/chat');
const stompClient = Stomp.over(socket);

// 연결
stompClient.connect({}, function (frame) {
    // 로비 채팅 구독
    stompClient.subscribe('/topic/lobby', function (message) {
        const chatMessage = JSON.parse(message.body);
        console.log('받은 메시지:', chatMessage);
    });
});

// 메시지 전송
stompClient.send("/api/lobby/chat", {}, JSON.stringify({
    'message': '안녕하세요!'
}));
```

### 4. 채팅 메시지 구조
```json
{
    "type": "TALK",           // ENTER, TALK, LEAVE
    "roomId": "lobby",        // 로비는 "lobby"
    "senderId": "123",
    "senderName": "사용자명",
    "message": "메시지 내용",
    "timestamp": "2024-01-01T12:00:00"
}
```

## 기술 스택
- **Spring Boot**: 백엔드 프레임워크
- **WebSocket**: 실시간 통신
- **STOMP**: 메시징 프로토콜
- **SockJS**: WebSocket 폴백 지원
- **JPA/Hibernate**: 데이터베이스 ORM
- **Gradle**: 빌드 도구

## 실행 방법
```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

## 개발 환경
- Java 17+
- Spring Boot 3.x
- Gradle 8.x
