package SingSongGame.BE.redis;

import SingSongGame.BE.chat.dto.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JsonParser {
    private final ObjectMapper objectMapper;

    // 여기서 DTO는 메시지 요청에 대한 DTO일까? 응답에 대한 DTO일까?
    public ChatMessage toChatDto(String chattingMessage) {
        try {
            return objectMapper.readValue(chattingMessage, ChatMessage.class);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
