package SingSongGame.BE.in_game.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.dto.response.GameStartCountdownResponse;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.song.application.SongService;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class InGameService {

    private final InGameRepository inGameRepository;
    private final RoomRepository roomRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final SongService songService;
    private final TaskScheduler taskScheduler;

    // 게임을 시작하는 메소드
    @Transactional
    public void startGame(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));

        room.updateGameStatus(GameStatus.IN_PROGRESS);
        // ToDo: Room 엔티티에 currentRound, currentSong 업데이트 로직 추가 필요

        // 5초 카운트다운 메시지 전송
        int countdownSeconds = 5;
        GameStartCountdownResponse countdownResponse = new GameStartCountdownResponse("게임이 " + countdownSeconds + "초 후에 시작됩니다!", countdownSeconds);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game-start", countdownResponse);

        // 5초 후에 첫 라운드 시작
        taskScheduler.schedule(() -> startNextRound(roomId), new Date(System.currentTimeMillis() + countdownSeconds * 1000));
    }

    // 현재 라운드가 끝난 후, 다음 라운드를 시작하는 메소드
    public void startNextRound(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));

        // TODO: RoomType에 따라 로직 분기 (현재는 getRandomSong 사용)
        SongResponse songResponse = songService.getRandomSong();

        // Room에 현재 라운드, 현재 노래 정보 업데이트
        room.updateCurrentRoundAndSong(room.getCurrentRound() == null ? 1 : room.getCurrentRound() + 1, songResponse.toSongEntity());
        roomRepository.save(room); // 변경사항 저장

        // 클라이언트에 라운드 정보 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/round-start", songResponse);
    }

    // 플레이어의 스코어를 증가시키는 메소드
    @Transactional
    public void addScore(User user, Long roomId, int scoreToAdd) {
        InGame inGame = inGameRepository.findByUserAndRoom(user, new Room(roomId))
                .orElseThrow(() -> new IllegalArgumentException("InGame 정보가 없습니다."));

        int updateScore = inGame.getScore() + scoreToAdd;
        inGame.updateScore(updateScore);
    }
}
