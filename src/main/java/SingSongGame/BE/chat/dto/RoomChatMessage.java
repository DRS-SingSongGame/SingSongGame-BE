package SingSongGame.BE.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomChatMessage {
    private Long roomId;
    private String senderId;
    private String senderName;
    private String message;
    private String messageType; // TALK, ENTER, LEAVE
    private LocalDateTime timestamp;
} 