package SingSongGame.BE.chat.service;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.ChatMessage;
import SingSongGame.BE.chat.dto.LobbyChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import SingSongGame.BE.online.application.OnlineUserService;
import SingSongGame.BE.online.persistence.OnlineLocation;
import SingSongGame.BE.online.persistence.OnlineUser;
import SingSongGame.BE.online.persistence.OnlineUserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class LobbyChatService {


    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final SimpMessageSendingOperations sendingOperations;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final OnlineUserRepository onlineUserRepository;
    private final OnlineUserService onlineUserService;

    public LobbyChatService(
            SimpMessageSendingOperations sendingOperations,
            @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.sendingOperations = sendingOperations;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }



    public void sendLobbyMessage(LobbyChatRequest request, User user) {
        ChatMessage chatMessage = ChatMessage.builder()
                                             .type(ChatMessage.MessageType.TALK)
                                             .roomId("lobby")
                                             .senderId(user.getId().toString())
                                             .senderName(user.getName())
                                             .message(request.getMessage())
                                             .timestamp(LocalDateTime.now().format(ISO_FORMATTER))
                                             .build();

        // Redis에 메시지 발행
        try {
            redisTemplate.convertAndSend("/topic/lobby", chatMessage);
            log.info("로비 Redis 메시지 발행 완료: {}", chatMessage);
        } catch (Exception e) {
            log.error("로비 Redis 메시지 발행 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void sendUserEnterLobby(User user) {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.ENTER)
                .roomId("lobby")
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(user.getName() + "님이 로비에 입장했습니다.")
                .timestamp(LocalDateTime.now().format(ISO_FORMATTER))
                .build();

        onlineUserService.addUser(user.getId(), user.getName(), user.getImageUrl(), OnlineLocation.LOBBY);

        sendingOperations.convertAndSend("/topic/lobby", chatMessage);

        broadcastOnlineUsers();
    }

    public void sendUserLeaveLobby(User user) {
        ChatMessage chatMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.LEAVE)
                .roomId("lobby")
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(user.getName() + "님이 로비를 나갔습니다.")
                .timestamp(LocalDateTime.now().format(ISO_FORMATTER))
                .build();

        onlineUserService.changeUserLocation(user.getId(), OnlineLocation.ROOM);

        sendingOperations.convertAndSend("/topic/lobby", chatMessage);

        broadcastOnlineUsers();
    }

    public void sendUserDisconnect(User user) {
        // 1. 유저 제거 (In-Memory에서 삭제)
        onlineUserService.removeUser(user.getId());

        // 2. 채팅 메시지 전송
        ChatMessage chatMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.LEAVE)
                .roomId("lobby")
                .senderId(user.getId().toString())
                .senderName(user.getName())
                .message(user.getName() + "님이 접속을 종료했습니다.")
                .timestamp(LocalDateTime.now())
                .build();

        sendingOperations.convertAndSend("/topic/lobby", chatMessage);

        // 3. 접속 유저 목록 업데이트 broadcast
        broadcastOnlineUsers();
    }

    public void broadcastOnlineUsers() {
        try {
            List<OnlineUser> allUsers = new ArrayList<>(onlineUserRepository.findAll().values());

            ChatMessage message = ChatMessage.builder()
                    .type(ChatMessage.MessageType.USER_LIST_UPDATE)
                    .roomId("lobby")
                    .message(objectMapper.writeValueAsString(allUsers)) // 유저 리스트 JSON 문자열로 변환
                    .timestamp(LocalDateTime.now())
                    .build();

            sendingOperations.convertAndSend("/topic/lobby", message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}