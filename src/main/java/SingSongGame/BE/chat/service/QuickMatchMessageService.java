package SingSongGame.BE.chat.service;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.WebSocketResponse;
import SingSongGame.BE.quick_match.application.dto.response.QuickMatchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.net.http.WebSocket;

@Service
@RequiredArgsConstructor
public class QuickMatchMessageService {

    private final SimpMessageSendingOperations messagingTemplate;

    public void sendMatchFoundMessage(User user, QuickMatchResponse responseDto) {
        WebSocketResponse response = new WebSocketResponse("MATCH_FOUND", responseDto);

        System.out.println("🎯 MATCH_FOUND 전송 대상: userId=" + user.getId());
        System.out.println("🎯 전송 경로: /queue/match");

        messagingTemplate.convertAndSendToUser(
                user.getId().toString(), // 1️⃣ 유저 식별자 (프론트 stomp client에서 connectHeaders로 넘긴 ID와 일치해야 함)
                "/queue/match",          // 2️⃣ 반드시 /queue/... 형식으로만 써야 함
                response
        );
    }
}
