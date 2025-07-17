package SingSongGame.BE.redis;

import SingSongGame.BE.in_game.dto.response.AnswerResultResponse;
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
public class InGameRedisSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    public InGameRedisSubscriber(
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
            
            // 패턴에서 roomId 추출
            String patternStr = new String(pattern);
            String roomId = patternStr.substring(patternStr.lastIndexOf("/") + 1);
            
            AnswerResultResponse answerResult = objectMapper.readValue(publishMessage, AnswerResultResponse.class);
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/answer", answerResult);
            
            log.info("InGame Redis 메시지 처리 완료: roomId={}, answerResult={}", roomId, answerResult);
        } catch (Exception e) {
            log.error("InGame Redis 메시지 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
} 