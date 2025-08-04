package SingSongGame.BE.chat.service;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.RoomChatMessage;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomChatService {

    private final SimpMessageSendingOperations sendingOperations;
    private final RoomRepository roomRepository;
    private final @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate;

    public void sendRoomMessage(User user, Long roomId, String message) {
        RoomChatMessage chatMessage = RoomChatMessage.builder()
                .roomId(roomId)
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(message)
                .messageType("TALK")
                .timestamp(LocalDateTime.now())
                .build();

        String channel = "/topic/room/" + roomId + "/chat";
        
        try {
            redisTemplate.convertAndSend(channel, chatMessage);
        } catch (Exception e) {
            log.error("RoomChat Redis Publish 실패: {}", e.getMessage(), e);
        }
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

        String channel = "/topic/room/" + roomId + "/chat";
        
        try {
            redisTemplate.convertAndSend(channel, chatMessage);
        } catch (Exception e) {
            log.error("RoomEnter Redis Publish 실패: {}", e.getMessage(), e);
        }
        
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

        String channel = "/topic/room/" + roomId + "/chat";
        
        try {
            redisTemplate.convertAndSend(channel, chatMessage);
        } catch (Exception e) {
            log.error("RoomLeave Redis Publish 실패: {}", e.getMessage(), e);
        }
        
        log.info("방 퇴장 메시지 전송: 방 {} - {}", roomId, user.getName());
    }
} 