package SingSongGame.BE.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StompHandler implements ChannelInterceptor {
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            StompCommand command = accessor.getCommand();
            
            switch (command) {
                case CONNECT:
                    log.info("STOMP 연결 요청: {}", accessor.getSessionId());
                    break;
                case DISCONNECT:
                    log.info("STOMP 연결 해제: {}", accessor.getSessionId());
                    break;
                case SUBSCRIBE:
                    log.info("STOMP 구독: {} -> {}", accessor.getSessionId(), accessor.getDestination());
                    break;
                case UNSUBSCRIBE:
                    log.info("STOMP 구독 해제: {} -> {}", accessor.getSessionId(), accessor.getDestination());
                    break;
                case SEND:
                    log.info("STOMP 메시지 전송: {} -> {}", accessor.getSessionId(), accessor.getDestination());
                    break;
            }
        }
        
        return message;
    }
}
