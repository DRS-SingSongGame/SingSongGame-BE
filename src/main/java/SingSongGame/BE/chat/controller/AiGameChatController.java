package SingSongGame.BE.chat.controller;


import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.RoomChatRequest;
import SingSongGame.BE.chat.service.AiGameChatService;
import SingSongGame.BE.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AiGameChatController {
    private final AiGameChatService aiGameChatService;
    private final UserService userService;

    @MessageMapping("/ai-room/{roomId}/chat")
    public void sendAiGameChat(@Payload RoomChatRequest request,
                               SimpMessageHeaderAccessor headerAccessor,
                               @DestinationVariable("roomId") Long roomId) {
        Principal auth = headerAccessor.getUser();
        String email = null;

        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object rawPrincipal = token.getPrincipal();
            if (rawPrincipal instanceof User userEntity) {
                email = userEntity.getEmail();
                log.info("사용자 이메일: {}", email);
            }
        }

        User user = userService.findByEmail(email);
        log.info("[AI GAME CHAT] 방 {} - {}: {}", roomId, user.getName(), request.getMessage());


        aiGameChatService.sendRoomMessage(user, roomId, request.getMessage()); // 브로드캐스트 내부 처리
    }

}
