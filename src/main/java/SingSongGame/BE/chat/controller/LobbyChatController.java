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
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LobbyChatController {

    private final LobbyChatService lobbyChatService;
    private final UserService userService;

    @MessageMapping("/lobby/chat")
    public void sendLobbyMessage(@Payload LobbyChatRequest request, SimpMessageHeaderAccessor headerAccessor) {
        // WebSocket 세션에서 사용자 정보 가져오기
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
        
        if (username == null) {
            log.warn("사용자 정보가 없습니다. 메시지 전송을 건너뜁니다.");
            return;
        }
        
        // 사용자 이름으로 User 객체 조회
        User user = userService.findByName(username);

        log.info("현재 사용자 : {}", user.getName());

        if (user == null) {
            log.warn("사용자를 찾을 수 없습니다: {}", username);
            return;
        }
        
        log.info("로비 채팅 메시지: {} - {}", user.getName(), request.getMessage());
        lobbyChatService.sendLobbyMessage(user, request.getMessage());
    }
} 