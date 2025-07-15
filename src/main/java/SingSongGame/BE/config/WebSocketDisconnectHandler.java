package SingSongGame.BE.config;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.service.LobbyChatService;
import SingSongGame.BE.online.application.OnlineUserService;
import SingSongGame.BE.online.persistence.SessionUserRegistry;
import SingSongGame.BE.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectHandler implements ApplicationListener<SessionDisconnectEvent> {

    private final Logger log = LoggerFactory.getLogger(WebSocketDisconnectHandler.class);

    private final SessionUserRegistry sessionUserRegistry;
    private final UserService userService;
    private final LobbyChatService chatService;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {

        String sessionId = event.getSessionId();
        log.info("🚪 WebSocket Disconnect 발생: {}", sessionId);

        Long userId = sessionUserRegistry.getUserIdBySessionId(sessionId);
        if (userId != null) {
            User user = userService.findById(userId);
            if (user != null) {
                log.info("📌 disconnect 된 사용자 이름: {}", user.getName());
                chatService.sendUserDisconnect(user);
                return;
            } else {
                log.warn("❌ 사용자 정보 조회 실패 - userId: {}", userId);
            }
        } else {
            log.warn("❌ sessionId로 사용자 매핑 실패: {}", sessionId);
        }
    }
}
