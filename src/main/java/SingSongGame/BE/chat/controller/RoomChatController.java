package SingSongGame.BE.chat.controller;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.RoomChatRequest;
import SingSongGame.BE.chat.service.RoomChatService;
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
public class RoomChatController {

    private final RoomChatService roomChatService;
    private final UserService userService;

    @MessageMapping("/room/{roomId}/chat")
    public void sendRoomMessage(@Payload RoomChatRequest request, SimpMessageHeaderAccessor headerAccessor , @DestinationVariable("roomId") Long roomId) {


        // WebSocket 세션에서 사용자 정보 가져오기
        Principal auth = headerAccessor.getUser();
        String email = null;

        if (auth instanceof UsernamePasswordAuthenticationToken token) {

            Object rawPrincipal = token.getPrincipal();
            if (rawPrincipal instanceof User userEntity) {
                email = userEntity.getEmail();
            }
        }
        User user = userService.findByEmail(email);

        log.info("방 채팅 메시지: 방 {} - {}: {}", roomId, user.getName(), request.getMessage());
        roomChatService.sendRoomMessage(user, roomId, request.getMessage());
    }
} 