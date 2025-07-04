package SingSongGame.BE.in_game.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.dto.response.AnswerResultResponse;
import SingSongGame.BE.room.application.RoomSongService;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.song.persistence.Song;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InGameChatService {

    private final SimpMessageSendingOperations sendingOperations;
    private final RoomRepository roomRepository;
    private final RoomSongService roomSongService;
    private final InGameService inGameService;

    public void verifyAnswer(User user, Long roomId, String message) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        Song currentSong = roomSongService.getCurrentSongForRoom(roomId);

        if (currentSong != null && message.trim().equalsIgnoreCase(currentSong.getAnswer())) {
            inGameService.addScore(user, roomId, 1);

            AnswerResultResponse result = new AnswerResultResponse(
                    user.getName(), true, currentSong.getTitle()
            );

            sendingOperations.convertAndSend("/topic/room/" + roomId + "/answer", result);

            log.info("✅ [{}] 정답 맞춤: {} - {}", roomId, user.getName(), currentSong.getTitle());
        }
    }
}
