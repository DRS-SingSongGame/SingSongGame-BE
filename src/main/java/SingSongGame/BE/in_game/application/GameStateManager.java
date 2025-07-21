package SingSongGame.BE.in_game.application;

import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.in_game.persistence.GameSessionRepository;
import SingSongGame.BE.room.persistence.GameStatus;
import SingSongGame.BE.room.persistence.Room;
import SingSongGame.BE.song.persistence.Song;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameStateManager {
    
    private final GameSessionRepository gameSessionRepository;
    
    @Transactional
    public GameSession initializeGame(Room room, Set<String> keywords) {
        Long roomId = room.getId();
        
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElse(null);
        
        if (gameSession != null) {
            gameSession.resetForNewGame();
            gameSession.setKeywords(keywords);
            gameSession.setGameStatus(GameStatus.IN_PROGRESS);
            gameSession.setMaxRound(room.getMaxRound());
            gameSession.setUpdatedAt(LocalDateTime.now());
        } else {
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
        
        return gameSessionRepository.save(gameSession);
    }
    
    @Transactional
    public GameSession updateRoundInfo(Long roomId, int nextRound, Song song) {
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));
        
        gameSession.getUsedSongIds().add(song.getId());
        gameSession.updateRoundInfo(nextRound, song, LocalDateTime.now());
        gameSession.setRoundAnswered(false);
        
        return gameSessionRepository.save(gameSession);
    }
    
    @Transactional
    public void markRoundAnswered(Long roomId) {
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));
        
        gameSession.setRoundAnswered(true);
        gameSessionRepository.save(gameSession);
    }
    
    @Transactional
    public void updatePlayerScore(Long roomId, Long userId, int score) {
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));
        
        gameSession.updatePlayerScore(userId, score);
        gameSessionRepository.save(gameSession);
    }
    
    @Transactional
    public void endGame(Long roomId) {
        GameSession gameSession = gameSessionRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));
        
        gameSession.updateGameStatus(GameStatus.WAITING);
        gameSession.resetForNewGame();
        gameSessionRepository.save(gameSession);
    }
    
    public GameSession getGameSession(Long roomId) {
        return gameSessionRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("GameSession not found with id: " + roomId));
    }
    
    public boolean isGameFinished(GameSession gameSession) {
        return gameSession.getCurrentRound() >= gameSession.getMaxRound();
    }
}