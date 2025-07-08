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
//        String username = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
//
//        log.info("방 채팅 사용자 정보: {}", username);
//
//        if (username == null) {
//            log.warn("사용자 정보가 없습니다. 방 채팅 메시지 전송을 건너뜁니다.");
//            return;
//        }
        Principal auth = headerAccessor.getUser();
        String email = null;

        // 2) Authentication → UsernamePasswordAuthenticationToken 으로 캐스팅
        if (auth instanceof UsernamePasswordAuthenticationToken token) {

            // 3) token.getPrincipal() → 실제 User 객체
            Object rawPrincipal = token.getPrincipal();
            if (rawPrincipal instanceof User userEntity) {

                // 4) 이제 email 꺼내기
                email = userEntity.getEmail();
//                log.info("사용자 이메일: {}", email);
            }
        }
        
        // 사용자 이름으로 User 객체 조회
        User user = userService.findByEmail(email);
//        if (user == null) {
//            log.warn("사용자를 찾을 수 없습니다: {}", username);
//            return;
//        }

        log.info("방 채팅 메시지: 방 {} - {}: {}", roomId, user.getName(), request.getMessage());
        roomChatService.sendRoomMessage(user, roomId, request.getMessage());
    }
} 