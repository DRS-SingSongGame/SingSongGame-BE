package SingSongGame.BE.in_game.application;

import SingSongGame.BE.auth.persistence.User;
import SingSongGame.BE.in_game.persistence.InGame;
import SingSongGame.BE.in_game.persistence.InGameRepository;
import SingSongGame.BE.room.persistence.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScoreCalculator {
    
    private final InGameRepository inGameRepository;
    private final GameStateManager gameStateManager;
    
    public int calculateScore(LocalDateTime roundStartTime, LocalDateTime answerTime) {
        long secondsElapsed = Duration.between(roundStartTime, answerTime).getSeconds();
        int score = (int) (100 - (secondsElapsed * 2));
        return Math.max(score, 0);
    }
    
    @Transactional
    public int addScore(User user, Long roomId, int scoreToAdd) {
        log.info("🔍 addScore() 호출: user={}, roomId={}, scoreToAdd={}", user.getId(), roomId, scoreToAdd);
        
        InGame inGame = inGameRepository.findByUserAndRoom(user, new Room(roomId))
                .orElseThrow(() -> new IllegalArgumentException("InGame 정보가 없습니다."));
        
        int updateScore = inGame.getScore() + scoreToAdd;
        inGame.updateScore(updateScore);
        
        gameStateManager.updatePlayerScore(roomId, user.getId(), updateScore);
        
        return scoreToAdd;
    }
    
    @Transactional
    public void resetInGameScores(Long roomId) {
        List<InGame> inGameList = inGameRepository.findByRoomId(roomId);
        for (InGame inGame : inGameList) {
            inGame.setScore(0);
        }
    }
}