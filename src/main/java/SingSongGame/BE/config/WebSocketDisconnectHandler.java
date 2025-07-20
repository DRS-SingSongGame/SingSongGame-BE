package SingSongGame.BE.config;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.service.LobbyChatService;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.online.application.OnlineUserService;
import SingSongGame.BE.online.persistence.SessionUserRegistry;
import SingSongGame.BE.room.application.RoomService;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectHandler implements ApplicationListener<SessionDisconnectEvent> {

    private final Logger log = LoggerFactory.getLogger(WebSocketDisconnectHandler.class);

    private final SessionUserRegistry sessionUserRegistry;
    private final UserService userService;
    private final LobbyChatService chatService;
    private final RoomService roomService; // 추가
    private final InGameRepository inGameRepository; // 추가


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
                handleRoomDisconnect(user);
                return;
            } else {
                log.warn("❌ 사용자 정보 조회 실패 - userId: {}", userId);
            }
        } else {
            log.warn("❌ sessionId로 사용자 매핑 실패: {}", sessionId);
        }
    }
    private void handleRoomDisconnect(User user) {
        try {
            // 현재 사용자가 입장한 방 찾기
            List<InGame> userInGames = inGameRepository.findAllByUser(user);

            for (InGame inGame : userInGames) {
                Room room = inGame.getRoom();
                log.info("🏠 사용자 {}가 방 {} (이름: {})에서 자동 퇴장 처리",
                        user.getName(), room.getId(), room.getName());

                // RoomService의 leaveRoom 메서드 호출
                roomService.leaveRoom(room.getId(), user);

                log.info("✅ 방 퇴장 처리 완료");
            }

        } catch (Exception e) {
            log.error("❌ 방 퇴장 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
