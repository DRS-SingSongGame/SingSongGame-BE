package SingSongGame.BE.in_game.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.dto.response.GameStartCountdownResponse;
import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.in_game.persistence.GameSessionRepository;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.room.persistence.RoomType;
import SingSongGame.BE.room_keyword.KeywordService;
import SingSongGame.BE.song.application.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.TaskScheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InGameServiceTest {
    
    @Mock
    private GameSessionRepository gameSessionRepository;
    
    @Mock
    private RoomRepository roomRepository;
    
    @Mock
    private SimpMessageSendingOperations messagingTemplate;
    
    @Mock
    private TaskScheduler taskScheduler;
    
    @Mock
    private ApplicationContext applicationContext;
    
    @Mock
    private InGameRepository inGameRepository;
    
    @Mock
    private SongService songService;
    
    @Mock
    private ScheduledFuture<Void> scheduledFuture;

    @Mock
    private KeywordService keywordService;
    
    @InjectMocks
    private InGameService inGameService;
    
    private Room testRoom;
    private User testUser;
    private GameSession existingGameSession;
    private Set<String> testKeywords;

    @BeforeEach
    void setUp() {

    }

    @Test
    void 게임시작_시작메세지와세션저장() {
        // 방 정보와 키워드를 준비합니다.
        Long roomId = 1L;
        Room room = Room.builder().id(roomId).maxRound(3).build();
        Set<String> keywords = new HashSet<>(Set.of("pop", "ballad"));

        // 저장되는 GameSession을 가로채기 위한 리스트입니다.
        final List<GameSession> savedSessions = new ArrayList<>();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(gameSessionRepository.findById(roomId)).thenReturn(Optional.empty());
        when(gameSessionRepository.save(any(GameSession.class)))
                .thenAnswer(invocation -> {
                    GameSession gs = invocation.getArgument(0);
                    savedSessions.add(gs);
                    return gs;
                });

        // 메시지 전송 내용을 수집합니다.
        final List<String> destinations = new ArrayList<>();
        final List<Object> payloads = new ArrayList<>();
        doAnswer(invocation -> {
            destinations.add(invocation.getArgument(0));
            payloads.add(invocation.getArgument(1));
            return null;
        }).when(messagingTemplate).convertAndSend(anyString(), (Object) any());

        // 스케줄러는 실제 실행되지 않도록 더미 값을 반환합니다.
        when(taskScheduler.schedule(any(Runnable.class), any(Date.class)))
                .thenReturn(null);

        // startGame 메서드 실행
        inGameService.startGame(roomId, keywords);

        // GameSession이 저장되었는지와 상태를 확인합니다.
        assertThat(savedSessions).hasSize(1);
        assertThat(savedSessions).hasSize(0);
        GameSession saved = savedSessions.get(0);
        assertThat(saved.getGameStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(saved.getKeywords()).isEqualTo(keywords);

        // 메시지가 올바른 위치로 전송되었는지 확인합니다.
        assertThat(destinations).contains("/topic/room/" + roomId + "/game-start");
        assertThat(payloads).hasSize(1);
    }
}