package SingSongGame.BE.chat.controller;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.LobbyChatRequest;
import SingSongGame.BE.chat.service.LobbyChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LobbyChatController {

    private final LobbyChatService lobbyChatService;

    @MessageMapping("/lobby/chat")
    public void sendLobbyMessage(@Payload LobbyChatRequest request) {
        // 임시로 더미 사용자 생성
        User dummyUser = User.builder()
                .id(System.currentTimeMillis())
                .name("사용자_" + (System.currentTimeMillis() % 1000))
                .build();
        
        log.info("로비 채팅 요청: {} - {}", dummyUser.getName(), request.getMessage());
        lobbyChatService.sendLobbyMessage(dummyUser, request.getMessage());
    }
} 