package SingSongGame.BE.ai_game.application;

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

        private static final int TOTAL_ROUNDS = 10;
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

            gameSessionRepository.findById(roomId).ifPresent(gameSessionRepository::delete);

            GameSession gameSession = GameSession.builder()
                    .room(room)
                    .gameStatus(GameStatus.IN_PROGRESS)
                    .currentRound(0)
                    .playerScores(new HashMap<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            gameSessionRepository.save(gameSession);

            messagingTemplate.convertAndSend(
                    "/topic/ai-room/" + roomId + "/game-start",
                    new AiGameStartCountdownResponse("TTS 기반 게임이 시작됩니다!", 3)
            );

//            ScheduledFuture<?> future = taskScheduler.schedule(
//                    () -> startNextRound(roomId),
//                    new Date(System.currentTimeMillis() + 3000)); // 3초 후 시작
//            scheduledTasks.put(roomId, future);
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

            Song song = songService.getRandomSong().toSongEntity();
            int nextRound = gameSession.getCurrentRound() == null ? 1 : gameSession.getCurrentRound() + 1;

            gameSession.updateRoundInfo(nextRound, song, LocalDateTime.now());
            gameSession.setRoundAnswered(false);
            gameSessionRepository.save(gameSession);

            SongResponse songResponse = SongResponse.from(song, nextRound);
            messagingTemplate.convertAndSend("/topic/ai-room/" + roomId + "/round-start", songResponse);

//            ScheduledFuture<?> future = taskScheduler.schedule(
//                    () -> startNextRound(roomId),
//                    new Date(System.currentTimeMillis() + ROUND_DURATION_SECONDS * 1000));
//            scheduledTasks.put(roomId, future);
        }

        @Transactional
        public void verifyAnswer(User user, Long roomId, String answer) {
            GameSession gameSession = gameSessionRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

            if (gameSession.isRoundAnswered()) return;

            Song currentSong = gameSession.getCurrentSong();
            if (currentSong != null && currentSong.getAnswer().equalsIgnoreCase(answer)) {
                gameSession.setRoundAnswered(true);
                gameSessionRepository.save(gameSession);

                ScheduledFuture<?> currentTask = scheduledTasks.get(roomId);
                if (currentTask != null) {
                    currentTask.cancel(false);
                    scheduledTasks.remove(roomId);
                }

                String winnerName = (user != null) ? user.getName() : "익명 사용자";
                messagingTemplate.convertAndSend("/topic/ai-room/" + roomId + "/answer-correct",
                        new AnswerCorrectResponse(winnerName, currentSong.getAnswer()));

                ScheduledFuture<?> nextRoundTask = taskScheduler.schedule(
                        () -> startNextRound(roomId),
                        new Date(System.currentTimeMillis() + ANSWER_REVEAL_DURATION_SECONDS * 1000));
                scheduledTasks.put(roomId, nextRoundTask);
            }
        }

        @Transactional
        public void endGame(Long roomId) {
            GameSession gameSession = gameSessionRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));

            gameSession.updateGameStatus(GameStatus.WAITING);
            gameSessionRepository.save(gameSession);

            messagingTemplate.convertAndSend("/topic/ai-room/" + roomId + "/game-end", "TTS 게임이 종료되었습니다!");
        }
    }


