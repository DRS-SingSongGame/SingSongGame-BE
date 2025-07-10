package SingSongGame.BE.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomChatRequest {
    private Long roomId;
    private String message;
    private String senderId;
    private String senderName;
} 