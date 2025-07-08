package SingSongGame.BE.chat.service;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.RoomChatMessage;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGameChatService {

    private final RoomRepository roomRepository;
    private final SimpMessageSendingOperations sendingOperations;

    public void sendRoomMessage(User user, Long roomId, String message) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        RoomChatMessage chatMessage = RoomChatMessage.builder()
                .roomId(roomId)
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(message)
                .messageType("TALK")
                .timestamp(LocalDateTime.now())
                .build();

        // 특정 방의 모든 사용자에게 메시지 전송
        sendingOperations.convertAndSend("/topic/ai-room/" + roomId + "/chat", chatMessage);

        log.info("AI방 채팅 메시지 전송: 방 {} - {}: {}", roomId, user.getName(), message);
        log.info("✅ 채팅 유저 정보 - ID: {}, 이름: {}, 이메일: {}", user.getId(), user.getName(), user.getEmail());
    }
}
