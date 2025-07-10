package SingSongGame.BE.chat.controller;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.LobbyChatRequest;
import SingSongGame.BE.chat.service.LobbyChatService;
import SingSongGame.BE.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LobbyChatController {

    private final LobbyChatService lobbyChatService;
    private final UserService userService;

    @MessageMapping("/lobby/chat")
    public void sendLobbyMessage(@Payload LobbyChatRequest request, SimpMessageHeaderAccessor headerAccessor) {

        headerAccessor.getMessageHeaders().forEach((key, value) -> {
            System.out.println("Header Key: " + key + ", Value: " + value);
        });

        Principal auth = headerAccessor.getUser();
        String email = null;

        // 2) Authentication → UsernamePasswordAuthenticationToken 으로 캐스팅
        if (auth instanceof UsernamePasswordAuthenticationToken token) {

            // 3) token.getPrincipal() → 실제 User 객체
            Object rawPrincipal = token.getPrincipal();
            if (rawPrincipal instanceof User userEntity) {

                // 4) 이제 email 꺼내기
                email = userEntity.getEmail();
                log.info("사용자 이메일: {}", email);
            }
        }

        // 사용자 이름으로 User 객체 조회
        User user = userService.findByEmail(email);

        log.info("현재 사용자 : {}", user.getName());
        
        log.info("로비 채팅 메시지: {} - {}", user.getName(), request.getMessage());
        
        // 클라이언트에서 보낸 정보를 그대로 사용하여 Redis에 발행
        lobbyChatService.sendLobbyMessage(request);
    }
} 