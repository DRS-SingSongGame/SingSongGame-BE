package SingSongGame.BE.chat.controller;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.LobbyChatRequest;
import SingSongGame.BE.chat.service.LobbyChatService;
import SingSongGame.BE.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.aspectj.weaver.patterns.IToken;
import org.aspectj.weaver.patterns.ITokenSource;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

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

//        if (user == null) {
//            log.warn("사용자를 찾을 수 없습니다: {}", username);
//            return;
//        }
        
        log.info("로비 채팅 메시지: {} - {}", user.getName(), request.getMessage());
        lobbyChatService.sendLobbyMessage(user, request.getMessage());
    }
} 