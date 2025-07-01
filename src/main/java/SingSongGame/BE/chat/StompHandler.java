package SingSongGame.BE.chat;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.common.util.JwtProvider;
import SingSongGame.BE.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {
    
    private final JwtProvider jwtProvider;
    private final UserService userService;
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            
            switch (command) {
                case CONNECT:
                    log.info("STOMP 연결 요청: {}", accessor.getSessionId());
                    // JWT 토큰 검증 및 사용자 정보 설정
                    String token = null;
                    
                    // 1. Authorization 헤더에서 토큰 확인
                    List<String> authHeaders = accessor.getNativeHeader("Authorization");
                    if (authHeaders != null && !authHeaders.isEmpty()) {
                        String authHeader = authHeaders.get(0);
                        if (authHeader.startsWith("Bearer ")) {
                            token = authHeader.substring(7);
                        }
                    }
                    
                    // 2. 쿠키에서 토큰 확인 (Authorization 헤더가 없는 경우)
                    if (token == null) {
                        List<String> cookies = accessor.getNativeHeader("Cookie");
                        if (cookies != null && !cookies.isEmpty()) {
                            String cookieHeader = cookies.get(0);
                            String[] cookiePairs = cookieHeader.split(";");
                            for (String pair : cookiePairs) {
                                String[] keyValue = pair.trim().split("=");
                                if (keyValue.length == 2 && "access".equals(keyValue[0].trim())) {
                                    token = keyValue[1].trim();
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (token != null) {
                        try {
                            Long userId = jwtProvider.getUserIdFromToken(token);
                            User user = userService.findById(userId);
                            if (user != null) {
                                accessor.setUser(() -> user.getName());
                                log.info("사용자 인증 성공: {}", user.getName());
                            } else {
                                log.warn("사용자를 찾을 수 없음: userId={}", userId);
                            }
                        } catch (Exception e) {
                            log.error("JWT 토큰 검증 실패: {}", e.getMessage());
                        }
                    } else {
                        log.warn("JWT 토큰을 찾을 수 없음");
                    }
                    break;
                case DISCONNECT:
                    log.info("STOMP 연결 해제: {}", accessor.getSessionId());
                    break;
                case SUBSCRIBE:
                    log.info("STOMP 구독: {} -> {}", accessor.getSessionId(), accessor.getDestination());
                    break;
                case UNSUBSCRIBE:
                    log.info("STOMP 구독 해제: {} -> {}", accessor.getSessionId(), accessor.getDestination());
                    break;
                case SEND:
                    log.info("STOMP 메시지 전송: {} -> {}", accessor.getSessionId(), accessor.getDestination());
                    break;
            }
        }
        
        return message;
    }
}
