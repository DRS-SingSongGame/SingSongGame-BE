package SingSongGame.BE.ai_game.application;

import SingSongGame.BE.ai_game.dto.response.AiAnswerCorrectResponse;
import SingSongGame.BE.ai_game.dto.response.AiGameStartCountdownResponse;
import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.application.AnswerValidator;
import SingSongGame.BE.in_game.application.InGameService;
import SingSongGame.BE.in_game.dto.response.AnswerCorrectResponse;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.room_keyword.KeywordService;
import SingSongGame.BE.song.application.SongService;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import SingSongGame.BE.song.persistence.Song;
import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.in_game.persistence.GameSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class AiGameService {

        private static final int TOTAL_ROUNDS = 2;
        private static final int ROUND_DURATION_SECONDS = 30;
        private static final int ANSWER_REVEAL_DURATION_SECONDS = 5;

        private final RoomRepository roomRepository;
        private final GameSessionRepository gameSessionRepository;
        private final SimpMessageSendingOperations messagingTemplate;
        private final InGameService inGameService;
        private final SongService songService;
        private final TaskScheduler taskScheduler;
        private final KeywordService keywordService;
        private final AnswerValidator answerValidator;

        private final Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        @Transactional
        public void startGame(Long roomId, Set<String> keywords) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));

            gameSessionRepository.findById(roomId).ifPresent(gameSessionRepository::delete);

            // GameSession 생성
            GameSession gameSession = GameSession.builder()
                    .room(room)
                    .gameStatus(GameStatus.IN_PROGRESS)
                    .currentRound(0) // 초기 라운드 0
                    .playerScores(new HashMap<>()) // playerScores 초기값
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .maxRound(room.getMaxRound())
                    .keywords(keywords)
                    .build();
            gameSessionRepository.save(gameSession);
            keywordService.clearKeywords(roomId);

            messagingTemplate.convertAndSend(
                    "/topic/ai-room/" + roomId + "/game-start",
                    new AiGameStartCountdownResponse("TTS 기반 게임이 시작됩니다!", 3)
            );
            int countdownSeconds = 3;
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> {

                        // 🔥 게임 세션 상태 확인 후 시작
                        GameSession currentSession = gameSessionRepository.findById(roomId).orElse(null);
                        if (currentSession != null &&
                                currentSession.getGameStatus() == GameStatus.IN_PROGRESS &&
                                currentSession.getCurrentRound() == 0) {

                            startNextRound(roomId);
                        } else {
                            log.warn("⚠️ [첫 라운드 시작 취소] 게임 상태가 적절하지 않음 - roomId: {}, session: {}",
                                    roomId, currentSession);
                        }

                        // 스케줄러 작업 완료 후 제거
                        scheduledTasks.remove(roomId);
                    },
                    new Date(System.currentTimeMillis() + countdownSeconds * 1000)
            );
            scheduledTasks.put(roomId, future);

        }

        @Transactional
        public void startNextRound(Long roomId) {
            log.info("🎯 [startNextRound] 라운드 시작 - roomId: {}", roomId);

            GameSession gameSession = gameSessionRepository.findById(roomId)
                    .orElseThrow(() -> {
                        log.error("❌ [세션 찾기 실패] roomId: {}", roomId);
                        return new IllegalArgumentException("GameSession not found with id: " + roomId);
                    });


            if (gameSession.getCurrentRound() >= gameSession.getMaxRound()) {
                log.info("🏁 [게임 종료] 최대 라운드 도달 - roomId: {}", roomId);
                endGame(roomId);
                return;
            }

            String currentArtist = gameSession.getCurrentSong() != null ?
                    gameSession.getCurrentSong().getArtist() : null;

            Song song;
            Set<String> keywords = gameSession.getKeywords();

            // 🔥 키워드 관련 상세 로그
            log.info("🏷️  [키워드 확인] keywords: {}", keywords);
            log.info("🏷️  [키워드 타입] keywords class: {}", keywords != null ? keywords.getClass() : "null");
            log.info("🏷️  [키워드 isEmpty] isEmpty: {}", keywords == null ? "null" : keywords.isEmpty());

            if (keywords != null) {
                log.info("🏷️  [키워드 개수] size: {}", keywords.size());
                keywords.forEach(keyword -> log.info("🏷️  [개별 키워드] '{}'", keyword));
            }

            log.info("📝 [사용된 곡 ID] usedSongIds: {}", gameSession.getUsedSongIds());

            if (keywords != null && !keywords.isEmpty()) {
                log.info("🎵 [키워드 기반 곡 선택] keywords: {}", keywords);

                song = songService.getRandomSongByTagNames(keywords, gameSession.getUsedSongIds(), currentArtist);

                log.info("✅ [키워드 기반 곡 선택 완료] songId: {}, title: {}",
                        song.getId(), song.getTitle());
            } else {
                log.info("🎵 [전체 곡에서 랜덤 선택] 키워드 없음");
                log.warn("⚠️  [키워드 문제] keywords가 null이거나 비어있음: {}", keywords);

                song = songService.getRandomSong(gameSession.getUsedSongIds(), currentArtist);

                log.info("✅ [랜덤 곡 선택 완료] songId: {}, title: {}",
                        song.getId(), song.getTitle());
            }


            // ✅ 출제한 노래 ID 저장
            gameSession.getUsedSongIds().add(song.getId());

            int nextRound = gameSession.getCurrentRound() == null ? 1 : gameSession.getCurrentRound() + 1;

            gameSession.updateRoundInfo(nextRound, song, LocalDateTime.now());
            gameSession.setRoundAnswered(false);
            gameSessionRepository.save(gameSession);

            // ⏱️ 재생 시각: 1.5초 뒤
            long playbackStartTimestamp = System.currentTimeMillis() + 5000;

            // ✨ 응답 객체에 추가
            SongResponse songResponse = SongResponse.from(song, nextRound, gameSession.getMaxRound());
            Map<String, Object> payload = new HashMap<>();
            payload.put("song", songResponse);
            payload.put("playbackStartTime", playbackStartTimestamp);

            // 전송
            messagingTemplate.convertAndSend(
                    "/topic/ai-room/" + roomId + "/round-start",
                    payload
            );
        }

        @Transactional
        public void startGameWithFirstRound(Long roomId, Set<String> keywords) {
            // 게임 세션 생성
            startGame(roomId, keywords);

            // 🔥 트랜잭션이 커밋된 후 즉시 첫 라운드 시작
            startNextRound(roomId);
        }

        @Transactional
        public void verifyAnswer(User user, Long roomId, String answer, int timeLeft) {
            GameSession gameSession = gameSessionRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

            if (gameSession.isRoundAnswered()) return;

            Song currentSong = gameSession.getCurrentSong();
            if (currentSong != null && answerValidator.normalizeAnswer(currentSong.getAnswer()).equals(answerValidator.normalizeAnswer(answer))) {
                gameSession.setRoundAnswered(true);
                gameSessionRepository.save(gameSession);

                int baseScore = 50;
                int bonusScore = (int) Math.round((timeLeft / 60.0) * 50);  // 예: 60초 남았으면 100점
                int totalScore = baseScore + bonusScore;

                ScheduledFuture<?> currentTask = scheduledTasks.get(roomId);
                if (currentTask != null) {
                    currentTask.cancel(false);
                    scheduledTasks.remove(roomId);
                }

                messagingTemplate.convertAndSend(
                        "/topic/ai-room/" + roomId + "/answer-correct",
                        new AiAnswerCorrectResponse(
                                user.getId().toString(),
                                user.getName(),
                                currentSong.getTitle(),
                                currentSong.getArtist(),
                                totalScore

                        )
                );

            }
        }

        @Transactional
        public void endGame(Long roomId) {
            GameSession gameSession = gameSessionRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

            gameSession.updateGameStatus(GameStatus.WAITING);
            gameSessionRepository.save(gameSession);

            Map<String, Object> payload = new HashMap<>();
            payload.put("message", "TTS 게임이 종료되었습니다!");

            messagingTemplate.convertAndSend("/topic/ai-room/" + roomId + "/game-end", payload);
        }
    }


