package SingSongGame.BE.in_game.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.dto.response.FinalResult;
import SingSongGame.BE.in_game.dto.response.GameEndResponse;
import SingSongGame.BE.in_game.dto.response.GameStartCountdownResponse;
import SingSongGame.BE.in_game.dto.response.AnswerCorrectResponse;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.in_game.persistence.GameSessionRepository;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.room_keyword.KeywordService;
import SingSongGame.BE.song.application.SongService;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import SingSongGame.BE.song.persistence.Song;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class InGameService {

    private static final int TOTAL_ROUNDS = 2; // 총 라운드 수
    private static final int ROUND_DURATION_SECONDS = 30; // 각 라운드 지속 시간 (초)
    private static final int ANSWER_REVEAL_DURATION_SECONDS = 5; // 정답 공개 후 다음 라운드까지의 시간 (초)

    private final Logger log = LoggerFactory.getLogger(InGameService.class);

    private final InGameRepository inGameRepository;
    private final RoomRepository roomRepository; // RoomRepository는 GameSession 생성 시 Room을 찾기 위해 필요
    private final GameSessionRepository gameSessionRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final SongService songService;
    private final KeywordService keywordService;
    private final TaskScheduler taskScheduler;
    private final ApplicationContext applicationContext;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    // 게임을 시작하는 메소드
    @Transactional
    public void startGame(Long roomId, Set<String> keywords) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));

        // ✅ 기존 GameSession 조회 또는 새로 생성
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElse(null);

        if (gameSession != null) {
            // ✅ 기존 세션이 있으면 업데이트
            gameSession.resetForNewGame(); // 기존 메서드 재활용
            gameSession.setKeywords(keywords);
            gameSession.setGameStatus(GameStatus.IN_PROGRESS);
            gameSession.setMaxRound(room.getMaxRound());
            gameSession.setUpdatedAt(LocalDateTime.now());
        } else {
            // ✅ 새 세션 생성
            gameSession = GameSession.builder()
                    .room(room)
                    .gameStatus(GameStatus.IN_PROGRESS)
                    .currentRound(0)
                    .playerScores(new HashMap<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .maxRound(room.getMaxRound())
                    .keywords(keywords)
                    .build();
        }

        gameSessionRepository.save(gameSession);
        // ✅ 저장 직후 바로 확인
        System.out.println("🎮 저장된 키워드: " + gameSession.getKeywords());

        // ✅ DB에서 다시 읽어와서 확인
        GameSession savedSession = gameSessionRepository.findById(roomId).orElse(null);
        if (savedSession != null) {
            System.out.println("🎮 DB에서 읽은 키워드: " + savedSession.getKeywords());
        }

        keywordService.clearKeywords(roomId);
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

        if (gameSession.getCurrentRound() >= gameSession.getMaxRound()) {
            applicationContext.getBean(InGameService.class).endGame(roomId);
            return;
        }

        // ✅ 키워드 기반 랜덤 노래 추출
        Song song;
        Set<String> keywords = gameSession.getKeywords();

        if (keywords != null && !keywords.isEmpty()) {
            song = songService
                    .getRandomSongByTagNames(keywords, gameSession.getUsedSongIds());

        } else {
            song = songService
                    .getRandomSong(gameSession.getUsedSongIds());
        }

        // ✅ 출제한 노래 ID 저장
        gameSession.getUsedSongIds().add(song.getId());

        int nextRound = gameSession.getCurrentRound() == null ? 1 : gameSession.getCurrentRound() + 1;

        // ✅ 라운드 정보 업데이트
        gameSession.updateRoundInfo(
                nextRound,
                song,
                LocalDateTime.now()
        );
        gameSession.setRoundAnswered(false);
        gameSessionRepository.save(gameSession);

        // ✅ 라운드 시작 메시지 전송
        SongResponse songResponse = songService.createSongResponse(song, nextRound, gameSession.getMaxRound());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/round-start", songResponse);

        // ✅ 라운드 종료 타이머 설정
        ScheduledFuture<?> future = taskScheduler.schedule(() -> {
            GameSession latestSession = gameSessionRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("GameSession not found"));

            if (!latestSession.isRoundAnswered()) {
                // 정답자 없음 알림
                messagingTemplate.convertAndSend(
                        "/topic/room/" + roomId + "/round-failed",
                        Map.of("title", latestSession.getCurrentSong().getTitle())
                );

                // 3초 후 다음 라운드
                ScheduledFuture<?> delayTask = taskScheduler.schedule(
                        () -> startNextRound(roomId),
                        new Date(System.currentTimeMillis() + 3000)
                );
                scheduledTasks.put(roomId, delayTask);

            } else {
                // 정답자 있었음 → 다음 라운드 즉시 실행
                startNextRound(roomId);
            }
        }, new Date(System.currentTimeMillis() + ROUND_DURATION_SECONDS * 1000));

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
        if (currentSong != null && normalizeAnswer(currentSong.getAnswer()).equals(normalizeAnswer(answer))) {
            // 정답 맞혔을 때
            int score = calculateScore(gameSession.getRoundStartTime());
            int scoreGain = applicationContext.getBean(InGameService.class).addScore(user, roomId, score);

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
            messagingTemplate.convertAndSend("/topic/room/" + roomId + "/answer-correct", new AnswerCorrectResponse(winnerName, currentSong.getAnswer(), currentSong.getTitle(), gameSession.getPlayerScores(), scoreGain ));

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
    public int addScore(User user, Long roomId, int scoreToAdd) {
         InGame inGame = inGameRepository.findByUserAndRoom(user, new Room(roomId))
                 .orElseThrow(() -> new IllegalArgumentException("InGame 정보가 없습니다."));

         int prevScore = inGame.getScore();
         int updateScore = inGame.getScore() + scoreToAdd;
         inGame.updateScore(updateScore);

         // GameSession의 플레이어 점수도 업데이트
         GameSession gameSession = gameSessionRepository.findById(roomId)
                 .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));
         gameSession.updatePlayerScore(user.getId(), updateScore);
         gameSessionRepository.save(gameSession);

         return scoreToAdd;
    }

    @Transactional
    public void endGame(Long roomId) {
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

        // ✅ 결과 먼저 계산
        List<FinalResult> finalResults = gameSession.getPlayerScores().entrySet().stream()
                .map(entry -> new FinalResult(entry.getKey(), entry.getValue()))
                .toList();

        GameEndResponse response = new GameEndResponse(finalResults);
        log.info("🔥 Final Player Scores: {}", gameSession.getPlayerScores());
        log.info("🔥 Final Results to send: {}", finalResults);

        // ✅ 그 이후에 상태 초기화
        gameSession.updateGameStatus(GameStatus.WAITING);
        gameSession.resetForNewGame();
        resetInGameScores(roomId);
        gameSessionRepository.save(gameSession);

        // ✅ 클라이언트에게 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game-end", response);
    }

    @Transactional
    public void resetInGameScores(Long roomId) {
        List<InGame> inGameList = inGameRepository.findByRoomId(roomId);
        for (InGame inGame : inGameList) {
            inGame.setScore(0); // or inGame.resetScore()
        }
    }

    public String normalizeAnswer(String input) {
        return input == null ? "" : input.replaceAll("\\s+", "")  // 모든 공백 제거
                .toLowerCase();           // 소문자화
    }
}
