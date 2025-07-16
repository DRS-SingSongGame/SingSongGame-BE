package SingSongGame.BE.chat.service;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.chat.dto.ChatMessage;
import SingSongGame.BE.chat.dto.LobbyChatRequest;
import SingSongGame.BE.online.application.OnlineUserService;
import SingSongGame.BE.online.persistence.OnlineUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.ArrayList;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LobbyChatServiceTest {

    @Mock
    SimpMessageSendingOperations sendingOperations;

    @Mock
    @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    OnlineUserService onlineUserService;

    @Mock
    OnlineUserRepository onlineUserRepository;

    @InjectMocks
    LobbyChatService lobbyChatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendLobbyMessage_메시지발행() {
        User user = User.builder().id(1L).name("kim").build();
        LobbyChatRequest req = LobbyChatRequest.builder().message("hi").build();

        final List<String> topics = new ArrayList<>();
        final List<Object> payloads = new ArrayList<>();
        doAnswer(invocation -> {
            topics.add(invocation.getArgument(0));
            payloads.add(invocation.getArgument(1));
            return null;
        }).when(redisTemplate).convertAndSend(anyString(), any(Object.class));

        lobbyChatService.sendLobbyMessage(req, user);

        assertThat(topics).contains("/topic/lobby");
        assertThat(payloads).hasSize(1);
        ChatMessage msg = (ChatMessage) payloads.get(0);
        assertThat(msg.getMessage()).isEqualTo("hi");
        assertThat(msg.getType()).isEqualTo(ChatMessage.MessageType.TALK);
    }
}