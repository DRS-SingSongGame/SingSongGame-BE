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
        ENTER, TALK, LEAVE
    }
    
    private MessageType type;
    private String roomId; // 로비는 "lobby", 게임방은 roomId
    private String senderId;
    private String senderName;
    private String message;
    private LocalDateTime timestamp;

    // LobbyChatRequest를 받는 생성자
    public ChatMessage(LobbyChatRequest lobbyChatRequest) {
        this.type = MessageType.TALK;
        this.roomId = "lobby";
        this.senderId = lobbyChatRequest.getSenderId();
        this.senderName = lobbyChatRequest.getSenderName();
        this.message = lobbyChatRequest.getMessage();
        this.timestamp = LocalDateTime.now();
    }

    // RoomChatRequest를 받는 생성자
    public ChatMessage(RoomChatRequest roomChatRequest) {
        this.type = MessageType.TALK;
        this.roomId = "lobby";
        this.senderId = roomChatRequest.getSenderId();
        this.senderName = roomChatRequest.getSenderName();
        this.message = roomChatRequest.getMessage();
        this.timestamp = LocalDateTime.now();
    }
}





