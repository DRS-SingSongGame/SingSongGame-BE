package SingSongGame.BE.chat.service;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyChatService {

    private final SimpMessageSendingOperations sendingOperations;

    public void sendLobbyMessage(User user, String message) {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.TALK)
                .roomId("lobby")
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        sendingOperations.convertAndSend("/topic/lobby", chatMessage);
    }

    public void sendUserEnterLobby(User user) {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.ENTER)
                .roomId("lobby")
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(user.getName() + "님이 로비에 입장했습니다.")
                .timestamp(LocalDateTime.now())
                .build();

        sendingOperations.convertAndSend("/topic/lobby", chatMessage);
    }

    public void sendUserLeaveLobby(User user) {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.LEAVE)
                .roomId("lobby")
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(user.getName() + "님이 로비를 나갔습니다.")
                .timestamp(LocalDateTime.now())
                .build();

        sendingOperations.convertAndSend("/topic/lobby", chatMessage);
    }
} 