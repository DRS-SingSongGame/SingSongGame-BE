package SingSongGame.BE.redis;

import SingSongGame.BE.chat.dto.ChatMessage;
import SingSongGame.BE.chat.dto.RoomChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    public RedisSubscriber(
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
            RoomChatRequest roomMessage = objectMapper.readValue(publishMessage, RoomChatRequest.class);

            if (roomMessage.getMessage() != null) {
                ChatMessage chatMessageResponse = new ChatMessage(roomMessage);
                messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), chatMessageResponse);
            }
        } catch (Exception e) {
            log.error("Room Redis 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
