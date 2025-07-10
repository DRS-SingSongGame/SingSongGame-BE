package SingSongGame.BE.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    
    public enum MessageType {
        ENTER, TALK, LEAVE, USER_LIST_UPDATE
    }
    
    private MessageType type;
    private String roomId; // 로비는 "lobby", 게임방은 roomId
    private String senderId;
    private String senderName;
    private String message;
    private LocalDateTime timestamp;
} 