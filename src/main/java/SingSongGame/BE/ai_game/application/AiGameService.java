package SingSongGame.BE.ai_game.application;

import SingSongGame.BE.ai_game.dto.response.AiAnswerCorrectResponse;
import SingSongGame.BE.ai_game.dto.response.AiGameStartCountdownResponse;
import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.dto.response.AnswerCorrectResponse;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
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
        private final SongService songService;
        private final TaskScheduler taskScheduler;

        private final Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

        @Transactional
        public void startGame(Long roomId) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));

            GameSession gameSession = gameSessionRepository.findById(roomId)
                    .map(existingSession -> {
                        // 기존 세션이 있으면 초기화해서 재사용
                        existingSession.resetForNewGame();
                        existingSession.updateGameStatus(GameStatus.IN_PROGRESS);
                        return existingSession;
                    })
                    .orElseGet(() -> {
                        // 기존 세션이 없으면 새로 생성
                        return GameSession.builder()
                                .room(room)
                                .gameStatus(GameStatus.IN_PROGRESS)
                                .currentRound(0)
                                .playerScores(new HashMap<>())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                    });

            gameSessionRepository.save(gameSession);

            messagingTemplate.convertAndSend(
                    "/topic/ai-room/" + roomId + "/game-start",
                    new AiGameStartCountdownResponse("TTS 기반 게임이 시작됩니다!", 3)
            );
        }

        @Transactional
        public void startNextRound(Long roomId) {
            log.info("🎯 [startNextRound] 라운드 시작 - roomId: {}", roomId);

            GameSession gameSession = gameSessionRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

            if (gameSession.getCurrentRound() >= TOTAL_ROUNDS) {
                endGame(roomId);
                return;
            }

            Song song = songService.getRandomSong();
            int nextRound = gameSession.getCurrentRound() == null ? 1 : gameSession.getCurrentRound() + 1;

            gameSession.updateRoundInfo(nextRound, song, LocalDateTime.now());
            gameSession.setRoundAnswered(false);
            gameSessionRepository.save(gameSession);

            // ⏱️ 재생 시각: 1.5초 뒤
            long playbackStartTimestamp = System.currentTimeMillis() + 5000;

            // ✨ 응답 객체에 추가
            SongResponse songResponse = SongResponse.from(song, nextRound);
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
        public void verifyAnswer(User user, Long roomId, String answer, int timeLeft) {
            GameSession gameSession = gameSessionRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

            if (gameSession.isRoundAnswered()) return;

            Song currentSong = gameSession.getCurrentSong();
            if (currentSong != null && currentSong.getAnswer().equalsIgnoreCase(answer)) {
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


