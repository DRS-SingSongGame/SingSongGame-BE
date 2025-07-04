package SingSongGame.BE.chat.controller;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.RoomChatRequest;
import SingSongGame.BE.chat.service.RoomChatService;
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
public class RoomChatController {

    private final RoomChatService roomChatService;
    private final UserService userService;

    @MessageMapping("/room/chat")
    public void sendRoomMessage(@Payload RoomChatRequest request, SimpMessageHeaderAccessor headerAccessor) {


        // WebSocket 세션에서 사용자 정보 가져오기
        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
        
        log.info("방 채팅 사용자 정보: {}", username);
        
        if (username == null) {
            log.warn("사용자 정보가 없습니다. 방 채팅 메시지 전송을 건너뜁니다.");
            return;
        }
        
        // 사용자 이름으로 User 객체 조회
        User user = userService.findByName(username);
        if (user == null) {
            log.warn("사용자를 찾을 수 없습니다: {}", username);
            return;
        }

        log.info("방 채팅 메시지: 방 {} - {}: {}", request.getRoomId(), user.getName(), request.getMessage());
        roomChatService.sendRoomMessage(user, request.getRoomId(), request.getMessage());
    }
} 