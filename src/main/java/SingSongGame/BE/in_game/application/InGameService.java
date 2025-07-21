package SingSongGame.BE.in_game.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.dto.response.FinalResult;
import SingSongGame.BE.in_game.dto.response.GameEndResponse;
import SingSongGame.BE.in_game.dto.response.GameStartCountdownResponse;
import SingSongGame.BE.in_game.dto.response.AnswerCorrectResponse;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.quick_match.application.rating.TierChangeResult;
import SingSongGame.BE.quick_match.cache.QuickMatchResultCache;
import SingSongGame.BE.quick_match.persistence.QuickMatchRepository;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.room.persistence.RoomRepository;
import SingSongGame.BE.room_keyword.KeywordService;
import SingSongGame.BE.quick_match.application.QuickMatchResultService;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoom;
import SingSongGame.BE.quick_match.persistence.QuickMatchRoomPlayer;
import SingSongGame.BE.room.persistence.*;
import SingSongGame.BE.song.application.SongService;
import SingSongGame.BE.song.application.dto.response.SongResponse;
import SingSongGame.BE.song.persistence.Song;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InGameService {

    private static final int ROUND_DURATION_SECONDS = 30;
    private static final int ANSWER_REVEAL_DURATION_SECONDS = 5;

    private final InGameRepository inGameRepository;
    private final RoomRepository roomRepository;
    private final SimpMessageSendingOperations messagingTemplate;
    private final SongService songService;
    private final KeywordService keywordService;
    private final ApplicationContext applicationContext;
    private final QuickMatchResultService quickMatchResultService;
    private final QuickMatchResultCache quickMatchResultCache;
    private final QuickMatchRepository quickMatchRoomRepository;
    
    // 새로운 컴포넌트들
    private final GameStateManager gameStateManager;
    private final AnswerValidator answerValidator;
    private final ScoreCalculator scoreCalculator;
    private final GameScheduler gameScheduler;

    @Transactional
    public void startGame(Long roomId, Set<String> keywords) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found with id: " + roomId));

        if (room.getRoomType() == RoomType.QUICK_MATCH) {
            room.setMaxRound(2);
        }

        GameSession gameSession = gameStateManager.initializeGame(room, keywords);
        
        if (room.getRoomType() == RoomType.QUICK_MATCH) {
            initializeQuickMatchInGameRecords(room);
        }

        keywordService.clearKeywords(roomId);
        
        int countdownSeconds = 5;
        GameStartCountdownResponse countdownResponse = new GameStartCountdownResponse(
            "게임이 " + countdownSeconds + "초 후에 시작됩니다!", countdownSeconds
        );
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game-start", countdownResponse);

        gameScheduler.scheduleGameStart(roomId, countdownSeconds);
    }
    
    private void initializeQuickMatchInGameRecords(Room room) {
        List<User> users = room.getQuickMatchRoom().getUsers();
        for (User user : users) {
            InGame inGame = InGame.builder()
                    .user(user)
                    .room(room)
                    .score(0)
                    .build();
            inGameRepository.save(inGame);
            log.info("✅ [QuickMatch] InGame 생성: userId={}, roomId={}", user.getId(), room.getId());
        }
    }

    @Transactional
    public void startNextRound(Long roomId) {
        GameSession gameSession = gameStateManager.getGameSession(roomId);

        if (gameStateManager.isGameFinished(gameSession)) {
            applicationContext.getBean(InGameService.class).endGame(roomId);
            return;
        }

        Song song = selectSongForRound(gameSession);
        int nextRound = gameSession.getCurrentRound() == null ? 1 : gameSession.getCurrentRound() + 1;

        gameSession = gameStateManager.updateRoundInfo(roomId, nextRound, song);

        SongResponse songResponse = songService.createSongResponse(song, nextRound, gameSession.getMaxRound());
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/round-start", songResponse);

        gameScheduler.scheduleRoundEnd(roomId, ROUND_DURATION_SECONDS);
    }
    
    private Song selectSongForRound(GameSession gameSession) {
        String currentArtist = gameSession.getCurrentSong() != null ?
                gameSession.getCurrentSong().getArtist() : null;
        Set<String> keywords = gameSession.getKeywords();

        if (keywords != null && !keywords.isEmpty()) {
            return songService.getRandomSongByTagNames(keywords, gameSession.getUsedSongIds(), currentArtist);
        } else {
            return songService.getRandomSong(gameSession.getUsedSongIds(), currentArtist);
        }
    }


    @Transactional
    public void verifyAnswer(User user, Long roomId, String answer) {
        try {
            GameSession gameSession = gameStateManager.getGameSession(roomId);
            
            if (!answerValidator.canAcceptAnswer(gameSession)) {
                return;
            }

            if (answerValidator.isCorrectAnswer(gameSession, answer)) {
                handleCorrectAnswer(user, roomId, gameSession);
            }
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            log.info("해당 유저 롤백 {}", user.getName());
        }
    }
    
    private void handleCorrectAnswer(User user, Long roomId, GameSession gameSession) {
        int score = scoreCalculator.calculateScore(gameSession.getRoundStartTime());
        int scoreGain = applicationContext.getBean(InGameService.class).addScore(user, roomId, score);

        gameStateManager.markRoundAnswered(roomId);
        gameScheduler.cancelScheduledTask(roomId);

        String winnerName = user != null ? user.getName() : "익명 사용자";
        Song currentSong = gameSession.getCurrentSong();
        
        AnswerCorrectResponse response = new AnswerCorrectResponse(
            winnerName, currentSong.getAnswer(), currentSong.getTitle(), 
            gameSession.getPlayerScores(), scoreGain
        );
        
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/answer-correct", response);
        gameScheduler.scheduleAnswerReveal(roomId, ANSWER_REVEAL_DURATION_SECONDS);
    }

    @Transactional
    public int addScore(User user, Long roomId, int scoreToAdd) {
        return scoreCalculator.addScore(user, roomId, scoreToAdd);
    }

    @Transactional
    public void endGame(Long roomId) {
        GameSession gameSession = gameStateManager.getGameSession(roomId);
        Room room = gameSession.getRoom();
        
        log.info("✅ RoomType = {}", room.getRoomType());
        List<RoomPlayer> roomPlayers = room.getPlayers();
        log.info("✅ RoomPlayers: {}", roomPlayers.stream().map(p -> p.getUser().getId()).toList());

        Map<Long, Integer> finalScoreMap = calculateFinalScores(gameSession, roomPlayers);
        List<FinalResult> finalResults = createFinalResults(finalScoreMap);
        
        GameEndResponse response = new GameEndResponse(finalResults);
        log.info("🔥 Final Player Scores: {}", finalScoreMap);
        log.info("🔥 Final Results to send: {}", finalResults);

        gameStateManager.endGame(roomId);
        scoreCalculator.resetInGameScores(roomId);
        updateRoomPlayersScores(roomPlayers, finalScoreMap);

        if (room.getRoomType() == RoomType.QUICK_MATCH) {
            processQuickMatchResults(room, roomPlayers);
        }

        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/game-end", response);
    }
    
    private Map<Long, Integer> calculateFinalScores(GameSession gameSession, List<RoomPlayer> roomPlayers) {
        Map<Long, Integer> finalScoreMap = new HashMap<>(gameSession.getPlayerScores());
        for (RoomPlayer player : roomPlayers) {
            finalScoreMap.putIfAbsent(player.getUser().getId(), 0);
        }
        return finalScoreMap;
    }
    
    private List<FinalResult> createFinalResults(Map<Long, Integer> finalScoreMap) {
        return finalScoreMap.entrySet().stream()
                .map(entry -> new FinalResult(entry.getKey(), entry.getValue()))
                .toList();
    }
    
    private void updateRoomPlayersScores(List<RoomPlayer> roomPlayers, Map<Long, Integer> finalScoreMap) {
        for (RoomPlayer player : roomPlayers) {
            Long userId = player.getUser().getId();
            int score = finalScoreMap.getOrDefault(userId, 0);
            player.setScore(score);
            log.info("📌 [Score Copy] userId={}, copiedScore={}", userId, score);
        }
    }
    
    private void processQuickMatchResults(Room room, List<RoomPlayer> roomPlayers) {
        log.info("✅ [MMR 계산 시작] QUICK_MATCH 모드 실행됨");

        List<QuickMatchRoomPlayer> quickPlayers = roomPlayers.stream()
                .map(p -> new QuickMatchRoomPlayer(p.getUser(), p.getScore()))
                .collect(Collectors.toList());

        List<TierChangeResult> results = quickMatchResultService.processQuickMatchResult(quickPlayers);

        QuickMatchRoom quickMatchRoom = quickMatchRoomRepository.findByRoom(room)
                .orElseThrow(() -> new IllegalStateException("QuickMatchRoom not found for Room"));

        String roomCode = quickMatchRoom.getRoomCode();
        quickMatchResultCache.put(roomCode, results);
    }
}
