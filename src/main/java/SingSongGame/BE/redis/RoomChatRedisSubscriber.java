package SingSongGame.BE.redis;

import SingSongGame.BE.chat.dto.RoomChatMessage;
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
public class RoomChatRedisSubscriber implements MessageListener {
    private final  Integer ROOM_ID_INDEX = 3;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    public RoomChatRedisSubscriber(
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
            String channel = new String(message.getChannel());
            String roomId = extractRoomId(channel);
                
            RoomChatMessage chatMessage = objectMapper.readValue(publishMessage, RoomChatMessage.class);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/chat", chatMessage);
            } catch (Exception e) {
            log.error("RoomChat Redis 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private String extractRoomId(String channelName) {
        String[] parts = channelName.split("/");
        return parts[ROOM_ID_INDEX];
    }
}
