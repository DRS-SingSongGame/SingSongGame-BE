package SingSongGame.BE.chat.service;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.RoomChatMessage;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomChatService {

    private final SimpMessageSendingOperations sendingOperations;
    private final RoomRepository roomRepository;

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
        sendingOperations.convertAndSend("/topic/room/" + roomId + "/chat", chatMessage);
        
        log.info("방 채팅 메시지 전송: 방 {} - {}: {}", roomId, user.getName(), message);
    }

    public void sendRoomEnterMessage(User user, Long roomId) {
        RoomChatMessage chatMessage = RoomChatMessage.builder()
                .roomId(roomId)
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(user.getName() + "님이 입장하셨습니다.")
                .messageType("ENTER")
                .timestamp(LocalDateTime.now())
                .build();

        sendingOperations.convertAndSend("/topic/room/" + roomId + "/chat", chatMessage);
        log.info("방 입장 메시지 전송: 방 {} - {}", roomId, user.getName());
    }

    public void sendRoomLeaveMessage(User user, Long roomId) {
        RoomChatMessage chatMessage = RoomChatMessage.builder()
                .roomId(roomId)
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(user.getName() + "님이 퇴장하셨습니다.")
                .messageType("LEAVE")
                .timestamp(LocalDateTime.now())
                .build();

        sendingOperations.convertAndSend("/topic/room/" + roomId + "/chat", chatMessage);
        log.info("방 퇴장 메시지 전송: 방 {} - {}", roomId, user.getName());
    }
} 