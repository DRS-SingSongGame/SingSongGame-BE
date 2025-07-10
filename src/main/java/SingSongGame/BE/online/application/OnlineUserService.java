package SingSongGame.BE.online.application;

import SingSongGame.BE.chat.dto.ChatMessage;
import SingSongGame.BE.online.application.dto.response.OnlineUserResponse;
import SingSongGame.BE.online.persistence.OnlineLocation;
import SingSongGame.BE.online.persistence.OnlineUser;
import SingSongGame.BE.online.persistence.OnlineUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final OnlineUserRepository onlineUserRepository;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final ObjectMapper objectMapper;

    public void addUser(Long userId, String username, String imageUrl, OnlineLocation location) {
        OnlineUser user = new OnlineUser(userId, username, imageUrl, location);
        onlineUserRepository.save(user);
    }

    public void changeUserLocation(Long userId, OnlineLocation location) {
        OnlineUser existingUser = onlineUserRepository.findById(userId);
        if (existingUser == null) {
            throw new IllegalArgumentException("해당 유저는 현재 접속 중이 아닙니다: " + userId);
        }

        OnlineUser updatedUser = new OnlineUser(
                existingUser.getUserId(),
                existingUser.getUsername(),
                existingUser.getImageUrl(),
                location
        );

        onlineUserRepository.save(updatedUser);
    }

    public void removeUser(Long userId) {
        onlineUserRepository.delete(userId);
    }

    public List<OnlineUserResponse> getAllOnlineUsers() {
        return onlineUserRepository.findAll().values().stream()
                .map(user -> new OnlineUserResponse(user.getUserId(), user.getUsername(), user.getImageUrl(), user.getLocation()))
                .toList();
    }

    public void broadcastOnlineUsers() {
        List<OnlineUser> userList = new ArrayList<>(onlineUserRepository.findAll().values());

        try {
            ChatMessage msg = ChatMessage.builder()
                    .type(ChatMessage.MessageType.USER_LIST_UPDATE)
                    .roomId("lobby")  // 그냥 /topic/lobby로 전송
                    .message(objectMapper.writeValueAsString(userList)) // 유저 목록을 JSON 문자열로 전송
                    .timestamp(LocalDateTime.now().format(ISO_FORMATTER).toString())
                    .build();

            simpMessagingTemplate.convertAndSend("/topic/lobby", msg);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
