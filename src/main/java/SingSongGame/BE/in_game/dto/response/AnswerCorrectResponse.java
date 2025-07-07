package SingSongGame.BE.in_game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnswerCorrectResponse {
    private String winnerNickname;
    private String correctAnswer;
    private String correctTitle;
    Map<Long, Integer> updatedScores;
    private int scoreGain;
}
