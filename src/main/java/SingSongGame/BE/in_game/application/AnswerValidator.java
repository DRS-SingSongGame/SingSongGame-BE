package SingSongGame.BE.in_game.application;

import SingSongGame.BE.in_game.persistence.GameSession;
import SingSongGame.BE.song.persistence.Song;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnswerValidator {
    
    public boolean isCorrectAnswer(GameSession gameSession, String userAnswer) {
        if (gameSession.isRoundAnswered()) {
            return false;
        }
        
        Song currentSong = gameSession.getCurrentSong();
        if (currentSong == null) {
            return false;
        }
        
        String correctAnswer = normalizeAnswer(currentSong.getAnswer());
        String normalizedUserAnswer = normalizeAnswer(userAnswer);
        
        return correctAnswer.equals(normalizedUserAnswer);
    }
    
    public String normalizeAnswer(String input) {
        return input == null ? "" : input.replaceAll("\\s+", "").toLowerCase();
    }
    
    public boolean canAcceptAnswer(GameSession gameSession) {
        return !gameSession.isRoundAnswered();
    }
}