package SingSongGame.BE.redis;

import SingSongGame.BE.chat.dto.ChatMessage;
import SingSongGame.BE.chat.dto.LobbyChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LobbyRedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    public LobbyRedisSubscriber(
            ObjectMapper objectMapper,
            @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate,
            SimpMessageSendingOperations messagingTemplate) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            log.info("로비 Redis 메시지 수신: {}", publishMessage);

            // JSON을 그대로 WebSocket으로 전송
            messagingTemplate.convertAndSend("/topic/lobby", publishMessage);

            log.info("로비 채팅 메시지 전송 완료");

        } catch (Exception e) {
            log.error("로비 Redis 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
} 