package SingSongGame.BE.in_game.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.dto.response.GameStartCountdownResponse;
import SingSongGame.BE.in_game.dto.response.AnswerCorrectResponse;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.in_game.persistence.GameSessionRepository;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.song.application.SongService;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import SingSongGame.BE.song.persistence.Song;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class InGameService {

    private static final int TOTAL_ROUNDS = 10; // 총 라운드 수
    private static final int ROUND_DURATION_SECONDS = 30; // 각 라운드 지속 시간 (초)
    private static final int ANSWER_REVEAL_DURATION_SECONDS = 5; // 정답 공개 후 다음 라운드까지의 시간 (초)

    private final InGameRepository inGameRepository;
    private final RoomRepository roomRepository; // RoomRepository는 GameSession 생성 시 Room을 찾기 위해 필요
    private final GameSessionRepository gameSessionRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final SongService songService;
    private final TaskScheduler taskScheduler;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    // 게임을 시작하는 메소드
    @Transactional
    public void startGame(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));

        // 기존 GameSession이 있다면 삭제 (재시작 시나 오류 복구 시)
        gameSessionRepository.findById(roomId).ifPresent(gameSessionRepository::delete);

        // GameSession 생성
        GameSession gameSession = GameSession.builder()
                .room(room)
                .gameStatus(GameStatus.IN_PROGRESS)
                .currentRound(0) // 초기 라운드 0
                .playerScores(new HashMap<>()) // playerScores 초기값
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        gameSessionRepository.save(gameSession);

        // 5초 카운트다운 메시지 전송
        int countdownSeconds = 5;
        GameStartCountdownResponse countdownResponse = new GameStartCountdownResponse("게임이 " + countdownSeconds + "초 후에 시작됩니다!", countdownSeconds);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game-start", countdownResponse);

        // 5초 후에 첫 라운드 시작
        ScheduledFuture<?> future = taskScheduler.schedule(() -> startNextRound(roomId), new Date(System.currentTimeMillis() + countdownSeconds * 1000));
        scheduledTasks.put(roomId, future);
    }

    // 현재 라운드가 끝난 후, 다음 라운드를 시작하는 메소드
    @Transactional
    public void startNextRound(Long roomId) {
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

        if (gameSession.getCurrentRound() >= TOTAL_ROUNDS) {
            endGame(roomId);
            return;
        }

        // TODO: RoomType에 따라 로직 분기 (현재는 getRandomSong 사용)
        Song song = songService.getRandomSong().toSongEntity();
        int nextRound = gameSession.getCurrentRound() == null ? 1 : gameSession.getCurrentRound() + 1;

        // GameSession에 현재 라운드, 현재 노래 정보 업데이트
        gameSession.updateRoundInfo(
                nextRound,
                song,
                LocalDateTime.now()
        );
        gameSession.setRoundAnswered(false); // 새 라운드 시작 시 정답 여부 초기화
        gameSessionRepository.save(gameSession);

        // 클라이언트에 라운드 정보 전송
        SongResponse songResponse = SongResponse.from(song, nextRound);
//        System.out.println("Sending round-start: audioUrl = " + songResponse.audioUrl()); // 이 줄 추가
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/round-start", songResponse);

        // 라운드 지속 시간 후에 다음 라운드 시작 (정답 여부와 관계없이)
        ScheduledFuture<?> future = taskScheduler.schedule(() -> startNextRound(roomId), new Date(System.currentTimeMillis() + ROUND_DURATION_SECONDS * 1000));
        scheduledTasks.put(roomId, future);
    }

    @Transactional
    public void verifyAnswer(User user, Long roomId, String answer) {
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

        System.out.println("verifyAnswer: User " + user.getName() + " submitted answer: " + answer + " for roomId: " + roomId); // 이 줄 추가

        // 이미 정답이 나왔으면 더 이상 처리하지 않음
        if (gameSession.isRoundAnswered()) {
            return;
        }

        Song currentSong = gameSession.getCurrentSong();
        if (currentSong != null && currentSong.getAnswer().equalsIgnoreCase(answer)) {
            // 정답 맞혔을 때
            // int score = calculateScore(gameSession.getRoundStartTime());
            // addScore(user, roomId, score);

            gameSession.setRoundAnswered(true); // 정답 처리 플래그 설정
            gameSessionRepository.save(gameSession);

            // 기존 다음 라운드 스케줄링 취소
            ScheduledFuture<?> currentTask = scheduledTasks.get(roomId);
            if (currentTask != null) {
                currentTask.cancel(false);
                scheduledTasks.remove(roomId);
            }

            // 정답 공개 메시지 전송 (정답 포함)
            String winnerName = (user != null) ? user.getName() : "익명 사용자";
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/answer-correct", new AnswerCorrectResponse(winnerName, currentSong.getAnswer()));

            // 10초 후에 다음 라운드 시작 스케줄링
            ScheduledFuture<?> nextRoundTask = taskScheduler.schedule(() -> startNextRound(roomId), new Date(System.currentTimeMillis() + ANSWER_REVEAL_DURATION_SECONDS * 1000));
            scheduledTasks.put(roomId, nextRoundTask);
        }
    }

    private int calculateScore(LocalDateTime roundStartTime) {
        long secondsElapsed = Duration.between(roundStartTime, LocalDateTime.now()).getSeconds();
        int score = (int) (100 - (secondsElapsed * 2)); // 예시 점수 계산 로직
        return Math.max(score, 0);
    }

    // 플레이어의 스코어를 증가시키는 메소드
    @Transactional
    public void addScore(User user, Long roomId, int scoreToAdd) {
        // InGame inGame = inGameRepository.findByUserAndRoom(user, new Room(roomId))
        //         .orElseThrow(() -> new IllegalArgumentException("InGame 정보가 없습니다."));

        // int updateScore = inGame.getScore() + scoreToAdd;
        // inGame.updateScore(updateScore);

        // // GameSession의 플레이어 점수도 업데이트
        // GameSession gameSession = gameSessionRepository.findById(roomId)
        //         .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));
        // gameSession.updatePlayerScore(user.getId(), updateScore);
        // gameSessionRepository.save(gameSession);
    }

    @Transactional
    public void endGame(Long roomId) {
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

        gameSession.updateGameStatus(GameStatus.WAITING);
        gameSessionRepository.save(gameSession);

        // 클라이언트에게 게임 종료 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game-end", "게임이 종료되었습니다! 새로운 게임을 시작할 수 있습니다.");
    }
}
