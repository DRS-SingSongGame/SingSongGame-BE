package SingSongGame.BE.in_game.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.dto.response.AnswerResultResponse;
import SingSongGame.BE.room.application.RoomSongService;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.song.persistence.Song;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InGameChatService {

    private final SimpMessageSendingOperations sendingOperations;
    private final RoomRepository roomRepository;
    private final RoomSongService roomSongService;
    private final InGameService inGameService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public InGameChatService(
            SimpMessageSendingOperations sendingOperations,
            RoomRepository roomRepository,
            RoomSongService roomSongService,
            InGameService inGameService,
            @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper) {
        this.sendingOperations = sendingOperations;
        this.roomRepository = roomRepository;
        this.roomSongService = roomSongService;
        this.inGameService = inGameService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void verifyAnswer(User user, Long roomId, String message) {
        Song currentSong = roomSongService.getCurrentSongForRoom(roomId);

        if (currentSong != null && message.trim().equalsIgnoreCase(currentSong.getAnswer())) {
            inGameService.addScore(user, roomId, 1);

            AnswerResultResponse result = new AnswerResultResponse(
                    user.getName(), true, currentSong.getTitle()
            );

            // Redis에 메시지 발행
            try {
                redisTemplate.convertAndSend("/topic/room/" + roomId + "/answer", result);
            } catch (Exception e) {
                log.error("InGame Redis 메시지 발행 중 오류 발생: {}", e.getMessage(), e);
            }
        }
    }
}
