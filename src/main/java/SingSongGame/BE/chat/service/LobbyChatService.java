package SingSongGame.BE.chat.service;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.ChatMessage;
import SingSongGame.BE.chat.dto.LobbyChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class LobbyChatService {

    private final SimpMessageSendingOperations sendingOperations;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public LobbyChatService(
            SimpMessageSendingOperations sendingOperations,
            @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.sendingOperations = sendingOperations;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }


    public void sendLobbyMessage(LobbyChatRequest request) {
        ChatMessage chatMessage = ChatMessage.builder()
                                             .type(ChatMessage.MessageType.valueOf(request.getType()))
                                             .roomId(request.getRoomId())
                                             .senderId(request.getSenderId())
                                             .senderName(request.getSenderName())
                                             .message(request.getMessage())
                                             .timestamp(LocalDateTime.now().toString())
                                             .build();

        // Redis에 메시지 발행
        try {
            String jsonMessage = objectMapper.writeValueAsString(chatMessage);
            redisTemplate.convertAndSend("/topic/lobby", jsonMessage);
            log.info("로비 Redis 메시지 발행 완료: {}", jsonMessage);
        } catch (Exception e) {
            log.error("로비 Redis 메시지 발행 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void sendUserEnterLobby(User user) {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.ENTER)
                .roomId("lobby")
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(user.getName() + "님이 로비에 입장했습니다.")
                .timestamp(LocalDateTime.now().toString())
                .build();

        sendingOperations.convertAndSend("/topic/lobby", chatMessage);
    }

    public void sendUserLeaveLobby(User user) {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.LEAVE)
                .roomId("lobby")
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(user.getName() + "님이 로비를 나갔습니다.")
                .timestamp(LocalDateTime.now().toString())
                .build();

        sendingOperations.convertAndSend("/topic/lobby", chatMessage);
    }
} 